package jStrg.tests.unit_tests;

import jStrg.database.IGenericDao;
import jStrg.environment.Environment;
import jStrg.file_system.*;
import jStrg.network_management.storage_management.core.Location;
import jStrg.network_management.storage_management.core.StorageCell;
import jStrg.tests.helper;
import org.junit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static jStrg.tests.helper.*;
import static org.junit.Assert.*;

/**
 * tests for StorageCellts
 */
public class ConnectorTest {

    private static Application somethinglikedropbox;
    private static LinkedHashMap<String, File> testfilelist;
    private static User user;


    @BeforeClass
    public static void before() throws Exception {
        Settings.read_global_config();
        helper.clearEnvironment();
        somethinglikedropbox = new Application("SomethingLikeDropbox");

        create_locations(somethinglikedropbox);

        user = new User(0, 0, "testuser", "bla", 0, 50000, 50000, somethinglikedropbox);


        testfilelist = get_testfilelist(user, somethinglikedropbox);
        System.out.println("<-- setup done, begin test -->");
        cleanupCache();
        cleanupBackends();
        user.db_update();
    }

    @AfterClass
    public static void after() throws Exception {
        somethinglikedropbox = null;
        helper.clearEnvironment();
    }

    private static void cleanupBackends() {
        IGenericDao dao = Environment.data().get_dao(StorageCell.class);
        for (Object typemapentry : dao.findAll()) {
            StorageCell connector = (StorageCell) typemapentry;
            if (connector.get_maintenance()) {
                continue;
            }
            for (Map.Entry<String, File> fileentry : testfilelist.entrySet()) {
                try {
                    if (connector.contains(fileentry.getKey()))
                        connector.delete(fileentry.getKey());
                } catch (Exception e) {
                    System.out.println("Error deleting file in location: " + e);
                }
            }
        }

    }

    private static void cleanupCache() {
        for (Map.Entry<String, File> fileentry : testfilelist.entrySet()) {
            Path path = Paths.get("/tmp/" + fileentry.getValue().get_real_file().getName());
            if (Files.exists(path)) {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    System.out.println("Error deleting: " + path);
                }
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        testfilelist = get_testfilelist(user, somethinglikedropbox);
        helper.setupStoragePools(user);
    }

    @After
    public void tearDown() throws Exception {
        cleanupBackends();
        try {
            cleanupCache();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void testGet_free_space(Location _connector) throws Exception {
        System.out.println("test: free space");
        Long free_space = _connector.get_free_space();
        assertNotNull(free_space);
        assertTrue(free_space != 0);
        System.out.println("Test reported " + free_space + " free space.");
    }

    public void testDelete(Location _connector) throws Exception {
        Map.Entry<String, File> onefile = testfilelist.entrySet().iterator().next();
        FileVersion fileversion = new FileVersion(user, onefile.getValue());
        fileversion.set_checksum(onefile.getKey());
        long old_free_space = _connector.get_free_space();
        long new_free_space = old_free_space - onefile.getValue().get_real_file().length();

        _connector.write_file(onefile.getValue().get_real_file(), fileversion);

        assertTrue(_connector.contains(onefile.getKey()));
        System.out.println(" > written: " + onefile.getValue());
        System.out.println("  -> Free space calculation: got size: " + _connector.get_free_space());
        System.out.println("                             expected: " + new_free_space);
        if (!(_connector.get_type() == Location.TYPE.DISK))
            assertTrue(_connector.get_free_space() == new_free_space);

        _connector.delete(fileversion.get_checksum());

        assertFalse(_connector.contains(fileversion.get_checksum()));
        System.out.println(" > deleted: " + onefile.getValue());
        System.out.println("  -> Free space calculation: got size: " + _connector.get_free_space() + " ");
        System.out.println("                             expected: " + old_free_space);
        if (!(_connector.get_type() == Location.TYPE.DISK))
            assertTrue(_connector.get_free_space() == old_free_space);

        Location validate_location = get_temporary_location(_connector.get_type(), somethinglikedropbox);
        System.out.println(" > overall space test: " + onefile.getValue());
        System.out.println("  -> Free space calculation: got size: " + _connector.get_free_space());
        System.out.println("                             expected: " + old_free_space);
        if (!(_connector.get_type() == Location.TYPE.DISK))
            assertTrue(validate_location.get_free_space() == old_free_space);
        validate_location.delete();
    }

    public void testContains(Location _connector) throws Exception {
        Map.Entry<String, File> onefile = testfilelist.entrySet().iterator().next();
        FileVersion fileversion = new FileVersion(user, onefile.getValue());
        fileversion.set_checksum(onefile.getKey());
        _connector.write_file(onefile.getValue().get_real_file(), fileversion);

        assertTrue(_connector.contains(onefile.getKey()));
        assertFalse(_connector.contains(onefile.getKey() + onefile.getKey()));
    }

    @Test
    public void testEnough_free_space() throws Exception {

        // no google account available at the moment, so disable it for this test
        user.remove_storagepool(Location.TYPE.GOOGLE);

        Map.Entry<String, File> onefile = testfilelist.entrySet().iterator().next();
        Boolean returnval = Location.enough_space_available(user, onefile.getValue().get_real_size());
        assertTrue("file that fits", returnval);
        returnval = Location.enough_space_available(user, 5368709120L * 100); // default max bucket * 100
        assertFalse("file that doesnt fit", returnval);

        // reactivate google for other tests
        user.add_storagepool(Location.TYPE.GOOGLE);
    }

    public void testStage_file_to_cache_location(Location _connector) throws Exception {
        System.out.println("test: stage file");
        testWrite_file(_connector);
        for (Map.Entry<String, File> fileentry : testfilelist.entrySet()) {
            FileVersion fileversion = new FileVersion(user, fileentry.getValue());
            fileversion.set_checksum(fileentry.getKey());
            String destination = "/tmp/" + fileentry.getValue().get_real_file().getName();
            if (Files.exists(Paths.get(destination)))
                Files.delete(Paths.get(destination));
            java.io.File result = _connector.stage_file_to_cache_location(fileversion, destination);

            assertNotNull(result);
            assertTrue(result.toString().equals(destination));
            assertTrue(Location.file_checksum(result).equals(fileentry.getKey()));
            System.out.println(" > downloaded: " + fileentry.getValue());
        }
    }

    public void testWrite_file(Location _connector) throws Exception {
        System.out.println("test: write_file");
        long free_space = _connector.get_free_space();
        System.out.println("  -> Free space calculation: got size: " + _connector.get_free_space());
        for (Map.Entry<String, File> fileentry : testfilelist.entrySet()) {
            FileVersion fileversion = new FileVersion(user, fileentry.getValue());
            fileversion.set_checksum(fileentry.getKey());
            free_space -= fileentry.getValue().get_real_file().length();
            StorageCell result = _connector.write_file(fileentry.getValue().get_real_file(), fileversion);

            System.out.println(" > written: " + fileentry.getValue());
            System.out.println("  -> Free space calculation: got size: " + _connector.get_free_space());
            System.out.println("                             expected: " + free_space);
            assertNotNull(result);
            assertTrue(result == _connector);
            if (!(_connector.get_type() == Location.TYPE.DISK))
                assertTrue(result.get_free_space() == free_space);

        }
        Location validate_location = get_temporary_location(_connector.get_type(), somethinglikedropbox);
        System.out.println("Overall space:");
        System.out.println("  -> got size: " + validate_location.get_free_space());
        System.out.println("     expected: " + free_space);
        if (!(_connector.get_type() == Location.TYPE.DISK)) { // other processes invalidates test
            assertTrue(validate_location.get_free_space() == free_space);
        }
    }

    @Test
    public void testGoogleConnectorWrite_file() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.GOOGLE).iterator().next();
        testWrite_file(connector);
    }

    @Test
    public void testGoogleConnectorContains() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.GOOGLE).iterator().next();
        testContains(connector);
    }

    @Test
    public void testGoogleConnectorStage_file_to_cache_location() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.GOOGLE).iterator().next();
        testStage_file_to_cache_location(connector);
    }

    @Test
    public void testGoogleConnectorDelete() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.GOOGLE).iterator().next();
        testDelete(connector);
    }

    @Test
    public void testGoogleConnectorGet_free_space() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.GOOGLE).iterator().next();
        testGet_free_space(connector);
    }

    @Test
    public void testS3ConnectorWrite_file() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.S3).iterator().next();
        testWrite_file(connector);
    }

    @Test
    public void testS3ConnectorContains() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.S3).iterator().next();
        testContains(connector);
    }

    @Test
    public void testS3ConnectorStage_file_to_cache_location() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.S3).iterator().next();
        testStage_file_to_cache_location(connector);
    }

    @Test
    public void testS3ConnectorDelete() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.S3).iterator().next();
        testDelete(connector);
    }

    @Test
    public void testS3ConnectorGet_free_space() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.S3).iterator().next();
        testGet_free_space(connector);
    }

    @Test
    public void testDiskConnectorWrite_file() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.DISK).iterator().next();
        testWrite_file(connector);
    }

    @Test
    public void testDiskConnectorContains() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.DISK).iterator().next();
        testContains(connector);
    }

    @Test
    public void testDiskConnectorStage_file_to_cache_location() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.DISK).iterator().next();
        testStage_file_to_cache_location(connector);
    }

    @Test
    public void testDiskConnectorDelete() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.DISK).iterator().next();
        testDelete(connector);
    }

    @Test
    public void testDiskConnectorGet_free_space() throws Exception {
        Location connector = Environment.data().get_dao_location().find_all_by_type(Location.TYPE.DISK).iterator().next();
        testGet_free_space(connector);
    }

    @Test
    public void testMakeFilePersistent() {
        File testfile = testfilelist.entrySet().iterator().next().getValue();
        java.io.File real_file = testfile.get_real_file();
        Location disk = Location.find_by_type(Location.TYPE.DISK).iterator().next();
        long free_space = disk.get_free_space();
        user.remove_storagepool(Location.TYPE.GOOGLE);
        user.remove_storagepool(Location.TYPE.S3);
        // file uploaded?
        testfile.test_import_file(testfile.get_real_file().getAbsolutePath());
        assertTrue(testfile.get_persistent());
        free_space -= real_file.length();
        long verify_space = disk.get_free_space();
        assertTrue(verify_space == free_space);

        // second user with a new file
        User second_user = new User(12, 2, "user", "pw", 10, 20, 30, somethinglikedropbox);
        second_user.add_storagepool(Location.TYPE.DISK);
        second_user.db_update();
        File second_file = new File(second_user.get_rootfolder(), "testfile2");
        assertTrue(second_file.test_import_file(real_file.getAbsolutePath()));

        assertTrue("dedup working", disk.get_free_space() == free_space);

        java.io.File file2 = null;
        for (Map.Entry<String, File> file : testfilelist.entrySet()) {
            if (!file.getKey().equals(testfile.get_current_checksum())) {
                file2 = file.getValue().get_real_file();
                break;
            }
        }
        assertNotNull(file2);
        // third file for second user, no dedup cause new file
        File third_file = new File(second_user.get_rootfolder(), "testfile3");
        third_file.test_import_file(file2.getAbsolutePath());
        free_space -= file2.length();
        assertTrue("no dedup, reducing free", disk.get_free_space() == free_space);


        user.add_storagepool(Location.TYPE.GOOGLE);
        user.add_storagepool(Location.TYPE.S3);
    }
}
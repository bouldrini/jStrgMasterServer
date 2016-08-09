package jStrg.tests.unit_tests;

import jStrg.file_system.*;
import jStrg.network_management.storage_management.core.Location;
import jStrg.tests.helper;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static jStrg.tests.helper.create_locations;
import static jStrg.tests.helper.search_testfile_path;
import static org.junit.Assert.*;

/**
 * Created by henne on 15.06.16.
 */
public class ClusterManagerTest {

    private static Application somethinglikedropbox;
    private static List<java.io.File> testfilelist = new ArrayList<>();
    private static User user;

    @BeforeClass
    public static void before() throws Exception {
        Settings.read_global_config();
        helper.clearEnvironment();
    }

    @AfterClass
    public static void after() throws Exception {

    }

    @Before
    public void setUp() throws Exception {


        somethinglikedropbox = new Application("SomethingLikeDropbox");

        create_locations(somethinglikedropbox);

        user = new User(0, 0, "testuser", "bla", 0, 50000, 50000, somethinglikedropbox);
        helper.setupStoragePools(user);
        user.db_update();

        for (java.io.File file : search_testfile_path().listFiles()) {
            testfilelist.add(file);
        }
        System.out.println("<-- setup done, begin test -->");

    }

    @After
    public void tearDown() throws Exception {
        somethinglikedropbox = null;
        helper.clearEnvironment();
    }


    @Test
    public void testHas_enough_space() throws Exception {

    }

    @Test
    public void testLightScrub() throws Exception {

        File file = new File(user.get_rootfolder(), "testfile");

        file.test_import_file(testfilelist.get(0).getAbsolutePath());
        FileVersion version = file.get_current_version();

        Location location = Location.find_by_type(Location.TYPE.DISK).iterator().next();
        assertNotNull(location);

        assertTrue(somethinglikedropbox.m_cluster_manager.zombie_versions().size() == 0);

        location.delete(version.get_checksum());
        assertFalse(location.contains(version.get_checksum()));

        assertTrue(somethinglikedropbox.m_cluster_manager.zombie_versions().size() == 1);

        assertTrue(somethinglikedropbox.m_cluster_manager.lightScrub());

        assertTrue(somethinglikedropbox.m_cluster_manager.zombie_versions().size() == 0);

        for (Location.TYPE type : user.storagepools()) {
            Location.find_by_type(type).iterator().next().delete(version.get_checksum());
        }

        assertFalse(somethinglikedropbox.m_cluster_manager.lightScrub());

        assertTrue(somethinglikedropbox.m_cluster_manager.zombie_versions().size() == 1);

    }

    @Test
    public void testZombie_versions() throws Exception {
        File file = new File(user.get_rootfolder(), "testfile");

        file.test_import_file(testfilelist.get(0).getAbsolutePath());
        FileVersion version = file.get_current_version();

        Location location = Location.find_by_type(Location.TYPE.DISK).iterator().next();
        assertNotNull(location);

        assertTrue(somethinglikedropbox.m_cluster_manager.zombie_versions().size() == 0);

        location.delete(version.get_checksum());
        assertFalse(location.contains(version.get_checksum()));

        assertTrue(somethinglikedropbox.m_cluster_manager.zombie_versions().size() == 1);

        assertNotNull(location.write_file(testfilelist.get(0), version));

        assertTrue(somethinglikedropbox.m_cluster_manager.zombie_versions().size() == 0);

    }

    @Test
    public void testRecover() throws Exception {

        File file = new File(user.get_rootfolder(), "testfile");

        file.test_import_file(testfilelist.get(0).getAbsolutePath());
        FileVersion version = file.get_current_version();

        Location location = Location.find_by_type(Location.TYPE.DISK).iterator().next();
        assertNotNull(location);

        assertTrue(somethinglikedropbox.m_cluster_manager.zombie_versions().size() == 0);

        location.delete(version.get_checksum());
        assertFalse(location.contains(version.get_checksum()));

        assertTrue(somethinglikedropbox.m_cluster_manager.zombie_versions().size() == 1);

        assertTrue(somethinglikedropbox.m_cluster_manager.recover(version));

        assertTrue(somethinglikedropbox.m_cluster_manager.zombie_versions().size() == 0);

    }
}
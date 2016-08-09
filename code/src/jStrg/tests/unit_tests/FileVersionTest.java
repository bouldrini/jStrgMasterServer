package jStrg.tests.unit_tests;

import jStrg.file_system.*;
import jStrg.network_management.storage_management.core.Location;
import jStrg.network_management.storage_management.core.StorageCell;
import jStrg.tests.helper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static jStrg.tests.helper.create_locations;
import static jStrg.tests.helper.get_testfilelist;
import static org.junit.Assert.*;

/**
 * Created by henne on 07.06.16.
 */
public class FileVersionTest {

    private static User testuser = null;
    private static Application app = null;
    private static LinkedHashMap<String, File> testfilelist;

    @BeforeClass
    public static void once_before() {
        Settings.read_global_config();
        helper.clearEnvironment();
    }

    @org.junit.Before
    public void setUp() throws Exception {
        app = new Application("somethinglikedropbox");
        testuser = new User(0, 0, "user1", "pw", 0, 0, 0, app);
        create_locations(app);
        testfilelist = get_testfilelist(testuser, app);
        helper.setupStoragePools(testuser);
        testuser.db_update();
    }

    @After
    public void tearDown() {
        helper.clearEnvironment();
    }

    @Test
    public void testFind_by_file() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {
        User testuser2 = new User(0, 0, "user2", "pw", 0, 0, 0, app);
        helper.setupStoragePools(testuser2);
        testuser2.db_update();
        java.io.File testfile = testfilelist.entrySet().iterator().next().getValue().get_real_file();
        // two users, same files
        File file_user1 = new File(testuser.get_rootfolder(), "testfile_user1");
        File file_user2 = new File(testuser2.get_rootfolder(), "testfile_user2");
        file_user1.test_import_file(testfile.getAbsolutePath());
        file_user2.test_import_file(testfile.getAbsolutePath());
        // successful upload?
        assertTrue(file_user1.get_persistent());
        assertTrue(file_user2.get_persistent());

        // get a new file
        java.io.File new_file = null;
        for (Map.Entry<String, File> entry : testfilelist.entrySet()) {
            if (entry.getValue().get_real_file() != null && entry.getValue().get_real_file().length() != testfile.length()) {
                new_file = entry.getValue().get_real_file();
                break;
            }
        }
        assertNotNull(new_file);
        String new_file_chksum = Location.file_checksum(new_file);
        // user2 uploads a new file, and will delete old version
        file_user2.test_import_file(new_file.getAbsolutePath());
        FileVersion to_be_removed = file_user2.get_current_version().get_previous();
        assertNotNull(to_be_removed);

        to_be_removed.delete();
        // user1 is still able to download his file
        java.io.File get_real_file = Location.stage_file_to_cache(file_user1.get_current_version());
        assertNotNull(get_real_file);
        String chksum_to_be_removed = file_user1.get_current_version().get_checksum();
        assertTrue(Location.file_checksum(get_real_file).equals(chksum_to_be_removed));
        // save a location that contains that version
        Set<StorageCell> locations = file_user1.get_current_version().get_location();
        for (StorageCell cell : locations) {
            assertTrue(cell.contains(chksum_to_be_removed));
        }

        // user1 uploads new version
        file_user1.test_import_file(new_file.getAbsolutePath());
        assertNotNull(file_user1.get_current_version().get_previous());
        // user1 deletes old version
        file_user1.get_current_version().get_previous().delete();
        assertTrue(FileVersion.find_by_chksum(chksum_to_be_removed).isEmpty());
        // old version not longer existing in backend
        for (StorageCell cell : locations) {
            assertFalse(cell.contains(chksum_to_be_removed));
        }

        // new version still downloadable
        get_real_file = Location.stage_file_to_cache(file_user1.get_current_version());
        assertNotNull(get_real_file);
        assertEquals(Location.file_checksum(get_real_file), new_file_chksum);
        get_real_file = Location.stage_file_to_cache(file_user2.get_current_version());
        assertNotNull(get_real_file);
        assertEquals(Location.file_checksum(get_real_file), new_file_chksum);
    }

    @Test
    public void testFind_by_app() throws Exception {
        User testuser2 = new User(0, 0, "user2", "pw", 0, 0, 0, app);
        helper.setupStoragePools(testuser2);
        testuser2.db_update();
        java.io.File testfile = testfilelist.entrySet().iterator().next().getValue().get_real_file();
        // two users, same files
        File file_user1 = new File(testuser.get_rootfolder(), "testfile_user1");
        File file_user2 = new File(testuser2.get_rootfolder(), "testfile_user2");
        file_user1.test_import_file(testfile.getAbsolutePath());
        file_user2.test_import_file(testfile.getAbsolutePath());
        // successful upload?
        assertTrue(file_user1.get_persistent());
        assertTrue(file_user2.get_persistent());

        List<FileVersion> versionlist = FileVersion.find_by_app(app);

        assertFalse(versionlist.size() == 0);
        assertTrue(versionlist.size() == 2);

        for (FileVersion version : versionlist) {
            assertTrue(
                    file_user1.get_current_version().get_checksum().equals(version.get_checksum())
                            || file_user2.get_current_version().get_checksum().equals(version.get_checksum())
            );
        }
    }

    @Test
    public void testDeleteAll() throws Exception {

    }

    @Test
    public void testFind_by_chksum() throws Exception {

    }

    @Test
    public void testFile() throws Exception {

    }
}
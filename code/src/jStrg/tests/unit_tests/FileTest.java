package jStrg.tests.unit_tests;

import jStrg.file_system.Application;
import jStrg.file_system.File;
import jStrg.file_system.Settings;
import jStrg.file_system.User;
import jStrg.tests.helper;
import org.junit.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by henne on 25.01.16.
 */
public class FileTest {

    private static Application somethinglikedropbox;
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
        user = new User(0, 0, "testuser", "bla", 0, 50000, 50000, somethinglikedropbox);
        helper.create_locations(somethinglikedropbox);
    }

    @After
    public void tearDown() throws Exception {
        somethinglikedropbox = null;
        user = null;
        helper.clearEnvironment();
    }

    @Test
    public void testTestfile_to_cache() {
        java.io.File file = helper.testfile_to_cache();
        assertNotNull(file);
        assertTrue(Files.exists(Paths.get(file.toString())));
        System.out.println("file copied to:" + file.getAbsolutePath());
    }

    @Test
    public void testGet_privileges() throws Exception {

    }

    @Test
    public void testGet_current_checksum() throws Exception {

    }

    @Test
    public void testAll() throws Exception {

    }

    @Test
    public void testFind() throws Exception {

    }

    @Test
    public void testFind_by_filepath() throws Exception {

    }

    @Test
    public void testAdd_privilege() throws Exception {

    }

    @Test
    public void testInit_privileges() throws Exception {

    }

    @Test
    public void testApplication() throws Exception {

    }

    @Test
    public void testFirst() throws Exception {

    }

    @Test
    public void testLast() throws Exception {

    }

    @Test
    public void testFile_folder() throws Exception {

    }

    @Test
    public void testTest_simple_get_file() throws Exception {

    }

    @Test
    public void testTest_simple_get_file1() throws Exception {

    }

    @Test
    public void testRestore_file() throws Exception {

    }

    @Test
    public void testTest_import_file() throws Exception {

    }

    @Test
    public void testTest_sync_file() throws Exception {

    }

    @Test
    public void testGet_file_by_path() throws Exception {

    }

    @Test
    public void testGet_id() throws Exception {

    }

    @Test
    public void testGet_real_file() throws Exception {

    }

    @Test
    public void testSet_real_file() throws Exception {

    }

    @Test
    public void testGet_real_size() throws Exception {

    }

    @Test
    public void testUpdate_real_size() throws Exception {

    }

    @Test
    public void testGet_persistent() throws Exception {

    }

    @Test
    public void testGet_user_id() throws Exception {

    }

    @Test
    public void testGet_current_version() throws Exception {

    }

    @Test
    public void testToString() throws Exception {

    }

    @Test
    public void testToPath() throws Exception {

    }

    @Test
    public void testGet_parent() throws Exception {

    }

    @Test
    public void testGet_path() throws Exception {

    }

    @Test
    public void testSet_parent() throws Exception {

    }

    @Test
    public void testValid_for_file_folder() throws Exception {

    }
}
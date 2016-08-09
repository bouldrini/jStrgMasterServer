package jStrg.tests.unit_tests;

import jStrg.file_system.Application;
import jStrg.file_system.FileFolder;
import jStrg.file_system.Settings;
import jStrg.file_system.User;
import jStrg.tests.helper;
import org.junit.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by henne on 29.06.16.
 */
public class FileFolderTest {

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
    }

    @After
    public void tearDown() throws Exception {
        somethinglikedropbox = null;
        user = null;
        helper.clearEnvironment();
    }

    @Test
    public void testDb_update() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {
        FileFolder rootfolder = user.get_rootfolder();

        FileFolder folder1 = new FileFolder(rootfolder, "toBeDeleted");
        FileFolder folder2 = new FileFolder(folder1, "toBeDeleted");

        assertNotNull(FileFolder.find(folder1.get_id()));
        assertNotNull(FileFolder.find(folder2.get_id()));

        folder1.delete();

        assertNull(FileFolder.find(folder1.get_id()));
        assertNull(FileFolder.find(folder2.get_id()));

    }

    @Test
    public void testDelete_all() throws Exception {

    }

    @Test
    public void testFind_rootfolder() throws Exception {

    }

    @Test
    public void testFind() throws Exception {

    }

    @Test
    public void testPrivileges() throws Exception {

    }

    @Test
    public void testGet_path() throws Exception {

    }

    @Test
    public void testGet_filefolder_by_path() throws Exception {

    }

    @Test
    public void testGet_files() throws Exception {

    }

    @Test
    public void testGet_folders() throws Exception {

    }

    @Test
    public void testGet_parent() throws Exception {

    }

    @Test
    public void testSet_parent_folder() throws Exception {

    }
}
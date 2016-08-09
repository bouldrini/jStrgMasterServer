package jStrg.tests.unit_tests;

import jStrg.data_types.privileges.AccessModifier;
import jStrg.environment.Environment;
import jStrg.file_system.*;
import jStrg.network_management.storage_management.core.Location;
import jStrg.tests.helper;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class UserTest {

    private static Application app;
    private User testUser;
    private String application_title = "SomethingLikeDropbox";

    @BeforeClass
    public static void onceBefore() {
        Settings.read_global_config();
    }

    @Before
    public void setUp() throws Exception {
        helper.clearEnvironment();
        app = new Application(application_title);
        this.testUser = new User(12, 2, "user", "pw", 10, 20, 30, app);
    }

    @After
    public void tearDown() throws Exception {
        helper.clearEnvironment();
    }


    @Test
    public void testAuthenticate() throws Exception {
        assertTrue(User.authenticate("user", "pw", app));
    }

    @Test
    public void testHas_storagepool() throws Exception {
        assertFalse(testUser.has_storagepool(Location.TYPE.CACHE));
        testUser.add_storagepool(Location.TYPE.GOOGLE);
        assertTrue(testUser.has_storagepool(Location.TYPE.GOOGLE));
        testUser.remove_storagepool(Location.TYPE.GOOGLE);
        assertFalse(testUser.has_storagepool(Location.TYPE.GOOGLE));
    }

    @Test
    public void testRemove_storagepool() throws Exception {
        testUser.add_storagepool(Location.TYPE.GOOGLE);
        assertTrue(testUser.has_storagepool(Location.TYPE.GOOGLE));
        testUser.remove_storagepool(Location.TYPE.GOOGLE);
        assertFalse(testUser.has_storagepool(Location.TYPE.GOOGLE));
    }

    @Test
    public void testAdd_storagepool() throws Exception {
        testUser.add_storagepool(Location.TYPE.GOOGLE);
        assertTrue(testUser.has_storagepool(Location.TYPE.GOOGLE));
        testUser.remove_storagepool(Location.TYPE.GOOGLE);
    }


    @Test
    public void testFind() throws Exception {
        assertEquals(testUser, User.find(testUser.get_id()));
    }

    @Test
    public void testLast() throws Exception {
        assertEquals(testUser, User.last(app));
        User userTest2 = new User(2, 2, "user2", "pw", 10, 20, 30, app);
        assertEquals(userTest2, User.last(app));
        helper.clearEnvironment();
        assertNull(User.last(app));
    }

    @Test
    public void testFindByLogin() throws Exception {
        assertEquals(testUser, User.find_by_name("user", app));
        assertNotEquals(testUser, User.find_by_name("user1", app));
        assertNotEquals(testUser, User.find_by_name("user2", app));
        assertNotEquals(testUser, User.find_by_name("user3", app));
        assertNotEquals(testUser, User.find_by_name("user1", app));
        assertNotEquals(testUser, User.find_by_name("user1", app));
        assertNotEquals(testUser, User.find_by_name("user4", app));
        assertNotEquals(testUser, User.find_by_name("user1", app));
    }

    @Test
    public void testPrivileges() throws Exception {
        List<Privilege> p = new ArrayList<Privilege>();
        List<Privilege> privilegeList = new ArrayList<Privilege>();
        p.addAll(Privilege.all()); //add auto generated Privileg
        User testUser2 = new User(2, 2, "user2", "pw", 10, 20, 30, app);

        Privilege priv1 = new Privilege(0, testUser.get_id(), app, 0, Privilege.TYPE.FILEFOLDER, true, true, true, true, true);
        Privilege priv2 = new Privilege(0, testUser2.get_id(), app, 0, Privilege.TYPE.FILEFOLDER, true, true, true, true, false);
        p.add(priv1);

        assertThat(testUser.privileges().toArray(), IsNot.not(IsEqual.equalTo(testUser2.privileges().toArray())));
        assertArrayEquals(p.toArray(), testUser.privileges().toArray());
    }

    @Test
    public void testFile_folders() throws Exception {
        List<FileFolder> fileFolders = new ArrayList<FileFolder>();
        for (Privilege p : testUser.privileges()) {
            if (p.m_privilegable_type == Privilege.TYPE.FILEFOLDER) {
                fileFolders.add(p.privilegable());
            }
        }
        assertArrayEquals(fileFolders.toArray(), testUser.file_folders().toArray());
        Privilege priv1 = new Privilege(0, testUser.get_id(), app, 0, Privilege.TYPE.FILEFOLDER, true, true, true, true, true);
        fileFolders.add(priv1.privilegable());
        assertArrayEquals(fileFolders.toArray(), testUser.file_folders().toArray());
        Privilege priv2 = new Privilege(0, testUser.get_id(), app, 0, Privilege.TYPE.FILE, true, true, true, true, true);
        assertArrayEquals(fileFolders.toArray(), testUser.file_folders().toArray());
    }

    @Test
    public void testFiles() throws Exception {
        assertTrue("no files", testUser.files().isEmpty());
        File testfile = new File(testUser.get_rootfolder(), "testfile");
        Privilege priv2 = Privilege.find_user_privilege_for_entiy(testUser, testfile);
        assertNotNull("manual search for privilege", priv2);

        Set<File> file = new LinkedHashSet<>();
        file.add(priv2.privilegable());
        Object[] file_from_priv2 = file.toArray();
        Object[] file_to_test = testUser.files().toArray();
        assertArrayEquals(file_from_priv2, file_to_test);
        assertTrue(file_to_test.length == 1);

        User second_user = new User(0, 0, "user2", "pw", 0, 0, 0, app);
        File shared_file = new File(second_user.get_rootfolder(), "sharedfile");
        Privilege shared = new Privilege(0, testUser.get_id(), app, shared_file.get_id(), Privilege.TYPE.FILE, true, false, false, false, false);
        Set<File> files_to_be_shared = new LinkedHashSet<>();
        files_to_be_shared.add(shared_file);
        files_to_be_shared.add(testfile);
        Set<File> file_shared_test = testUser.files();

        assertTrue(file_shared_test.size() == 2);

        for (File loopfile : files_to_be_shared) {
            boolean found = false;
            for (File test : file_shared_test) {
                if (test.equals(loopfile)) {
                    found = true;
                }
            }
            assertTrue(found);
        }
    }

    @Test
    public void testGet_id() throws Exception {
        int userid = User.find(testUser.get_id()).get_id();
        assertEquals(userid, testUser.get_id());
    }


    @Test
    public void testPrivilege_for() throws Exception {
        FileFolder fileFolder = new FileFolder(testUser.get_rootfolder(), "testFolder");
        assertEquals(fileFolder.privileges().get(0), testUser.privilege_for(fileFolder));
    }

    @Test
    public void testPrivilege_for1() throws Exception {
        FileFolder fileFolder = new FileFolder(testUser.get_rootfolder(), "testFolder");
        File file = new File(fileFolder, "test.txt");
        //assertEquals(file.get_privileges().get(0), testUser.privilege_for(file));
    }

    @Test
    public void testGet_rootfolder() throws Exception {
        assertNotNull(testUser.get_rootfolder());
        FileFolder folder_to_find = null;
        Set<FileFolder> userfolders = testUser.file_folders();
        // search all folders and check against user folders and get user root folders
        boolean alreadyfoundone = false;
        for (FileFolder folder : FileFolder.all()) {
            for (FileFolder userowned_folder : userfolders) {
                if (folder.get_id() == userowned_folder.get_id() && folder.is_root_folder()) {
                    folder_to_find = folder;
                    assertFalse("here should be only one root folder", alreadyfoundone);
                    alreadyfoundone = true;
                }
            }
        }

        assertNotNull("found folder", folder_to_find);
        assertTrue(testUser.get_rootfolder().get_id() == folder_to_find.get_id());
    }

    @Test
    public void testToString() throws Exception {
        assertNotNull(testUser);
    }

    @Test
    public void testDb_update() throws Exception {
        String newpassword = "thisisaneverusedpassword_82347489247236487213647237542";
        testUser.set_password(newpassword);
        int id = testUser.get_id();
        testUser = null;
        Environment.data().closeDBconnection();
        Environment.data().openDBconnection();
        User verifyuser = User.find(id);

        assertTrue(verifyuser.verify_password(newpassword));
    }

    @Test
    public void testAll() throws Exception {
        assertTrue(User.all().size() == 1);

        int to_add = 5;
        for (int index = 0; index < to_add; ++index) {
            new User(0, 0, "test" + index, "pass" + index, 0, 0, 0, app);
        }
        Environment.data().closeDBconnection();
        Environment.data().openDBconnection();
        assertTrue(User.all().size() == 1 + to_add);
        helper.clearEnvironment();
        assertTrue(User.all().size() == 0);
    }

    @Test
    public void testSet_password() throws Exception {
        String newpassword = "thisisaneverusedpassword_82347489247236487213647237542";
        testUser.set_password(newpassword);
        int id = testUser.get_id();
        testUser = null;
        User verifyuser = User.find(id);
        assertFalse(verifyuser.verify_password("novalidpasssword"));
        assertTrue(verifyuser.verify_password(newpassword));
    }

    @Test
    public void testApplication() throws Exception {
        assertNotNull(testUser.application());
    }

    @Test
    public void testOnDbLoad() throws Exception {
        int userid = testUser.get_id();
        for (Location.TYPE type : Location.TYPE.values()) {
            testUser.remove_storagepool(type);
        }
        testUser = null;
        Environment.data().closeDBconnection();
        Environment.data().openDBconnection();
        User newuser = User.find(userid);
        for (Location.TYPE type : Location.TYPE.values()) {
            assertFalse(newuser.has_storagepool(type));
        }

        for (Location.TYPE type : Location.TYPE.values()) {
            newuser.add_storagepool(type);
            newuser.db_update();
        }
        newuser = null;
        Environment.data().closeDBconnection();
        Environment.data().openDBconnection();

        newuser = User.find(userid);

        for (Location.TYPE type : Location.TYPE.values()) {
            if (type == Location.TYPE.CACHE) {
                assertFalse(newuser.has_storagepool(type));
            } else {
                assertTrue(newuser.has_storagepool(type));
            }
        }
    }

    @Test
    public void testChmod() {
        File file = new File(testUser.get_rootfolder(), "testfile");

        Privilege priv = Privilege.find_user_privilege_for_entiy(testUser, file);
        AccessModifier modifier_to_test = priv.get_modifier();
        AccessModifier modifier_blueprint = new AccessModifier();
        modifier_blueprint.set_read(true);
        modifier_blueprint.set_write(true);
        modifier_blueprint.set_delete(true);
        modifier_blueprint.set_invite(true);

        assertEquals(modifier_blueprint.toString(), modifier_to_test.toString());

        // now change the modifier
        modifier_blueprint.set_write(false);
        priv.chmod(modifier_blueprint);
        priv.db_update();
        priv = null;
        Environment.data().closeDBconnection();
        Environment.data().openDBconnection();

        // check
        priv = Privilege.find_user_privilege_for_entiy(testUser, file);
        modifier_to_test = priv.get_modifier();
        assertEquals(modifier_blueprint.toString(), modifier_to_test.toString());

        // now change all attributes
        modifier_blueprint.set_write(true);
        modifier_blueprint.set_delete(false);
        modifier_blueprint.set_invite(false);
        modifier_blueprint.set_read(false);

        priv.chmod(modifier_blueprint);
        priv.db_update();
        priv = null;
        Environment.data().closeDBconnection();
        Environment.data().openDBconnection();

        // check
        priv = Privilege.find_user_privilege_for_entiy(testUser, file);
        modifier_to_test = priv.get_modifier();
        assertEquals(modifier_blueprint.toString(), modifier_to_test.toString());
    }
}
package jStrg.tests.unit_tests.console_tests;

import jStrg.data_types.exceptions.ConfigException;
import jStrg.file_system.*;
import jStrg.network_management.storage_management.core.Location;
import jStrg.simple_console.Console;
import jStrg.tests.helper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import static org.junit.Assert.*;

/**
 * test through jstrg console cmd
 * This is testing logic to decide wether files are pushed or pulled, creates folder hierarchies and a lot more
 */
public class jstrgTest {


    private static Console console;
    private static Application somethinglikedropbox;
    private static User user;
    private static LinkedList<java.io.File> testfilelist;

    private static String application_title = "SomethingLikeDropbox";

    @BeforeClass
    public static void once_before() throws ConfigException, Exception {
        Settings.read_global_config();
        helper.clearEnvironment();
        somethinglikedropbox = new Application(application_title);
        somethinglikedropbox = Application.find_by_title(application_title);
        Settings.user_folder_root = "/tmp/test_user_folder";

        helper.createUserFolder();
        console = Console.getInstance(somethinglikedropbox.get_id());
        console.init_console();
        user = new User(0, 0, "testuser", "bla", 0, 50000, 50000, somethinglikedropbox);
        user.add_storagepool(Location.TYPE.DISK);
        user.db_update();
        //testfilelist = helper.get_testfilelist(user, somethinglikedropbox);
        helper.create_locations(somethinglikedropbox);
        java.io.File testdataRoot = new java.io.File("code/testdata/");
        System.out.println("Looking for files in " + testdataRoot);
        java.io.File filelist[] = testdataRoot.listFiles();
        System.out.println(filelist);

        testfilelist = new LinkedList<>();
        for (java.io.File file : filelist) {
            System.out.println("found testfile: " + file);
            testfilelist.add(file);
        }
    }

    @AfterClass
    public static void once_after() {
        console = null;
        somethinglikedropbox = null;
        helper.deleteFolder(new java.io.File(Settings.user_folder_root));
        testfilelist = null;
        helper.clearEnvironment();
    }

    @Test
    public void testRun() throws Exception {
        console.debug_pass_cmd("context switch " + user.get_id());
        for (java.io.File file : testfilelist) {
            console.debug_pass_cmd("put " + file);
        }
        for (File file : user.get_rootfolder().get_files()) {
            assertTrue(file.get_persistent());
        }
        console.debug_pass_cmd("jstrg sync");
        for (File file : user.get_rootfolder().get_files()) {
            assertTrue(Files.exists(Paths.get(Settings.user_folder_root + "/" + file.get_title())));
        }
        console.debug_pass_cmd("mkdir bla");
        console.debug_pass_cmd("cd bla");
        System.out.println("current working dir: " + console.get_context_folder().get_path());
        FileFolder assertfolder = FileFolder.get_filefolder_by_path("bla", user);
        assertNotNull(assertfolder);
        FileFolder contextfolder = console.get_context_folder();
        assertNotNull(contextfolder);
        assertTrue(contextfolder.toString().equals(assertfolder.toString()));
        System.out.println("expected: /" + FileFolder.get_filefolder_by_path("bla", user).get_title());


        console.debug_pass_cmd("jstrg sync");

        Files.createDirectory(Paths.get(Settings.user_folder_root + "/bla/blubb"));

        for (java.io.File file : testfilelist) {
            console.debug_pass_cmd("put " + file);
        }
        Files.copy(Paths.get(testfilelist.iterator().next().getAbsolutePath()), Paths.get(Settings.user_folder_root + "/bla/blubb/testfile"));
        console.debug_pass_cmd("jstrg sync");

        for (File file : user.get_rootfolder().get_files()) {
            assertTrue(Files.exists(Paths.get(Settings.user_folder_root + "/bla/" + file.get_title())));
        }
        File new_file = File.get_file_by_path("/bla/blubb/testfile", user);
        assertNotNull(new_file);
        assertTrue(new_file.get_persistent());

        console.debug_pass_cmd("jstrg sync");
        console.debug_pass_cmd("jstrg sync");
        console.debug_pass_cmd("jstrg sync");

        assertTrue(FileFolder.get_filefolder_by_path("bla/blubb", user).get_files().size() == 1);
        FileFolder.all().size();
        boolean found = false;
        for (FileFolder folder : user.get_rootfolder().get_folders()) {
            if (folder != null) {
                if (folder.get_title().equals("bla")) {
                    assertFalse("multiple instances of the same folder", found);
                    found = true;
                }
            }
        }
    }
}
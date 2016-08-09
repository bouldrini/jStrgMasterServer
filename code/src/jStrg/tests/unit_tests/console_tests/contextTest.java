package jStrg.tests.unit_tests.console_tests;

import jStrg.file_system.Application;
import jStrg.file_system.Settings;
import jStrg.file_system.User;
import jStrg.simple_console.Console;
import jStrg.tests.helper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by henne on 14.02.16.
 */
public class contextTest {

    private static Console console;
    private static Application somethinglikedropbox;
    private static User user;

    @BeforeClass
    public static void once_before() {
        helper.clearEnvironment();
        somethinglikedropbox = new Application("SomethingLikeDropbox");
        Settings.user_folder_root = "/tmp/test_user_folder";

        try {
            Files.createDirectory(Paths.get(Settings.user_folder_root));
            console = Console.getInstance(somethinglikedropbox.get_id());
            console.init_console();
        } catch (IOException e) {
            Settings.LOGGER.severe("failure while init console: " + e);
        }
        user = new User(0, 0, "testuser", "bla", 0, 50000, 50000, somethinglikedropbox);
    }

    @AfterClass
    public static void once_after() {
        console = null;
        somethinglikedropbox = null;
        try {
            Files.delete(Paths.get(Settings.user_folder_root));
        } catch (IOException e) {
            System.out.println("Error while deleting: " + Settings.user_folder_root);
        }
        helper.clearEnvironment();
    }


    @Test
    public void testHas_rights() throws Exception {

    }

    @Test
    public void testRun() throws Exception {
        console.debug_pass_cmd("context switch admin");
        assertNull(console.get_context_user());
        console.debug_pass_cmd("context switch exit");
        console.debug_pass_cmd("context switch " + user.get_id());
        assertNotNull(console.get_context_user());
        assertTrue(console.get_context_user() == user);

    }
}
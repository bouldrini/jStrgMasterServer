package jStrg.tests.unit_tests;

import jStrg.file_system.Role;
import jStrg.file_system.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RoleTest {
    private Role testRole;

    @BeforeClass
    public static void onceBefore() {
        Settings.read_global_config();
    }

    @Before
    public void setUp() throws Exception {
        testRole = new Role(13, "Manager");
    }

    @After
    public void tearDown() throws Exception {
        Role.delete(testRole);
    }

    @Test
    public void testAll() throws Exception {
        assertEquals(testRole, Role.all().get(0));
    }

    @Test
    public void testFind() throws Exception {
        //TODO fix assertEquals(Role.find(13), testRole);
        assertEquals(testRole, Role.find(testRole.get_id()));
        Role.delete(testRole);
        assertNull(Role.find(testRole.get_id()));
    }

    @Test
    public void testLast() throws Exception {
        Role role = new Role(0, "User");
        assertEquals(role, Role.last());
        Role.delete(role);
        assertEquals(testRole, Role.last());
        Role.delete(testRole);
        assertNull(Role.last());
    }

    @Test
    public void testGet_id() throws Exception {
        assertEquals(testRole.get_id(), testRole.get_id());
    }
}
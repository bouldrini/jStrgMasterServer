package jStrg.file_system;

// REQUIREMENTS

import jStrg.database.DatabaseEntity;
import jStrg.database.IGenericDao;
import jStrg.environment.Environment;

import javax.persistence.Entity;
import java.util.Arrays;
import java.util.List;

@Entity
public class Role extends DatabaseEntity {

    static List<String> persistent_attributes = Arrays.asList("id", "title");
    // ATTRIBUTES
    String m_title;

    public Role() {
    }

    // CONSTRUCTORS
    public Role(int _id, String _title) {
        this.m_title = _title;

        dao().create(this);
    }

    // DATABASE TRANSACTIONS

    /**
     * query for all roles existing in the database
     *
     * @return list of roles
     */
    public static List<Role> all() {
        return dao().findAll();
    }

    /**
     * get dao object of this class
     *
     * @return dao object
     */
    private static IGenericDao dao() {
        return Environment.data().get_dao(Role.class);
    }

    public static void delete(Role _role) {
        dao().delete(_role);
    }

    /**
     * deletes all entrys in database for this class
     */
    public static void delete_all() {
        dao().deleteAll();
    }

    /**
     * query for a Role by ID
     *
     * @param _role_id int
     * @return User
     */
    public static Role find(int _role_id) {
        return (Role) dao().findById(_role_id);
    }

    /**
     * query for a roles by its title
     *
     * @param _title string
     * @return Role
     */
    public static Role find_by_title(String _title) {
        Role role = null;
        for (Role cur_role : all()) {
            if (cur_role.m_title == _title) role = cur_role;
        }
        ;
        return role;
    }

    /**
     * query for the last role existing in database
     *
     * @return Role
     */
    public static Role last() {
        List<Role> roles = dao().findAll();
        return (roles.size() != 0) ? roles.get(roles.size() - 1) : null;
    }

    // RELATIONS

    // GETTER / SETTER
    public int get_id() {
        return m_id;
    }

    // HELPER
    public String toString() {
        StringBuilder returnstring = new StringBuilder("<Role::{");
        returnstring.append("m_id: " + this.m_id);
        returnstring.append(", m_title: '" + this.m_title);
        returnstring.append("'}>");
        return returnstring.toString();
    }
}

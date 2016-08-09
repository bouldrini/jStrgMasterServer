package jStrg.file_system;

// REQUIREMENTS

import jStrg.data_types.privileges.AccessModifier;
import jStrg.database.DatabaseEntity;
import jStrg.database.IGenericDao;
import jStrg.database.IPrivilegeDao;
import jStrg.environment.Environment;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

@Table(
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"m_privilegable_id", "m_user_m_id", "m_privilegable_type"})
)
@Entity
public class Privilege extends DatabaseEntity {

    static List<String> persistent_attributes = Arrays.asList("id", "authorisable_id");
    public TYPE m_privilegable_type;
    // ATTRIBUTES
    @ManyToOne
    User m_user;
    int m_privilegable_id;
    @ManyToOne
    Application m_application;
    private boolean m_read = false;
    private boolean m_write = false;
    private boolean m_delete = false;
    private boolean m_invite = false;
    public Privilege() {

    }
    // CONSTRUCTORS
    public Privilege(int _id, int _user_id, Application _application, int _privilegable_id, TYPE _privilegable_type, boolean _read, boolean _write, boolean _delete, boolean _invite, boolean _owner) {
        AccessModifier modifier = new AccessModifier();
        modifier.set_read(_read);
        modifier.set_write(_write);
        modifier.set_delete(_delete);
        modifier.set_invite(_invite);
        init_privilege(_user_id, _application, _privilegable_id, _privilegable_type, modifier);
    }
    public Privilege(int _user_id, Application _application, int _privilegable_id, TYPE _privilegable_type, AccessModifier _modifier) {
        init_privilege(_user_id, _application, _privilegable_id, _privilegable_type, _modifier);
    }

    /**
     * get dao object of this class
     *
     * @return dao object
     */
    private static IGenericDao dao() {
        return Environment.data().get_dao(MethodHandles.lookup().lookupClass());
    }

    /**
     * class specific dao
     *
     * @return dao object
     */
    private static IPrivilegeDao specific_dao() {
        return Environment.data().get_dao_privilege();
    }

    // DATABASE TRANSACTIONS

    /**
     * query for all privileges
     *
     * @return list of Privileges
     */
    public static List<Privilege> all() {
        return dao().findAll();
    }

    /**
     * query for all privileges of a specific application
     *
     * @param _application int
     * @return list of Privileges
     */
    public static List<Privilege> all(Application _application) {
        return specific_dao().application_privileges(_application);
    }

    /**
     * query for a privilege by ID
     *
     * @param _privilege_id int
     * @return User
     */
    public static Privilege find(int _privilege_id) {
        return (Privilege) dao().findById(_privilege_id);
    }

    /**
     * deletes all entrys in database for this class
     */
    public static void delete_all() {
        dao().deleteAll();
    }

    /**
     * query for the first privilege of a specific application
     *
     * @param _application int
     * @return Privilege
     */
    public static Privilege first(Application _application) {
        List<Privilege> privileges = specific_dao().application_privileges(_application);
        return (privileges.size() != 0) ? privileges.get(0) : null;
    }

    /**
     * query for the last user of a specific application
     *
     * @param _application int
     * @return Privilege
     */
    public static Privilege last(Application _application) {
        List<Privilege> privileges = specific_dao().application_privileges(_application);
        return (privileges.size() != 0) ? privileges.get(privileges.size() - 1) : null;
    }

    /**
     * get all privileges of a specific user
     * @param _user user
     * @return list of privileges
     */
    public static List<Privilege> user_privileges(User _user) {
        return specific_dao().user_privileges(_user);
    }

    /**
     * get a type of privileges of a specific user
     * @param _user user
     * @param _type type
     * @return list of privileges
     */
    public static List<Privilege> user_privileges(User _user, Privilege.TYPE _type) {
        return specific_dao().user_privileges(_user, _type);
    }

    /**
     * search for the users privilege to a specific file
     *
     * @param _user view on filesystem
     * @param _file file to look at
     * @return found privilege, null of no privilege exists
     */
    public static Privilege find_user_privilege_for_entiy(User _user, File _file) {
        return specific_dao().find_user_privilege_for_file(_user, _file);
    }

    public static Privilege find_user_privilege_for_entiy(User _user, FileFolder _folder) {
        return specific_dao().find_user_privilege_for_filefolder(_user, _folder);
    }

    /**
     * get a type of privileges with specific rights of a specific user
     *
     * @return list of privileges
     */
    public static List<Privilege> user_privileges(User _user, Privilege.TYPE _type, AccessModifier _modifier) {
        return specific_dao().user_privileges(_user, _type, _modifier);
    }

    // RELATIONS

    private void init_privilege(int _user_id, Application _application, int _privilegable_id, TYPE _privilegable_type, AccessModifier _modifier) {

        this.m_user = User.find(_user_id);
        this.m_application = _application;
        this.m_privilegable_id = _privilegable_id;
        this.m_privilegable_type = _privilegable_type;
        this.chmod(_modifier);
        dao().create(this);
    }

    /**
     * query for the last privilege of a specific application
     * =======
     * updates object in database
     */
    public void db_update() {
        dao().update(this);
    }

    /**
     * get a access modifier object with parameters of this privilege, can be used for changing or searching
     *
     * @return modifier filled with values of privilege
     */
    public AccessModifier get_modifier() {
        AccessModifier modifier = new AccessModifier();
        modifier.set_read(m_read);
        modifier.set_write(m_write);
        modifier.set_delete(m_delete);
        modifier.set_invite(m_invite);
        return modifier;
    }

    /**
     * pass a AccessModifier object to configure this privilege
     *
     * @param _modifier
     */
    public void chmod(AccessModifier _modifier) {
        if (_modifier.read() != null)
            m_read = _modifier.read();
        if (_modifier.write() != null)
            m_write = _modifier.write();
        if (_modifier.delete() != null)
            m_delete = _modifier.delete();
        if (_modifier.invite() != null)
            m_invite = _modifier.invite();
    }

    /**
     * query for the unknown thing the privilege object grants privileges to, commonly a file folder or a file
     *
     * @param <Privilegable> commonly a file folder or a file
     * @return Privilegable (might be a file folder or a file)
     */
    public <Privilegable> Privilegable privilegable() {
        Privilegable privilegable = null;
        if (this.m_privilegable_type == TYPE.FILE) {
            privilegable = (Privilegable) File.find(m_privilegable_id);
        } else if (this.m_privilegable_type == TYPE.FILEFOLDER) {
            privilegable = (Privilegable) FileFolder.find(this.m_privilegable_id);
        }
        return privilegable;
    }

    /**
     * query for the user this privilege belongs to
     *
     * @return User
     */
    public User user() {
        return m_user;
    }

    public int get_id() {
        return m_id;
    }

    public boolean read() {
        return m_read;
    }

    public boolean write() {
        return m_write;
    }

    public boolean delete() {
        return m_delete;
    }

    public boolean invite() {
        return m_invite;
    }

    // HELPER
    public String toString() {
        StringBuilder returnstring = new StringBuilder("<Privelege::{");
        returnstring.append("m_id: " + this.m_id);
        returnstring.append(", m_user_id: " + this.m_user.get_id());
        returnstring.append(", m_privilegable_id: " + this.m_privilegable_id);
        returnstring.append(", m_privilegable_type: '" + this.m_privilegable_type);
        returnstring.append("', m_read: " + (this.m_read ? "true" : "false"));
        returnstring.append(", m_write: " + (this.m_write ? "true" : "false"));
        returnstring.append(", m_delete: " + (this.m_delete ? "true" : "false"));
        returnstring.append(", m_invite: " + (this.m_invite ? "true" : "false"));
        returnstring.append("}>");
        return returnstring.toString();
    }

    public String toJson(){
        StringBuilder json = new StringBuilder();
        json.append("{\"read\" : ").append(this.m_read).append(",");
        json.append("\"write\" : ").append(this.m_write).append(",");
        json.append("\"del\" : ").append(this.m_delete).append(",");
        json.append("\"invite\" : ").append(this.m_invite).append("}");
        return json.toString();
    }

    public enum TYPE {
        FILE,
        FILEFOLDER
    }
}
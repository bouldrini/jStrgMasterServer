package jStrg.file_system;

// REQUIREMENTS

import jStrg.data_types.privileges.AccessModifier;
import jStrg.data_types.security.Secret;
import jStrg.database.DatabaseEntity;
import jStrg.database.IGenericDao;
import jStrg.database.IUserDao;
import jStrg.environment.Environment;
import jStrg.network_management.storage_management.core.Location;
import jStrg.network_management.storage_management.internal.StorageServer;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.persistence.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.logging.Logger;

@Entity
public class User extends DatabaseEntity {

    private final static Logger LOGGER = Logger.getLogger(Settings.location_logging_target);
    static List<String> persistent_attributes = Arrays.asList("id", "role_id", "lastname", "firstname");
    @Column(unique = true)
    public String m_username;
    public long m_used_space;
    public long m_total_space;
    public long m_unused_space;
    @ManyToOne
    public StorageServer m_storage_server;
    // ATTRIBUTES

    @ManyToOne
    Role m_role;

    @ManyToOne
    Application m_application;
    private String m_secret;
    private String m_salt;
    private long m_last_sync;
    @Transient
    private LinkedHashSet<Location.TYPE> m_storagepool;
    private Boolean m_pool_s3 = false;
    private Boolean m_pool_disk = false;
    private Boolean m_pool_google = false;
    private Boolean m_pool_server = false;
    private String m_session_id;

    public User() {
    }
    // CONSTRUCTORS
    public User(String _username, String _password) {
        init_user(Role.find_by_title("admin"), _username, _password, 0L, 0L, 0L, null);
    }
    public User(int _id, String _username, String _password, Application _application) {
        init_user(Role.find_by_title("operator"), _username, _password, 0L, 0L, 0L, _application);
    }

    public User(int _id, int _role_id, String _username, String _password, long _used_space, long _unused_space, long _total_space, Application _application) {
        init_user(Role.find(_role_id), _username, _password, _used_space, _unused_space, _total_space, _application);
    }

    private void init_user(Role _role, String _username, String _password, long _used_space, long _unused_space, long _total_space, Application _application) {
        m_role = _role;
        m_username = _username;
        Secret secret = generate_secret(_password);
        m_secret = secret.key_encoded();
        m_salt = secret.salt_encoded();
        m_used_space = _used_space;
        m_total_space = _total_space;
        m_unused_space = _unused_space;
        m_application = _application;

        // allocate the users space in all clusters

//        try {
//            if (application().m_setting.m_use_disk_location) {
//                this.m_storage_server = application().get_free_storage_server();
//                System.out.println("set storage server to " + m_storage_server);
//            }
//        } catch (NullPointerException e) {
//            // catch until depency between non working storage servers and application is resolved
//        }

        m_last_sync = 0L;
        build_storagepool();
        dao().create(this);
        // Create default (root) folder
        FileFolder root_foler = new FileFolder("", this); // Folder with empty name == root folder
    }

    /**
     * get dao object of this class
     *
     * @return dao object
     */
    private static IGenericDao dao() {
        return Environment.data().get_dao(User.class);
    }

    /**
     * special db funktions for this class
     *
     * @return dao
     */
    private static IUserDao specific_dao() {
        return Environment.data().get_dao_user();
    }

    // DATABASE

    /**
     * deletes all entrys in database for this class
     */
    public static void delete_all() {
        // TODO cleanup files and filefolders
        dao().deleteAll();
    }

    /**
     * authenticates login data for a specific application
     *
     * @param _username    string
     * @param _password    string
     * @param _application int
     * @return boolean
     */
    static public boolean authenticate(String _username, String _password, Application _application) {
        User user = find_by_name(_username, _application);
        return user != null && user.verify_password(_password);
    }

    /**
     * authenticates login data
     *
     * @param _username string
     * @param _password string
     * @return boolean success
     */
    static public boolean authenticate(String _username, String _password) {
        User user = find_by_name(_username);
        return user != null && user.verify_password(_password);
    }

    /**
     * query for all users of a specific application
     *
     * @return list of users
     */
    public static List<User> all() {
        return dao().findAll();
    }

    // AUTHENTICATION

    /**
     * query for all users of a specific application
     *
     * @param _application int
     * @return list of users
     */
    public static List<User> all(Application _application) {
        return specific_dao().find_all_of_application(_application);
    }

    /**
     * query for a user by ID
     *
     * @param _user_id int
     * @return User
     */
    public static User find(int _user_id) {
        return (User) dao().findById(_user_id);
    }

    // STORAGE

    /**
     * query for the last user of a specific application
     *
     * @param _application int
     * @return User
     */
    public static User last(Application _application) {
        List<User> users = User.all(_application);
        return (users.size() != 0) ? users.get(users.size() - 1) : null;
    }

    /**
     * find a user of an application by his login data
     *
     * @param _username    string
     * @param _application int
     * @return User
     */
    public static User find_by_name(String _username, Application _application) {
        return specific_dao().find_by_name(_username, _application);
    }

    /**
     * find a user by his login data
     *
     * @param _username string
     * @return User
     */
    public static User find_by_name(String _username) {
        return specific_dao().find_by_name(_username);
    }


    // DATABASE TRANSACTIONS

    private void build_storagepool() {
        m_storagepool = new LinkedHashSet<>();
        if (m_pool_disk)
            m_storagepool.add(Location.TYPE.DISK);
        if (m_pool_google)
            m_storagepool.add(Location.TYPE.GOOGLE);
        if (m_pool_s3)
            m_storagepool.add(Location.TYPE.S3);
        if (m_pool_server)
            m_storagepool.add(Location.TYPE.SERVER);
    }

    /**
     * updates object in database
     */
    public void db_update() {
        dao().update(this);
    }

    /**
     * checks if a user has a specific storage pool type
     *
     * @param _type Location.TYPE
     * @return boolean
     */
    public boolean has_storagepool(Location.TYPE _type) {
        return m_storagepool.contains(_type);
    }

    /**
     * removes a users storage pool type
     *
     * @param _type Location.TYPE
     */
    public void remove_storagepool(Location.TYPE _type) {
        if (m_storagepool.contains(_type)) {
            m_storagepool.remove(_type);
        }
        switch (_type) {
            case DISK:
                m_pool_disk = false;
                break;
            case S3:
                m_pool_s3 = false;
                break;
            case GOOGLE:
                m_pool_google = false;
                break;
            case SERVER:
                m_pool_server = false;
                break;
        }
    }

    /**
     * adds a storage pool type to a users
     *
     * @param _type Location.TYPE
     */
    public void add_storagepool(Location.TYPE _type) {
        if (!m_storagepool.contains(_type)) {
            m_storagepool.add(_type);
        }
        switch (_type) {
            case DISK:
                m_pool_disk = true;
                break;
            case GOOGLE:
                m_pool_google = true;
                break;
            case S3:
                m_pool_s3 = true;
                break;
            case SERVER:
                m_pool_server = true;
                break;
        }
    }

    /**
     * takes a password and a salt and genrates a secret object with hashed secret.
     *
     * @param _password cleartext password
     * @param _salt     Base64 encoded salt
     * @return Secret object. null in case of fatal error
     * @see Secret
     */
    private Secret get_hashed_secret(String _password, String _salt) {

        Secret secret = new Secret();
        byte[] salt = Base64.getDecoder().decode(_salt);

        try {
            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(Settings.m_password_hash_alg);
            PBEKeySpec spec = new PBEKeySpec(_password.toCharArray(), salt, Settings.m_password_hash_iterations, Settings.m_password_key_length);
            SecretKey generatedkey = keyfactory.generateSecret(spec);
            secret.set_key(generatedkey.getEncoded());
            secret.set_salt(salt);
            return secret;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.severe("key algorithm or spec not available. passwords don't work. " + e);
        }
        return null;
    }

    /**
     * internal, generate hashed password and salt
     *
     * @param _password password in cleartext
     * @return secret object with new generated data that can be used to store on disk
     */
    private Secret generate_secret(String _password) {
        // get a new salt
        final Random randomgenerator = new SecureRandom();
        byte[] salt = new byte[Settings.m_password_salt_length];
        randomgenerator.nextBytes(salt);

        return get_hashed_secret(_password, Base64.getEncoder().encodeToString(salt));
    }

    /**
     * function for other classes to update password for a user
     *
     * @param _password new password in cleartext
     */
    public void set_password(String _password) {
        Secret secret = generate_secret(_password);
        if (secret != null) {
            m_secret = secret.key_encoded();
            m_salt = secret.salt_encoded();
            db_update();
            LOGGER.fine("user: " + m_username + " has updated his password.");
        } else {
            LOGGER.warning("failed update password for user: " + m_username);
        }
    }

    /**
     * get all privileges of a specific user
     *
     * @return list of privileges
     */
    public List<Privilege> privileges() {
        return Privilege.user_privileges(this);
    }

    // RELATIONS

    /**
     * get all file folders that belong to a specific user
     *
     * @return list of file folders
     */
    public Set<FileFolder> file_folders() {
        Set<FileFolder> filefolders = new LinkedHashSet<>();
        AccessModifier modifier = new AccessModifier();
        modifier.set_read(true);
        for (Privilege privilege : Privilege.user_privileges(this, Privilege.TYPE.FILEFOLDER, modifier)) {
            filefolders.add(privilege.privilegable()); //TODO possible to cast entries inside a list?
        }
        return filefolders;
    }

    /**
     * get all files that belong to a specific user
     *
     * @return list of files
     */
    public Set<File> files() {
        Set<File> files = new LinkedHashSet<>();
        AccessModifier modifier = new AccessModifier();
        modifier.set_read(true);
        for (Privilege privilege : Privilege.user_privileges(this, Privilege.TYPE.FILE, modifier)) {
            files.add(privilege.privilegable()); //TODO possible to cast entries inside a list?
        }
        return files;
    }

    /**
     * query for the application the user belongs to
     *
     * @return Application
     */
    public Application application() {
        return m_application;
    }

    // GETTER / SETTER
    public int get_id() {
        return m_id;
    }

    /**
     * last timestamp a user successfully synced his files, not used yet
     *
     * @return unix timestamp
     */
    public long get_last_sync() {
        return m_last_sync;
    }

    public void set_last_sync(long m_last_sync) {
        this.m_last_sync = m_last_sync;
    }

    /**
     * m_last_sync as java Date
     *
     * @return java date
     */
    public Date get_last_sync_as_date() {
        return new Date(m_last_sync);
    }

    /**
     * setter, takes current system time
     */
    public void update_last_sync() {
        m_last_sync = System.currentTimeMillis();
    }

    // HELPER

    /**
     * verifies given password string
     *
     * @param _password password in cleartext
     * @return password match
     */
    public boolean verify_password(String _password) {
        Secret verifythis = get_hashed_secret(_password, m_salt);
        return verifythis != null && m_secret.equals(verifythis.key_encoded());
    }

    /**
     * get a users privilege for a specific file folder
     *
     * @param _file_folder FileFolder
     * @return list of file folders
     */
    public Privilege privilege_for(FileFolder _file_folder) {
        return Privilege.find_user_privilege_for_entiy(this, _file_folder);
    }

    /**
     * get a users privilege for a specific file
     *
     * @param _file File
     * @return list of files
     */
    public Privilege privilege_for(File _file) {
        return Privilege.find_user_privilege_for_entiy(this, _file);
    }

    /**
     * get a users root folder
     *
     * @return rootfolder object
     */
    public FileFolder get_rootfolder() {
        return FileFolder.find_rootfolder(this);
    }

    /**
     * check if a user has a specific role
     *
     * @param _role Role
     * @return boolean
     */
    public boolean has_role(Role _role) {
        if (this.m_role.get_id() == _role.get_id()) return true;
        return false;
    }

    public Set<Location.TYPE> storagepools() {
        return m_storagepool;
    }

    public String toJson() {
        StringBuilder jSon = new StringBuilder("\"User\" : {");
        jSon.append("\"id\" : ").append(get_id()).append(",");
        jSon.append("\"username\" : ").append("\"").append(m_username).append("\",");
        jSon.append("\"total_space\" : ").append(m_total_space).append(",");
        jSon.append("\"used_space\" : ").append(m_unused_space).append(",");
        jSon.append("\"role_id\" : ").append(m_role.get_id()).append(",");
        jSon.append("\"role_name\" : ").append("\"").append(m_role.m_title).append("\",");
        jSon.append("\"session_id\" : ").append("\"").append(m_session_id).append("\",");
        jSon.append("\"application_id\" : ").append(m_application.get_id()).append(",");
        jSon.append("\"root_folder_id\" : ").append(get_rootfolder().get_id());
        jSon.append("}");
        return jSon.toString();
    }

    public String toString() {
        StringBuilder returnstring = new StringBuilder("<User::{");
        returnstring.append("m_id: " + m_id);
        returnstring.append(", m_application_id: " + application().get_id());
        returnstring.append(", m_username: " + m_username);
        returnstring.append(", m_total_space: " + m_total_space);
        returnstring.append(", m_used_space: " + m_used_space);
        returnstring.append(", m_unused_space: " + m_unused_space);
        returnstring.append(", last_sync: '" + get_last_sync_as_date());
        return returnstring.append(" }>").toString();
    }

    @PostLoad
    public void onDbLoad() {
        build_storagepool();
    }

    public boolean has_enough_space(long _file_size) {
        return this.m_unused_space > _file_size;
    }

    public void set_session_id(String _session_id) {
        this.m_session_id = _session_id;
    }

    public String get_session_id(){ return this.m_session_id;}
}

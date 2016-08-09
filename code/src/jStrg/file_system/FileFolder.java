package jStrg.file_system;

// REQUIREMENTS

import jStrg.database.DatabaseEntity;
import jStrg.database.IFileFolderDao;
import jStrg.database.IGenericDao;
import jStrg.environment.Environment;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
public class FileFolder extends DatabaseEntity {

    // CONSTRUCTORS
    public FileFolder() {
    }
    /**
     * this constructor is used to create a regular folder
     * !! DO NOT USE TO CREATE ROOTFOLDER !!!
     *
     * @param _parent parent rootfolder, must not be null. not checked
     * @param _title
     */
    public FileFolder(FileFolder _parent, String _title) {
        init_file_folder(_parent, _parent.get_user(), _title, _parent.get_user());
    }
    /**
     * create a new filefolder for given user
     *
     * @param _title name
     * @param _user  user that doesnt has a rootfolder yet
     */
    public FileFolder(String _title, User _user) {
        init_file_folder(null, _user, _title, _user);
    }

    public FileFolder(String _title, User _user, User _uploader) {
        init_file_folder(null, _user, _title, _uploader);
    }

    private boolean init_file_folder(FileFolder _parent, User _user, String _title, User _uploader) {
        boolean result = false;
        m_uploader = _uploader;
        m_title = _title;
        m_user = _user;
        m_application = _user.application();

        if (_parent != null) {
            m_parent_folder = _parent;
        } else {
            m_parent_folder = null;
            m_root_folder = true;
        }

        dao().create(this);
        Privilege privilege = new Privilege(0, m_user.get_id(), m_application, m_id, Privilege.TYPE.FILEFOLDER, true, true, true, true, true);
        return result;
    }

    // ATTRIBUTES
    private Boolean m_root_folder = false;

    // RELATIONS
    @ManyToOne
    private User m_user;
    @ManyToOne
    private User m_uploader;
    @ManyToOne
    public Application m_application;
    @ManyToOne
    private FileFolder m_parent_folder; // eine hierachiebene höher
    String m_title;
    @ManyToMany
    public Set<FileType> m_file_types;

    // DATABASE TRANSACTIONS
    /**
     * query for all file folders
     *
     * @return List of FileFolders
     */
    public static List<FileFolder> all() {
        return dao().findAll();
    }


    // DATABASE TRANSACTIONS

    /**
     * query for all file folders of a specific application
     *
     * @param _app_id int
     * @return List of FileFolders
     */
    public static List<FileFolder> all_of_application(int _app_id) {
        // TODO: use _application_id
        return dao().findAll();
    }

    /**
     * get dao object of this class
     *
     * @return dao object
     */
    private static IGenericDao dao() {
        return Environment.data().get_dao(FileFolder.class);
    }

    /**
     * get class specific dao
     *
     * @return dao object
     */
    private static IFileFolderDao specific_dao() {
        return Environment.data().get_dao_filefolder();
    }



    public void db_update() {
        dao().update(this);
    }

    public static List<FileFolder> find_by_parent(FileFolder _parent) {
        return specific_dao().find_by_parent(_parent);
    }

    /**
     * deletes this firefolder from db. Instance belongs, but not useful. Dont save a reference to this.
     */
    public void delete() {
        for (File file : this.get_files()) {
            file.delete();
        }
        for (FileFolder folder : this.get_folders()) {
            folder.delete();
        }
        dao().delete(this);
    }

    /**
     * deletes all entrys in database for this class
     */
    public static void delete_all() {
        dao().deleteAll();
    }

    public static FileFolder find_rootfolder(User _user) {
        return specific_dao().find_rootfolder(_user);
    }

    /**
     * query for the last file folder of a specific application
     *
     * @param _application_id int
     * @return FileFolder
     */
    public static FileFolder last(int _application_id) {
        // TODO: use _application_id
        //TODO db query
        return (all().size() != 0) ? all().get(all().size() - 1) : null;
    }

    /**
     * query for a file folder by ID
     *
     * @param _file_folder_id int
     * @return FileFolder
     */
    public static FileFolder find(int _file_folder_id) {
        return (FileFolder) dao().findById(_file_folder_id);
    }

    /**
     * search for a filefolder id by given path. If nothing is found, "-1" is returned.
     *
     * @param _path complete path for that folder
     * @param _user owner
     * @return FileFolder
     */
    public static FileFolder get_filefolder_by_path(String _path, User _user) {
        _path = _path.startsWith("/") ? _path.substring(1) : _path;
        String path[] = _path.split("/");
        if (path.length == 0) {
            return null;
        }
        List<FileFolder> folderlist_to_check = specific_dao().find_by_title(path[path.length - 1], _user);

        // check for the searched folder and validate path. Multiple folders with same name but different paths possible
        for (FileFolder folder_to_validate : folderlist_to_check) {
            FileFolder check_for_root_folder = folder_to_validate;
            for (int index = path.length - 1; index >= 0; index--) {
                if (check_for_root_folder.m_title.equals(path[index])) { // walk the path upwards and validate names
                    check_for_root_folder = check_for_root_folder.get_parent();
                } else {
                    check_for_root_folder = null;
                    break;
                }
            }
            if (check_for_root_folder != null) {
                if (check_for_root_folder.m_title.equals("") && check_for_root_folder.is_root_folder()) {
                    return folder_to_validate; // Folder found, loop can terminate
                }
            }
        }
        return null;
    }

    private boolean init_file_folder(FileFolder _parent, User _user, String _title) {
        boolean result = false;

        this.m_title = _title;
        this.m_user = _user;
        this.m_application = _user.application();

        if (_parent != null) {
            m_parent_folder = _parent;
        } else {
            m_parent_folder = null;
            m_root_folder = true;
        }

        dao().create(this);
        Privilege privilege = new Privilege(0, m_user.get_id(), m_application, this.m_id, Privilege.TYPE.FILEFOLDER, true, true, true, true, true);
        return result;
    }

    // RELATIONS

    /**
     * query for all privileges that exists for this file folder
     *
     * @return list of privileges
     */
    public List<Privilege> privileges() {
        List<Privilege> privileges = new ArrayList<Privilege>();
        for (Privilege privilege : Privilege.all()) {
            if (privilege.m_privilegable_type == Privilege.TYPE.FILEFOLDER && privilege.m_privilegable_id == m_id) {
                privileges.add(privilege);
            }
        }
        return privileges;
    }

//    /**
//     * query for all file type mappings for this file folder
//     *
//     * @return list of file type mappings
//     */
//    public List<FileTypeMapping> file_type_mappings() {
//        List<FileTypeMapping> mappings = new ArrayList<FileTypeMapping>();
//        for (FileTypeMapping mapping : Environment.FILE_TYPE_MAPPINGS) {
//            if (mapping.m_file_folder_id == m_id) {
//                mappings.add(mapping);
//            }
//        }
//        return mappings;
//    }

    /**
     * query for the application the file folder belongs to
     *
     * @return Application
     */
    public Application application() {
        return m_application;
    }

    /**
     * query for all file types valid for this file folder
     *
     * @return list of file types
     */
//    public List<FileType> file_types() {
//        List<FileType> file_types = new ArrayList<FileType>();
//        for (FileTypeMapping mapping : file_type_mappings()) {
//            if (!file_types.contains(mapping.file_type())) {
//                file_types.add(mapping.file_type());
//            }
//        }
//        return file_types;
//    }

    // GETTER / SETTER
    public int get_id() {
        return m_id;
    }

    public String get_title() {
        return m_title;
    }

    public void set_title(String _title) {
        this.m_title = _title;
    }

    // HELPER
    public String toString() {
        StringBuilder returnstring = new StringBuilder("<FileFolder::{");
        returnstring.append("m_id: " + m_id);
        //if (application() != null) {
        returnstring.append(", m_application_id: " + application());
        /*} else {
            returnstring.append("none");
        }*/
        returnstring.append(", m_title: " + m_title);
        if (!dao().isActive()) {
            returnstring.append(", m_file_path: " + get_path());
        }
        returnstring.append(", m_user_id: '" + m_user.get_id()+ "'");
        returnstring.append(", m_uploader_id: '" + m_uploader.get_id()+ "'");
        returnstring.append(", m_parent_folder: " + m_parent_folder);
        return returnstring.append(" }>").toString();
    }

    /**
     * returns the complete path for that folder
     *
     * @return path as String
     */
    public String get_path() {
        String returnString = "";
        if (!m_title.equals("") && !is_root_folder()) {
            returnString = m_parent_folder.get_path() + "/" + m_title;
        }
        return returnString;
    }

    /**
     * search for files inside this folder
     *
     * @return set of fileids
     */
    public List<File> get_files() {
        return File.find_by_parent(this);
    }

    /**
     * search for folders inside this folder
     *
     * @return set of FileFolder objects
     */
    public List<FileFolder> get_folders() {
        return FileFolder.find_by_parent(this);
    }

    // TODO: complete javadoc
    public FileFolder get_parent() {
        return m_parent_folder;
    }

    // TODO: complete javadoc
    public void set_parent_folder(int m_parent_folder) {
        m_parent_folder = m_parent_folder;
    }


    public Boolean is_root_folder() {
        return m_root_folder;
    }

    public void set_root_folder(Boolean _root) {
        m_root_folder = _root;
    }

    public User get_user() {
        return m_user;
    }

    public static FileFolder create_by_path(String _file_path, User _user, User _uploader) {
        String folders[] = _file_path.split("/");
        String current_path = "";
        FileFolder first_existing_folder = null;

        int create_from_index = 0;
        for(int i = 0; i < folders.length; i++){
            if(!folders[i].equals("")){
                current_path = current_path + folders[i] + "/";
                FileFolder folder = FileFolder.get_filefolder_by_path(current_path, _user);
                if(folder != null){
                    first_existing_folder = folder;
                } else {
                    create_from_index = i;
                    break;
                }
            }
        }

        if(first_existing_folder == null){
            first_existing_folder = _user.get_rootfolder();
        } else {
            for(int k = create_from_index; k < folders.length; k++){
                FileFolder folder = new FileFolder(first_existing_folder, folders[k]);
                first_existing_folder = folder;
            }
        }

        return first_existing_folder;
    }

    public static FileFolder find_first_existing(String _file_path, User _user) {
        String folders[] = _file_path.split("/");
        String current_path = "";
        FileFolder first_existing_folder = null;

        int create_from_index = 0;

        for(int i = 0; i < folders.length; i++){
            if(!folders[i].equals("")){
                current_path = current_path + folders[i] + "/";
                System.out.println(current_path);
                FileFolder folder = FileFolder.get_filefolder_by_path(current_path, _user);
                if(folder != null){
                    first_existing_folder = folder;
                } else {
                    break;
                }
            }
        }

        if(first_existing_folder == null){
            first_existing_folder = _user.get_rootfolder();
        }
        return first_existing_folder;
    }

    public String toJson(int user_id) {
        StringBuilder json = new StringBuilder("\"Folder\" : {");
        json.append("\"id\" :").append(m_id).append(",");
        if (get_parent() != null) {
            json.append("\"parent_id\" :").append(get_parent().get_id()).append(",");
        }
        json.append("\"title\" :").append("\"").append(m_title).append("\",");
        json.append("\"privilege\" : ");
        this.privileges().stream().filter(privilege -> privilege.m_user.get_id() == user_id && privilege.read()).forEach(privilege -> json.append(privilege.toJson()));
        json.append(",");
        json.append("\"folders\" : [");
        for (FileFolder folder : get_folders()) {
            folder.privileges().stream().filter(privilege -> privilege.m_user.get_id() == user_id && privilege.read()).forEach(privilege -> {
                json.append("{").append("\"id\" : ").append(folder.get_id()).append(",");
                json.append("\"isFolder\" : ").append(true).append(",");
                json.append("\"privilege\" : ").append(privilege.toJson()).append(",");
                json.append("\"title\" : ").append("\"").append(folder.get_title()).append("\" },");
            });
        }
        if (json.charAt(json.length() - 1) == ',')
            json.deleteCharAt(json.length() - 1);
        json.append("],");
        json.append("\"files\" : [");
        for (File file : get_files()) {
            file.privileges().stream().filter(privilege -> privilege.m_user.get_id() == user_id && privilege.read()).forEach(privilege -> {
                json.append("{").append("\"id\" : ").append(file.get_id()).append(",");
                json.append("\"parent_id\" : ").append(this.get_id()).append(",");
                json.append("\"isFolder\" : ").append(false).append(",");
                json.append("\"privilege\" :").append(privilege.toJson()).append(",");
                json.append("\"title\" : ").append("\"").append(file.get_title()).append("\" },");
            });
        }
        if (json.charAt(json.length() - 1) == ',')
            json.deleteCharAt(json.length() - 1);
        json.append("]}");
        return json.toString();
    }
}

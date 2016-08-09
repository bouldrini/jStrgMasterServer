package jStrg.file_system;

// REQUIREMENTS

import jStrg.database.DatabaseEntity;
import jStrg.database.IFileDao;
import jStrg.database.IGenericDao;
import jStrg.environment.Environment;
import jStrg.network_management.storage_management.CacheFileLock;
import jStrg.network_management.storage_management.core.Location;
import jStrg.network_management.storage_management.core.StorageCell;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Entity
public class File extends DatabaseEntity {
    private final static Logger LOGGER = Logger.getLogger(Settings.location_logging_target);

    // CONSTRUCTORS

    public File() {
    }
    public File(FileFolder _parent, String _title) {
        init_file(_parent, _parent.get_user(), _title, _parent.application());
    }

    public File (FileFolder _parent, String _title, User _uploader) {
        init_file(_parent, _parent.get_user(), _title, _parent.application(), _uploader);
    }

    private void init_file(FileFolder _parent, User _user, String _title, Application _application, User _uploader) {String extension = "";

        int i = _title.lastIndexOf('.');
        if (i > 0) {
            m_file_extension = _title.substring(i+1);
        }


        m_persistent = false;
        m_uploader = _uploader;
        m_user = _user;
        m_application = _application;
        m_title = _title;
        m_parent = _parent;
        m_current_version = null;

        dao().create(this);
        Privilege privilege = new Privilege(0, get_user().get_id(), _application, m_id, Privilege.TYPE.FILE, true, true, true, true, true);
    }

    private void init_file(FileFolder _parent, User _user, String _title, Application _application) {
        init_file(_parent, _parent.get_user(), _title, _parent.application(), null);
    }

    // ATTRIBUTES

    private String m_file_extension;
    @ManyToOne
    private User m_user;
    @ManyToOne
    private User m_uploader;
    @ManyToOne
    private Application m_application;

    /**
     * folder which this file is located in
     */
    @ManyToOne
    private FileFolder m_parent;
    /**
     * user readable file name
     */
    private String m_title;
    /**
     * while file processing, this is the real file on the server
     */
    @Transient
    private java.io.File m_real_file;
    /**
     * size gotten from m_real_file
     */
    @Transient
    private long m_real_size;
    /**
     * when file is properly saved in backends, this flag is set
     */
    private Boolean m_persistent;
    /**
     * first file version id
     */
    @ManyToOne
    private FileVersion m_current_version;

    // DATABASE TRANSACTIONS
    public static List<File> all() {
        return dao().findAll();
    }

    public static List<File> find_by_parent(FileFolder _folder) {
        return specific_dao().find_by_parent(_folder);
    }

    /**
     * get dao object of this class
     *
     * @return dao object
     */
    private static IGenericDao dao() {
        return Environment.data().get_dao(File.class);
    }

    /**
     * get specific dao object of this class
     *
     * @return dao object
     */
    private static IFileDao specific_dao() {
        return Environment.data().get_dao_file();
    }

    /**
     * deletes all entrys in database for this class
     */
    public static void delete_all() {
        for (File file : all()) {
            file.delete();
        }
    }

    /**
     * query for a file by ID
     *
     * @param _file_id int
     * @return File
     */
    public static File find(int _file_id) {
        return (File) dao().findById(_file_id);
    }

    /**
     * query for the first file of a specific application
     *
     * @param _application_id int
     * @return File
     */
    public static File first(int _application_id) {
        return all().iterator().next();
    }

    /**
     * query for the last file of a specific application
     *
     * @param _application_id int
     * @return File
     */
    public static File last(int _application_id) {
        List<File> files = all();
        return (files.size() != 0) ? files.get(files.size() - 1) : null;
    }

    /**
     * simple get like an ftp client. Downloads the file and writes it to a alternative destination
     * always gets the last version of that file
     *
     * @param _id          fileid to get
     * @param _destination destination to write
     * @return boolean
     */
    public static boolean test_simple_get_file(int _id, String _destination) {
        File file = find(_id);
        if (file == null) {
            LOGGER.warning("File id " + _id + " not found in database");
            return false;
        }
        return test_simple_get_file(
                file
                , file.get_current_version()
                , _destination
        );
    }

    /**
     * simple get like an ftp client. Downloads a specific file version and writes it to a alternative destination
     *
     * @param _file        file o get
     * @param _fileversion version to get
     * @param _destination destination to write
     * @return boolean
     */
    public static boolean test_simple_get_file(File _file, FileVersion _fileversion, String _destination) { // Gets file_id and writes it to destination
        boolean returnbool = false;
        _file.set_real_file(Location.stage_file_to_cache(_fileversion));
        if (_file.get_real_file() == null) {
            LOGGER.severe("failure to stage file, staging returned true but real_file not present.");
            return false;
        }

        try {
            OutputStream output_stream = new FileOutputStream(_destination);
            BufferedOutputStream buffered_output = new BufferedOutputStream(output_stream);
            InputStream input_stream = new FileInputStream(_file.get_real_file().getAbsolutePath());
            BufferedInputStream buffered_input = new BufferedInputStream(input_stream);
            MessageDigest digester = MessageDigest.getInstance(Settings.default_hashing_algorithm);
            byte buffer[] = new byte[Settings.bytes_per_upload_chunk];
            int read;
            while ((read = buffered_input.read(buffer)) != -1) {
                buffered_output.write(buffer, 0, read);
                digester.update(buffer, 0, read);
            }

            buffered_input.close();
            buffered_output.close();
            output_stream.close();

            if (!DatatypeConverter.printHexBinary(digester.digest()).equals(_fileversion.get_checksum())) {
                LOGGER.warning("checksums doesn't match for file: " + _file + "got: " + DatatypeConverter.printHexBinary(digester.digest()));
                throw new IOException("Checksums doesn't match");
            }
            LOGGER.finest("download successful, Location: " + _destination + ", " + Settings.default_hashing_algorithm + ": " + _fileversion.get_checksum());
            java.io.File metadataworker = new java.io.File(_destination);
            if (!metadataworker.setLastModified(_fileversion.get_last_modified())) {
                LOGGER.warning("update the Metadata of " + metadataworker + " failed.");
            }
            returnbool = true;
        } catch (FileNotFoundException e) {
            LOGGER.warning("File not found." + e);
        } catch (IOException e) {
            LOGGER.warning("Error while processing data: " + e);

        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe("Digest Algorithm not found: " + e);
        } finally {
            try {
                if (!CacheFileLock.getInstance().release(_fileversion)) {
                    throw new IOException("file delete went wrong.");
                }
                _file.m_real_file = null;
            } catch (IOException e) {
                LOGGER.warning("Error while deleting cache file: " + e);
            } catch (NullPointerException e) {
                LOGGER.severe("Something went wrong, file not updated.");
            }
        }

        return returnbool;
    }

    /**
     * Searches for a file by path and userid.
     *
     * @param _path complete path for that file
     * @param _user id of the owner
     * @return File object
     */
    public static File get_file_by_path(String _path, User _user) {
        _path = _path.startsWith("/") ? _path.substring(1) : _path;
        String path[] = _path.split("/");
        String title = path[path.length - 1];

        for (File file : specific_dao().find_by_title(title, _user)) {
            if (file.get_path().equals("/" + _path)) {
                return file;
            }
        }
        return null;
    }


    // RELATIONS

    public String get_current_checksum() {
        return get_current_version().get_checksum();
    }

    /**
     * updates object in database
     */
    public void db_update() {
        dao().update(this);
    }

    /**
     * updates object in database
     */
    public void db_reread() {
        dao().findById(get_id());
    }

    /**
     * delete file, has to delete fileversion when there is a current version
     */
    public void delete() {
        specific_dao().delete(this);
    }

    /**
     * query for the application the file belongs to
     *
     * @return Application
     */
    public Application application() {
        return m_application;
    }

    /**
     * restores File in user folder
     *
     * @return success
     */
    public boolean restore_file() { // writes the file at the originating Location
        return File.test_simple_get_file(get_id(), Settings.user_folder_root + get_path());
    }

    /**
     * rollback file to a specific FileVersion. File modified timestamp is set to rollback date.
     * Git style: old fileversions remain, instead a new version is created as a copy with new timestamps
     *
     * @param _fileversionID id of the version
     * @return success
     */
    public boolean test_rollback_file(int _fileversionID) {
        boolean ret = false;
        FileVersion current_version = get_current_version();
        FileVersion rollback_to = FileVersion.find(_fileversionID);
        if (rollback_to != null && get_persistent() && !rollback_to.equals(current_version)) {
            FileVersion new_version = new FileVersion(rollback_to);
            new_version.set_last_modified(System.currentTimeMillis());
            new_version.set_previous(current_version);
            set_current_version(new_version);
            new_version.db_sync();
            this.db_update();
            ret = true;
        }

        return ret;
    }

    /**
     * imports a file to the system. File does not have to be in user root.
     * This file will be uploaded to the system. Does not alter any configurations like the path in user folder.
     *
     * @param _file complete path as string,
     * @return success
     */
    public boolean test_import_file(String _file) {
        FileVersion old_fileversion = get_current_version();
        FileVersion new_fileversion = new FileVersion(get_user(), this);
        boolean ret = test_import_file(_file, new_fileversion);

        if (ret) {
            set_current_version(new_fileversion);

            this.db_update();
            if (old_fileversion != null) {
                new_fileversion.set_previous(old_fileversion);
                new_fileversion.db_sync();
            }
        } else {
            new_fileversion.delete();
        }

        return ret;
    }

    /**
     * internal method for creating a specific file version, FileVersion must exist before.
     *
     * @param _file        path to the file that would be imported. for now a local file
     * @param _fileversion fileversion that corresponds to file
     * @return success
     */
    private boolean test_import_file(String _file, FileVersion _fileversion) { // Testklasse, dies muss durch einen ordentlichen Mechanismus ersetzt werden. Da kein Netzwerk etc. erlaubt gibt es das hier.
        Set<StorageCell> locationset = new LinkedHashSet<>();
        LOGGER.finest("new file import: " + _file);
        set_real_file(new java.io.File(_file));
        set_real_size(m_real_file.length());
        LOGGER.finest("filesize: " + m_real_size + " byte");
        Location cache_location = Location.get_cache_location_for_size(m_real_size);
        if (cache_location == null) {
            LOGGER.severe("no suitable cache location found for size: " + m_real_size);
            return false;
        }
        String cache_path = cache_location.get_path() + "/" + _fileversion.get_id() + ".tmp";
        CacheFileLock.getInstance().lock(_fileversion.get_id(), cache_path);
        // first, import file. Later this is done by streams, so it is here
        try {
            OutputStream output_stream = new FileOutputStream(cache_path);
            BufferedOutputStream buffered_output = new BufferedOutputStream(output_stream);
            InputStream input_stream = new FileInputStream(_file);
            BufferedInputStream buffered_input = new BufferedInputStream(input_stream);
            MessageDigest digester = MessageDigest.getInstance(Settings.default_hashing_algorithm);
            byte buffer[] = new byte[Settings.bytes_per_upload_chunk];
            int read;
            while ((read = buffered_input.read(buffer)) != -1) {
                buffered_output.write(buffer, 0, read);
                digester.update(buffer, 0, read);
            }

            buffered_input.close();
            buffered_output.close();
            output_stream.close();
            _fileversion.set_last_modified(get_real_file().lastModified());
            set_real_file(new java.io.File(cache_path));
            String checksum = DatatypeConverter.printHexBinary(digester.digest());
            _fileversion.set_checksum(checksum);
            _fileversion.set_size(get_real_size());
            LOGGER.finest("upload successful, Location: " + cache_path + ", " + Settings.default_hashing_algorithm + ": " + checksum);
            CacheFileLock.getInstance().set_readable(_fileversion.get_id());
            locationset = Location.make_file_persistent(this.get_real_file(), _fileversion);

            LOGGER.finest("make_file_persistent returned: " + locationset);
            if (locationset.size() > 0) {
                m_persistent = true;
                _fileversion.set_locationset(locationset);
                LOGGER.fine("new file import complete for id:" + m_id + " name: " + m_title + " checksum: " + checksum);
            }

        } catch (FileNotFoundException e) {
            LOGGER.warning("File not found." + e);
        } catch (IOException e) {
            LOGGER.warning("Error while processing data: " + e);

        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe("Digest Algorithm not found: " + e);
        } finally {
            try {
                if (!CacheFileLock.getInstance().release(_fileversion)) {
                    throw new IOException("file could not released: " + m_real_file);
                }
                m_real_file = null;
            } catch (IOException e) {
                LOGGER.warning("Error while deleting cache file: " + e);
            }
        }

        if (locationset.size() > 0) {
            return true;
        } else {
            LOGGER.log(Level.WARNING, "fileupload went wrong: " + this);
            return false;
        }
    }

    /**
     * syncing a file. Determines Path and modified timestamps and decides what to do.
     *
     * @return success or failure
     */
    public boolean test_sync_file() {
        LOGGER.finest("syncing file: " + this);
        boolean ret = false;
        boolean force_restore = false;
        long localfile_time = 0L;
        String absolutePath = Settings.user_folder_root + get_path();
        try {
            localfile_time = Files.getLastModifiedTime(Paths.get(absolutePath)).toMillis();
        } catch (IOException e) {
            if (e.getClass().getName().equals("java.nio.file.NoSuchFileException")) {
                LOGGER.finest("client out of sync, restoring file: " + this);
                force_restore = true;
            } else {
                LOGGER.warning("Error while accessing: " + absolutePath + " " + e);
            }
        } finally {
            FileVersion version = get_current_version();
            if (version == null) {
                LOGGER.finest("new file import: " + this);
                if (!Files.exists(Paths.get(absolutePath)))
                    LOGGER.severe("file found in database, but is not persistent and not present on filesystem");
                else
                    ret = test_import_file(absolutePath);
            } else if (!get_persistent() || version.get_last_modified() < localfile_time || (!get_persistent() && version.get_last_modified() == localfile_time)) {
                LOGGER.finest("local file newer: " + new Date(localfile_time) + " as: " + new Date(version.get_last_modified()));
                ret = test_import_file(absolutePath);
                if (ret)
                    get_current_version().set_last_modified(localfile_time);
            } else if (version.get_last_modified() > localfile_time || force_restore) {
                ret = restore_file();
                LOGGER.finest("remote file (" + new Date(version.get_last_modified()) + ") will be used. localfiletime: " + new Date(localfile_time));
            } else if (get_persistent() && version.get_last_modified() == localfile_time) {
                LOGGER.finest("File in sync: " + this);
                ret = true;
            } else {
                LOGGER.severe("Inconsistent Data found for file: " + this);
            }

        }
        return ret;
    }

    // Getter und Setter
    public int get_id() {
        return m_id;
    }

    public java.io.File get_real_file() {
        return m_real_file;
    }

    public void set_real_file(java.io.File m_real_file) {
        this.m_real_file = m_real_file;
    }

    public long get_real_size() {
        return m_real_size;
    }

    private void set_real_size(long _size) {
        m_real_size = _size;
    }

    public void set_title(String _title) {
        this.m_title = _title;
    }

    public boolean update_real_size() {
        if (get_real_file() != null) {
            set_real_size(get_real_file().length());
            return true;
        } else {
            return false;
        }
    }

    public Boolean get_persistent() {
        return m_persistent;
    }

    public User get_user() {
        return m_user;
    }

    public FileVersion get_current_version() {
        return m_current_version;
    }

    public void set_current_version(FileVersion m_current_version) {
        this.m_current_version = m_current_version;
    }

    // HELPER
    public String toString() {
        StringBuilder returnstring = new StringBuilder("<File::{m_id: " + m_id + ", m_application: ");
        if (application() != null) {
            returnstring.append(application().m_title);
        } else {
            returnstring.append("none");
        }
        returnstring.append(", m_user_id: " + m_user.get_id() + " m_title: '" + m_title + "' m_persistent: '" + m_persistent + "'");
        if (!dao().isActive() && get_parent() != null) {
            returnstring.append(", path: '" + get_path() + "'");
        }
        if (m_real_file != null) {
            returnstring.append(", File: '" + m_real_file + "'");
        }
        if(m_uploader != null){
            returnstring.append(", m_uploader_id: '" + m_uploader.get_id()+ "'");
        }
        return returnstring.append(" }>").toString();
    }

    public FileFolder get_parent() {
        return m_parent;
    }

    public void set_parent(FileFolder m_parent) {
        this.m_parent = m_parent;
    }

    public void set_persistent(boolean _persistent) {
        m_persistent = _persistent;
    }

    public String get_title() {
        return m_title;
    }

    /**
     * query for all privileges that exists for this file folder
     *
     * @return list of privileges
     */
    public List<Privilege> privileges() {
        List<Privilege> privileges = new ArrayList<Privilege>();
        for (Privilege privilege : Privilege.all()) {
            if (privilege.m_privilegable_type == Privilege.TYPE.FILE && privilege.m_privilegable_id == m_id) {
                privileges.add(privilege);
            }
        }
        return privileges;
    }

    /**
     * get complete path for this file
     *
     * @return String containing the path
     */
    public String get_path() {
        String ret = "";
        if (get_parent() != null) {
            ret = get_parent().get_path();
        } else {
            ret = "<no parent>";
            LOGGER.severe("no parent found for file: " + this);
        }
        return ret + "/" + m_title;
    }

    public String toJson(int user_id) {
        StringBuilder json = new StringBuilder("\"File\" : {");
        json.append("\"id\" :").append(m_id).append(",");
        json.append("\"parent_id\" : ").append(get_parent().get_id()).append(",");
        json.append("\"privilege\" : ");
        this. privileges().stream().filter(privilege -> privilege.m_user.get_id() == user_id && privilege.read()).forEach(privilege -> {
            json.append(privilege.toJson());
        });
        json.append(",");
        json.append("\"title\" : ").append("\"").append(m_title).append("\",");
        json.append("\"extension\" : ").append("\"").append(m_file_extension).append("\",");
        json.append("\"persistent\" : ").append(m_persistent);
        json.append("}");
        return json.toString();
    }

//    public boolean valid_for_file_folder(FileFolder _folder) {
//        for (FileType type : _folder.file_types()) {
//            if (this.m_file_extension.equals(type.m_file_extension)) {
//                return true;
//            }
//        }
//        return false;
//    }

}

package jStrg.file_system;

// REQUIREMENTS
// TODO: Complete Javadoc

import jStrg.database.DatabaseEntity;
import jStrg.database.IFileVersionDao;
import jStrg.database.IGenericDao;
import jStrg.environment.Environment;
import jStrg.network_management.storage_management.core.StorageCell;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
public class FileVersion extends DatabaseEntity {


    protected FileVersion() {
    }

    public FileVersion(File _file) {
        init_fileversion(_file);
    }

    public FileVersion(User _user, File _file) { //TODO delete
        init_fileversion(_file);
    }

    public FileVersion(FileVersion _other) {
        m_user = _other.user();
        m_file = _other.file();

        m_last_modified = _other.get_last_modified();
        m_size = _other.get_size();
        m_checksum = _other.get_checksum();
        m_location = _other.get_location();
        m_previous = _other.get_previous();
        dao().create(this);
    }

    private void init_fileversion(File _file) {
        m_user = _file.get_user();
        m_file = _file;
        m_last_modified = 0L;
        m_location = new LinkedHashSet<>();
        dao().create(this);
    }

    // ATTRIBUTES
    @ManyToOne
    User m_user;
    @ManyToOne
    File m_file;
    private long m_last_modified;

    // ATTRIBUTES
    private long m_size;
    private String m_checksum;
    @ManyToMany
    private Set<StorageCell> m_location;
    @OneToOne
    private FileVersion m_previous = null;

    // DATABASE TRANSACTIONS
    public static List<FileVersion> all() {
        return dao().findAll();
    }

    /**
     * get all fileversions of a file
     *
     * @param _file
     * @return
     */
    public static List<FileVersion> find_by_file(File _file) {
        return specific_dao().find_by_file(_file);
    }

    public static FileVersion find(int _id) {
        return (FileVersion) dao().findById(_id);
    }

    public static FileVersion last() {
        List<FileVersion> versions = all();
        return (versions.size() != 0) ? versions.get(versions.size() - 1) : null;
    }

    public static List<FileVersion> find_by_app(Application _app) {
        return specific_dao().find_by_app(_app);
    }

    /**
     * delete all versions
     */
    public static void deleteAll() { // has to delete files in storage backend
        for (FileVersion version : all()) {
            version.delete();
        }
    }

    /**
     * get dao object of this class
     *
     * @return dao object
     */
    private static IGenericDao dao() {
        return Environment.data().get_dao(FileVersion.class);
    }

    /**
     * get dao object of this class
     *
     * @return dao object
     */
    private static IFileVersionDao specific_dao() {
        return Environment.data().get_dao_fileversion();
    }

    public static List<FileVersion> find_by_chksum(String _chksum) {
        return specific_dao().find_by_chksum(_chksum);
    }

    /**
     * updates object in database
     */
    public void db_sync() {
        dao().update(this);
    }

    /**
     * deletes fileversion
     */
    public void delete() {
        FileVersion upper = version_reference();
        if (upper != null) {
            upper.set_previous(get_previous());
            upper.db_sync();
        }

        // TODO delete from location but query for dedup versions
        for (StorageCell location : get_location()) {
            List<StorageCell> reference_list = find_references_chksum_to_location(get_checksum(), (location));
            if (reference_list.size() == 1) {
                reference_list.iterator().next().delete(get_checksum());
            }
        }

        dao().delete(this);
    }

    /**
     * get fileversions that this version is a previous one
     *
     * @return fileversion with m_previos referencing this
     */
    private FileVersion version_reference() {
        return specific_dao().find_by_previous(this);
    }

    private List<StorageCell> find_references_chksum_to_location(String _chksum, StorageCell _location) {
        return specific_dao().find_references_chksum_to_location(_chksum, _location);
    }

    /**
     * returns File object this version is referenced to
     *
     * @return File Object belonging to this version
     */
    public File get_file() {
        return m_file;
    }

    // RELATIONS
    public User user() {
        return m_user;
    }

    public File file() {
        return m_file;
    }

    // GETTER / SETTER
    public int get_id() {
        return m_id;
    }

    public String get_checksum() {
        return m_checksum;
    }

    public void set_checksum(String _checksum) {
        m_checksum = _checksum;
    }


    public long get_last_modified() {
        return m_last_modified;
    }

    public void set_last_modified(long _last_modified) {
        m_last_modified = _last_modified;
    }


    public long get_size() {
        return m_size;
    }

    public void set_size(long _size) {
        m_size = _size;
    }


    public Set<StorageCell> get_location() {
        return m_location;
    }

    public void add_location(StorageCell _location) {
        if (!m_location.contains(_location))
            m_location.add(_location);
    }

    public void remove_location(StorageCell _location) {
        if (m_location.contains(_location))
            m_location.remove(_location);
    }


    public FileVersion get_previous() {
        return m_previous;
    }

    public void set_previous(FileVersion _previous) {
        m_previous = _previous;
    }

    public void set_locationset(Set<StorageCell> _location) {
        m_location = _location;
    }


    // HELPER
    public String toString() {
        StringBuilder returnstring = new StringBuilder("<FileVersion::{");
        returnstring.append("m_id: " + this.m_id);
        returnstring.append(", m_user_id: " + this.user().get_id());
        returnstring.append(", m_file_id: " + this.file().get_id());
        returnstring.append("}>");
        return returnstring.toString();
    }

    public String toJsonList(){
        DateFormat dfm = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss");
        StringBuilder json = new StringBuilder("\"Version\" : [");
        json.append("{\"id\" : ").append(this.get_id()).append(",");
        json.append("\"version\" : ").append(this.get_version()).append(",");
        json.append("\"modified\" : \"").append(dfm.format(new Date(this.get_last_modified()))).append("\",");
        json.append("\"size\" : ").append(this.get_size()).append("}");
        FileVersion version = this.get_previous();
        while (version != null){
            json.append(",{\"id\" : ").append(version.get_id()).append(",");
            json.append("\"version\" : ").append(version.get_version()).append(",");
            json.append("\"modified\" : \"").append(dfm.format(new Date(version.get_last_modified()))).append("\",");
            json.append("\"size\" : ").append(version.get_size()).append("}");
            version = version.get_previous();
        }
        json.append("]");

        return json.toString();
    }
}

package jStrg.file_system;

// REQUIREMENTS

import jStrg.database.DatabaseEntity;
import jStrg.database.IFileTypeDao;
import jStrg.database.IGenericDao;
import jStrg.environment.Environment;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;

@Table(
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"m_file_extension"})
)
@Entity
public class FileType extends DatabaseEntity {

    // ATTRIBUTES
    public String m_file_extension;

    // CONSTRUCTORS
    public FileType() {
    }

    public FileType(int _id, String _file_extension) {
        this.m_file_extension = _file_extension;
        dao().create(this);
    }

    // DATABASE TRANSACTIONS
    public static List<FileType> all() {
        return dao().findAll();
    }

    // comment me
    private static IGenericDao dao() {
        return Environment.data().get_dao(FileType.class);
    }

    /**
     * dao with specific funktions for this class
     *
     * @return applicationdao
     */
    private static IFileTypeDao specific_dao() {
        return Environment.data().get_dao_file_type();
    }

    /**
     * deletes all entrys in database for this class
     */
    public static void delete_all() {
        for (FileType type : all()) {
            specific_dao().delete(type);
        }
    }

    /**
     * query for the last file type
     *
     * @return FileType
     */
    public static FileType last() {
        List<FileType> file_types = all();
        return file_types.size() > 0 ? file_types.get(file_types.size() - 1) : null;
    }

    /**
     * query for a file type by ID
     *
     * @param _file_type_id int
     * @return FileType
     */
    public static FileType find(int _file_type_id) {
        return (FileType) dao().findById(_file_type_id);
    }

    // GETTER / SETTER
    public int get_id() {
        return m_id;
    }

    // HELPER
    public String toString() {
        StringBuilder returnstring = new StringBuilder("<FileType::{");
        returnstring.append("m_id:" + this.m_id);
        returnstring.append(", m_file_extension: '" + m_file_extension);
        returnstring.append("'}>");
        return returnstring.toString();
    }
}

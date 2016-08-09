package jStrg.database;

import jStrg.file_system.File;
import jStrg.file_system.FileFolder;
import jStrg.file_system.FileVersion;
import jStrg.file_system.User;

import java.util.List;

/*
    Interface for Dao Objects
 */
public interface IFileDao {


    List<File> find_by_title(String _title);
    List<File> find_by_title(String _title, User _user);
    List<File> find_by_parent(FileFolder _folder);


    void delete(File _file);
}
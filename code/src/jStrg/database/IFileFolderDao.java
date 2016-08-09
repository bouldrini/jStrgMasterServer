package jStrg.database;

import jStrg.file_system.FileFolder;
import jStrg.file_system.User;

import java.util.List;

/*
    Interface for Dao Objects
 */
public interface IFileFolderDao {
    FileFolder find_rootfolder(User _user);

    List<FileFolder> find_by_title(String _title, User _user);


    List<FileFolder> find_by_parent(FileFolder _parent);
}
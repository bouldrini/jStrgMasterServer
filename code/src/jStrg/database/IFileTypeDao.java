package jStrg.database;

import jStrg.file_system.FileType;

/*
    Interface for Dao Objects
 */
public interface IFileTypeDao {
    public FileType find_by_file_extension(String _file_extension);

    void delete(FileType _file_type);


}
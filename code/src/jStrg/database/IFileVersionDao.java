package jStrg.database;

import jStrg.file_system.Application;
import jStrg.file_system.File;
import jStrg.file_system.FileVersion;
import jStrg.network_management.storage_management.core.StorageCell;

import java.util.List;

/*
    Interface for Dao Objects
 */
public interface IFileVersionDao {
    List<FileVersion> find_by_chksum(String _chksum);

    FileVersion find_by_previous(FileVersion _version);

    List<StorageCell> find_references_chksum_to_location(String _chksum, StorageCell _location);

    List<FileVersion> find_by_file(File _file);

    List<FileVersion> find_by_app(Application _app);
}
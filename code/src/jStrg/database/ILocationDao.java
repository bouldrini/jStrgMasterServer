package jStrg.database;

import jStrg.file_system.Application;
import jStrg.network_management.storage_management.core.Location;
import jStrg.network_management.storage_management.core.StorageCell;

import java.util.List;

/*
    Interface for Dao Objects
 */
public interface ILocationDao {
    List<StorageCell> find_all_by_type(Location.TYPE _type);

    Boolean contains_type(Location.TYPE _type);

    List<StorageCell> find_with_and_condition(Location.TYPE _type, Application _app);

}
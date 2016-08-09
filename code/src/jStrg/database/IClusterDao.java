package jStrg.database;

import jStrg.file_system.Application;
import jStrg.network_management.storage_management.cluster.Cluster;

import java.util.List;

/*
    Interface for Dao Objects
 */
public interface IClusterDao {
    List<Cluster> find(Application _app);

}
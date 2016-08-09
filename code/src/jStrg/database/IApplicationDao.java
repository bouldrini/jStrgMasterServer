package jStrg.database;

import jStrg.file_system.Application;

/*
    Interface for Dao Objects
 */
public interface IApplicationDao {
    public Application find_by_title(String _title);

    void delete(Application _app);


}
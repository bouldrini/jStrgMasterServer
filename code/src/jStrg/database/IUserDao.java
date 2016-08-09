package jStrg.database;

import jStrg.file_system.Application;
import jStrg.file_system.User;

import java.util.List;

/*
    Interface for Dao Objects
 */
public interface IUserDao {
    User find_by_name(String _name);

    User find_by_name(String _name, Application _application);

    List<User> find_all_of_application(Application _application);
}
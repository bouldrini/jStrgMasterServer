package jStrg.simple_console;

import jStrg.data_types.exceptions.ConfigException;

/**
 * Created by henne on 04.01.16.
 */
public interface Command {


    boolean has_rights(ContextType _type);

    String run(String[] _args) throws ConfigException;

    void usage();

    enum ContextType {
        UNKNOWN, ADMIN, USER
    }
}

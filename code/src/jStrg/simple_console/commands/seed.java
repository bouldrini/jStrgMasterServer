package jStrg.simple_console.commands;

import jStrg.environment.Environment;

import java.io.IOException;

/**
 * Created by henne on 05.01.16.
 */
public class seed extends CommandBlueprint {

    public seed() {
        m_context_type = ContextType.ADMIN;
    }

    public String run(String[] _args) {
        try {
            Environment.seed(true);
            return "ok";
        } catch (IOException e) {
            LOGGER.warning("Irgendwas mit Richard " + e);
        }
        return "not ok";
    }

}

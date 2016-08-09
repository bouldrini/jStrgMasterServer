package jStrg.simple_console.commands;

import jStrg.file_system.Settings;
import jStrg.simple_console.Command;
import jStrg.simple_console.Console;

import java.util.logging.Logger;

/**
 * Created by henne on 04.01.16.
 */
abstract public class CommandBlueprint implements Command {

    protected final static Logger LOGGER = Logger.getLogger(Settings.location_logging_target);
    protected static String cmd_incomplete_message = "command incomplete";
    protected static String cmd_notfound_message = "command not found";
    protected static String cmd_argument_miss_message = "command argument missing";
    protected Console CONSOLE;
    protected ContextType m_context_type;

    public CommandBlueprint() {

    }

    protected Console get_console() {
        if (CONSOLE == null)
            CONSOLE = Console.getInstance();
        return CONSOLE;
    }


    public boolean has_rights(ContextType _type) {
        return m_context_type != ContextType.UNKNOWN && m_context_type == _type;
    }

    protected void println(String _out) {
        if (Console.out == null) {
            System.out.println(_out);
        } else {
            Console.out.println(_out);
            Console.out.flush();
        }
    }

    public void usage() {
        println("No specific help found for this command. Please look at general usage with \"help\".");
    }
}

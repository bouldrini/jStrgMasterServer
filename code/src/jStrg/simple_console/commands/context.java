package jStrg.simple_console.commands;

import jStrg.data_types.exceptions.ConfigException;
import jStrg.file_system.User;
import jStrg.simple_console.Command;

/**
 * Created by henne on 05.01.16.
 */
public class context extends CommandBlueprint {

    @Override
    public boolean has_rights(ContextType _type) {
        return true;
    }

    public String run(String[] _args) throws ConfigException {
        if (_args.length < 2) {
            return cmd_incomplete_message;
        }
        String ret = "";
        switch (_args[1]) {
            case "switch":
                ContextType new_type = ContextType.UNKNOWN;
                if (_args.length < 3) {
                    return cmd_argument_miss_message;
                }
                if (_args[2].equals("admin")) {
                    new_type = Command.ContextType.ADMIN;
                    get_console().set_context_user(null);
                } else {
                    try {
                        if (User.find(Integer.parseInt(_args[2])) == null) {
                            return "User id " + _args[2] + " not found";
                        }
                        get_console().set_context_user(User.find(Integer.parseInt(_args[2])));
                        new_type = ContextType.USER;
                    } catch (NumberFormatException e) {
                        return "please enter a valid userid. Cannot interpret: " + _args[2];
                    }
                }
                ret = get_console().update_context(new_type);
                break;
            case "exit":
                ret = get_console().update_context(ContextType.UNKNOWN);
                LOGGER.fine("leaved context");
                break;
            default:

        }
        return ret;
    }
}

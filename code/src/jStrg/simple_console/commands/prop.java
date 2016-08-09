package jStrg.simple_console.commands;

import jStrg.file_system.Settings;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by henne on 05.01.16.
 */
public class prop extends CommandBlueprint {

    public prop() {
        m_context_type = ContextType.USER;
    }

    public String run(String[] _args) {
        String ret = "";
        if (_args.length < 2) {
            return cmd_incomplete_message;
        }
        switch (_args[1]) {
            case "get":
                ret = get_property(_args);
                break;
            case "set":
                ret = set_property(_args);
                break;
            default:
                ret = cmd_notfound_message;
                break;
        }
        return ret;
    }

    private String get_property(String[] _args) {
        String ret = "";
        if (_args.length < 3) {
            return cmd_incomplete_message;
        }
        switch (_args[2]) {
            case "user_folder":
                ret = Settings.user_folder_root;
                break;
            default:
                ret = "property not found.";
                break;
        }
        return ret;
    }

    private String set_property(String[] _args) {
        String ret = "";
        if (_args.length < 4) {
            return cmd_incomplete_message;
        }
        switch (_args[2]) {
            case "user_folder":
                if (Files.exists(Paths.get(_args[3])) && Files.isDirectory(Paths.get(_args[3]))) {
                    Settings.user_folder_root = _args[3];
                    ret = "new root directory: " + Settings.user_folder_root;
                } else {
                    ret = "Directory doesnt exist.";
                }
                break;
            default:
                ret = "property not found, or is read only";
                break;
        }
        return ret;
    }
}

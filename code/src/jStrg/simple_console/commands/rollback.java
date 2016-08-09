package jStrg.simple_console.commands;

import jStrg.file_system.File;
import jStrg.file_system.FileVersion;
import jStrg.simple_console.Command;

import java.util.Date;

/**
 * Created by henne on 20.02.16.
 */
public class rollback extends CommandBlueprint {
    public rollback() {
        m_context_type = Command.ContextType.USER;
    }

    public String run(String[] _args) {
        String ret = "";

        switch (_args.length) {
            case 2:
                ret = list_versions(_args[1]);
                break;
            case 3:
                ret = rollback_version(_args[1], Integer.parseInt(_args[2]));
                break;
            default:
                ret = cmd_argument_miss_message;
                break;
        }

        return ret;
    }

    private String rollback_version(String _filename, int _version_nr) {
        File file = File.get_file_by_path(get_console().get_context_folder().get_path() + "/" + _filename, get_console().get_context_user());
        if (file == null)
            return "could not found file: " + _filename;
        FileVersion version = file.get_current_version();
        if (version.get_previous() == null)
            return "this file has no older versions.";
        for (int depth = 0; depth < _version_nr; depth++) {
            version = version.get_previous();
            if (version == null) {
                return "didnt found a version. Max rollback version: " + depth;
            }
        }

        boolean success = file.test_rollback_file(version.get_id());
        if (success) {
            return "rollback file: success";
        } else {
            return "failure";
        }

    }

    private String list_versions(String _filename) {
        File file = File.get_file_by_path(get_console().get_context_folder().get_path() + "/" + _filename, get_console().get_context_user());
        if (file == null)
            return "could not found file: " + _filename;

        FileVersion next_version = file.get_current_version();

        int entrys = 0;
        while (next_version != null) {
            if (next_version.get_previous() != null) {
                next_version = next_version.get_previous();
                println(++entrys + " -> Version vom " + new Date(next_version.get_last_modified()) + ", size: " + next_version.get_size());
            } else {
                break;
            }
        }
        return "found " + entrys + " versions";
    }

}

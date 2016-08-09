package jStrg.simple_console.commands;

import jStrg.file_system.FileFolder;

/**
 * Created by henne on 05.01.16.
 */
public class mkdir extends CommandBlueprint {
    public mkdir() {
        m_context_type = ContextType.USER;
    }

    public String run(String[] _args) {
        String ret = "";
        String path;
        FileFolder folder = get_console().get_context_folder();
        if (_args.length < 2) {
            return cmd_argument_miss_message;
        }
        if (folder.get_title().equals("") && folder.is_root_folder()) {
            path = _args[1];
        } else {
            path = folder.get_path() + "/" + _args[1];
        }
        LOGGER.finest("check if folder already exists: " + path);
        if (FileFolder.get_filefolder_by_path(path, CONSOLE.get_context_user()) == null) {
            FileFolder new_folder = new FileFolder(folder, _args[1]); // <---
            ret = "created new folder: " + new_folder.get_path();
            get_console().update_completer();
        } else {
            ret = "Folder already exists";
        }

        return ret;
    }
}

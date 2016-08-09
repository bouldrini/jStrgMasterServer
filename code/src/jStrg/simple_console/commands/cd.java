package jStrg.simple_console.commands;

import jStrg.file_system.FileFolder;

/**
 * Created by henne on 05.01.16.
 */
public class cd extends CommandBlueprint {

    String ret = "";

    public cd() {
        m_context_type = ContextType.USER;
    }

    public String run(String[] _args) {

        if (_args.length < 2) {
            get_console().set_context_folder(FileFolder.find(get_console().get_context_user().get_rootfolder().get_id()));
            get_console().update_completer();
            return "now under root";
        } else if (_args[1].equals("..")) {
            FileFolder new_folder = get_console().get_context_folder().get_parent();
            get_console().set_context_folder(new_folder);
            get_console().update_completer();
            return "current directory: " + new_folder.get_path();
        }
        String complete_path[] = _args[1].split("/");
        LOGGER.finest("looking for: " + complete_path[complete_path.length - 1]);
        if (complete_path.length == 1) {
            for (FileFolder folder : get_console().get_context_folder().get_folders()) {
                if (folder == null) {
                    ret = "null";
                }
                LOGGER.finest("is " + folder + " the right candidate?");
                if (folder.get_title().equals(_args[1])) {
                    get_console().set_context_folder(folder);

                    ret = "current directory: " + get_console().get_context_folder().get_path();
                }
                if (ret.equals("")) {
                    ret = "Folder " + _args[1] + " not found.";
                }
            }
        } else {
            ret = "only folder names, no path please.";
        }
        get_console().update_completer();
        return ret;
    }
}

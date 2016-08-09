package jStrg.simple_console.commands;

import jStrg.data_types.exceptions.ConfigException;
import jStrg.file_system.FileFolder;
import jStrg.file_system.Settings;
import jStrg.file_system.User;
import jStrg.simple_console.Command;

import java.io.File;

/**
 * Created by henne on 04.01.16.
 */
public class jstrg extends CommandBlueprint implements Command {

    public jstrg() {
        m_context_type = ContextType.USER;
    }

    private static boolean recover(FileFolder folder) {
        File worker;
        boolean without_failure = true;

        for (FileFolder current_folder : folder.get_folders()) {
            worker = new File(Settings.user_folder_root + current_folder.get_path());
            if (!worker.exists()) {
                if (!worker.mkdir()) {
                    LOGGER.warning("creating directory failed: " + Settings.user_folder_root + current_folder.get_path());
                    without_failure = false;
                }
            }
            if (!recover(current_folder)) {
                without_failure = false;
            }
        }
        for (jStrg.file_system.File file : folder.get_files()) {
            LOGGER.finest("checking file: " + file);
            boolean success = file.test_sync_file();
            LOGGER.finest("test_sync_file returned: " + success);
            if (!success) {
                without_failure = false;
                LOGGER.warning("error for sync file: " + file);
            }
        }
        return without_failure;
    }

    public String run(String[] _args) throws ConfigException {
        String ret = "";
        if (_args.length < 2) {
            return cmd_incomplete_message;
        }
        File file = new File(Settings.user_folder_root);
        if (file.toString().equals("") || !file.canWrite()) {
            throw new ConfigException("root folder for user not accessible: \"" + file + "\"");
        }
        switch (_args[1]) {
            case "recover":
                ret = recover();
                break;
            case "sync":
                ret = sync();
                break;
            default:
                ret = cmd_notfound_message;
                break;
        }
        return ret;
    }

    private String sync() {
        String ret = "";
        File dir = new File(Settings.user_folder_root);
        if (dir.listFiles().length == 0) {
            return recover();
        }
        FileFolder rootfolder = FileFolder.find(get_console().get_context_user().get_rootfolder().get_id());
        if (sync(rootfolder) && recover(rootfolder)) {
            get_console().update_completer();
            ret = "completed without errors";
        } else {
            ret = "Error during revovery";
        }

        return ret;
    }

    private boolean sync(FileFolder _fileFolder) {
        File worker;
        boolean without_failure = true;
        File dir = new File(Settings.user_folder_root + _fileFolder.get_path());

        for (File localfile : dir.listFiles()) {
            User user = get_console().get_context_user();
            if (localfile.isDirectory()) {
                FileFolder folder = FileFolder.get_filefolder_by_path(_fileFolder.get_path() + "/" + localfile.getName(), user);
                if (folder == null) {
                    folder = new FileFolder(_fileFolder, localfile.getName());
                }
                if (!sync(folder)) {
                    without_failure = false;
                }
            } else if (localfile.isFile()) {
                jStrg.file_system.File remotefile = jStrg.file_system.File.get_file_by_path(_fileFolder.get_path() + "/" + localfile.getName(), user);
                if (remotefile == null) {
                    LOGGER.finest("new local file found: " + localfile);
                    remotefile = new jStrg.file_system.File(_fileFolder, localfile.getName());
                }
                if (!remotefile.test_sync_file()) {
                    without_failure = false;
                    LOGGER.warning("error for sync file: " + remotefile);
                }
            } else {
                LOGGER.warning("doesnt know what to do with: " + localfile);
            }
        }
        return without_failure;
    }

    private String recover() {
        String ret = "";
        File dir = new File(Settings.user_folder_root);
        if (dir.listFiles().length != 0) {
            return "Failure while recovery, Directory not empty.";
        }

        if (recover(FileFolder.find(get_console().get_context_user().get_rootfolder().get_id()))) {
            ret = "completed without errors";
        } else {
            ret = "Error during revovery";
        }
        return ret;
    }
}

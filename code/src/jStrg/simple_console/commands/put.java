package jStrg.simple_console.commands;

import java.io.File;

/**
 * Created by henne on 05.01.16.
 */
public class put extends CommandBlueprint {

    public put() {
        m_context_type = ContextType.USER;
    }

    public String run(String[] _args) {

        if (_args.length < 2) {
            return cmd_incomplete_message;
        }
        String ret = "";
        File real_file = new File(new File(_args[1]).getAbsolutePath());
        if (real_file.canRead()) {
            println("uploading file: " + real_file.getName());
        } else {
            println("cant read file: " + real_file.getAbsolutePath());
        }
        LOGGER.finest("file put: " + get_console().get_context_folder().get_id() + " " + get_console().get_context_user().get_id() + " " + real_file.getName());

        // TODO: replace last parameter with application_id

        jStrg.file_system.File new_file = jStrg.file_system.File.get_file_by_path(CONSOLE.get_context_folder().get_path()
                        + "/"
                        + real_file.getName()
                , CONSOLE.get_context_user());
        if (new_file == null)
            new_file = new jStrg.file_system.File(get_console().get_context_folder(), real_file.getName());
        if (new_file.test_import_file(real_file.getAbsolutePath())) {
            ret = "upload successful";
        } else {
            ret = "upload failed";
        }
        return ret;
    }

}

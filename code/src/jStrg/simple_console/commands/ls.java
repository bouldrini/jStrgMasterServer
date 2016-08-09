package jStrg.simple_console.commands;

import jStrg.file_system.File;
import jStrg.file_system.FileFolder;

/**
 * Created by henne on 05.01.16.
 */
public class ls extends CommandBlueprint {

    public ls() {
        m_context_type = ContextType.USER;
    }

    public String run(String[] _args) {

        String ret = "";
        long entrys = 0;
        for (FileFolder folder : get_console().get_context_folder().get_folders()) {
            println(folder.get_title() + "/");
        }
        for (File file : get_console().get_context_folder().get_files()) {
            println(file.get_title());
            entrys++;
        }
        return "found: " + Long.toString(entrys);
    }

}

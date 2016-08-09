package jStrg.simple_console.commands;

import jStrg.database.IGenericDao;
import jStrg.environment.Environment;
import jStrg.file_system.File;
import jStrg.file_system.User;
import jStrg.network_management.storage_management.core.StorageCell;

/**
 * list objects
 */
public class list extends CommandBlueprint {

    public list() {
        m_context_type = ContextType.ADMIN;
    }

    public String run(String[] _args) {
        if (_args.length < 2) {
            return cmd_incomplete_message;
        }

        long entrys = 0L;
        switch (_args[1].toLowerCase()) {
            case "locations":
                IGenericDao dao = Environment.data().get_dao(StorageCell.class);
                for (Object locationmapentry : dao.findAll()) {
                    StorageCell location = (StorageCell) locationmapentry;
                    println(location.toString());

                }
                break;
            case "user":
                for (User user : User.all(CONSOLE.application())) {
                    println(user.toString());
                    entrys++;
                }
                break;
            case "files":
                for (Object fileobject : File.all()) {
                    File file = (File) fileobject;
                    println(file.toString());
                    entrys++;
                }
                break;
            default:
                LOGGER.warning("list invoked with: " + _args[0]);
        }

        return Long.toString(entrys);
    }
}

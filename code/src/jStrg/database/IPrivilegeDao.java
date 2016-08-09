package jStrg.database;

import jStrg.data_types.privileges.AccessModifier;
import jStrg.file_system.*;

import java.util.List;

/*
    Interface for Dao Objects
 */
public interface IPrivilegeDao {

    List<Privilege> user_privileges(User _user);

    List<Privilege> user_privileges(User _user, Privilege.TYPE _privtype);

    List<Privilege> user_privileges(User _user, Privilege.TYPE _privtype, AccessModifier _modifier);

    Privilege find_user_privilege_for_file(User _user, File _file);

    Privilege find_user_privilege_for_filefolder(User _user, FileFolder _folder);

    List<Privilege> application_privileges(Application _application);

    List<Privilege> folder_privileges(FileFolder _folder);

    List<Privilege> folder_privileges(FileFolder _folder, User _user);

}
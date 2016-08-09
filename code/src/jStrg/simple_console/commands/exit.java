package jStrg.simple_console.commands;

/**
 * Created by henne on 05.01.16.
 */
public class exit extends CommandBlueprint {

    @Override
    public boolean has_rights(ContextType _type) {
        return true;
    }

    public String run(String[] _args) {
        get_console().set_wants_to_exit();
        return "bye";
    }
}

package jStrg.simple_console;
/*
 * Copyright (c) 2002-2015, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */

import jStrg.data_types.exceptions.ConfigException;
import jStrg.file_system.*;
import jStrg.simple_console.commands.*;
import jline.console.ConsoleReader;
import jline.console.completer.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;


public class Console {
    public static Logger LOGGER = Logger.getLogger(Settings.console_logging_target);
    public static PrintWriter out;
    static AggregateCompleter persistent_completer;
    static AggregateCompleter context_completer;
    private static Console m_console;
    public int m_application_id;
    private boolean m_wants_to_exit;
    private Command.ContextType m_context_type = Command.ContextType.UNKNOWN;
    private User m_context_user;
    private FileFolder m_context_folder;
    private LinkedHashMap<String, Command> m_commands = new LinkedHashMap<>();

    private Console() {
        m_wants_to_exit = false;
        init_commands();
    }

    public static Console getInstance(int _application_id) {
        if (m_console == null) {
            m_console = getInstance();
            m_console.set_application_id(_application_id);
        }
        return m_console;

    }

    public static Console getInstance() {
        if (m_console == null)
            m_console = new Console();
        return m_console;
    }

    private static void fill_persistent_completors() {
        StringsCompleter single_commands = new StringsCompleter(
                "exit"
        );
        persistent_completer = new AggregateCompleter(single_commands);
    }

    private void init_commands() {
        m_commands.put("cd", new cd());
        m_commands.put("context", new context());
        m_commands.put("exit", new exit());
        m_commands.put("jstrg", new jstrg());
        m_commands.put("list", new list());
        m_commands.put("ls", new ls());
        m_commands.put("mkdir", new mkdir());
        m_commands.put("prop", new prop());
        m_commands.put("put", new put());
        m_commands.put("rollback", new rollback());
    }

    /**
     * can be used to test interactive console inside IDE, takes the command and passes it to the parser
     *
     * @param _args command to excecute
     * @return output that is normally printed to the console
     */
    public String debug_pass_cmd(String _args) {
        String ret = "";
        try {
            ret = parse_args(_args);
        } catch (ConfigException ce) {
            LOGGER.severe("Config Error: " + ce);
        }
        return ret;
    }

    public void init_console() throws IOException {
        if (!Settings.m_use_console) {
            return;
        }
        try {
            fill_persistent_completors();
            update_context_completer();
            Character mask = null;
            String trigger = null;
            boolean color = false;

            ConsoleReader reader = new ConsoleReader();

            reader.setPrompt("> ");

            List<Completer> completors = new LinkedList<Completer>();

            //completors.add(new FileNameCompleter());

            //completors.add(new StringsCompleter("foo", "bar", "baz"));


            color = true;
            // reader.setPrompt("\u001B[42mfoo\u001B[0m@bar\u001B[32m@baz\u001B[0m> ");
            //completors.add(new AnsiStringsCompleter("\u001B[1mfoo\u001B[0m", "bar", "\u001B[32mbaz\u001B[0m"));
            ArgumentCompleter showcommand = new ArgumentCompleter(
                    new StringsCompleter("show")
                    , new StringsCompleter("location", "user", "file")
                    , new NullCompleter()
            );
            //AggregateCompleter aggComp = new AggregateCompleter(showcommand);
            //completors.add(aggComp);
            // Parse the buffer line and complete each token
            //ArgumentCompleter argComp = new ArgumentCompleter(aggComp);

            // Don't require all completors to match

            LOGGER.finest("context String: " + context_completer.toString());
            AggregateCompleter list_aggr = new AggregateCompleter(showcommand, persistent_completer, context_completer);
            completors.add(list_aggr);


            CandidateListCompletionHandler handler = new CandidateListCompletionHandler();
            handler.setStripAnsi(false);
            reader.setCompletionHandler(handler);
            for (Completer c : completors) {
                reader.addCompleter(c);
            }

            String line;
            out = new PrintWriter(reader.getOutput());

            while ((line = reader.readLine()) != null) {
                try {
                    println(parse_args(line));
                } catch (ConfigException ce) {
                    LOGGER.warning("Configuration error: " + ce);
                }
                if (m_wants_to_exit) {
                    break;
                }
                // If we input the special word then we will mask
                // the next line.
                if ((trigger != null) && (line.compareTo(trigger) == 0)) {
                    line = reader.readLine("password> ", mask);
                }
                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                    break;
                }
                if (line.equalsIgnoreCase("cls")) {
                    reader.clearScreen();
                }
                list_aggr = new AggregateCompleter(showcommand, persistent_completer, context_completer);
                for (Completer completer : reader.getCompleters()) {
                    reader.removeCompleter(completer);
                }
                reader.addCompleter(list_aggr);

            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public Application application() {
        return Application.find(m_application_id);
    }

    private String parse_args(String _args) throws ConfigException {
        String[] args = _args.trim().split(" ");
        StringBuilder args_output = new StringBuilder();
        for (String arg : args) {
            args_output.append(arg + " ");
        }
        LOGGER.finest("got command: " + args_output);

        String ret = "";
        if (!m_commands.containsKey(args[0])) {
            usage();
            return "command not found.";
        }

        if (!m_commands.get(args[0]).has_rights(m_context_type)) {
            if (m_context_user == null) {
                LOGGER.warning("user with no context tried to excecute: " + args[0]);
            } else {
                LOGGER.warning("User: " + m_context_user.get_id() + " tried to excecute: " + args[0]);
            }
            return "not allowed, incident will be reported";
        }
        if (args[(args.length - 1)].equals("?")) {
            m_commands.get(args[0]).usage();
        }
        return m_commands.get(args[0]).run(args);
    }

    public String update_context(Command.ContextType _type) throws ConfigException {
        String ret = "";
        switch (_type) {
            case ADMIN:
                m_context_type = Command.ContextType.ADMIN;
                m_context_user = null;
                ret = "switched to admin";
                LOGGER.fine("an admin is around");
                break;
            case USER:
                ret = "switched to user";
                m_context_type = Command.ContextType.USER;
                m_context_folder = m_context_user.get_rootfolder();
                LOGGER.fine("switched context to user: " + m_context_user);
                break;
            default:
                m_context_user = null;
                m_context_type = null;
                ret = "leaved context";
                LOGGER.fine("leaved context");
                break;
        }
        update_completer();
        return ret;
    }

    public void update_completer() {
        update_context_completer();
        if (m_context_type == Command.ContextType.ADMIN) {
            return;
        }
        AggregateCompleter cd_completer = new AggregateCompleter();
        for (FileFolder filefolder : get_context_folder().get_folders()) {
            ArgumentCompleter add_arg_entry = new ArgumentCompleter(
                    new StringsCompleter("cd"),
                    new StringsCompleter(filefolder.get_title()),
                    new NullCompleter()
            );
            cd_completer = new AggregateCompleter(cd_completer, add_arg_entry);
        }
        for (File file : get_context_folder().get_files()) {
            ArgumentCompleter add_arg_entry = new ArgumentCompleter(
                    new StringsCompleter("rollback"),
                    new StringsCompleter(file.get_title()),
                    new NullCompleter()
            );
            cd_completer = new AggregateCompleter(cd_completer, add_arg_entry);
        }

        context_completer = new AggregateCompleter(context_completer, cd_completer);
    }

    private void update_context_completer() {

        ArgumentCompleter context_command = new ArgumentCompleter(
                new StringsCompleter("context")
                , new StringsCompleter("switch", "show", "exit")
                , new NullCompleter()
        );
        if (m_context_type == null) {
            context_completer = new AggregateCompleter(context_command);
        } else if (m_context_type == Command.ContextType.USER) {
            StringsCompleter ls_cmd = new StringsCompleter("ls", "mkdir");
            ArgumentCompleter put_cmd = new ArgumentCompleter(
                    new StringsCompleter("put", "get")
                    , new FileNameCompleter()
                    , new NullCompleter()
            );
            ArgumentCompleter prop_cmd = new ArgumentCompleter(
                    new StringsCompleter("prop")
                    , new StringsCompleter("get", "set")
                    , new StringsCompleter("user_folder")
                    , new NullCompleter()
            );
            ArgumentCompleter jstrg_cmd = new ArgumentCompleter(
                    new StringsCompleter("jstrg")
                    , new StringsCompleter("recover", "sync")
                    , new NullCompleter()
            );

            context_completer = new AggregateCompleter(context_command, ls_cmd, put_cmd, prop_cmd, jstrg_cmd);
        } else if (m_context_type == Command.ContextType.ADMIN) {
            StringsCompleter seed_cmd = new StringsCompleter("seed");
            ArgumentCompleter listcommand = new ArgumentCompleter(
                    new StringsCompleter("list")
                    , new StringsCompleter("locations", "user", "files")
                    , new NullCompleter()
            );
            context_completer = new AggregateCompleter(context_command, seed_cmd, listcommand);
        } else {
            context_completer = new AggregateCompleter(context_command);
        }
        LOGGER.finest("new context_completer: " + context_completer);
    }

    public void usage() {
        String lsep = System.lineSeparator();
        println("" + lsep +
                "Usage:" + lsep +
                " context - switch context to admin, userid or none" + lsep +
                " cd - change current directory, without argument to rootfolder, \"..\" one level up" + lsep +
                " jstrg - sync user folder, recover is included in sync" + lsep +
                " mkdir - create folder in current directory" + lsep +
                " put - put file from local filesystem in ftp style, after that sync is needed to restore file to user folder" + lsep +
                " prop - view or change user editable propertys" + lsep +
                " ls - list files and folders of current directory" + lsep +
                " seed - admin command for testing" + lsep +
                " list - admin command for viewing internal database" + lsep +
                " exit - stop process" + lsep +
                ""
        );
    }


    private void println(String _out) {
        if (out == null) {
            System.out.println(_out);
        } else {
            out.println(_out);
            out.flush();
        }
    }

    private int get_application_id() {
        return m_application_id;
    }

    private void set_application_id(int m_application_id) {
        this.m_application_id = m_application_id;
    }


    public User get_context_user() {
        return m_context_user;
    }

    public void set_context_user(User m_context_user) {
        this.m_context_user = m_context_user;
    }

    public FileFolder get_context_folder() {
        return m_context_folder;
    }

    public void set_context_folder(FileFolder m_context_folder) {
        this.m_context_folder = m_context_folder;
    }

    public void set_wants_to_exit() {
        this.m_wants_to_exit = true;
    }

}
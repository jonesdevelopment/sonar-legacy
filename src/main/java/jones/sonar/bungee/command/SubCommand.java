package jones.sonar.bungee.command;

import java.util.Collection;

public abstract class SubCommand {

    public final String name, description, permission;

    public final String[] aliases;

    public final Collection<String> commands;

    public abstract void execute(final CommandExecution execution);

    /*
     * This class needs custom constructors because we have
     * a command alias which needs to be defined and no alias
     * at the same time.
     */

    public SubCommand(final String name, final String description, final String permission, final Collection<String> commands, final String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.permission = permission;
        this.commands = commands;
    }

    public SubCommand(final String name, final String description, final String permission, final Collection<String> commands) {
        this.name = name;
        this.description = description;
        this.aliases = new String[] { name };
        this.permission = permission;
        this.commands = commands;
    }
}

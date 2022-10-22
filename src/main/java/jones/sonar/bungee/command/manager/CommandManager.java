package jones.sonar.bungee.command.manager;

import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.command.impl.*;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class CommandManager {
    public final List<SubCommand> SUB_COMMANDS = new ArrayList<>();

    public void initialize() {
        addCommands(new PingCommand(),
                new InfoCommand(),
                new StatsCommand(),
                new ReloadCommand(),
                new BlacklistCommand(),
                new WhitelistCommand(),
                new MonitorCommand(),
                new VerifyCommand(),
                new VerboseCommand(),
                new NotifyCommand());
    }

    private void addCommands(final SubCommand... command) {
        SUB_COMMANDS.addAll(Arrays.asList(command));
    }
}

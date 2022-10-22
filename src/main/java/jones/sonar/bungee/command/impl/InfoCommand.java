package jones.sonar.bungee.command.impl;

import jones.sonar.bungee.command.CommandExecution;
import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.config.Messages;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.OperatingSystem;
import jones.sonar.universal.util.PerformanceMonitor;
import jones.sonar.universal.util.Sensibility;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public final class InfoCommand extends SubCommand {

    public InfoCommand() {
        super("info",
                "Show server information",
                "sonar.info",
                null);
    }

    @Override
    public void execute(final CommandExecution execution) {
        execution.send(Messages.Values.HEADER_BAR);

        final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();

        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fProxy: " + SonarBungee.INSTANCE.proxy.getName());
        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fProxy protocol version: " + SonarBungee.INSTANCE.proxy.getProtocolVersion());

        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fOperating system: " + OperatingSystem.getOSName());

        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fAvailable processors: " + operatingSystem.getAvailableProcessors() + " cores");

        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fGlobal CPU load (all cores): " + PerformanceMonitor.formatCPULoad() + "%");
        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fAverage CPU load (all cores): " + PerformanceMonitor.formatAverageCPULoad() + "%");

        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fTotal available memory: " + PerformanceMonitor.getTotalMemory() + " MB");
        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fMemory (used/free): " + PerformanceMonitor.getUsedMemory() + " MB / " + PerformanceMonitor.getFreeMemory() + " MB");

        if (SonarBungee.INSTANCE.proxy.getPlayers().size() > 0) {
            execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fAverage §flatency §fof §fall §fplayers: "
                    + SonarBungee.INSTANCE.FORMAT.format(SonarBungee.INSTANCE.proxy.getPlayers().stream()
                    .mapToLong(ProxiedPlayer::getPing).sum() / SonarBungee.INSTANCE.proxy.getPlayers().size()) + " ms");
        }

        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fIs §fyour §fserver §funder §fattack? " + (Sensibility.isUnderAttack() ? "§cYes" : "§aNo"));
        execution.send(Messages.Values.FOOTER_BAR);
    }
}

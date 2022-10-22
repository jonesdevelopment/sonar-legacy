package jones.sonar.universal.util;

import lombok.experimental.UtilityClass;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

@UtilityClass
public class PerformanceMonitor {
    public final Runtime RUNTIME = Runtime.getRuntime();

    public OperatingSystemMXBean OPERATING_SYSTEM = ManagementFactory.getOperatingSystemMXBean();

    public long getTotalMemory() {
        return RUNTIME.totalMemory() / 1000000L;
    }

    public long getFreeMemory() {
        return RUNTIME.freeMemory() / 1000000L;
    }

    public long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }

    public double getCPULoad() {
        return OPERATING_SYSTEM.getSystemLoadAverage() * 10;
    }

    public double getAverageCPULoad() {
        return getCPULoad() / OPERATING_SYSTEM.getAvailableProcessors();
    }

    public String formatCPULoad() {
        return String.format("%.2f", getCPULoad());
    }

    public String formatAverageCPULoad() {
        return String.format("%.2f", getAverageCPULoad());
    }
}

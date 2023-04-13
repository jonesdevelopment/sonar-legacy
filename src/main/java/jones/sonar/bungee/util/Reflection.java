package jones.sonar.bungee.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.netty.PipelineUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@UtilityClass
public class Reflection {
    public boolean inject(final ChannelInitializer<Channel> interceptor, final int version) {
        if (version < 8) {
            Logger.ERROR.log("Your Java version is not compatible! Please use Java 8 or higher.");
            return false;
        }

        final AtomicBoolean success = new AtomicBoolean(false);

        Arrays.stream(PipelineUtils.class.getDeclaredFields())
                .filter(Objects::nonNull)
                .filter(field -> field.getName().equalsIgnoreCase("server_child"))
                .forEach(field -> {
                    if (version == 8) {
                        field.setAccessible(true);

                        try {
                            final Field modifier = Field.class.getDeclaredField("modifiers");

                            modifier.setAccessible(true);
                            modifier.setInt(field, field.getModifiers() & 0xFFFFFFEF);

                            field.set(PipelineUtils.class, interceptor);

                            success.set(true);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    } else {
                        try {
                            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");

                            unsafeField.setAccessible(true);

                            final Unsafe unsafe = (Unsafe) unsafeField.get(null);

                            unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), interceptor);

                            success.set(true);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                });

        return success.get();
    }

    public int getVersion() {
        try {
            String version = System.getProperty("java.version");

            if (version.startsWith("1.")) {
                version = version.substring(2, 3);
            } else {
                int dot = version.indexOf(".");

                if (dot != -1) {
                    version = version.substring(0, dot);
                }
            }

            return Integer.parseInt(version);
        } catch (Exception exception) {
            return 0;
        }
    }
}

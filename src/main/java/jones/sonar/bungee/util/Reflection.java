/*
 *  Copyright (c) 2022, jones (https://jonesdev.xyz) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jones.sonar.bungee.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import jones.sonar.universal.util.logging.Logger;
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

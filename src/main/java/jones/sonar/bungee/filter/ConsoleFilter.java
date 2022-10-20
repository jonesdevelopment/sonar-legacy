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

package jones.sonar.bungee.filter;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;

@UtilityClass
public final class ConsoleFilter {
    public void apply() {
        if (isLog4J()) {
            new Log4JConsoleFilter(); // load the log4j filter if detected
            return;
        }

        new DefaultConsoleFilter();
    }

    private boolean isLog4J() {
        try {
            LogManager.getRootLogger();
            return true;
        } catch (NoClassDefFoundError classDefFoundError) {
            return false;
        }
    }
}

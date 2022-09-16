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
package jones.sonar.whitelist;

import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class Whitelist {
    public final Set<InetAddress> WHITELISTED = new HashSet<>();

    public long size() {
        return WHITELISTED.size();
    }

    public void addToWhitelist(final InetAddress inetAddress) {
        if (isWhitelisted(inetAddress)) return;

        WHITELISTED.add(inetAddress);
    }

    public boolean isWhitelisted(final InetAddress inetAddress) {
        return WHITELISTED.contains(inetAddress);
    }
}

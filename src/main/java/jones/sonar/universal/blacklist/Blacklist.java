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
package jones.sonar.universal.blacklist;

import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class Blacklist {
    public final Set<InetAddress> BLACKLISTED = new HashSet<>();

    public long size() {
        return BLACKLISTED.size();
    }

    public void removeFromBlacklist(final InetAddress inetAddress) {
        if (!isBlacklisted(inetAddress)) return;

        BLACKLISTED.remove(inetAddress);
    }

    public void addToBlacklist(final InetAddress inetAddress) {
        if (isBlacklisted(inetAddress)) return;

        BLACKLISTED.add(inetAddress);
    }

    public boolean isBlacklisted(final InetAddress inetAddress) {
        return BLACKLISTED.contains(inetAddress);
    }
}

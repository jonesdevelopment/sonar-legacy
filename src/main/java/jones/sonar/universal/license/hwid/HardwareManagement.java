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

package jones.sonar.universal.license.hwid;

import jones.sonar.universal.util.OperatingSystem;
import lombok.experimental.UtilityClass;

import java.net.NetworkInterface;
import java.util.Base64;
import java.util.UUID;

@UtilityClass
public class HardwareManagement {
    protected String get() throws Exception {

        // get the hardware address of the next element in the interfaces() array
        final byte[] networkAddress = NetworkInterface.getNetworkInterfaces().nextElement().getHardwareAddress();

        final StringBuilder networkInformationBuilder = new StringBuilder();

        // cache and save all characters out of the network address
        for (final byte character : networkAddress) {
            networkInformationBuilder.append((char) (character ^ 2));       // apply some XOR to make it harder for decryption/deobfuscation
        }

        // store the network address dynamically
        final String networkInformation = networkInformationBuilder.toString();

        // hash and return the hardware id
        return hash(networkInformation

                // also include the OS name and arch
                + OperatingSystem.getOSName()
                + OperatingSystem.OS_ARCH);
    }

    private String hash(String information) throws Exception {
        information = UUID.nameUUIDFromBytes(information.getBytes()).toString();

        information = "#" + information.hashCode();

        return new String(Base64.getEncoder().encode(information.getBytes()));
    }
}

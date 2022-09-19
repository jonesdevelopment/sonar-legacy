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

import java.math.BigInteger;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@UtilityClass
public class HardwareManagement {
    protected String get() throws Exception {

        // get network interface
        final NetworkInterface networkInterface = NetworkInterface.getNetworkInterfaces().nextElement();

        // get the hardware address of the next element in the interfaces() array
        final byte[] networkAddress = networkInterface.getHardwareAddress();

        final StringBuilder networkInformationBuilder = new StringBuilder();

        // cache and save all characters out of the network address
        for (final byte character : networkAddress) {
            networkInformationBuilder.append((char) (character ^ -networkInterface.getName().length()));       // apply some XOR to make it harder for decryption/deobfuscation
        }

        // store the network address dynamically
        final String networkInformation = networkInformationBuilder.toString();

        // hash and return the hardware id
        return hash(networkInformation

                // also include the OS name and arch
                + OperatingSystem.getOSName()
                + OperatingSystem.OS_ARCH

                // use the network interface name
                + networkInterface.getName());
    }

    private String hash(String information) throws Exception {

        // get the unique id from the string
        information = UUID.nameUUIDFromBytes(information.getBytes()).toString();

        final String base64 = new String(Base64.getEncoder().encode(information.getBytes()));

        // take the hash code from the base64 encoded string
        information = Integer.toHexString(base64.hashCode());

        // return the hash code of the hardware id
        return getMD5Hash(information);
    }

    private String getMD5Hash(final String input) throws Exception {

        // Static getInstance method is called with hashing MD5
        final MessageDigest digest = MessageDigest.getInstance("MD5");

        // digest() method is called to calculate message digest
        // of an input digest() return array of byte
        byte[] messageDigest = digest.digest(input.getBytes());

        // Convert byte array into signum representation
        final BigInteger bigInteger = new BigInteger(1, messageDigest);

        // Convert message digest into hex value
        final StringBuilder hashed = new StringBuilder(bigInteger.toString(16));

        while (hashed.length() < 32) {
            hashed.insert(0, "0");
        }

        return hashed.toString();
    }
}

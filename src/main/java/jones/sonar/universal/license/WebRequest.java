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

package jones.sonar.universal.license;

import jones.sonar.universal.license.response.WebResponse;
import jones.sonar.universal.util.BuiltByBitID;
import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

public final class WebRequest {
    protected WebResponse perform(final License license) {
        final String WEB_URL = "aHR0cHM6Ly9hcGkuam9uZXNkZXYueHl6L3YxL2xpY2Vuc2VzLw==",
            HARDWARE_ID_URL = "P2h3aWQ9";

        final String MCMID_URL = "P21jbWlkPQ==";

        final String AGENT = "VXNlci1BZ2VudA==",
                AGENT_KEY = "X1NvbmFyX0FudGlfQm90XzNCTjc4QTJVSjc4QjkyTU1BMzVLTDc4WVhaOUpGMjNf";

        final String AUTH = "QXV0aG9yaXphdGlvbg==",
                AUTH_KEY = "Basic am9uZXNfaWthc2hiY2l3YmZpd2U6N2FzOTh6Y2FjenE3dzllZGgxODM5MGlqZWlkMzJ3amMwcTlFRXdyNzY4dzlpb2pua2pmUE9JVVpXVFpU";

        try {
            final HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(fromBase64(WEB_URL)
                    + license.key
                    + fromBase64(HARDWARE_ID_URL)
                    + license.hardwareID.encryptedInformation
                    + fromBase64(MCMID_URL).replace("?", "&")
                    + BuiltByBitID.getId()).openConnection();       // 351538

            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(3500);

            urlConnection.setRequestProperty(fromBase64(AGENT), AGENT_KEY);
            urlConnection.setRequestProperty(fromBase64(AUTH), AUTH_KEY);

            urlConnection.connect();

            final boolean validResponse = urlConnection.getServerCertificates() != null
                    && urlConnection.getResponseMessage() != null
                    && urlConnection.getResponseMessage().length() == 2;

            WebResponse response = null;

            for (final WebResponse value : WebResponse.values()) {
                if (value.getExpectedResponseCode() == urlConnection.getResponseCode()) {
                    response = value;
                    break;
                }
            }

            if (response == null || !validResponse) {
                return WebResponse.INVALID;
            }

            return response;
        } catch (Exception exception) {
            return WebResponse.NONE;
        }
    }

    private String fromBase64(final String data) {
        return new String(Base64.decodeBase64(data));
    }
}

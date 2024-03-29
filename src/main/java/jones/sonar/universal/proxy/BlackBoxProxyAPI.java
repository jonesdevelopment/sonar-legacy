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

package jones.sonar.universal.proxy;

import jones.sonar.bungee.config.Config;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

public final class BlackBoxProxyAPI implements ProxyAPI {

    @Getter
    private final Collection<InetAddress> proxies = new ArrayList<>();

    @Override
    public boolean isInProxyCache(final InetAddress inetAddress) {
        return proxies.contains(inetAddress);
    }

    public boolean isUsingProxy(final InetAddress inetAddress) {
        if (proxies.contains(inetAddress)) return true;

        final String result = fetchSourceCode("https://blackbox.ipinfo.app/lookup/" + String.valueOf(inetAddress).replace("/", ""));
        final boolean proxy = result.equals("Y");

        if (proxy) proxies.add(inetAddress);
        return proxy;
    }

    private String fetchSourceCode(final String url) {
        try {
            final URLConnection httpURLConnection = new URL(url).openConnection();

            httpURLConnection.setConnectTimeout(Config.Values.ANTI_PROXY_TIMEOUT);
            httpURLConnection.setReadTimeout(Config.Values.ANTI_PROXY_TIMEOUT);

            httpURLConnection.setRequestProperty("User-Agent", "Sonar Antibot - Proxy API");

            final InputStream inputStream = httpURLConnection.getInputStream();
            final ByteArrayOutputStream result = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];

            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            buffer = null; // free memory??!!?

            return result.toString("UTF-8");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return "N";
        }
    }
}

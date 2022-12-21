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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jones.sonar.bungee.config.Config;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class BlackBoxProxyAPI implements ProxyAPI {

    // TODO: Caffeine caching? Probably not needed...
    @Getter
    private final Cache<InetAddress, Boolean> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1L, TimeUnit.DAYS)
            .build();

    public boolean isUsingProxy(final InetAddress inetAddress) {
        try {
            if (cache.asMap().containsKey(inetAddress)) {
                return isUsingProxyCached(inetAddress);
            }

            cache.put(inetAddress, fetchSourceCode("https://blackbox.ipinfo.app/lookup/" + String.valueOf(inetAddress).replace("/", "")).equals("Y"));
            return isUsingProxyCached(inetAddress);
        } catch (Throwable throwable) {
            return false; // api not reachable, let the player join
        }
    }

    private boolean isUsingProxyCached(final InetAddress inetAddress) {
        try {
            return cache.asMap().containsKey(inetAddress) ? cache.get(inetAddress, () -> false) : false;
        } catch (ExecutionException e) {
            return false;
        }
    }

    private String fetchSourceCode(final String url) throws Throwable {
        final URLConnection httpURLConnection = new URL(url).openConnection();

        httpURLConnection.setConnectTimeout(Config.Values.ANTI_PROXY_TIMEOUT);
        httpURLConnection.setReadTimeout(Config.Values.ANTI_PROXY_TIMEOUT);

        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

        final InputStream inputStream = httpURLConnection.getInputStream();
        final ByteArrayOutputStream result = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];

        int length;

        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        buffer = null; // free memory??!!?

        return result.toString("UTF-8");
    }
}

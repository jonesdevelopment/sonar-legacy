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

package jones.sonar.api.data;

import jones.sonar.api.APIClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;

@Getter
@APIClass(since = "1.3.1")
@RequiredArgsConstructor
public final class BotLevel {
    private final long onlineUsers;
    private final long level;
    private final InetAddress inetAddress;
}
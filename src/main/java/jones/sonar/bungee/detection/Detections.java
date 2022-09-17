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

package jones.sonar.bungee.detection;

public interface Detections {
    Detection FIRST_JOIN_KICK    = new Detection(DetectionResult.DENIED,
            "1");

    Detection INVALID_NAME       = new Detection(DetectionResult.DENIED,
            "2");

    Detection TOO_FAST_RECONNECT = new Detection(DetectionResult.DENIED,
            "3");

    Detection TOO_MANY_ONLINE    = new Detection(DetectionResult.DENIED,
            "4");

    Detection PLAYER_IN_QUEUE    = new Detection(DetectionResult.DENIED,
            "5");

    Detection DURING_ATTACK      = new Detection(DetectionResult.DENIED,
            "6");

    Detection BLACKLIST          = new Detection(DetectionResult.DENIED,
            "");

    Detection ALLOW              = new Detection(DetectionResult.ALLOWED,
            "");
}

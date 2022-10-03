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

package jones.sonar.universal.util;

import jones.sonar.universal.counter.Counter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Sensibility {
    public boolean currentlyUnderAttack = false;
    public long minJoinsPerSecond = 6L;
    public long sinceLastAttack = 0L;

    public boolean isUnderAttack() {
        return isUnderAttackJoins()
                || isUnderAttackHandshakes()
                || Counter.CONNECTIONS_PER_SECOND.get() > minJoinsPerSecond * 3L;
    }

    public boolean isUnderAttackJoins() {
        return Counter.JOINS_PER_SECOND.get() > minJoinsPerSecond;
    }

    public boolean isUnderAttackHandshakes() {
        return Counter.HANDSHAKES_PER_SECOND.get() > (minJoinsPerSecond * 2L);
    }
}

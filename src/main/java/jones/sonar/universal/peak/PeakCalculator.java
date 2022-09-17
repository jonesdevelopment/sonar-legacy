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

package jones.sonar.universal.peak;

import jones.sonar.universal.counter.CounterMap;
import jones.sonar.universal.peak.result.PeakCalculation;
import jones.sonar.universal.peak.result.PeakSubmitResult;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class PeakCalculator {
    private final PeakCalculation NONE = new PeakCalculation(PeakSubmitResult.UNKNOWN,
            null, 0, 0);

    private final Map<CounterMap, PeakRecord> PEAKS = new HashMap<>();

    public PeakCalculation submit(final CounterMap counter) {
        final long current = counter.get();

        // only use this if needed
        final PeakRecord currentAsPeak = new PeakRecord(current);

        // if the counter doesn't exist, create it
        if (!contains(counter)) {
            PEAKS.put(counter, currentAsPeak);

            return new PeakCalculation(PeakSubmitResult.NOT_EXISTING, counter, current, current);
        }

        // the last peak from the counter was less than the current value
        else if (current > get(counter)) {
            final long lastPeak = get(counter);

            PEAKS.replace(counter, currentAsPeak);

            return new PeakCalculation(PeakSubmitResult.NEW_PEAK, counter, current, lastPeak);
        }

        return NONE;
    }

    public long get(final CounterMap counter) {
        if (contains(counter)) {
            return PEAKS.get(counter).value;
        }

        return 0;
    }

    private boolean contains(final CounterMap counter) {
        return PEAKS.containsKey(counter);
    }
}

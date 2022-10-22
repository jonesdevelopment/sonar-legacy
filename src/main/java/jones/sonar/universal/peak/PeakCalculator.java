package jones.sonar.universal.peak;

public final class PeakCalculator {

    public long realLastPeak = 0L, lastPeak = 0L, newPeak = 0L,
            lastPeakChange = 0L;

    public boolean didBroadcast = false;

    public void submit(final long current) {
        if (current > newPeak) {
            lastPeak = newPeak;
            newPeak = current;
            didBroadcast = false;
            lastPeakChange = System.currentTimeMillis();
        }
    }

    public void reset() {
        realLastPeak = 0L;
        lastPeak = 0L;
        newPeak = 0L;
    }
}

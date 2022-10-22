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

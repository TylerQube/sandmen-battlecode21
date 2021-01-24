package sandmenplayer.units;

import battlecode.common.GameActionException;
import sandmenplayer.Movement;

public class Slanderer {
    public static void runSlanderer() throws GameActionException {
        Movement.processSurroundings();
        Movement.runMovement();
    }
}

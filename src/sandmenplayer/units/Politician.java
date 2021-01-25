package sandmenplayer.units;

import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import sandmenplayer.Movement;
import sandmenplayer.RobotPlayer;

public class Politician extends RobotPlayer {
    public static void runPolitician() throws GameActionException {
        empowerStrategy();
        Movement.processSurroundings();
        Movement.runMovement();
    }

    public static void empowerStrategy() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);

        // empower if Enemy EC in range
        for (RobotInfo inf : attackable) {
            if (inf.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                tryEmpower(actionRadius);
                return;
            }
        }

        // empower if lots of bots
        if (attackable.length > 3) {
            tryEmpower(actionRadius);
            return;
        }
    }

    public static void tryEmpower(int actionRadius) throws GameActionException {
        if (rc.canEmpower(actionRadius))
            rc.empower(actionRadius);
    }
}
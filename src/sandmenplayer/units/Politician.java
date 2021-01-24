package sandmenplayer.units;

import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import sandmenplayer.Movement;
import sandmenplayer.RobotPlayer;

public class Politician extends RobotPlayer {
    public static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        Movement.processSurroundings();
        Movement.runMovement();
    }
}
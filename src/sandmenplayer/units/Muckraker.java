package sandmenplayer.units;

import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import sandmenplayer.Movement;
import sandmenplayer.RobotPlayer;

public class Muckraker extends RobotPlayer {
    public static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        Movement.processSurroundings();
        Movement.runMovement();
    }
}

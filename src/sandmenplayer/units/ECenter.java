package sandmenplayer.units;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import sandmenplayer.Communication;
import sandmenplayer.RobotPlayer;

public class ECenter extends RobotPlayer {
    public static void runEnlightenmentCenter() throws GameActionException {
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 50;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
                // save robot ID after building
                for(RobotInfo rbt : rc.senseNearbyRobots(1)) {
                    // only save ID if it is friendly and matches type of built robot
                    if(rbt.getTeam().equals(rc.getTeam()) && rbt.getType().equals(toBuild)) {
                        robotIDs.add(rbt.getID());
                    }
                }
            } else {
                break;
            }
        }

        // remove robot ID if it is destroyed
        for(Integer rbtID : robotIDs) {
            if(!rc.canGetFlag(rbtID)) {
                robotIDs.remove(rbtID);
            } else {
                if(rc.canSetFlag(rc.getFlag(rbtID))) {
                    rc.setFlag(rc.getFlag(rbtID));
                    System.out.println("move to: (" + Communication.getLocationFromFlag(rc.getFlag(rbtID)).x + ", " + Communication.getLocationFromFlag(rc.getFlag(rbtID)).y + ")");
                }
            }
        }
    }
}

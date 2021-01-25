package sandmenplayer.units;

import battlecode.common.*;
import sandmenplayer.Communication;
import sandmenplayer.RobotPlayer;
import sandmenplayer.Signals;

import java.util.HashSet;
import java.util.Set;

public class ECenter extends RobotPlayer {
    public static Set<Integer> robotIDs = new HashSet<>();

    public static void runEnlightenmentCenter() throws GameActionException {
        RobotType toBuild = null;
        if (turnCount == 1) {
            toBuild = RobotType.SLANDERER;
        } else {
            toBuild = RobotType.MUCKRAKER;
        }

        int influenceGive = 1;
        boolean built = false;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influenceGive)) {
                rc.buildRobot(toBuild, dir, influenceGive);
                built = true;
                // save robot ID after building
                int newID = rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID();
                robotIDs.add(newID);
            }
        }
        if(!built)
            System.out.println("EC: I couldn't build anything!");


        // remove robot ID if it is destroyed
        for(Integer rbtID : robotIDs) {
            if(!rc.canGetFlag(rbtID)) {
                robotIDs.remove(rbtID);
            }/* else {
                if(rc.canSetFlag(rc.getFlag(rbtID))) {
                    rc.setFlag(rc.getFlag(rbtID));
                    System.out.println("move to: (" + Communication.getLocationFromFlag(rc.getFlag(rbtID)).x + ", " + Communication.getLocationFromFlag(rc.getFlag(rbtID)).y + ")");
                }
            }*/
        }
    }
}

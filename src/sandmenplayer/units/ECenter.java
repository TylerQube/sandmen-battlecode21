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

        boolean defenseFull = true;
        MapLocation testLoc;
        // Check if EC has a full ring of defenses
        for (Direction dir : directions) {
            testLoc = rc.getLocation().add(dir);
            if (!rc.isLocationOccupied(testLoc) && rc.onTheMap(testLoc))
                defenseFull = false;
        }

        // signal robots to move if defense is complete
        if (defenseFull) {
            System.out.println("All adjacent tiles are full!");
            for (Direction dir : directions) {
                testLoc = rc.getLocation().add(dir);
                // if location is valid and is occupied by an ally
                if (rc.onTheMap(testLoc) && rc.isLocationOccupied(testLoc) && Communication.isAlly(rc.senseRobotAtLocation(testLoc))) {
                    int flag = Communication.getFlagFromLocation(testLoc, Signals.BEGIN_MOVING);
                    if(rc.canSetFlag(flag)) {
                        rc.setFlag(flag);
                        break;
                    }
                }

            }
        } else {
            System.out.println("Defense is not full!");
        }


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

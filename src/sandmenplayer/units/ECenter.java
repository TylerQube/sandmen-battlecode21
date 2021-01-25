package sandmenplayer.units;

import battlecode.common.*;
import sandmenplayer.Communication;
import sandmenplayer.RobotPlayer;
import sandmenplayer.Signals;

import java.util.HashSet;
import java.util.Set;

public class ECenter extends RobotPlayer {
    public static Set<Integer> robotIDs = new HashSet<>();
    public static Set<MapLocation> enemyECLocations = new HashSet<>();

    public static MapLocation slandererHideaway = null;

    public static void runEnlightenmentCenter() throws GameActionException {
        if(enemyECLocations.size() == 0) {
            runEarlyPhase();
        } else if(enemyECLocations.size() > 0) {
            // attack phase
            System.out.println("ENEMY EC FOUND at " + enemyECLocations.toArray()[0].toString());
        }
    }

    public static void runEarlyPhase() throws GameActionException {
        RobotType toBuild = null;
        if (turnCount == 1) {
            toBuild = RobotType.SLANDERER;
        } else {
            toBuild = RobotType.MUCKRAKER;
        }

        tryBuildRobot(toBuild);
        checkExistingRobots();
    }

    public static void tryBuildRobot(RobotType rbtType) throws GameActionException {
        int influenceGive = 1;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(rbtType, dir, influenceGive)) {
                rc.buildRobot(rbtType, dir, influenceGive);
                // save robot ID after building
                int newID = rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID();
                robotIDs.add(newID);
            }
        }
    }

    public static void checkExistingRobots() throws GameActionException {
        // remove robot ID if it is destroyed
        for(Integer rbtID : robotIDs) {
            if(!rc.canGetFlag(rbtID)) {
                robotIDs.remove(rbtID);
            } else {
                processRobotFlag(rc.getFlag(rbtID));
            }
        }
    }

    public static void processRobotFlag(int flag) throws GameActionException {
        MapLocation signalLoc = Communication.getLocationFromFlag(flag);
        int signal = Communication.getSignalFromFlag(flag);

        switch(signal) {
            case Signals.EC_ENEMY:
                // enemy EC found, store location
                enemyECLocations.add(signalLoc);
                break;
            case Signals.SLANDERER_EDGE:
                MapLocation curLoc = rc.getLocation();
                // set new slanderer hideaway if closer than current location
                if(slandererHideaway == null || curLoc.distanceSquaredTo(signalLoc) < rc.getLocation().distanceSquaredTo(slandererHideaway)) {
                    slandererHideaway = signalLoc;
                    if(rc.canSetFlag(flag)) {
                        rc.setFlag(flag);
                        System.out.println("New slanderer loc at " + slandererHideaway.toString());
                    }

                }

        }
    }
}

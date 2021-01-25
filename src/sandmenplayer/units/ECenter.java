package sandmenplayer.units;

import battlecode.common.*;
import sandmenplayer.Communication;
import sandmenplayer.RobotPlayer;
import sandmenplayer.Signals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ECenter extends RobotPlayer {
    public static Set<Integer> robotIDs = new HashSet<>();
    public static Set<MapLocation> enemyECLocations = new HashSet<>();

    public static void runEnlightenmentCenter() throws GameActionException {
        if(turnCount < 18) {
            runEarlyPhase();
            return;
        }
        runDefaultPhase();
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
    
    static boolean muckBuild = false;
    public static void runDefaultPhase() throws GameActionException {
        RobotType toBuild = null;
        if (!muckBuild) {
            toBuild = RobotType.SLANDERER;
        } else {
            toBuild = RobotType.MUCKRAKER;
        }

        if(tryBuildRobot(toBuild))
            muckBuild = !muckBuild;
        checkExistingRobots();
    }

    // return whether robot was built successfully
    public static boolean tryBuildRobot(RobotType rbtType) throws GameActionException {
        int influenceGive = 1;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(rbtType, dir, influenceGive)) {
                rc.buildRobot(rbtType, dir, influenceGive);
                // save robot ID after building
                int newID = rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID();
                robotIDs.add(newID);
                return true;
            }
        }
        return false;
    }

    public static void checkExistingRobots() throws GameActionException {
        // remove robot ID if it is destroyed
        for(Iterator<Integer> itr = robotIDs.iterator(); itr.hasNext();) {
            Integer rbtID = itr.next();
            if(!rc.canGetFlag(rbtID)) {
                itr.remove();
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
        }
    }
}

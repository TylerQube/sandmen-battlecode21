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

    public static MapLocation slandererHideaway = null;

    public static void runEnlightenmentCenter() throws GameActionException {
        poliInf = Math.max(20, rc.getInfluence()/25);
        muckInf = Math.max(1, rc.getInfluence()/100);
        slandererInf = Math.max(5, rc.getInfluence()/50);

        if(enemyECLocations.size() > 0 && rc.getInfluence() >= 100) {
            runAttackPhase();
            System.out.println("ENEMY EC FOUND at " + enemyECLocations.toArray()[0].toString());
        } else if(turnCount < 18) {
            runEarlyPhase();
        } else {
            runDefaultPhase();
        }
    }

    static int poliInf;
    static int muckInf;
    static int slandererInf;

    static int currentGiveInf;

    public static void runEarlyPhase() throws GameActionException {
        RobotType toBuild = null;
        if (turnCount == 1) {
            toBuild = RobotType.SLANDERER;
            currentGiveInf = slandererInf;
        } else {
            toBuild = RobotType.MUCKRAKER;
            currentGiveInf = muckInf;
        }

        tryBuildRobot(toBuild, currentGiveInf);
        checkExistingRobots();
    }
    
    static boolean muckBuild = false;
    public static void runDefaultPhase() throws GameActionException {
        RobotType toBuild = null;
        if (!muckBuild) {
            toBuild = RobotType.SLANDERER;
            currentGiveInf = slandererInf;
        } else {
            toBuild = RobotType.MUCKRAKER;
            currentGiveInf = muckInf;
        }

        if(tryBuildRobot(toBuild, currentGiveInf))
            muckBuild = !muckBuild;
        checkExistingRobots();
    }

    static boolean poliBuild = true;

    public static void runAttackPhase() throws GameActionException {
        // set flag to communicate enemy EC
        // only first located EC for now
        int enemyEcFlag = Communication.getFlagFromLocation(enemyECLocations.iterator().next(), Signals.EC_ENEMY);
        if(rc.canSetFlag(enemyEcFlag))
            rc.setFlag(enemyEcFlag);

        // spawn units
        RobotType toBuild = null;
        if (poliBuild) {
            toBuild = RobotType.POLITICIAN;
            currentGiveInf = poliInf;
        } else {
            toBuild = RobotType.MUCKRAKER;
            currentGiveInf = muckInf;
        }

        if(tryBuildRobot(toBuild, currentGiveInf))
            poliBuild = !poliBuild;
    }


    // return whether robot was built successfully
    public static boolean tryBuildRobot(RobotType rbtType, int influenceGive) throws GameActionException {
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

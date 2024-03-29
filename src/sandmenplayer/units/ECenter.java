package sandmenplayer.units;

import battlecode.common.*;
import sandmenplayer.Communication;
import sandmenplayer.RobotPlayer;
import sandmenplayer.Signals;

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.lang.Math;

public class ECenter extends RobotPlayer {
    public static Set<Integer> robotIDs = new HashSet<>();
    public static Set<MapLocation> enemyECLocations = new HashSet<>();
    public static Set<MapLocation> neutralECLocations = new HashSet<>();
    public static MapLocation slandererHideaway = null;
    static int muckInf;
    static int slandererInf;
    static int currentGiveInf;

    public static void runEnlightenmentCenter() throws GameActionException {
        muckInf = Math.max(1, rc.getInfluence()/100);
        slandererInf = Math.max(5, rc.getInfluence()/50);

        if(enemyECLocations.size() > 0 && rc.getInfluence() >= 100) {
            System.out.println("ENEMY ATTACK PHASE");
            runEnemyAttackPhase();
        } else if(neutralECLocations.size() > 0 && rc.getInfluence() >= 100) {
            System.out.println("NEUTRAL ATTACK PHASE");
            runNeutralAttackPhase();
        } else if(turnCount < 18) {
            System.out.println("EARLY PHASE");
            runEarlyPhase();
        } else {
            System.out.println("DEFAULT PHASE");
            runDefaultPhase();
        }
    }

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
            System.out.println("Trying to spawn slandeerer");
        } else {
            toBuild = RobotType.MUCKRAKER;
            currentGiveInf = muckInf;
            System.out.println("Trying to spawn muckraker");

        }

        if(tryBuildRobot(toBuild, currentGiveInf))
            muckBuild = !muckBuild;
        checkExistingRobots();
        
        if(rc.canBid(2)){
            rc.bid(2);
        }
    }
    static boolean poliBuild = true;

    static int attackBuildCount = 1;

    public static void runEnemyAttackPhase() throws GameActionException {
        // set flag to communicate enemy EC
        // only first located EC for now
        int enemyEcFlag = Communication.getFlagFromLocation(enemyECLocations.iterator().next(), Signals.EC_ENEMY);
        int phaseMax = 175;
        if(rc.canSetFlag(enemyEcFlag))
            rc.setFlag(enemyEcFlag);
        RobotType[] spawnOrder = {RobotType.POLITICIAN, RobotType.POLITICIAN, RobotType.SLANDERER};
        // spawn units
        RobotType toBuild = spawnOrder[attackBuildCount % spawnOrder.length];

        if(tryBuildRobot(toBuild, randomPoli(phaseMax)))
            attackBuildCount += 1;
    }

    public static void runNeutralAttackPhase() throws GameActionException {
        // set flag to communicate enemy EC
        // only first located EC for now
        int neutralEcFlag = Communication.getFlagFromLocation(neutralECLocations.iterator().next(), Signals.EC_NEUTRAL);
        int phaseMax = 150;
        if(rc.canSetFlag(neutralEcFlag))
            rc.setFlag(neutralEcFlag);
        RobotType[] spawnOrder = {RobotType.POLITICIAN, RobotType.POLITICIAN, RobotType.SLANDERER};
        // spawn units
        RobotType toBuild = spawnOrder[attackBuildCount % spawnOrder.length];

        if(tryBuildRobot(toBuild, randomPoli(phaseMax)))
            attackBuildCount += 1;
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

    public static int randomPoli(int phaseMax) throws GameActionException{
        int ranMax = phaseMax; 
        int ranMin = 1; 
        int ranRange = ranMax - ranMin + 1; 
        int ranBid = (int)(Math.random() * ranRange) + ranMin;
        return ranBid;
    }

    public static void processRobotFlag(int flag) throws GameActionException {
        MapLocation signalLoc = Communication.getLocationFromFlag(flag);
        int signal = Communication.getSignalFromFlag(flag);

        switch(signal) {
            case Signals.EC_ENEMY:
                // enemy EC found, store location
                enemyECLocations.add(signalLoc);
                break;
            case Signals.EC_NEUTRAL:
                // enemy EC found, store location
                neutralECLocations.add(signalLoc);
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

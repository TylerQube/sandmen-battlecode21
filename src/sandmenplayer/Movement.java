package sandmenplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Movement extends RobotPlayer {

    public static void runMovement() throws GameActionException {
        // reset target location each time
        targetLocation = null;
        if(ecID != -1) {
            // get flag communication from stored EC ID
            if(rc.canGetFlag(ecID) && rc.getFlag(ecID) != 0) {
                // default flag value is zero, ignore if it hasn't been set
                int ecFlag = rc.getFlag(ecID);
                if(Communication.getSignalFromFlag(ecFlag) == Signals.ATTACK) {
                    targetLocation = Communication.getLocationFromFlag(ecFlag);
                    // when receiving new instruction, closest historical distance is current MapLocation
                    closestToTarget = rc.getLocation().distanceSquaredTo(targetLocation);
                }
            }
            // if the EC sent a location, move towards it
            if(targetLocation != null)
                moveTowardsTarget(targetLocation);
            else
                moveTowardsTarget(rc.getLocation().add(randomDirection()));
        }
    }

    public static void processSurroundings() throws GameActionException {
        for(RobotInfo rbt : rc.senseNearbyRobots()) {
            // if neutral EC is found, broadcast location
            if(rbt.getType().equals(RobotType.ENLIGHTENMENT_CENTER) && !rbt.getTeam().isPlayer()) {
                int flagColor = Communication.getFlagFromLocation(rbt.getLocation(), Signals.CLAIM);
                if(rc.canSetFlag(flagColor))
                    rc.setFlag(flagColor);
            }
        }
    }
}

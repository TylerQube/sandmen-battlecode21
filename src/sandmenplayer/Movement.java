package sandmenplayer;

import battlecode.common.*;

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

    public static void moveTowardsTarget(MapLocation targetLocation) throws GameActionException {
        MapLocation curLocation = rc.getLocation();

        if(!rc.isReady()) {
            // Robot can't move
            return;
        }

        Direction testDir = curLocation.directionTo(targetLocation);
        // test all directions starting with direct path to target location
        for(int i = 0; i < 8; i++) {
            // only move if location is above passability threshold
            if(rc.sensePassability(curLocation.add(testDir)) > passabilityThreshold) {
                // only move if the robot will be closer to the target
                if(curLocation.add(testDir).distanceSquaredTo(targetLocation) < closestToTarget && rc.canMove(testDir) && rc.isReady()) {
                    rc.move(testDir);
                    // update new closest distance to target
                    closestToTarget = curLocation.add(testDir).distanceSquaredTo(targetLocation);
                    return;
                }
            }
            testDir = testDir.rotateRight();
        }

        testDir = curLocation.directionTo(targetLocation);
        // if there is no direct square to get closer, keep the obstacle on the left
        for(int i = 0; i < 8; i++) {
            if(rc.sensePassability(curLocation.add(testDir)) > passabilityThreshold) {
                if (rc.canMove(testDir) && rc.isReady()) {
                    rc.move(testDir);
                }
            }
            testDir = testDir.rotateRight();
        }

/*      if(rc.isReady() && rc.sensePassability(rc.getLocation().add(dirToTarget)) >= passabilityThreshold) {
          tryMove(dirToTarget);
      } else {
          if(avoidDir == null) {
               avoidDir = dirToTarget.rotateLeft();
          }
          for(int i = 0; i < 8; i++) {
              if(rc.canMove(avoidDir) && rc.sensePassability(rc.getLocation().add(avoidDir)) > passabilityThreshold) {
                  rc.move(avoidDir);
                 break;
              }
           }
          avoidDir = avoidDir.rotateRight();
        }
*/
    }
}

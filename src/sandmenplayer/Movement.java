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
            }
        }
        // if the EC sent a location, move towards it
        if(shouldMove) {
            if(targetLocation != null)
                moveTowardsTarget(targetLocation);
            else
                if(defaultDirection == null)
                    System.out.println("I am not trying to move anywhere.");
                else
                    System.out.println("I'm trying to move " + defaultDirection.toString());
                bugPath(defaultDirection);
        }
    }

    public static void processSurroundings() throws GameActionException {
        int flagColor = -1;
        for(RobotInfo rbt : rc.senseNearbyRobots()) {
            if(rbt.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                if(rbt.getTeam().equals(rc.getTeam().opponent())) {
                    flagColor = Communication.getFlagFromLocation(rbt.getLocation(), Signals.EC_ENEMY);
                    break;
                }
                if(!rbt.getTeam().isPlayer()) {
                    flagColor = Communication.getFlagFromLocation(rbt.getLocation(), Signals.EC_NEUTRAL);
                    break;
                }
            }
            else if(rbt.getType().equals(RobotType.SLANDERER)) {
                if(rbt.getTeam().equals(rc.getTeam().opponent())) {
                    flagColor = Communication.getFlagFromLocation(rbt.getLocation(), Signals.SLANDERER_ENEMY);
                    break;
                }
            }
            else if(rbt.getType().equals(RobotType.POLITICIAN)) {
                if(rbt.getTeam().equals(rc.getTeam().opponent())) {
                    flagColor = Communication.getFlagFromLocation(rbt.getLocation(), Signals.POLITICIAN_ENEMY);
                    break;
                }
            }
            else if(rbt.getType().equals(RobotType.MUCKRAKER)) {
                if(rbt.getTeam().equals(rc.getTeam().opponent())) {
                    flagColor = Communication.getFlagFromLocation(rbt.getLocation(), Signals.MUCKRAKER_ENEMY);
                    break;
                }
            }

            if(flagColor != -1 && rc.canSetFlag(flagColor))
                rc.setFlag(flagColor);
        }

        for(RobotInfo rbt : rc.senseNearbyRobots()) {
            if(rbt.getType().equals(RobotType.ENLIGHTENMENT_CENTER) && Communication.isAlly(rbt)) {
                if(rbt.getLocation().isAdjacentTo(rc.getLocation())) {
                    boolean defenseFull = true;
                    for(int i = 0; i < 9; i++) {
                        MapLocation testLoc = rbt.getLocation().add(Direction.allDirections()[i]);
                        if(!rc.onTheMap(testLoc)) {
                            continue;
                        }

                        if(!rc.isLocationOccupied(testLoc) || !Communication.isAlly(rc.senseRobotAtLocation(testLoc))) {
                            defenseFull = false;
                        }
                    }
                    // enable movement if EC surrounded by ring of defense
                    if(defenseFull)
                        shouldMove = true;
                }
            }
        }
    }

    public static void bugPath(Direction targetDir) throws GameActionException {
        if(!rc.isReady()) {
            // Robot can't move
            return;
        }

        MapLocation curLocation = rc.getLocation();
        Direction testDirection = targetDir;

        boolean isRight = true;
        // search adjacent tiles in order of right, left, right, left
        for(int rotateAmt = 0; rotateAmt <= 7; rotateAmt += 1) {
            for(int i = 0; i < rotateAmt; i++) {
                testDirection = isRight ? testDirection.rotateRight() : testDirection.rotateLeft();
            }

            // test the direction
            if(rc.onTheMap(curLocation.add(testDirection))) {
                if(rc.sensePassability(curLocation.add(testDirection)) > passabilityThreshold) {
                    if (rc.canMove(testDirection) && rc.isReady()) {
                        rc.move(testDirection);
                        System.out.println("Moved " + testDirection.toString());
                        defaultDirection = testDirection;
                        return;
                    } else {
                        System.out.println("Can't move " + testDirection.toString());
                    }
                }
            } else {
                System.out.println(testDirection.toString() + " is off the map");
            }

            // flip rotation direction
            isRight = !isRight;
        }
    }

    public static void moveTowardsTarget(MapLocation targetLocation) throws GameActionException {
        MapLocation curLocation = rc.getLocation();

        // invalid target location
        if(!rc.onTheMap(targetLocation)) {
            targetLocation = null;
            return;
        }

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

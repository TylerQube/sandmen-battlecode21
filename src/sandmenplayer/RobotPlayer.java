package sandmenplayer;
import java.util.Set;
import java.util.HashSet;
import battlecode.common.*;
import sandmenplayer.units.ECenter;
import sandmenplayer.units.Muckraker;
import sandmenplayer.units.Politician;
import sandmenplayer.units.Slanderer;

public strictfp class RobotPlayer {
    public static RobotController rc;

    public static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static int turnCount;
    public static Set<Integer> robotIDs = new HashSet<>();
    // -1 means it wasn't created from an EC
    public static int ecID = -1;
    public static MapLocation targetLocation;
    public static Direction defaultDirection;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");

        // save EC ID when created
        defaultDirection = randomDirection();
        for(RobotInfo rbt : rc.senseNearbyRobots(1)) {
            if(rbt.getType().equals(RobotType.ENLIGHTENMENT_CENTER) && rbt.getTeam().equals(rc.getTeam())) {
                ecID = rbt.getID();
                // move opp direction from EC if possible
                defaultDirection = rc.getLocation().directionTo(rbt.getLocation()).rotateRight().rotateRight().rotateRight().rotateRight();
                break;
            }
        }

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: ECenter.runEnlightenmentCenter(); break;
                    case POLITICIAN:           Politician.runPolitician();          break;
                    case SLANDERER:            Slanderer.runSlanderer();           break;
                    case MUCKRAKER:            Muckraker.runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    // Tracks closest historical distance to target MapLocation
    public static float closestToTarget;


    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    public static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    public static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    public static final double passabilityThreshold = 0.6;
    public static Direction avoidDir = null;

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


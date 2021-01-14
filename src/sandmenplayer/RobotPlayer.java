package sandmenplayer;
import java.util.Set;
import java.util.HashSet;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static int turnCount;
    static Set<Integer> robotIDs = new HashSet<>();
    static int ecID;

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
        for(RobotInfo rbt : rc.senseNearbyRobots(1)) {
            if(rbt.getType().equals(RobotType.ENLIGHTENMENT_CENTER) && rbt.getTeam().equals(rc.getTeam())) {
                ecID = rbt.getID();
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
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 50;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
                // save robot ID after building
                for(RobotInfo rbt : rc.senseNearbyRobots(1)) {
                    // only save ID if it is friendly and matches type of built robot
                    if(rbt.getTeam().equals(rc.getTeam()) && rbt.getType().equals(toBuild)) {
                        robotIDs.add(rbt.getID());
                    }
                }
            } else {
                break;
            }
        }

        // remove robot ID if it is destroyed
        for(Integer rbtID : robotIDs) {
            if(!rc.canGetFlag(rbtID)) {
                robotIDs.remove(rbtID);
            }
        }
    }

    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runSlanderer() throws GameActionException {
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    static int getFlagFromLocation(MapLocation loc, int info) {
        int x = loc.x, y = loc.y;
        int flag = (x % 128) << 7;
        flag += (y % 128);
        flag += (info << 14);
        return flag;
    }

    static MapLocation getLocationFromFlag(int flag) {
        int bitmask = 0b1111111;
        int x = (flag >> 7) & bitmask;
        int y = flag & bitmask;
        int msgCode = (flag >> 14);

        MapLocation curLoc = rc.getLocation();
        // divide by 128 to get offset of absolute map coordinates from what would be evenly divisible by 128
        int offsetX = curLoc.x / 128;
        int offsetY = curLoc.y / 128;

        // calculate the location communicated through the flag
        MapLocation flagLoc = new MapLocation(offsetX * 128 + x, offsetY * 128 + y);
        // there are 4 possible coordinates from flag values, iterate through all and find the closest to the robot
        for(int i = -1; i <= 1; i+=1) {
            for(int j = -1; j <= 1; j+=1) {
                if (Math.abs(i) == Math.abs(j)) continue;
                MapLocation testLoc = flagLoc.translate(128*i, 128*j);
                if(rc.getLocation().distanceSquaredTo(testLoc) < rc.getLocation().distanceSquaredTo(absLoc)) {
                    flagLoc = testLoc;
                }
            }
        }

        return flagLoc;
    }

    static final double passabilityThreshold = 0.6;
    static Direction avoidDir = null;

    static void moveTowardsTarget(MapLocation targetLocation) throws GameActionException {
        MapLocation curLocation = rc.getLocation();
        if(curLocation.equals(targetLocation)) {
            // target reached
            // perform next action
            return;
        }

        if(!rc.isReady()) {
            // Robot can't move
            return;
        }

        Direction dirToTarget = curLocation.directionTo(targetLocation);
        if(rc.isReady() && rc.sensePassability(rc.getLocation().add(dirToTarget)) >= passabilityThreshold) {
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
    }
}


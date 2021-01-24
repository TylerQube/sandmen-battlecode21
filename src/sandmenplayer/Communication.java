package sandmenplayer;

import battlecode.common.MapLocation;

public class Communication extends RobotPlayer {
    public static int getFlagFromLocation(MapLocation loc, int info) {
        int x = loc.x, y = loc.y;
        int flag = (x % 128) << 7;
        flag += (y % 128);
        flag += (info << 14);
        return flag;
    }

    public static MapLocation getLocationFromFlag(int flag) {
        int bitmask = 0b1111111;
        int x = (flag >> 7) & bitmask;
        int y = flag & bitmask;

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
                if(rc.getLocation().distanceSquaredTo(testLoc) < rc.getLocation().distanceSquaredTo(flagLoc)) {
                    flagLoc = testLoc;
                }
            }
        }

        return flagLoc;
    }

    public static int getSignalFromFlag(int flag) {
        return flag >> 14;
    }
}

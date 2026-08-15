package igentuman.nr.radiation.simulation;

import net.minecraft.world.phys.Vec3;

public class SubChunkRadVector {

    public static final int DIR_COUNT = 6;
    public static final int DIR_PX = 0;
    public static final int DIR_NX = 1;
    public static final int DIR_PY = 2;
    public static final int DIR_NY = 3;
    public static final int DIR_PZ = 4;
    public static final int DIR_NZ = 5;

    public static final double[][] DIR_NORMALS = {
            { 1,  0,  0},
            {-1,  0,  0},
            { 0,  1,  0},
            { 0, -1,  0},
            { 0,  0,  1},
            { 0,  0, -1},
    };

    public double apexX, apexY, apexZ;

    public final double[] xRayBq = new double[DIR_COUNT];
    public final double[] neutronBq = new double[DIR_COUNT];

    public final Vec3[] tip = new Vec3[DIR_COUNT];

    public final double[] tipApexDist2 = new double[DIR_COUNT];

    public double maxBq;
    public long computedTick;
    public long ttlTicks;

    public SubChunkRadVector() {
        for (int i = 0; i < DIR_COUNT; i++) {
            tip[i] = Vec3.ZERO;
        }
    }

    public boolean isExpired(long now) {
        return now - computedTick > ttlTicks;
    }

    public boolean isEmpty() {
        for (int i = 0; i < DIR_COUNT; i++) {
            if (xRayBq[i] != 0 || neutronBq[i] != 0) return false;
        }
        return true;
    }

    public double totalXRay() {
        double s = 0;
        for (double v : xRayBq) s += v;
        return s;
    }

    public double totalNeutron() {
        double s = 0;
        for (double v : neutronBq) s += v;
        return s;
    }

    public static int classify(double dx, double dy, double dz) {
        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double az = Math.abs(dz);
        if (ax == 0 && ay == 0 && az == 0) return DIR_PX;
        if (ax >= ay && ax >= az) return dx >= 0 ? DIR_PX : DIR_NX;
        if (ay >= az) return dy >= 0 ? DIR_PY : DIR_NY;
        return dz >= 0 ? DIR_PZ : DIR_NZ;
    }
}

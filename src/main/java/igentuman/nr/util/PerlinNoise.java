package igentuman.nr.util;

public final class PerlinNoise {

    private PerlinNoise() {}

    private static final int[] PERM = buildPerm(0x9E3779B9);

    private static int[] buildPerm(int seed) {
        int[] p = new int[512];
        int[] base = new int[256];
        for (int i = 0; i < 256; i++) base[i] = i;

        java.util.Random rng = new java.util.Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = base[i];
            base[i] = base[j];
            base[j] = tmp;
        }
        for (int i = 0; i < 512; i++) p[i] = base[i & 255];
        return p;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    public static double noise(double x, double y, double z) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);

        double u = fade(x);
        double v = fade(y);
        double w = fade(z);

        int[] p = PERM;
        int A = p[X] + Y;
        int AA = p[A] + Z;
        int AB = p[A + 1] + Z;
        int B = p[X + 1] + Y;
        int BA = p[B] + Z;
        int BB = p[B + 1] + Z;

        return lerp(
                lerp(
                        lerp(grad(p[AA], x, y, z), grad(p[BA], x - 1, y, z), u),
                        lerp(grad(p[AB], x, y - 1, z), grad(p[BB], x - 1, y - 1, z), u),
                        v),
                lerp(
                        lerp(grad(p[AA + 1], x, y, z - 1), grad(p[BA + 1], x - 1, y, z - 1), u),
                        lerp(grad(p[AB + 1], x, y - 1, z - 1), grad(p[BB + 1], x - 1, y - 1, z - 1), u),
                        v),
                w);
    }
}

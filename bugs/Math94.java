// Defects4J Math-94 (Apache Commons Math, MathUtils.gcd)
// Extracted buggy method, minimized to compile standalone.
public final class Math94 {

    /**
     * Gets the greatest common divisor of the absolute value of two numbers,
     * using the "binary gcd" method (Knuth, TAOCP vol. 2, 4.5.2).
     */
    public static int gcd(int u, int v) {
        if (u * v == 0) {
            return (Math.abs(u) + Math.abs(v));
        }
        // keep u and v negative, as negative integers range down to
        // -2^31, while positive numbers can only be as large as 2^31-1
        if (u > 0) {
            u = -u;
        }
        if (v > 0) {
            v = -v;
        }
        // B1. [Find power of 2]
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 31) {
            u /= 2;
            v /= 2;
            k++;
        }
        if (k == 31) {
            throw new ArithmeticException("overflow: gcd is 2^31");
        }
        // B2. Initialize
        int t = ((u & 1) == 1) ? v : -(u / 2);
        // B3/B4
        do {
            while ((t & 1) == 0) {
                t /= 2;
            }
            // B5 [reset max(u,v)]
            if (t > 0) {
                u = -t;
            } else {
                v = t;
            }
            // B6/B3. take the difference
            t = (v - u) / 2;
        } while (t != 0);
        return -u * (1 << k);
    }
}

// Runs each extracted method with an input that triggers the known bug.
// Every line should print FAIL on the buggy code (proves the bug is real).
public class ReproduceBugs {

    static void check(String bug, boolean ok, String detail) {
        System.out.printf("%-8s %s  %s%n", bug, ok ? "PASS" : "FAIL", detail);
    }

    public static void main(String[] args) {
        try {
            Class<?>[] r = Lang33.toClass(new Object[] {"x", null});
            check("Lang-33", r[1] == null, "toClass([\"x\", null])");
        } catch (RuntimeException e) {
            check("Lang-33", false, "toClass([\"x\", null]) threw " + e.getClass().getSimpleName());
        }

        int g = Math94.gcd(1 << 16, 1 << 16);
        check("Math-94", g == 65536, "gcd(65536, 65536) = " + g + ", expected 65536");

        Chart1 renderer = new Chart1(new Chart1.CategoryPlot() {
            public int getIndexOf(Chart1 r) { return 0; }
            public Chart1.CategoryDataset getDataset(int i) { return () -> 3; }
        });
        int n = renderer.getLegendItems().size();
        check("Chart-1", n == 3, "legend items = " + n + ", expected 3");

        try {
            String s = Lang39.replaceEach("abc", new String[] {"a", null}, new String[] {"x", null});
            check("Lang-39", "xbc".equals(s), "replaceEach -> " + s);
        } catch (RuntimeException e) {
            check("Lang-39", false, "replaceEach with null entry threw " + e.getClass().getSimpleName());
        }
    }
}

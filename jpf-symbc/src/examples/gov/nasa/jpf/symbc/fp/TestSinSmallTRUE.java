package gov.nasa.jpf.symbc.fp;

public class TestSinSmallTRUE {

    public static void main(String[] args) {
        TestSinSmallTRUE t = new TestSinSmallTRUE();
        t.test(0.005, 2.0);
    }

    public void test(double x, double y) {
        if (x >= 0.0 && x <= 0.01) {
            double s = Math.sin(x);
            assert s < 0.02;
        }
    }
}

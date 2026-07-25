package gov.nasa.jpf.symbc.fp;

public class TestSinConstrainedTRUE {

    public static void main(String[] args) {
        TestSinConstrainedTRUE t = new TestSinConstrainedTRUE();
        t.test(1.55, 2.0);
    }

    public void test(double x, double y) {
        if (x >= 1.5 && x <= 1.6) {
            double s = Math.sin(x);
            assert s > 0.99;
        }
    }
}

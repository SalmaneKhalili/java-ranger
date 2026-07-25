package gov.nasa.jpf.symbc.fp;

public class TestSinGuardTRUE {

    public static void main(String[] args) {
        TestSinGuardTRUE t = new TestSinGuardTRUE();
        t.test(1.0, 2.0);
    }

    public void test(double x, double y) {
        if (x > 0.0 && x < 1.5707963267948966) {
            double s = Math.sin(x);
            assert s > 0.0;
        }
    }
}

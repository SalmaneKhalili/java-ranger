package gov.nasa.jpf.symbc.fp.sin;

public class TestSinRangeBoundTRUE {

    public static void main(String[] args) {
        TestSinRangeBoundTRUE t = new TestSinRangeBoundTRUE();
        t.test(1.0, 2.0);
    }

    public void test(double x, double y) {
        double s = Math.sin(x);
        assert !(s > 1.0 || s < -1.0);
    }
}

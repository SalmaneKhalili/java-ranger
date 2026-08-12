package gov.nasa.jpf.symbc.fp.sin;

public class TestSinRangeTRUE {

    public static void main(String[] args) {
        TestSinRangeTRUE t = new TestSinRangeTRUE();
        t.test(1.0, 2.0);
    }

    public void test(double x, double y) {
        double s = Math.sin(x);
        assert s >= -1.0 && s <= 1.0;
    }
}

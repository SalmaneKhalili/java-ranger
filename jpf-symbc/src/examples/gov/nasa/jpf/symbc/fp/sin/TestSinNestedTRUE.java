package gov.nasa.jpf.symbc.fp.sin;

public class TestSinNestedTRUE {

    public static void main(String[] args) {
        TestSinNestedTRUE t = new TestSinNestedTRUE();
        t.test(1.0, 2.0);
    }

    public void test(double x, double y) {
        double s = Math.sin(x);
        double ss = Math.sin(s);
        assert ss >= -1.0 && ss <= 1.0;
    }
}

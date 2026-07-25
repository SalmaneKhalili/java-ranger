package gov.nasa.jpf.symbc.fp;

public class TestSinNonNegFALSE {

    public static void main(String[] args) {
        TestSinNonNegFALSE t = new TestSinNonNegFALSE();
        t.test(1.0, 2.0);
    }

    public void test(double x, double y) {
        if (x >= 0.0 && x <= 3.141592653589793) {
            double s = Math.sin(x);
            assert s < 0.0;
        }
    }
}

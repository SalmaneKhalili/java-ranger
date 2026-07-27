package gov.nasa.jpf.symbc.fp;

public class TestSinNegTRUE {

    public static void main(String[] args) {
        TestSinNegTRUE t = new TestSinNegTRUE();
        t.test(-1.0, 2.0);
    }

    public void test(double x, double y) {
        if (x >= -3.0 && x <= -0.1) {
            double s = Math.sin(x);
            assert s < 0.0;
        }
    }
}

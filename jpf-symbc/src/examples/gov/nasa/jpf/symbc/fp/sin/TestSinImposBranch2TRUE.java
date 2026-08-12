package gov.nasa.jpf.symbc.fp.sin;

public class TestSinImposBranch2TRUE {

    public static void main(String[] args) {
        TestSinImposBranch2TRUE t = new TestSinImposBranch2TRUE();
        t.test(1.0, 2.0);
    }

    public void test(double x, double y) {
        double s = Math.sin(x);
        if (s < -2.0) {
            assert false;
        }
    }
}

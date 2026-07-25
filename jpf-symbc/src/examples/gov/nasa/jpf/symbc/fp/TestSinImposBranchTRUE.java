package gov.nasa.jpf.symbc.fp;

public class TestSinImposBranchTRUE {

    public static void main(String[] args) {
        TestSinImposBranchTRUE t = new TestSinImposBranchTRUE();
        t.test(1.0, 2.0);
    }

    public void test(double x, double y) {
        double s = Math.sin(x);
        if (s > 2.0) {
            assert false;
        }
    }
}

package gov.nasa.jpf.symbc.fp.sin;

public class TestSinFeasBranchFALSE {

    public static void main(String[] args) {
        TestSinFeasBranchFALSE t = new TestSinFeasBranchFALSE();
        t.test(1.0, 2.0);
    }

    public void test(double x, double y) {
        double s = Math.sin(x);
        if (s > 0.5) {
            assert false;
        }
    }
}

package gov.nasa.jpf.symbc.fp.cmp;

public class TestDCmplEqNaN {

    public static void main(String[] args) {
        TestDCmplEqNaN t = new TestDCmplEqNaN();
        t.test(0.0, 0.0);
    }

    public void test(double x, double y) {
        // DCMPL (double): NaN == anything is false (dcmpl returns -1, ifeq does not jump).
        // With NaN in the FP domain, assert false is reachable.
        // Expected verdict: FALSE
        assert x == y;
    }
}

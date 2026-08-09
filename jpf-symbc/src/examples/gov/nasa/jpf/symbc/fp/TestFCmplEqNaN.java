package gov.nasa.jpf.symbc.fp;

public class TestFCmplEqNaN {

    public static void main(String[] args) {
        TestFCmplEqNaN t = new TestFCmplEqNaN();
        t.test(0.0f, 0.0f);
    }

    public void test(float x, float y) {
        // FCMPL: NaN == anything is false (fcmpl returns -1, ifeq does not jump).
        // With NaN in the FP domain, assert false is reachable.
        // Expected verdict: FALSE
        assert x == y;
    }
}

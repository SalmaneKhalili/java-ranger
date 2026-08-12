package gov.nasa.jpf.symbc.fp;

public class TestFCmplGeNaN {

    public static void main(String[] args) {
        TestFCmplGeNaN t = new TestFCmplGeNaN();
        t.test(0.0f, 0.0f);
    }

    public void test(float x, float y) {
        // FCMPL: NaN >= NaN is false (fcmpl returns -1, ifge does not jump).
        // With NaN in the FP domain, assert false is reachable.
        // y is unused but needed to match sym#sym config.
        // Expected verdict: FALSE
        assert x >= y;
    }
}

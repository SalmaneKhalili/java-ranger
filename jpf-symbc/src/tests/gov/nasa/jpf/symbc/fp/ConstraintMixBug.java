package gov.nasa.jpf.symbc.fp;

public class ConstraintMixBug {
    public void testNaN(float x) {
        assert x != x;
    }
}

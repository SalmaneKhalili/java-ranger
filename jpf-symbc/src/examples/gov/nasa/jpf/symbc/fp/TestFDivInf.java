package gov.nasa.jpf.symbc.fp;

public class TestFDivInf {

    public static void main(String[] args) {
        TestFDivInf t = new TestFDivInf();
        t.testFDivInf(5.0f, 6.0f);
    }

    public void testFDivInf(float x, float y) {
        float z = x / y;
        // FDIV with both symbolic operands
        // y (divisor) can be zero, NaN, Inf, or normal
        // Simple check: NaN is the only case where z != z
        if (z == z) {
            System.out.println("Not NaN");
        } else {
            System.out.println("NaN path");
            assert false;
        }
    }
}

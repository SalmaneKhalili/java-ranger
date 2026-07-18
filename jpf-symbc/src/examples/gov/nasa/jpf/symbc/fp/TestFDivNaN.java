package gov.nasa.jpf.symbc.fp;

public class TestFDivNaN {

    public static void main(String[] args) {
        TestFDivNaN t = new TestFDivNaN();
        t.testFDivNaN(5.0f, 6.0f);
    }

    public void testFDivNaN(float x, float y) {
        float z = x / y;
        // FDIV with both symbolic operands
        // y (divisor) can be zero, NaN, or normal
        if (z != z) {
            System.out.println("NaN path");
            assert false;
        } else {
            System.out.println("Normal path");
        }
    }
}

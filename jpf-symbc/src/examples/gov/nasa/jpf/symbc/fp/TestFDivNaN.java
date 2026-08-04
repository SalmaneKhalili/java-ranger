package gov.nasa.jpf.symbc.fp;

import org.sosy_lab.sv_benchmarks.Verifier;

public class TestFDivNaN {

    public static void main(String[] args) {
        TestFDivNaN t = new TestFDivNaN();
        // Symcrete pattern: exercise both operand orders.
        t.testFDivNaN(5.0f, Verifier.nondetFloat());
        t.testFDivNaN(Verifier.nondetFloat(), 6.0f);
    }

    // FDIV NaN semantics: the NaN-divisor branch (choice 1 of the FDIV
    // choice generator) must push NaN.  The engine cannot yet express
    // "x != x is true for NaN" symbolically (the FCMP unordered case is
    // not explored), so the concrete result is classified via its IEEE 754
    // bit pattern instead.
    public void testFDivNaN(float x, float y) {
        float res = x / y;
        int absBits = Float.floatToIntBits(res) & 0x7FFFFFFF;
        if (absBits > 0x7F800000) {
            System.out.println("NaN result produced");
        } else {
            System.out.println("non-NaN result");
        }
    }
}

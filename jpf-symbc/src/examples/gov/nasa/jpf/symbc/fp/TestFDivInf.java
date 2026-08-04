package gov.nasa.jpf.symbc.fp;

import org.sosy_lab.sv_benchmarks.Verifier;

public class TestFDivInf {

    public static void main(String[] args) {
        TestFDivInf t = new TestFDivInf();
        // Symcrete pattern: exercise both operand orders.
        t.testFDivInf(5.0f, Verifier.nondetFloat());
        t.testFDivInf(Verifier.nondetFloat(), 6.0f);
    }

    // FDIV Infinity semantics: with symbolic.inf=true the divisor can be
    // infinite, and x/Inf is +-0 (Inf/Inf is NaN, but the dividend here is
    // finite).  The Inf-divisor case is choice 2 of the FDIV choice
    // generator.
    public void testFDivInf(float x, float y) {
        float res = x / y;
        if (y == Float.POSITIVE_INFINITY) {
            System.out.println("+Inf divisor -> zero");
            assert res == 0.0f;
        } else {
            System.out.println("non-+Inf divisor");
        }
    }
}

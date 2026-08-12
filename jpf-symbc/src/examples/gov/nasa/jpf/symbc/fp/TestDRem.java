package gov.nasa.jpf.symbc.fp;

import org.sosy_lab.sv_benchmarks.Verifier;

public class TestDRem {

    public static void main(String[] args) {
        TestDRem t = new TestDRem();

        // 1. Regular Symcrete Matrix (Concrete / Symbolic combinations)
        t.test(5.0, 6.0);                             // Concrete - Concrete
        t.test(5.0, Verifier.nondetDouble());         // Concrete - Symbolic
        t.test(Verifier.nondetDouble(), 6.0);         // Symbolic - Concrete
        t.test(Verifier.nondetDouble(), Verifier.nondetDouble()); // Symbolic - Symbolic

        // 2. IEEE 754 Edge Case Injections
        t.test(Double.NaN, Verifier.nondetDouble());              // NaN dividend
        t.test(Verifier.nondetDouble(), Double.POSITIVE_INFINITY); // +Inf divisor
        t.test(Double.NEGATIVE_INFINITY, Verifier.nondetDouble()); // -Inf dividend
        t.test(Verifier.nondetDouble(), 0.0);                   // Zero divisor
        t.test(-0.0, Verifier.nondetDouble());                  // Negative zero dividend

        // 3. NaN Unordered Comparison Check (DCMP Instruction)
        t.testNaN(Double.NaN);
        t.testNaN(Verifier.nondetDouble());
    }

    // DREM with symbolic operands: the result can be NaN, zero, or a
    // normal remainder. Unlike division, DREM never yields Infinity
    // (x % 0 = NaN, x % Inf = x, NaN/Inf % y = NaN). This branches on
    // the result classification so the symbolic engine must explore all
    // three outcomes.
    public void test(double x, double y) {
        double res = x % y;
        if (res != res) {
            System.out.println("nan");
        } else if (res == 0.0) {
            System.out.println("zero");
        } else {
            System.out.println("normal: " + res);
        }
    }

    // NaN is the only value for which x != x (IEEE 754). This assertion
    // fires whenever x is not NaN, exercising the NaN semantics of DCMP.
    public void testNaN(double x) {
        if (x != x) {
            System.out.println("x is NaN");
        } else {
            System.out.println("x is not NaN");
        }
    }
}

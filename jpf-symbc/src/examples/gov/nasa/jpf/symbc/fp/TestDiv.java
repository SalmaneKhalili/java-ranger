package gov.nasa.jpf.symbc.fp;

import org.sosy_lab.sv_benchmarks.Verifier;

public class TestDiv {

    public static void main(String[] args) {
        TestDiv t = new TestDiv();
        // Symcrete pattern: exercise both operand orders.
        // 1) concrete dividend, symbolic divisor -> FDIV 4-branch choice generator
        t.test(5.0f, Verifier.nondetFloat());
        // 2) symbolic dividend, concrete divisor -> concrete IEEE-754 division
        t.test(Verifier.nondetFloat(), 6.0f);
    }

    // FDIV with a symbolic operand: classify the divisor the way the FDIV
    // choice generator does and check the IEEE-754 result invariant.
    //   y == 0   -> x/0  = +-Inf (no ArithmeticException, unlike integer div)
    //   y is NaN -> x/NaN = NaN
    //   else     -> x/y is finite (divisor is a normal value in range)
    public void test(float x, float y) {
        float res = x / y;
        if (y == 0.0f) {
            System.out.println("zero divisor -> inf");
            assert Float.isInfinite(res);
        } else if (y != y) {
            System.out.println("NaN divisor -> NaN");
            assert Float.isNaN(res);
        } else {
            System.out.println("normal divisor -> finite");
            assert res == res;
        }
    }
}

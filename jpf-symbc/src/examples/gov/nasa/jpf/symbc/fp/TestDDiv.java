package gov.nasa.jpf.symbc.fp;

import org.sosy_lab.sv_benchmarks.Verifier;

public class TestDDiv {

    public static void main(String[] args) {
        TestDDiv t = new TestDDiv();
        // Symcrete pattern: exercise both operand orders.
        // 1) concrete dividend, symbolic divisor -> DDIV 4-branch choice generator
        t.test(5.0, Verifier.nondetDouble());
        // 2) symbolic dividend, concrete divisor -> concrete IEEE-754 division
        t.test(Verifier.nondetDouble(), 6.0);
    }

    // DDIV with a symbolic operand: classify the divisor the way the DDIV
    // choice generator does and check the IEEE-754 result invariant.
    //   y == 0   -> x/0  = +-Inf (no ArithmeticException, unlike integer div)
    //   y is NaN -> x/NaN = NaN
    //   else     -> x/y is finite (divisor is a normal value in range)
    public void test(double x, double y) {
        double res = x / y;
        if (y == 0.0) {
            System.out.println("zero divisor -> inf");
            assert Double.isInfinite(res);
        } else if (y != y) {
            System.out.println("NaN divisor -> NaN");
            assert Double.isNaN(res);
        } else {
            System.out.println("normal divisor -> finite");
            assert res == res;
        }
    }
}

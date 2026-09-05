package gov.nasa.jpf.symbc.fp;

import org.sosy_lab.sv_benchmarks.Verifier;

public class TestDDivSymSym {

    public static void main(String[] args) {
        double x = Verifier.nondetDouble();
        double y = Verifier.nondetDouble();
        test(x, y);
    }

    // DDIV with both operands symbolic: the result can be +Inf, -Inf, zero,
    // NaN, or a normal value. Each arm pushes its concrete class representative,
    // so downstream classification is deterministic per path.
    public static void test(double x, double y) {
        double res = x / y;
        if (res == Double.POSITIVE_INFINITY) {
            System.out.println("+inf");
        } else if (res == Double.NEGATIVE_INFINITY) {
            System.out.println("-inf");
        } else if (res == 0.0d) {
            System.out.println("zero");
        } else if (res != res) {
            System.out.println("nan");
        } else {
            System.out.println("normal: " + res);
        }
    }
}
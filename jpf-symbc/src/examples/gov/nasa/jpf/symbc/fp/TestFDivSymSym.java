package gov.nasa.jpf.symbc.fp;

import org.sosy_lab.sv_benchmarks.Verifier;

public class TestFDivSymSym {

    public static void main(String[] args) {
        float x = Verifier.nondetFloat();
        float y = Verifier.nondetFloat();
        test(x, y);
    }

    // FDIV with both operands symbolic: the result can be +Inf, -Inf, zero,
    // NaN, or a normal value. Each arm pushes its concrete class representative,
    // so downstream classification is deterministic per path.
    public static void test(float x, float y) {
        float res = x / y;
        if (res == Float.POSITIVE_INFINITY) {
            System.out.println("+inf");
        } else if (res == Float.NEGATIVE_INFINITY) {
            System.out.println("-inf");
        } else if (res == 0.0f) {
            System.out.println("zero");
        } else if (res != res) {
            System.out.println("nan");
        } else {
            System.out.println("normal: " + res);
        }
    }
}
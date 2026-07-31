package gov.nasa.jpf.symbc.fp;

public class TestDDiv {

    public static void main(String[] args) {
        TestDDiv t = new TestDDiv();
        t.test(5.0, 6.0);
    }

    // DDIV with symbolic operands: the result can be +Inf, -Inf, zero,
    // NaN, or a normal value. This branches on the result classification
    // so the symbolic engine must explore all five outcomes.
    public void test(double x, double y) {
        double res = x / y;
        if (res == Double.POSITIVE_INFINITY) {
            System.out.println("+inf");
        } else if (res == Double.NEGATIVE_INFINITY) {
            System.out.println("-inf");
        } else if (res == 0.0) {
            System.out.println("zero");
        } else if (res != res) {
            System.out.println("nan");
        } else {
            System.out.println("normal: " + res);
        }
    }

    // NaN is the only value for which x != x (IEEE 754). This assertion
    // fires whenever x is not NaN, exercising the NaN semantics of DCMPL.
    public void testNaN(double x) {
        assert x != x;
    }
}

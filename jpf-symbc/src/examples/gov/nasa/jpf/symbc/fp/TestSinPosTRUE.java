package gov.nasa.jpf.symbc.fp;

public class TestSinPosTRUE {

    public static void main(String[] args) {
        TestSinPosTRUE t = new TestSinPosTRUE();
        t.test(1.0, 2.0);
    }

    public void test(double x, double y) {
        if (x >= 0.1 && x <= 3.0) {
            double s = Math.sin(x);
            assert s > 0.0;
        }
    }
}

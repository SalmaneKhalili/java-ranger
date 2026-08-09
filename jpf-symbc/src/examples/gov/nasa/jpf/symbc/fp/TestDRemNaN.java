package gov.nasa.jpf.symbc.fp;

public class TestDRemNaN {

    public static void main(String[] args) {
        TestDRemNaN t = new TestDRemNaN();
        t.testDRemNaN(5.0, 6.0);
    }

    public void testDRemNaN(double x, double y) {
        double z = x % y;
        if (z != z) {
            System.out.println("NaN path");
            assert false;
        } else {
            System.out.println("Normal path");
        }
    }
}

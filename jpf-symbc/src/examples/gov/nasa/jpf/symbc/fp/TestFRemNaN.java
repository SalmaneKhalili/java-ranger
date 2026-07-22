package gov.nasa.jpf.symbc.fp;

public class TestFRemNaN {

    public static void main(String[] args) {
        TestFRemNaN t = new TestFRemNaN();
        t.testFRemNaN(5.0f, 6.0f);
    }

    public void testFRemNaN(float x, float y) {
        float z = x % y;
        if (z != z) {
            System.out.println("NaN path");
            assert false;
        } else {
            System.out.println("Normal path");
        }
    }
}

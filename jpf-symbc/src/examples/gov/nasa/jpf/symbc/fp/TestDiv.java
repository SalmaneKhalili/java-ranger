package gov.nasa.jpf.symbc.fp;

public class TestDiv {

    public static void main(String[] args) {
        TestDiv t = new TestDiv();
        t.testNaN(5.0f);

    }

    public void testNaN(float x){
        assert x != x; // fires because NaN != NaN, so x==x is false for NaN
    }

    public void test(float x, float y) {
        float res = x / y;
        // Branch on FDIV result classification
        if (res == Float.POSITIVE_INFINITY) {
            System.out.println("+inf");
        } else if (res == Float.NEGATIVE_INFINITY) {
            System.out.println("-inf");
        } else if (res == 0.0f) {
            System.out.println("zero");   // 0/x, x/inf
        } else if (res != res) {
            System.out.println("nan");
        } else {
            System.out.println("normal: " + res);
        }
    }

    public void testFDivChain(float x, float y) {
        float q = x / y;
        // Chain 1: 0/0 = NaN
        assert !(x == 0.0f && y == 0.0f && q == q);
    }
}

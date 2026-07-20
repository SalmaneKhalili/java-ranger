package rangerregression.ISUBOverflow_true;

import org.sosy_lab.sv_benchmarks.Verifier;

public class MainProp1 {

  public static void main(String[] args) {
    int a = Verifier.nondetInt();
    int b = Verifier.nondetInt();
    MainProp1 inst = new MainProp1();
    inst.test(a, b);
  }

  /*
   * Test ISUB overflow simulation: a - b + b must always equal a,
   * even when a - b overflows. This holds because subtraction overflow
   * is the inverse of addition overflow.
   * Expected verdict: TRUE
   */
  public void test(int a, int b) {
    int r = a - b;
    assert r + b == a : "overflow wrapping is not consistent";
    if (r > 0)
      System.out.println("R_POS");
    else if (r == 0)
      System.out.println("R_ZERO");
    else
      System.out.println("R_NEG");
  }
}

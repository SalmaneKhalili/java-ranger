package rangerregression.ISUBOverflow_false;

import org.sosy_lab.sv_benchmarks.Verifier;

public class MainProp1 {

  public static void main(String[] args) {
    int a = Verifier.nondetInt();
    if (a != Integer.MIN_VALUE) return; // force a to MIN_VALUE to trigger overflow
    MainProp1 inst = new MainProp1();
    inst.test(a);
  }

  /*
   * Test ISUB with overflow: MIN_VALUE - 1 underflows to MAX_VALUE.
   * The assertion "r < a" fails because the wrapped result (MAX_VALUE)
   * is greater than MIN_VALUE. Without overflow simulation, the solver
   * produces the unwrapped result (-2147483649) which IS < MIN_VALUE,
   * making the assertion pass incorrectly.
   * Expected verdict: FALSE
   */
  public void test(int a) {
    int r = a - 1;
    assert r < a : "overflow not simulated: MIN_VALUE - 1 should wrap to MAX_VALUE";
    if (r > 0)
      System.out.println("R_POS");
    else if (r == 0)
      System.out.println("R_ZERO");
    else
      System.out.println("R_NEG");
  }
}

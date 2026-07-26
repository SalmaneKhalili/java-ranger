package overflow.ISUBOverflow_true;

import org.sosy_lab.sv_benchmarks.Verifier;

public class MainProp1 {

  public static void main(String[] args) {
    int a = Verifier.nondetInt();
    if (a != Integer.MAX_VALUE) return; // force a to MAX_VALUE to trigger overflow
    MainProp1 inst = new MainProp1();
    inst.test(a);
  }

  /*
   * Test ISUB with overflow: MAX_VALUE - (-1) overflows to MIN_VALUE.
   * Without overflow simulation, r = 2147483648 which is NOT MIN_VALUE.
   * Since a is forced to MAX_VALUE, r can only ever be MIN_VALUE.
   * Expected verdict: TRUE
   */
  public void test(int a) {
    int r = a - (-1);
    assert r == Integer.MIN_VALUE : "overflow not simulated: MAX_VALUE - (-1) should wrap to MIN_VALUE";
  }
}

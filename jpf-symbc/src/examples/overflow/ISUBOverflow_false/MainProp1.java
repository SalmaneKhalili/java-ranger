package overflow.ISUBOverflow_false;

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
   * Without overflow simulation, r = -2147483649 which is NOT MAX_VALUE.
   * Since a is forced to MIN_VALUE, r can only ever be MAX_VALUE.
   * Expected verdict: FALSE
   */
  public void test(int a) {
    int r = a - 1;
    assert r != Integer.MAX_VALUE : "overflow not simulated: MIN_VALUE - 1 should wrap to MAX_VALUE";
  }
}

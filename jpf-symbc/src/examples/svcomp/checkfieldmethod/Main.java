package svcomp.checkfieldmethod;

/**
 * crafted an example to test what JR does when a field ssa has to run twice in a for loop.
 */

import org.sosy_lab.sv_benchmarks.Verifier;


class Main {

  public static void main(String[] args) {
    new Main().start();
  }

  private void start() {
    int i = Verifier.nondetInt();
    Ref ref = new Ref(i);
    int xCopy = ref.x;
    if (i < 0) {
      ref.self.x += 2;
      ref.incrementX_byThree();
      xCopy = ref.x;
    }
    assert i < 0 ? xCopy == i + 5 : true;
  }

  class Ref {

    public Ref self;
    int x = 0;

    public Ref(int x) {
      this.x = x;
      this.self = this;
    }

    void incrementX_byThree() {
      x += 3;
    }
  }
}

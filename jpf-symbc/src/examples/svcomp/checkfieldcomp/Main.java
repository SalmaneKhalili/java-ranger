package svcomp.checkfieldcomp;

/**
 * crafted an example to expose problematic field SSA transformation for sequential composition.
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
      xCopy = ref.x;
    }
    assert i < 0 ? xCopy == i + 2 : true;
  }

  class Ref {

    public Ref self;
    int x = 0;

    public Ref(int x) {
      this.x = x;
      this.self = this;
    }
  }
}

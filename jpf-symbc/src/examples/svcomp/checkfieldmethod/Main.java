package svcomp.checkfieldmethod;

/**
 * crafted an example to test what JR does when a field ssa has to run twice in a for loop.
 */

import org.sosy_lab.sv_benchmarks.Verifier;


class Main {

  Ref helper = new Ref();

  public static void main(String[] args) {
    new Main().start();
  }

  private void start() {
    int i = Verifier.nondetInt();
    helper.self = helper;
    helper.x = i;
    if (i < 0) {
      Ref refSelf = helper.self;
      refSelf.x += 2;
      helper.incrementX();
    }
    assert i < 0 ? helper.self.x == i + 5 : false;
  }

  class Ref {

    public Ref self;
    int x = 0; //Verifier.nondetInt();

    void incrementX() {
      x += 3;
    }
  }
}

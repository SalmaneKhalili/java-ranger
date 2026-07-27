# Math.sin() via Taylor Series Peer — Implementation Notes

## Branch
`fp-taylor-sin` (based on `fpSupport`)

## Idea
Instead of CEGIS concretization in the solver, intercept `Math.sin(x)` at the JPF native peer layer and replace it with a polynomial expression tree. The polynomial uses only `+, -, *, /` on `RealExpression` nodes, which every solver handles natively — no solver-specific `sin()` override needed.

## Changes (2 files)

### 1. Peer: `jpf-symbc/src/peers/gov/nasa/jpf/symbc/JPF_java_lang_Math.java`

Modified `sin__D__D` (line 301). When the argument is symbolic, instead of:
```java
RealExpression result = new MathRealExpression(MathFunction.SIN, sym_arg);
```
we build a 5th-order Taylor polynomial:
```java
RealExpression x3 = sym_arg._mul(sym_arg)._mul(sym_arg);       // x^3
RealExpression x5 = x3._mul(sym_arg)._mul(sym_arg);            // x^5
RealExpression result = sym_arg
    ._minus(x3._div(6.0))       // x - x^3/3!
    ._plus(x5._div(120.0));     // + x^5/5!
```

Concrete arguments still delegate to `Math.sin(a)`.

### 2. Solver: `jpf-symbc/src/main/gov/nasa/jpf/symbc/numeric/solvers/ProblemZ3BitVector.java`

Added `RealExpr` handling to `mult(Object, Object)` (line 822). The existing method only handled `BitVecExpr`, `IntExpr`, and `FPExpr`. The Taylor polynomial generates `RealExpr * RealExpr` multiplications when `symbolic.fp=false`:
```java
} else if (exp1 instanceof RealExpr && exp2 instanceof RealExpr) {
    return ctx.mkMul((RealExpr) exp1, (RealExpr) exp2);
}
```

## Config change
All `configSin*.jpf` files switched from `symbolic.fp=true` to `symbolic.fp=false`. With FP encoding, the nested `fp.mul`/`fp.div` nodes crash Z3 ("pure virtual method called"). With rational encoding (`symbolic.fp=false`), the polynomial is encoded as Z3 rationals which handle non-linear arithmetic.

## How it works

### Expression flow

```
User code: double s = Math.sin(x);
  → JPF intercepts via MJI peer
  → Peer builds polynomial: x - x³/6 + x⁵/120
  → Returns as RealExpression (BinaryRealExpression tree)
  → JPF stores as symbolic attribute on local variable `s`
  → When assertion/constraint references `s`, PCParser traverses the tree
  → Each _mul → pb.mult(), each _div → pb.div(), etc.
  → Z3 receives ordinary arithmetic (no SIN node)
```

### Why no solver changes needed

The polynomial is pure arithmetic. PCParser never sees `MathFunction.SIN` — it only sees `BinaryRealExpression` nodes with `+`, `-`, `*`, `/` operators. Every solver backend handles these natively.

The only solver fix needed was the `mult(Object, Object)` gap for `RealExpr`.

## Results (14 TestSin* tests)

| Test | Expected | Result |
|------|----------|--------|
| ConstrainedTRUE | TRUE | PASS |
| GuardTRUE | TRUE | PASS |
| NegTRUE | TRUE | PASS |
| NonNegTRUE | TRUE | PASS |
| PosTRUE | TRUE | PASS |
| SmallTRUE | TRUE | PASS |
| NestedTRUE | TRUE | Hang/crash |
| ImposBranchTRUE | TRUE | FAIL (Taylor > 1) |
| ImposBranch2TRUE | TRUE | FAIL (Taylor < -1) |
| RangeTRUE | TRUE | FAIL (Taylor outside [-1,1]) |
| RangeBoundTRUE | TRUE | FAIL |
| FeasBranchFALSE | FALSE | FAIL |
| GuardFALSE | FALSE | FAIL |
| NonNegFALSE | FALSE | FAIL |

**6/14 correct.** All passes are tests with guards constraining x to small ranges where the polynomial converges. All failures are from Taylor diverging for |x| > π/2 — expected without range reduction.

## Comparison with CEGIS

| | CEGIS | Taylor Peer |
|---|---|---|
| Correct | 14/14 | 6/14 |
| Speed | Slow (iterative Z3) | Fast (expression build) |
| Sound | Yes | No (approximation) |
| Solver deps | Needs `sin()` override | Minimal (one `mult` fix) |
| Range | Any input | Small |x| only (without reduction) |

## What would improve it

1. **Range reduction**: Reduce x to [-π, π] before Taylor using symbolic `x - 2π * floor(x / 2π)`. Requires a symbolic `floor()` or `remainder()` implementation.
2. **Higher order**: 7th or 9th order for better accuracy in [-π, π].
3. **Hybrid**: Use Taylor for bounded x, fall back to CEGIS for unbounded.

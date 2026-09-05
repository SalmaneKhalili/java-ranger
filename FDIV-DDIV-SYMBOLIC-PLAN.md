# FDIV/DDIV Symbolic Division — Implementation Plan (fp-fdiv-inf-nan-support)

Status: IN PROGRESS
Branch: `fp-fdiv-inf-nan-support`
Baseline: commit `90b00c6` ("spec-faithful 6-arm IEEE 754 outcome CG for FDIV/DDIV").

## Goal
Faithful IEEE 754 symbolic division for `float`/`double`: six outcome arms
(NaN, +Inf, -Inf, +0.0, -0.0, normal A/B), each guarded by unary *operand* class
predicates, posted via a disjunctive `GammaVarExpr` (ITE) tree translated to a Green
OR/AND/EQ formula by `AstToGreenVisitor`, and attached to the path condition as a single
`GreenConstraint`.

## Design decisions (confirmed with user)
1. **Keep the six `PCChoiceGenerator` choices** — one execution path per outcome class.
2. **Per-arm GammaVarExpr (ITE) tree**: each arm builds a nested `GammaVarExpr` whose
   conditions are `FPClassExpr` leaves (or AND/OR/NOT trees of them), and whose then/else
   leaves are class constants (or A/B for the normal arm). The final else-leaf repeats the
   arm's own class constant — unreachable on the chosen PC but keeps the expression total.
3. **AstToGreenExprVisitor** translates the Gamma to a flat Green disjunction:
   `OR(AND(cond0, EQ(result, leaf0)), AND(NOT(cond0) ∧ cond1, EQ(result, leaf1)), ...)`.
   The condition is used directly in AND operations **without visiting** — so `FPClassExpr`
   leaves survive into the final Green tree.
4. **GreenPbTranslator** translates `FPClassExpr` via `postVisitFPClass` to Z3 FP predicates.
5. **NaN arm identity**: `FPClassExpr(result, IS_NAN)` — NOT `EQ(result, NaN)` because
   IEEE 754 defines `NaN ≠ NaN` and Z3's `fp.eq` follows that.
6. **`symbolic.inf=true`** required in FP test configurations so operand/result domains
   include ±Inf (arms 2/3).
7. **Operand attribute**: `SymbolicReal(resultName, ...)` with same name as Green result
   variable (same Z3 constant).

## Φ outcome table (verbatim from the specification; A = dividend, B = divisor)
- Φ1 NaN: `isNaN(A) ∨ isNaN(B) ∨ (isZero(A)∧isZero(B)) ∨ (isInf(A)∧isInf(B))`
- Φ2 ±Inf: zero divisor (non-zero, non-infinite dividend) OR infinite dividend; sign = match/differ
     2a `Sign(A)=Sign(B)` -> +Inf, 2b `Sign(A)!=Sign(B)` -> -Inf
- Φ3 ±0.0: infinite divisor with finite dividend; sign = match/differ
     3a `Sign(A)=Sign(B)` -> +0.0, 3b `Sign(A)!=Sign(B)` -> -0.0
- Φ4 normal: finite / finite (non-zero divisor) -> `A/B`

## Files / changes
- `numeric/FPClassExpr.java` (new): Green `Expression` leaf wrapping an operand `Expression`
  and a `Comparator`; `accept` dispatches (via `instanceof`) to
  `GreenPbTranslator.postVisitFPClass`.
- `numeric/GreenPbTranslator.java`: `postVisitFPClass` maps comparators to Z3
  `mkFPIsNaN/IsInfinite/IsZero/IsPositive/IsNegative` (+ `logical_not`). Also fixed
  `postVisit(RealVariable)` to skip the int-only `geq/leq` bound posting in FP mode.
- `bytecode/FDIV.java`: 6-arm GammaVarExpr tree with `buildNaN`, `buildPosInf`, `buildNegInf`,
  `buildPosZero`, `buildNegZero`, `buildNormal`; `Pred.green()` returns `FPClassExpr` for
  symbolic operands, `Operation.TRUE`/`null` for concrete; `operandGreen` builds
  `RealVariable` from `SymbolicReal` name/bounds.
- `bytecode/DDIV.java`: double analogue.
- Examples: `TestFDiv.jpf` / `TestDDiv.jpf` set `symbolic.inf=true` and `target = ...TestFDiv` /
  `...TestDDiv`.
- New simple test: `TestFDivSymSym.java` / `.jpf` — single sym × sym division.

## Step-by-step tasks
- [x] Create `FPClassExpr.java` Green leaf.
- [x] Add `GreenPbTranslator.postVisitFPClass`; fix FP `postVisit(RealVariable)` bounds.
- [x] Rewrite `FDIV.java` (GammaVarExpr + AstToGreenVisitor).
- [x] Rewrite `DDIV.java` (double analogue).
- [x] Compile + `./gradlew :jpf-symbc:buildJars`.
- [x] Enable `symbolic.inf=true` in test configs; fix `TestFDiv.jpf` target.
- [x] Create `TestFDivSymSym` simple sym×sym test.
- [x] Run `TestFDivSymSym` end-to-end; verify all six outcomes explored (no errors).
- [x] Update `~/Desktop/DOCUMENTATION.md` (Gamma/ITE approach documented).
- [ ] Commit on `fp-fdiv-inf-nan-support`.

## Verification results (from TestFDivSymSym run)
- All 6 arms explored (fdiv_0 through fdiv_5).
- Arms 2–6 produce correct output labels: +inf, -inf, zero, zero, normal (with overflow sub-classifications).
- Arm 1 (NaN) guard+identity IS satisfiable (`IS_NAN(fdiv_0) -> true`).
- Downstream FCMP limitation: `res != res` decomposed as `lt || gt` by jpf-symbc — pre-existing, not caused by this implementation.
- Z3 SMT confirms correct Gamma disjunction structure (OR of guarded equalities with FPClassExpr leaves).
- "no errors detected" — clean run.

## Known limitation
- The downstream FCMP handler in jpf-symbc decomposes `res != res` as `lt || gt`, which
  is false for NaN — the "nan" println is never reached. Pre-existing engine issue.

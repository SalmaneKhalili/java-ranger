# Math.sin() Implementation Plan

Branch: `fp-math-sin-support` (from `fpSupport`)
Goal: Support `Math.sin()` in symbolic execution with soundness via CEGIS concretization.

## Checklist

- [x] Write 14 test files (TestSin*.java) + 14 config files
- [x] Build and confirm baseline (all crash with `Math.sin not supported`)
- [x] **Implement `sin()` in `ProblemZ3BitVector`**
  - [x] Add `sin()` override: create fresh FP var constrained to [-1,1]
  - [x] Track pending sins in a list `(resultVar, argExpr, sortBits)`
  - [x] Add `concretizePendingSins()`: CEGIS with NaN/Inf handling + sample seeding
  - [x] Call concretize inside `solve()` after first SAT
  - [x] `evalArgAsDouble()`: FPNum → double via IEEE 754 bit reconstruction
  - [x] `fpNumToDouble()`: sign/exponent/significand → longBitsToDouble
- [x] **Build and run full 14-test suite** — 14/14 correct
- [x] **Fix CEGIS failures** — NaN handling, subnormal sampling, sample x-constraint
- [ ] **Push and open PR on `vaibhavbsharma/java-ranger`**

## Design

### Two-Phase Concretization (CEGIS)

**Phase 1** (during `PCParser.parse()`):
- `pb.sin(exp)` → create fresh FP variable `_sin_N`, constrain to [-1,1]
- Track `(_sin_N, exp, sortBits)` in `pendingSins`
- Return `_sin_N` — all subsequent constraints use this stand-in

**Phase 2** (inside `ProblemZ3BitVector.solve()` after first SAT):

**Step A — Z3-based CEGIS** (up to 15 attempts):
- Eval arg from model → concrete double via `evalArgAsDouble()`
- Skip NaN/Inf values (exclude via `mkFPIsNaN`/`mkFPIsInfinite` predicates)
- Compute `Math.sin(concreteArg)`, add `_sin_N == concreteSinValue`
- Re-solve; if SAT → success, if UNSAT → exclude arg value and retry

**Step B — Sample seeding** (23 hardcoded values):
- If Z3-based CEGIS exhausted, try sample x values: ±0.5, ±1.0, ..., ±π
- For each sample: constrain `argExpr == sample AND _sin_N == sin(sample)`
- This ensures x is fixed to the sample (avoids inconsistent models)
- If SAT → success, if all fail → path marked UNSAT

### Soundness
- CEGIS success: model gives concrete input where PC holds AND sin is correct
- CEGIS failure: path pruned (may over-prune in rare cases, acceptable for bug-finding)
- Abstract [-1,1] range is sound overapproximation; CEGIS refines it

### Key files
- `ProblemZ3BitVector.java:189` — `sin()` override
- `ProblemZ3BitVector.java:211-300` — `concretizePendingSins()` with CEGIS + samples
- `ProblemZ3BitVector.java:301` — `evalArgAsDouble()` + `fpNumToDouble()`
- `ProblemZ3BitVector.java:340` — `solve()` calls concretize after SAT
- `ProblemGeneral.java:130` — `sin()` throws RuntimeException (NOT modified)
- `PCParser.java:331` — `case SIN: return pb.sin(...)` (no change needed)

### CEGIS fixes applied
1. **NaN handling**: Z3 FP model can return NaN for unconstrained vars. `mkFPEq(x, NaN)` is always false in IEEE 754, so exclusion by value doesn't work. Use `mkFPIsNaN(x)` predicate instead.
2. **Subnormal trap**: Z3 tends to pick subnormal FP values where sin(x) ≈ 0. Individual value exclusion is ineffective (~2^50 subnormal doubles). Fixed with sample seeding phase.
3. **Sample x-constraint**: Sample phase must constrain `argExpr == sample` to prevent inconsistent models (e.g., x fixed to -0.5 but PC says x > 0).
4. **MAX_CEGIS_ATTEMPTS**: Increased from 5 to 15 to give Z3 more chances to find good values.

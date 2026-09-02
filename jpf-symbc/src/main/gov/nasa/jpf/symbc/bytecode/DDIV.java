/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * Symbolic Pathfinder (jpf-symbc) is licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf.symbc.bytecode;

import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * YN: fixed choice selection in symcrete support (Yannic Noller <nolleryc@gmail.com>)
 *
 * IEEE 754 division result = A / B.  When at least one operand is symbolic,
 * the JVM's concrete ddiv semantics (x/0 = +-Inf, 0/0 = NaN, x/+-Inf = +-0,
 * finite/finite = a rounded real, no ArithmeticException) are explored as six
 * outcome arms, each guarded by a conjunction of unary *operand* class
 * predicates (on A and B), so the path condition never carries an fp.div term
 * -- which the FCMP/veritesting harness cannot solve efficiently:
 *
 *   1  NaN       isNaN(A) || isNaN(B) || (isZero(A)&&isZero(B)) ||
 *                (isInf(A)&&isInf(B))
 *   2  +Inf      zero divisor with non-zero, non-infinite dividend, or
 *                infinite dividend -- matching operand signs
 *   3  -Inf      the same shapes -- differing operand signs
 *   4  +0.0      infinite divisor with finite dividend -- matching signs
 *   5  -0.0      the same shape -- differing signs
 *   6  normal    finite / finite (non-zero divisor), a real number
 *
 * Guards are posted on the operands only, and mirror the Phi spec verbatim
 * (each ±Inf / ±0 arm conjoins the operand sign relation that selects the
 * result class).  A concrete class representative is pushed on the stack so
 * downstream concrete execution (and symcrete replay) sees the correct IEEE
 * 754 sign.  Note: per the spec, Inf/+-0 (infinite dividend, zero divisor)
 * matches no arm below -- isInf(A) excludes it from the ±Inf zero-divisor
 * disjunct -- and so falls through to the normal (Phi4) arm, e.g. as +-Inf.
 */
public class DDIV extends gov.nasa.jpf.jvm.bytecode.DDIV {

    private interface DClass { boolean test(double v); }

    @Override
    public Instruction execute(ThreadInfo th) {
        StackFrame sf = th.getModifiableTopFrame();

        RealExpression sym_v1 = (RealExpression) sf.getOperandAttr(1);
        double v1 = sf.peekDouble();
        RealExpression sym_v2 = (RealExpression) sf.getOperandAttr(3);
        double v2 = sf.peekDouble(2);

        // Both operands concrete: plain IEEE 754 division.
        if (sym_v1 == null && sym_v2 == null) {
            return super.execute(th);
        }

        // Symbolic result A/B, class-constrained by whichever arm is taken.
        RealExpression resultExpr;
        if (sym_v2 != null)
            resultExpr = (sym_v1 != null) ? sym_v2._div(sym_v1) : sym_v2._div(v1);
        else
            resultExpr = sym_v1._div_reverse(v2);

        ChoiceGenerator<?> cg;

        if (!th.isFirstStepInsn()) { // first time around
            cg = new PCChoiceGenerator(SymbolicInstructionFactory.collect_constraints ? 1 : 6);
            ((PCChoiceGenerator) cg).setOffset(this.position);
            ((PCChoiceGenerator) cg).setMethodName(this.getMethodInfo().getFullName());
            th.getVM().getSystemState().setNextChoiceGenerator(cg);
            return this;
        } else { // this is what really returns results
            cg = th.getVM().getSystemState().getChoiceGenerator();
            assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;

            // A = dividend (sym_v2 / v2), B = divisor (sym_v1 / v1)
            Op A = new Op(sym_v2, v2);
            Op B = new Op(sym_v1, v1);

            int choice;
            if (SymbolicInstructionFactory.collect_constraints) {
                // Replay the arm matching the concrete (random) trace.
                choice = classify(v2, v1);
                ((PCChoiceGenerator) cg).select(choice - 1);
            } else {
                choice = ((Integer) cg.getNextChoice()) + 1;
            }

            PathCondition pc;
            ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

            if (prev_cg == null)
                pc = new PathCondition();
            else
                pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();

            assert pc != null;

            boolean reachable = apply(choice, pc, A, B);

            double resultValue = resultValue(choice, v2, v1);

            if (!reachable) { // concrete operand contradicts the arm
                th.getVM().getSystemState().setIgnored(true);
                return getNext(th);
            }

            if (pc.simplify()) { // arm satisfiable under the operand bounds
                ((PCChoiceGenerator) cg).setCurrentPC(pc);

                sf = th.getModifiableTopFrame();
                sf.popDouble();
                sf.popDouble();
                sf.pushDouble(resultValue);

                sf.setLongOperandAttr(resultExpr);
                return getNext(th);
            } else { // infeasible arm (e.g. +-0 unreachable when divisor is bounded)
                th.getVM().getSystemState().setIgnored(true);
                return getNext(th);
            }
        }
    }

    // Posts the guard of arm `choice` as unary *operand* predicates so the
    // PC never contains an fp.div term.  Returns false if a concrete operand
    // contradicts the arm (nothing is posted in that case).  The disjuncts
    // mirror the Phi spec verbatim: each shape is conjoined with the operand
    // sign relation (matching / differing) that determines the result class.
    private static boolean apply(int choice, PathCondition pc, Op A, Op B) {
        switch (choice) {
        case 1: // Phi1 NaN: isNaN(A) || isNaN(B) || (isZero(A)&&isZero(B)) || (isInf(A)&&isInf(B))
            return guard(pc, A.nan())
                || guard(pc, A.notNan(), B.nan())
                || guard(pc, A.zero(), B.zero())
                || guard(pc, A.inf(), B.inf());
        case 2: // Phi2a +Inf: (zero divisor, non-zero divd, non-inf divd) || (inf divd)
                //            -- matching signs
            return guard(pc, A.notNan(), B.notNan(), B.zero(), A.notZero(), A.notInf(), A.pos(), B.pos())
                || guard(pc, A.notNan(), B.notNan(), B.zero(), A.notZero(), A.notInf(), A.neg(), B.neg())
                || guard(pc, A.notNan(), B.notNan(), A.inf(), B.notInf(), B.notZero(), A.pos(), B.pos())
                || guard(pc, A.notNan(), B.notNan(), A.inf(), B.notInf(), B.notZero(), A.neg(), B.neg());
        case 3: // Phi2b -Inf: same shapes -- differing signs
            return guard(pc, A.notNan(), B.notNan(), B.zero(), A.notZero(), A.notInf(), A.pos(), B.neg())
                || guard(pc, A.notNan(), B.notNan(), B.zero(), A.notZero(), A.notInf(), A.neg(), B.pos())
                || guard(pc, A.notNan(), B.notNan(), A.inf(), B.notInf(), B.notZero(), A.pos(), B.neg())
                || guard(pc, A.notNan(), B.notNan(), A.inf(), B.notInf(), B.notZero(), A.neg(), B.pos());
        case 4: // Phi3a +0.0: isInf(B) && !isInf(A) -- matching signs
            return guard(pc, A.notNan(), B.notNan(), B.inf(), A.notInf(), A.pos(), B.pos())
                || guard(pc, A.notNan(), B.notNan(), B.inf(), A.notInf(), A.neg(), B.neg());
        case 5: // Phi3b -0.0: isInf(B) && !isInf(A) -- differing signs
            return guard(pc, A.notNan(), B.notNan(), B.inf(), A.notInf(), A.pos(), B.neg())
                || guard(pc, A.notNan(), B.notNan(), B.inf(), A.notInf(), A.neg(), B.pos());
        default: // Phi4 normal: finite / finite (non-zero divisor)
            return guard(pc, A.notNan(), B.notNan(), B.notZero(), A.notInf(), B.notInf());
        }
    }

    // Concrete IEEE 754 class of v2/v1, mapped to the arm index.
    private static int classify(double v2, double v1) {
        double r = v2 / v1;
        if (Double.isNaN(r))
            return 1;
        if (Double.isInfinite(r))
            return r > 0 ? 2 : 3;
        if (r == 0.0d)
            return Double.doubleToRawLongBits(r) >= 0 ? 4 : 5;
        return 6;
    }

    // The concrete class representative pushed on the stack for each arm.
    private static double resultValue(int choice, double v2, double v1) {
        switch (choice) {
        case 1:
            return Double.NaN;
        case 2:
            return Double.POSITIVE_INFINITY;
        case 3:
            return Double.NEGATIVE_INFINITY;
        case 4:
            return 0.0d;
        case 5:
            return -0.0d;
        default:
            return v2 / v1;
        }
    }

    private static boolean isPos(double v) { return Double.doubleToRawLongBits(v) >= 0L && !Double.isNaN(v); }
    private static boolean isNeg(double v) { return Double.doubleToRawLongBits(v) < 0L; }

    // One operand's concrete value and, optionally, its symbolic expression.
    private static final class Op {
        final RealExpression sym; final double v;
        Op(RealExpression s, double cv) { sym = s; v = cv; }
        Pred nan()      { return new Pred(sym, v, Double::isNaN,      Comparator.IS_NAN,      true); }
        Pred notNan()   { return new Pred(sym, v, Double::isNaN,      Comparator.IS_NAN,      false); }
        Pred inf()      { return new Pred(sym, v, Double::isInfinite, Comparator.IS_INF,      true); }
        Pred notInf()   { return new Pred(sym, v, Double::isInfinite, Comparator.IS_INF,      false); }
        Pred zero()     { return new Pred(sym, v, z -> z == 0.0d,     Comparator.IS_ZERO,     true); }
        Pred notZero()  { return new Pred(sym, v, z -> z == 0.0d,     Comparator.IS_ZERO,     false); }
        Pred pos()      { return new Pred(sym, v, DDIV::isPos,        Comparator.IS_POSITIVE, true); }
        Pred neg()      { return new Pred(sym, v, DDIV::isNeg,        Comparator.IS_NEGATIVE, true); }
    }

    // A single unary IEEE 754 class predicate.  With a symbolic operand
    // (sym != null) it is posted to the path condition; with a concrete
    // operand it is evaluated against the operand's value at build time.
    private static final class Pred {
        final RealExpression sym; final double v;
        final DClass test; final Comparator cmp; final boolean take;
        Pred(RealExpression s, double cv, DClass t, Comparator c, boolean take) {
            sym = s; v = cv; test = t; cmp = c; this.take = take;
        }
        boolean holds() { return sym != null || test.test(v) == take; }
        void post(PathCondition pc) { if (sym != null) pc._addDet(sym, take ? cmp : cmp.not()); }
    }

    // Conjunction of predicates: returns false if any concrete predicate is
    // falsified, otherwise posts the symbolic predicates to the PC.  Two
    // passes so a falsified concrete predicate never leaves partial posts.
    // Used for a single arm's guard AND for the per-arm disjunction above
    // (short-circuit returns on the first sub-guard that survives), so the
    // OR of shapes is realized at the concrete/symbolic build level without
    // any LogicalORRealConstraints machinery.
    private static boolean guard(PathCondition pc, Pred... ps) {
        for (Pred p : ps) if (!p.holds()) return false;
        for (Pred p : ps) p.post(pc);
        return true;
    }
}
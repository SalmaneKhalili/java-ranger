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
import gov.nasa.jpf.symbc.numeric.FPClassExpr;
import gov.nasa.jpf.symbc.numeric.GreenConstraint;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicReal;
import gov.nasa.jpf.symbc.veritesting.VeritestingUtil.ExprUtil;
import gov.nasa.jpf.symbc.veritesting.ast.def.AssignmentStmt;
import gov.nasa.jpf.symbc.veritesting.ast.def.GammaVarExpr;
import gov.nasa.jpf.symbc.veritesting.ast.transformations.AstToGreen.AstToGreenVisitor;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.RealConstant;
import za.ac.sun.cs.green.expr.RealVariable;

/**
 * YN: fixed choice selection in symcrete support (Yannic Noller <nolleryc@gmail.com>)
 *
 * IEEE 754 division result = A / B.  When at least one operand is symbolic,
 * the JVM's concrete fdiv semantics (x/0 = +-Inf, 0/0 = NaN, x/+-Inf = +-0,
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
 * Each arm is represented on its JVM path by a GammaVarExpr (ITE) tree whose
 * then/else leaves are the IEEE 754 class constants (RealExpression.NAN,
 * POS_INF, ...) and whose conditions are the unary operand predicates above
 * (FPClassExpr).  The tree is bound to a fresh result variable via an
 * AssignmentStmt and posted to the path condition as one GreenConstraint.
 * Guards mirror the Phi spec verbatim (each +-Inf / +-0 arm conjoins the
 * operand sign relation that selects the result class).  A concrete class
 * representative is pushed on the stack so downstream concrete execution (and
 * symcrete replay) sees the correct IEEE 754 sign.  Note: per the spec,
 * Inf/+-0 (infinite dividend, zero divisor) matches no arm below -- isInf(A)
 * excludes it from the +-Inf zero-divisor disjunct -- and so falls through to
 * the normal (Phi4) arm, e.g. as +-Inf.  The only place fp.div appears is the
 * normal arm (choice 6).  The NaN arm (choice 1) is the exception: IEEE 754
 * defines NaN != NaN, so Z3's fp.eq makes result == NaN unsatisfiable -- its
 * identity is instead the isNaN(result) predicate (FPClassExpr).
 */
public class FDIV extends gov.nasa.jpf.jvm.bytecode.FDIV {

    private static int resultCounter = 0;

    private static final double SYM_MIN = -Double.MAX_VALUE;
    private static final double SYM_MAX = Double.MAX_VALUE;

    @Override
    public Instruction execute(ThreadInfo th) {
        StackFrame sf = th.getModifiableTopFrame();

        RealExpression sym_v2 = (RealExpression) sf.getOperandAttr(1); // dividend
        float v2 = sf.peekFloat(1);
        RealExpression sym_v1 = (RealExpression) sf.getOperandAttr(0); // divisor
        float v1 = sf.peekFloat(0);

        // Both operands concrete: plain IEEE 754 fdiv.
        if (sym_v1 == null && sym_v2 == null)
            return super.execute(th);

        ChoiceGenerator<?> cg;

        if (!th.isFirstStepInsn()) { // first time around
            cg = new PCChoiceGenerator(SymbolicInstructionFactory.collect_constraints ? 1 : 6);
            ((PCChoiceGenerator) cg).setOffset(this.position);
            ((PCChoiceGenerator) cg).setMethodName(this.getMethodInfo().getFullName());
            th.getVM().getSystemState().setNextChoiceGenerator(cg);
            return this;
        }

        cg = th.getVM().getSystemState().getChoiceGenerator();
        assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;

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

        // Operands as green expressions (RealVariable for a symbolic operand,
        // RealConstant for a concrete sibling).
        Expression gA = toGreen(sym_v2, v2);
        Expression gB = toGreen(sym_v1, v1);

        // Fresh result variable, bound to the chosen arm's ITE tree.  IEEE 754
        // defines NaN != NaN, so the NaN arm (choice 1) cannot bind its result
        // by equality to the NaN constant -- Z3's fp.eq would make that
        // identity unsatisfiable.  It is bound instead by the isNaN(result)
        // predicate.
        String varId = "fdiv_" + resultCounter++;
        Expression resultGreen = ExprUtil.createGreenVar("float", varId);
        Expression identity = (choice == 1)
                ? new FPClassExpr(resultGreen, Comparator.IS_NAN)
                : new AssignmentStmt(resultGreen, buildGammaForChoice(choice, gA, gB))
                        .accept(new AstToGreenVisitor());
        pc._addDet(new GreenConstraint(identity));

        if (pc.simplify()) { // arm satisfiable under the operand bounds
            ((PCChoiceGenerator) cg).setCurrentPC(pc);

            sf = th.getModifiableTopFrame();
            sf.popFloat();
            sf.popFloat();
            sf.pushFloat(resultValue(choice, v2, v1));
            sf.setOperandAttr(new SymbolicReal(varId, SYM_MIN, SYM_MAX));
            return getNext(th);
        } else { // infeasible arm
            th.getVM().getSystemState().setIgnored(true);
            return getNext(th);
        }
    }

    // ---------------- ITE builders ----------------

    // Choice 1 (NaN): isNaN(A) || (!isNaN(A) && isNaN(B)) ||
    //                 (isZero(A) && isZero(B)) || (isInf(A) && isInf(B))
    private static Expression buildNaN(Expression gA, Expression gB) {
        Expression nan = constOf(RealExpression.NAN);
        return new GammaVarExpr(isNan(gA), nan,
               new GammaVarExpr(and(notNan(gA), isNan(gB)), nan,
               new GammaVarExpr(and(isZero(gA), isZero(gB)), nan,
               new GammaVarExpr(and(isInf(gA), isInf(gB)), nan, nan))));
    }

    // Choice 2 (+Inf): zero divisor with non-zero dividend, or infinite
    // dividend with finite divisor -- matching signs.
    private static Expression buildPosInf(Expression gA, Expression gB) {
        Expression posInf = constOf(RealExpression.POS_INF);
        Expression d1 = and(notNan(gA), notNan(gB), isZero(gB), notZero(gA), notInf(gA), isPos(gA), isPos(gB));
        Expression d2 = and(notNan(gA), notNan(gB), isZero(gB), notZero(gA), notInf(gA), isNeg(gA), isNeg(gB));
        Expression d3 = and(notNan(gA), notNan(gB), isInf(gA), notInf(gB), notZero(gB), isPos(gA), isPos(gB));
        Expression d4 = and(notNan(gA), notNan(gB), isInf(gA), notInf(gB), notZero(gB), isNeg(gA), isNeg(gB));
        return new GammaVarExpr(d1, posInf,
               new GammaVarExpr(d2, posInf,
               new GammaVarExpr(d3, posInf,
               new GammaVarExpr(d4, posInf, posInf))));
    }

    // Choice 3 (-Inf): the same shapes -- differing signs.
    private static Expression buildNegInf(Expression gA, Expression gB) {
        Expression negInf = constOf(RealExpression.NEG_INF);
        Expression d1 = and(notNan(gA), notNan(gB), isZero(gB), notZero(gA), notInf(gA), isPos(gA), isNeg(gB));
        Expression d2 = and(notNan(gA), notNan(gB), isZero(gB), notZero(gA), notInf(gA), isNeg(gA), isPos(gB));
        Expression d3 = and(notNan(gA), notNan(gB), isInf(gA), notInf(gB), notZero(gB), isPos(gA), isNeg(gB));
        Expression d4 = and(notNan(gA), notNan(gB), isInf(gA), notInf(gB), notZero(gB), isNeg(gA), isPos(gB));
        return new GammaVarExpr(d1, negInf,
               new GammaVarExpr(d2, negInf,
               new GammaVarExpr(d3, negInf,
               new GammaVarExpr(d4, negInf, negInf))));
    }

    // Choice 4 (+0.0): infinite divisor, finite dividend -- matching signs.
    private static Expression buildPosZero(Expression gA, Expression gB) {
        Expression posZero = constOf(RealExpression.POS_ZERO);
        Expression c1 = and(notNan(gA), notNan(gB), isInf(gB), notInf(gA), isPos(gA), isPos(gB));
        Expression c2 = and(notNan(gA), notNan(gB), isInf(gB), notInf(gA), isNeg(gA), isNeg(gB));
        return new GammaVarExpr(c1, posZero, new GammaVarExpr(c2, posZero, posZero));
    }

    // Choice 5 (-0.0): the same shape -- differing signs.
    private static Expression buildNegZero(Expression gA, Expression gB) {
        Expression negZero = constOf(RealExpression.NEG_ZERO);
        Expression c1 = and(notNan(gA), notNan(gB), isInf(gB), notInf(gA), isPos(gA), isNeg(gB));
        Expression c2 = and(notNan(gA), notNan(gB), isInf(gB), notInf(gA), isNeg(gA), isPos(gB));
        return new GammaVarExpr(c1, negZero, new GammaVarExpr(c2, negZero, negZero));
    }

    // Choice 6 (normal): finite / finite(non-zero) -- the ONLY fp.div term.
    private static Expression buildNormal(Expression gA, Expression gB) {
        Expression div = new Operation(Operation.Operator.DIV, gA, gB);
        Expression guard = and(notNan(gA), notNan(gB), notZero(gB), notInf(gA), notInf(gB));
        return new GammaVarExpr(guard, div, div);
    }

    private static Expression buildGammaForChoice(int choice, Expression gA, Expression gB) {
        switch (choice) {
        case 1: return buildNaN(gA, gB);
        case 2: return buildPosInf(gA, gB);
        case 3: return buildNegInf(gA, gB);
        case 4: return buildPosZero(gA, gB);
        case 5: return buildNegZero(gA, gB);
        default: return buildNormal(gA, gB);
        }
    }

    // ---------------- expression helpers ----------------

    private static Expression toGreen(RealExpression sym, float v) {
        if (sym == null)
            return new RealConstant(v);
        if (sym instanceof SymbolicReal)
            return new RealVariable(((SymbolicReal) sym).getName(),
                    ((SymbolicReal) sym)._min, ((SymbolicReal) sym)._max);
        return ExprUtil.SPFToGreenExpr(sym);
    }

    // IEEE 754 class constant statics as green expressions.
    private static Expression constOf(RealExpression r) {
        return ExprUtil.SPFToGreenExpr(r);
    }

    // Unary FP class predicates -- FPClassExpr is a leaf that GreenPbTranslator
    // maps to Z3's mkFPIsNaN/mkFPIsInfinite/... (see postVisitFPClass).
    private static Expression isNan(Expression e)   { return new FPClassExpr(e, Comparator.IS_NAN); }
    private static Expression notNan(Expression e)  { return new FPClassExpr(e, Comparator.NOT_IS_NAN); }
    private static Expression isZero(Expression e)  { return new FPClassExpr(e, Comparator.IS_ZERO); }
    private static Expression notZero(Expression e) { return new FPClassExpr(e, Comparator.NOT_IS_ZERO); }
    private static Expression isInf(Expression e)   { return new FPClassExpr(e, Comparator.IS_INF); }
    private static Expression notInf(Expression e)  { return new FPClassExpr(e, Comparator.NOT_IS_INF); }
    private static Expression isPos(Expression e)   { return new FPClassExpr(e, Comparator.IS_POSITIVE); }
    private static Expression isNeg(Expression e)   { return new FPClassExpr(e, Comparator.IS_NEGATIVE); }

    private static Expression and(Expression... es) {
        Expression r = es[0];
        for (int i = 1; i < es.length; i++)
            r = new Operation(Operation.Operator.AND, r, es[i]);
        return r;
    }

    // ---------------- concrete classification / replay ----------------

    private static int classify(float v2, float v1) {
        float r = v2 / v1;
        if (Float.isNaN(r))
            return 1;
        if (Float.isInfinite(r))
            return r > 0 ? 2 : 3;
        if (r == 0.0f)
            return Float.floatToRawIntBits(r) >= 0 ? 4 : 5;
        return 6;
    }

    private static float resultValue(int choice, float v2, float v1) {
        switch (choice) {
        case 1: return Float.NaN;
        case 2: return Float.POSITIVE_INFINITY;
        case 3: return Float.NEGATIVE_INFINITY;
        case 4: return 0.0f;
        case 5: return -0.0f;
        default: return v2 / v1;
        }
    }
}
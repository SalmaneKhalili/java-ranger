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
import gov.nasa.jpf.vm.Types;

/**
 * YN: fixed choice selection in symcrete support (Yannic Noller <nolleryc@gmail.com>)
 */
public class FDIV extends gov.nasa.jpf.jvm.bytecode.FDIV {

    @Override
    public Instruction execute(ThreadInfo th) {

        StackFrame sf = th.getModifiableTopFrame();

        RealExpression sym_v1 = (RealExpression) sf.getOperandAttr(0);
        float v1 = sf.peekFloat(0);
        RealExpression sym_v2 = (RealExpression) sf.getOperandAttr(1);
        float v2 = sf.peekFloat(1);

        // Concrete divisor: IEEE 754 handles 0, NaN and Inf natively in
        // Java (x/0 = +-Inf, 0/0 = NaN), so defer to the concrete
        // implementation.  If the dividend is symbolic, attach the
        // symbolic result expression.
        if (sym_v1 == null) {
            Instruction next_insn = super.execute(th);
            if (sym_v2 != null) // result is symbolic expression
                sf.setOperandAttr(sym_v2._div(v1));
            return next_insn;
        }

        // Symbolic divisor: its IEEE 754 class determines the result.
        // Explore the four mutually-exclusive cases as a choice.
        ChoiceGenerator<?> cg;
        int choice;

        if (!th.isFirstStepInsn()) { // first time around
            cg = new PCChoiceGenerator(SymbolicInstructionFactory.collect_constraints ? 1 : 4);
            ((PCChoiceGenerator) cg).setOffset(this.position);
            ((PCChoiceGenerator) cg).setMethodName(this.getMethodInfo().getFullName());
            th.getVM().getSystemState().setNextChoiceGenerator(cg);
            return this;
        } else { // this is what really returns results
            cg = th.getVM().getSystemState().getChoiceGenerator();
            assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;
            if (SymbolicInstructionFactory.collect_constraints) {
                if (v1 == 0)
                    choice = 0;
                else if (Float.isNaN(v1))
                    choice = 1;
                else if (Float.isInfinite(v1))
                    choice = 2;
                else
                    choice = 3;
                ((PCChoiceGenerator) cg).select(choice);
            } else {
                choice = (Integer) cg.getNextChoice();
            }
        }

        PathCondition pc;
        ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

        if (prev_cg == null)
            pc = new PathCondition();
        else
            pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();

        assert pc != null;

        // ------------------------------------------------------------
        // 4-branch choice generator for FDIV (IEEE 754 semantics).
        //
        // Java's floating-point division (float) does NOT throw
        // ArithmeticException on division by zero — it yields +-Inf or
        // NaN per IEEE 754.  The symbolic path condition must therefore
        // explore all four mutually-exclusive cases for the divisor:
        //
        //   choice 0:  divisor == 0       → EQ sym_v1 0
        //   choice 1:  divisor is NaN     → IS_NAN sym_v1
        //   choice 2:  divisor is Inf     → IS_INF sym_v1
        //   choice 3:  normal divisor     → NE sym_v1 0  ∧  NOT_IS_NAN sym_v1
        //                                    ∧  NOT_IS_INF sym_v1
        //
        // The old code short-circuited div-by-zero with a concrete
        // exception, which was incorrect for IEEE 754 and prevented
        // the symbolic engine from exploring paths that produce
        // Infinity/NaN results.
        //
        // The result value is part of the choice: each branch computes
        // the concrete result consistent with that branch's semantics,
        // considering the dividend's IEEE 754 class as well.
        // ------------------------------------------------------------
        float resultValue;
        if (choice == 0) { // zero divisor
            pc._addDet(Comparator.EQ, sym_v1, 0);
            // dividend 0 -> NaN, otherwise +-Inf (sign = sign(v2) ^ sign(v1))
            if (v2 == 0)
                resultValue = Float.NaN;
            else
                resultValue = (Math.copySign(1.0f, v1) * Math.copySign(1.0f, v2) < 0)
                        ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
        } else if (choice == 1) { // NaN divisor
            pc._addDet(sym_v1, Comparator.IS_NAN);
            resultValue = Float.NaN; // x/NaN = NaN
        } else if (choice == 2) { // Inf divisor
            pc._addDet(sym_v1, Comparator.IS_INF);
            // Inf/Inf = NaN, otherwise +-0 (sign = sign(v2) ^ sign(v1))
            if (Float.isInfinite(v2))
                resultValue = Float.NaN;
            else
                resultValue = (Math.copySign(1.0f, v1) * Math.copySign(1.0f, v2) < 0)
                        ? -0.0f : +0.0f;
        } else { // normal divisor (non-zero, non-NaN, non-Inf)
            pc._addDet(Comparator.NE, sym_v1, 0);
            pc._addDet(sym_v1, Comparator.NOT_IS_NAN);
            pc._addDet(sym_v1, Comparator.NOT_IS_INF);
            // Concrete division handles a special dividend natively
            // (NaN/x = NaN, Inf/x = +-Inf, 0/x = +-0).
            resultValue = v2 / v1;
        }

        if (pc.simplify()) { // satisfiable
            ((PCChoiceGenerator) cg).setCurrentPC(pc);

            sf = th.getModifiableTopFrame();
            sf.popFloat();
            sf.popFloat();
            sf.pushFloat(resultValue);

            // set the symbolic result
            RealExpression result;
            if (sym_v2 != null)
                result = sym_v2._div(sym_v1);
            else
                result = sym_v1._div_reverse(v2);

            sf.setOperandAttr(result);
            return getNext(th);

        } else {
            th.getVM().getSystemState().setIgnored(true);
            return getNext(th);
        }

    }

}

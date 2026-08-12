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
 * Symbolic double remainder (IEEE 754).
 * ..., value1, value2 => ..., result
 */
public class DREM extends gov.nasa.jpf.jvm.bytecode.DREM {

    @Override
    public Instruction execute(ThreadInfo th) {

        StackFrame sf = th.getModifiableTopFrame();

        RealExpression sym_v1 = (RealExpression) sf.getOperandAttr(1);
        double v1 = sf.peekDouble();
        RealExpression sym_v2 = (RealExpression) sf.getOperandAttr(3);
        double v2 = sf.peekDouble(2);

        if (sym_v1 == null) {
            Instruction next_insn = super.execute(th);
            if (sym_v2 != null)
                sf.setLongOperandAttr(sym_v2._rem(v1));
            return next_insn;
        }

        ChoiceGenerator<?> cg;
        int choice;

        if (!th.isFirstStepInsn()) {
            cg = new PCChoiceGenerator(SymbolicInstructionFactory.collect_constraints ? 1 : 4);
            ((PCChoiceGenerator) cg).setOffset(this.position);
            ((PCChoiceGenerator) cg).setMethodName(this.getMethodInfo().getFullName());
            th.getVM().getSystemState().setNextChoiceGenerator(cg);
            return this;
        } else {
            cg = th.getVM().getSystemState().getChoiceGenerator();
            assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;
            if (SymbolicInstructionFactory.collect_constraints) {
                if (v1 == 0)
                    choice = 0;
                else if (Double.isNaN(v1))
                    choice = 1;
                else if (Double.isInfinite(v1))
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

        // The result value is part of the choice: each branch pushes the
        // concrete result consistent with that branch's IEEE 754
        // semantics, considering the dividend's IEEE 754 class as well.
        double resultValue;
        if (choice == 0) { // zero divisor
            pc._addDet(Comparator.EQ, sym_v1, 0);
            resultValue = Double.NaN; // x % 0 = NaN
        } else if (choice == 1) { // NaN divisor
            pc._addDet(sym_v1, Comparator.IS_NAN);
            resultValue = Double.NaN; // x % NaN = NaN
        } else if (choice == 2) { // Inf divisor
            pc._addDet(sym_v1, Comparator.IS_INF);
            // Inf % Inf = NaN, NaN % Inf = NaN, otherwise x % Inf = x
            if (Double.isInfinite(v2) || Double.isNaN(v2))
                resultValue = Double.NaN;
            else
                resultValue = v2;
        } else { // normal divisor (non-zero, non-NaN, non-Inf)
            pc._addDet(Comparator.NE, sym_v1, 0);
            pc._addDet(sym_v1, Comparator.NOT_IS_NAN);
            pc._addDet(sym_v1, Comparator.NOT_IS_INF);
            // Concrete remainder handles a special dividend natively
            // (NaN % x = NaN, Inf % x = NaN, 0 % x = +-0).
            resultValue = v2 % v1;
        }

        if (pc.simplify()) {
            ((PCChoiceGenerator) cg).setCurrentPC(pc);

            sf = th.getModifiableTopFrame();
            sf.popDouble();
            sf.popDouble();
            sf.pushDouble(resultValue);

            RealExpression result;
            if (sym_v2 != null)
                result = sym_v2._rem(sym_v1);
            else
                result = sym_v1._rem_reverse(v2);

            sf = th.getModifiableTopFrame();
            sf.setLongOperandAttr(result);
            return getNext(th);

        } else {
            th.getVM().getSystemState().setIgnored(true);
            return getNext(th);
        }
    }

}

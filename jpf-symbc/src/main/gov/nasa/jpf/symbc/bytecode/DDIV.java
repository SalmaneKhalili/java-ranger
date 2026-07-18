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
import gov.nasa.jpf.vm.Types;

/**
 * Divide 2 doubles ..., value1, value2 => ..., value2/value1
 * 
 * YN: fixed choice selection in symcrete support (Yannic Noller <nolleryc@gmail.com>)
 */
public class DDIV extends gov.nasa.jpf.jvm.bytecode.DDIV {

    @Override
    public Instruction execute(ThreadInfo th) {

        StackFrame sf = th.getModifiableTopFrame();

        RealExpression sym_v1 = (RealExpression) sf.getOperandAttr(1);
        double v1 = sf.peekDouble();
        RealExpression sym_v2 = (RealExpression) sf.getOperandAttr(3);
        double v2 = sf.peekDouble(2);
        if (sym_v1 == null) {
            Instruction next_insn = super.execute(th);

            if (sym_v2 != null) // result is symbolic expression
                sf.setLongOperandAttr(sym_v2._div(v1));
            return next_insn;
        }

        // div by zero / NaN / Inf check affects path condition
        // sym_v1 is non-null and should be checked against zero, NaN, and Inf

        ChoiceGenerator<?> cg;
        int choice;

        if (!th.isFirstStepInsn()) { // first time around
            cg = new PCChoiceGenerator(SymbolicInstructionFactory.collect_constraints ? 1 : 4);
            ((PCChoiceGenerator) cg).setOffset(this.position);
            ((PCChoiceGenerator) cg).setMethodName(this.getMethodInfo().getFullName());
            th.getVM().setNextChoiceGenerator(cg);
            return this;
        } else { // this is what really returns results
            cg = th.getVM().getChoiceGenerator();
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

        //super.execute(th); // pops v1, v2 and pushes r = v2 / v1;
        sf.popDouble();
        sf.popDouble();
        sf.pushDouble(v2/v1);

        PathCondition pc;
        ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

        if (prev_cg == null)
            pc = new PathCondition();
        else
            pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();

        assert pc != null;

        // ------------------------------------------------------------
        // 4-branch choice generator for DDIV (IEEE 754 semantics).
        //
        // Java's floating-point division (double) does NOT throw
        // ArithmeticException on division by zero — it yields +-Inf or
        // NaN per IEEE 754.  The symbolic path condition must therefore
        // explore all four mutually-exclusive cases for the divisor:
        //
        //   choice 0:  divisor == 0       -> EQ sym_v1 0
        //   choice 1:  divisor is NaN     -> IS_NAN sym_v1
        //   choice 2:  divisor is Inf     -> IS_INF sym_v1
        //   choice 3:  normal divisor     -> NE sym_v1 0  ∧  NOT_IS_NAN sym_v1
        //                                    ∧  NOT_IS_INF sym_v1
        //
        // The old code short-circuited div-by-zero by pushing 0.0,
        // which was incorrect for IEEE 754 and prevented the symbolic
        // engine from exploring paths that produce Infinity/NaN results.
        // ------------------------------------------------------------
        if (choice == 0) { // zero divisor
            pc._addDet(Comparator.EQ, sym_v1, 0);
        } else if (choice == 1) { // NaN divisor
            pc._addDet(sym_v1, Comparator.IS_NAN);
        } else if (choice == 2) { // Inf divisor
            pc._addDet(sym_v1, Comparator.IS_INF);
        } else { // normal divisor (non-zero, non-NaN, non-Inf)
            pc._addDet(Comparator.NE, sym_v1, 0);
            pc._addDet(sym_v1, Comparator.NOT_IS_NAN);
            pc._addDet(sym_v1, Comparator.NOT_IS_INF);
        }

        if (pc.simplify()) { // satisfiable
            ((PCChoiceGenerator) cg).setCurrentPC(pc);

            // set the result
            RealExpression result;
            if (sym_v2 != null)
                result = sym_v2._div(sym_v1);
            else
                result = sym_v1._div_reverse(v2);

            sf = th.getModifiableTopFrame();
            sf.setLongOperandAttr(result);
            return getNext(th);

        } else {
            th.getVM().getSystemState().setIgnored(true);
            return getNext(th);
        }

    }

}

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

package gov.nasa.jpf.symbc;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.solvers.FpSortUtil;

/**
 * Warns the user at the end of the run when a reduced-mantissa FP sort has
 * actually been used (via {@code symbolic.mantissa}). A reduced significand
 * deviates from IEEE 754, so the program is not executed with the same FP
 * semantics as a normal Java run and the results may be incorrect.
 */
public class MantissaWarningListener extends ListenerAdapter {

    public MantissaWarningListener() {
    }

    public MantissaWarningListener(Config conf, JPF jpf) {
    }

    @Override
    public void searchStarted(Search search) {
        FpSortUtil.reset();
    }

    @Override
    public void searchFinished(Search search) {
        if (!FpSortUtil.isReducedSortUsed()) {
            return;
        }
        String outcome = search.hasErrors() ? "reported errors" : "found no errors";
        String msg = "*** WARNING: this run used a reduced-mantissa FP sort "
                + "(symbolic.mantissa=" + SymbolicInstructionFactory.fpMantissa
                + " instead of the IEEE 754 significand: 24 for float, 53 for double). "
                + "The run " + outcome
                + ", but results may be incorrect / not IEEE-754 exact. ***";
        JPF.getLogger("gov.nasa.jpf").warning(msg);
        System.err.println(msg);
    }
}

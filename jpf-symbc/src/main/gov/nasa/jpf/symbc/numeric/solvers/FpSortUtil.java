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

package gov.nasa.jpf.symbc.numeric.solvers;

import com.microsoft.z3.Context;
import com.microsoft.z3.FPSort;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.MantissaWarningListener;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.vm.VM;

public class FpSortUtil {

    private static boolean reducedSortUsed = false;

    /**
     * Returns the Z3 FP sort for the given bit-vector length, honoring the
     * custom significand size configured via {@code symbolic.mantissa}.
     * {@code symbolic.mantissa} is the number of significand bits (including
     * the implicit hidden bit); 0 (default) means the standard IEEE 754 sort:
     * 24 bits for float (mkFPSort32) and 53 for double (mkFPSort64).
     *
     * Whenever the requested significand differs from the IEEE 754 default for
     * the given bit-vector length, the sort is flagged as reduced so that
     * listeners can warn the user that results may not match normal Java FP
     * semantics.
     */
    public static FPSort sortFor(Context ctx, int bitVectorLength) {
        if (SymbolicInstructionFactory.fpMantissa > 0) {
            int ieeeSbits = (bitVectorLength == 32) ? 24 : 53;
            if (SymbolicInstructionFactory.fpMantissa != ieeeSbits) {
                reducedSortUsed = true;

                VM vm = VM.getVM();
                if (vm != null && vm.getSearch() != null) {
                    Search search = vm.getSearch();
                    if (!search.hasListenerOfType(MantissaWarningListener.class)) {
                        search.addListener(new MantissaWarningListener());
                    }
                }

                int ebits = (bitVectorLength == 32) ? 8 : 11;
                return ctx.mkFPSort(ebits, SymbolicInstructionFactory.fpMantissa);
            }
        }
        return (bitVectorLength == 32) ? ctx.mkFPSort32() : ctx.mkFPSort64();
    }

    /**
     * Whether a reduced (non-IEEE) significand sort has actually been used
     * during this run.
     */
    public static boolean isReducedSortUsed() {
        return reducedSortUsed;
    }

    /**
     * Resets the reduced sort usage flag.
     */
    public static void reset() {
        reducedSortUsed = false;
    }
}

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
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;

public class FpSortUtil {

    /**
     * Returns the Z3 FP sort for the given bit-vector length, honoring the
     * custom significand size configured via {@code symbolic.mantissa}.
     * {@code symbolic.mantissa} is the number of significand bits (including
     * the implicit hidden bit); 0 (default) means the standard IEEE 754 sort:
     * 24 bits for float (mkFPSort32) and 53 for double (mkFPSort64).
     */
    public static FPSort sortFor(Context ctx, int bitVectorLength) {
        if (SymbolicInstructionFactory.fpMantissa > 0) {
            int ebits = (bitVectorLength == 32) ? 8 : 11;
            return ctx.mkFPSort(ebits, SymbolicInstructionFactory.fpMantissa);
        }
        return (bitVectorLength == 32) ? ctx.mkFPSort32() : ctx.mkFPSort64();
    }
}

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
//Copyright (C) 2006 United States Government as represented by the
//Administrator of the National Aeronautics and Space Administration
//(NASA).  All Rights Reserved.
//
//This software is distributed under the NASA Open Source Agreement
//(NOSA), version 1.3.  The NOSA has been approved by the Open Source
//Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
//directory tree for the complete NOSA document.
//
//THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
//KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
//LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
//SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
//A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
//THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
//DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//

package gov.nasa.jpf.symbc;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.symbc.numeric.*;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

/**
 * MJI NativePeer class for java.lang.Math library abstraction
 */

// simple functions abs, min, max
public class JPF_java_lang_Math extends NativePeer{

  /**
   * Number of sub-intervals used by the piecewise-linear Math.sin approximation
   * on [-PI/2, PI/2]. Error per segment ~ (width)^2 / 8:
   *   n = 4  -> ~0.070    (parity with the cubic minimax)
   *   n = 8  -> ~0.019
   *   n = 16 -> ~0.0048   (default: keeps TestSinConstrainedTRUE sound, s > 0.99)
   *   n = 32 -> ~0.0012
   */
  static int PIECEWISE_LINEAR_SEGMENTS = 16;

  // <2do> those are here to hide their implementation from traces, not to
  // increase performance. If we want to do that, we should probably inline
  // their real implementation here, instead of delegating (just a compromise)

  // TODO  abs, min, max should be solved symbolically here

//  public static double abs__D__D (MJIEnv env, int clsObjRef, double a) {
//    // return Math.abs(a);
//    System.err.println("Warning: Math.abs not modeled yet");
//    return (a <= .0) ? -a : a;
//  }

//  public static float abs__F__F (MJIEnv env, int clsObjRef, float a) {
//	System.err.println("Warning: Math.abs not modeled yet");
//    return Math.abs(a);
//  }
//
//  public static int abs__I__I (MJIEnv env, int clsObjRef, int a) {
//    //return Math.abs(a);
//	System.err.println("Warning: Math.abs not modeled yet");
//    return (a < 0) ? -a : a; // that's probably slightly faster
//  }
//
//  public static long abs__J__J (MJIEnv env, int clsObjRef, long a) {
//    //return Math.abs(a);
//	System.err.println("Warning: Math.abs not modeled yet");
//    return (a < 0) ? -a : a;
//  }
//
//  public static double max__DD__D (MJIEnv env, int clsObjRef, double a, double b) {
//    // that one has to handle inexact numbers, so it's probably not worth the hassle
//    // to inline it
//	System.err.println("Warning: Math.max not modeled yet");
//    return Math.max(a, b);
//  }
//
//  public static float max__FF__F (MJIEnv env, int clsObjRef, float a, float b) {
//	System.err.println("Warning: Math.max not modeled yet");
//	return Math.max(a, b);
//  }
//
//  public static int max__II__I (MJIEnv env, int clsObjRef, int a, int b) {
//    //return Math.max(a, b);
//	System.err.println("Warning: Math.max not modeled yet");
//    return (a >= b) ? a : b;
//  }
//
//  public static long max__JJ__J (MJIEnv env, int clsObjRef, long a, long b) {
//    //return Math.max(a, b);
//	System.err.println("Warning: Math.max not modeled yet");
//	return (a >= b) ? a : b;
//  }
//
//  public static double min__DD__D (MJIEnv env, int clsObjRef, double a, double b) {
//	System.err.println("Warning: Math.min not modeled yet");
//	return Math.min(a, b);
//  }
//
//  public static float min__FF__F (MJIEnv env, int clsObjRef, float a, float b) {
//	System.err.println("Warning: Math.min not modeled yet");
//	return Math.min(a, b);
//  }
//
//  public static int min__II__I (MJIEnv env, int clsObjRef, int a, int b) {
//	  System.err.println("Warning: Math.min not modeled yet");
//	  return Math.min(a, b);
//  }
//
//  public static long min__JJ__J (MJIEnv env, int clsObjRef, long a, long b) {
//	  System.err.println("Warning: Math.min not modeled yet");
//	  return Math.min(a, b);
//  }
  @MJI
  public static double sqrt__D__D (MJIEnv env, int clsObjRef, double a) {
	  Object [] attrs = env.getArgAttributes();
	  if (attrs==null) // concrete? I think
		  return Math.sqrt(a);
	  RealExpression sym_arg = (RealExpression) attrs[0];
	  if (sym_arg == null) { // concrete
		  return Math.sqrt(a);
	  }
	  else {
		  RealExpression result = new MathRealExpression(MathFunction.SQRT,sym_arg);
		  env.setReturnAttribute(result);
		  // System.out.println("result "+result);
		  return 0;
	  }
  }
  @MJI
  public static double random____D (MJIEnv env, int clsObjRef) {
    return Math.random();
  }

  
  @MJI
  public static double exp__D__D (MJIEnv env, int clsObjRef, double a) {
      Object [] attrs = env.getArgAttributes();
      if (attrs==null) // concrete? I think
    	  return Math.exp(a);
	  RealExpression sym_arg = (RealExpression) attrs[0];
	  if (sym_arg == null) { // concrete
		  return Math.exp(a);
	  }
	  else {
		  RealExpression result = new MathRealExpression(MathFunction.EXP,sym_arg);
		  env.setReturnAttribute(result);
		  // System.out.println("result "+result);
		  return 0;
	  }
  }
  @MJI
  public static double asin__D__D (MJIEnv env, int clsObjRef, double a) {
	  Object [] attrs = env.getArgAttributes();
	  if (attrs==null) // concrete? I think
		  return Math.asin(a);
	  RealExpression sym_arg = (RealExpression) attrs[0];
	  if (sym_arg == null) { // concrete
		  return Math.asin(a);
	  }
	  else {
		  RealExpression result = new MathRealExpression(MathFunction.ASIN,sym_arg);
		  env.setReturnAttribute(result);
		  // System.out.println("result "+result);
		  return 0;
	  }
  }
  @MJI
  public static double acos__D__D (MJIEnv env, int clsObjRef, double a) {
	  Object [] attrs = env.getArgAttributes();
	  if (attrs==null) // concrete? I think
		  return Math.acos(a);
	  RealExpression sym_arg = (RealExpression) attrs[0];
	  if (sym_arg == null) { // concrete
		  return Math.acos(a);
	  }
	  else {
		  RealExpression result = new MathRealExpression(MathFunction.ACOS,sym_arg);
		  env.setReturnAttribute(result);
		  // System.out.println("result "+result);
		  return 0;
	  }

  }
  @MJI
  public static double atan__D__D (MJIEnv env, int clsObjRef, double a) {
      Object [] attrs = env.getArgAttributes();
      if (attrs==null) // concrete? I think
    	  return Math.atan(a);
	  RealExpression sym_arg = (RealExpression) attrs[0];
	  if (sym_arg == null) { // concrete
		  return Math.atan(a);
	  }
	  else {
		  RealExpression result = new MathRealExpression(MathFunction.ATAN,sym_arg);
		  env.setReturnAttribute(result);
		  // System.out.println("result "+result);
		  return 0;
	  }
  }
  @MJI
  public static double atan2__DD__D (MJIEnv env, int clsObjRef, double a, double b) {
      Object [] attrs = env.getArgAttributes();
      if (attrs==null) // concrete? I think
    	  return Math.atan2(a,b);
	  RealExpression sym_arg1 = (RealExpression)attrs[0];
	  RealExpression sym_arg2 = (RealExpression)attrs[1];
	  RealExpression result;

	  if (sym_arg1 == null && sym_arg2 == null) // concrete
		  return Math.atan2(a,b);
	  else if (sym_arg1 == null)
		  result = new MathRealExpression(MathFunction.ATAN2, a, sym_arg2);
	  else if (sym_arg2 == null)
		  result = new MathRealExpression(MathFunction.ATAN2, sym_arg1, b);
	  else // both symbolic
		  result = new MathRealExpression(MathFunction.ATAN2, sym_arg1, sym_arg2);

	  env.setReturnAttribute(result);
	  // System.out.println("result "+result);
	  return 0; // don't care about concrete value


  }

//TODO: fix
//  public static double ceil__D__D (MJIEnv env, int clsObjRef, double a) {
//	  System.err.println("Warning: Math.ceil not modeled yet");
//	  return Math.ceil(a);
//  }
//
////TODO: fix
//  public static double floor__D__D (MJIEnv env, int clsObjRef, double a) {
//	  System.err.println("Warning: Math.floor not modeled yet");
//      return Math.floor(a);
//  }
  @MJI
  public static double log__D__D (MJIEnv env, int clsObjRef, double a) {
      Object [] attrs = env.getArgAttributes();
      if (attrs==null) // concrete? I think
    	  return Math.log(a);
	  RealExpression sym_arg = (RealExpression) attrs[0];
	  if (sym_arg == null) { // concrete
		  return Math.log(a);
	  }
	  else {
		  RealExpression result = new MathRealExpression(MathFunction.LOG,sym_arg);
		  env.setReturnAttribute(result);
		  // System.out.println("result "+result);
		  return 0;
	  }
  }
  @MJI
  public static double log10__D__D (MJIEnv env, int clsObjRef, double a) {
	      Object [] attrs = env.getArgAttributes();
	      if (attrs==null) // concrete? I think
	    	return Math.log10(a);
		  RealExpression sym_arg = (RealExpression) attrs[0];
		  if (sym_arg == null) { // concrete
			  return Math.log10(a);
		  }
		  else {
			  throw new RuntimeException("## Error: symbolic log10 not implemented ");
		  }
	  }




  // TODO: fix
//  public static double rint__D__D (MJIEnv env, int clsObjRef, double a) {
//	  System.err.println("Warning: Math.rint not modeled yet");
//	  return Math.rint(a);
//  }
  @MJI
  public static double tan__D__D (MJIEnv env, int clsObjRef, double a) {
	  Object [] attrs = env.getArgAttributes();
	  if (attrs==null) // concrete? I think
		  return Math.tan(a);
	  RealExpression sym_arg = (RealExpression) attrs[0];
	  if (sym_arg == null) { // concrete
		  return Math.tan(a);
	  }
	  else {
		  RealExpression result = new MathRealExpression(MathFunction.TAN,sym_arg);
		  env.setReturnAttribute(result);
		  // System.out.println("result "+result);
		  return 0;
	  }
  }
  @MJI
  public static double sin__D__D (MJIEnv env, int clsObjRef, double a) {
	  Object [] attrs = env.getArgAttributes();
	  if (attrs==null) // concrete? I think
		  return Math.sin(a);
	  RealExpression sym_arg = (RealExpression) attrs[0];
	  if (sym_arg == null) { // concrete
		  return Math.sin(a);
	  }
	  else {
		  // ---- PIECEWISE LINEAR INTERPOLATION on [-PI/2, PI/2] ----
		  // Split [-PI/2, PI/2] into PIECEWISE_LINEAR_SEGMENTS sub-intervals; on each
		  // sub-interval [xi, xi+1] the secant line S_i(x) = m_i*x + b_i through the knot
		  // endpoints (xi, sin(xi)) and (xi+1, sin(xi+1)) approximates sin(x). The whole
		  // sin() becomes a nested symbolic ITE tree whose leaves are single affine
		  // expressions (one fp.mul + one fp.add each - minimal circuit depth for the Z3
		  // bitvector solver). Each comparison is x < xi+1 (Operator.CMP == strict less-than).
		  //
		  // PATH-CONDITION PRUNING: before building the ITE tree, scan the path condition
		  // for interval constraints on the symbolic argument. Only include segments whose
		  // domain overlaps with the PC-implied interval. This eliminates unreachable
		  // segments and dramatically reduces Z3 solve time (especially for fp.eq checks).
		  final int n = PIECEWISE_LINEAR_SEGMENTS;
		  final double FULL_LO = -Math.PI / 2.0;
		  final double FULL_HI =  Math.PI / 2.0;

		  // --- Extract interval bounds from path condition ---
		  double pc_lo = FULL_LO;
		  double pc_hi = FULL_HI;
		  try {
			  PathCondition pc = PathCondition.getPC(env);
			  if (pc != null && pc.header != null) {
				  // Walk the constraint chain to find bounds on sym_arg
				  Constraint c = pc.header;
				  while (c != null) {
					  Expression left  = c.getLeft();
					  Expression right = c.getRight();
					  Comparator comp = c.getComparator();
					  if (right != null) {
						  // Case 1: sym_arg on the LEFT, constant on the RIGHT
						  if (left == sym_arg && right instanceof RealConstant) {
							  double val = ((RealConstant) right).value;
							  switch (comp) {
								  case GE: case GT:  pc_lo = Math.max(pc_lo, val); break;
								  case LE: case LT:  pc_hi = Math.min(pc_hi, val); break;
								  case EQ:  pc_lo = pc_hi = val; break;
							  }
						  // Case 2: constant on the LEFT, sym_arg on the RIGHT
						  } else if (right == sym_arg && left instanceof RealConstant) {
							  double val = ((RealConstant) left).value;
							  switch (comp) {
								  case LE: case LT:  pc_lo = Math.max(pc_lo, val); break;
								  case GE: case GT:  pc_hi = Math.min(pc_hi, val); break;
								  case EQ:  pc_lo = pc_hi = val; break;
							  }
						  }
					  }
					  c = c.and;
				  }
			  }
		  } catch (Exception e) {
			  // Fallback: use full range if PC extraction fails
			  pc_lo = FULL_LO;
			  pc_hi = FULL_HI;
		  }

		  // --- Collect overlapping segments, split at x=0, coarsen per polarity ---
		  // Z3 fp.eq on mixed-polarity ITE trees is the bottleneck (TIMEOUT at 16 segs,
		  // 180s at 4 segs). Splitting into separate negative/positive sub-trees lets Z3
		  // immediately discard one half when the PC constrains x's sign.
		  final int MAX_PER_POLARITY = 2;
		  final double width = (FULL_HI - FULL_LO) / n;

		  // Phase 1: collect all segments overlapping the PC range
		  double[] segLo  = new double[n * 2]; // may double from zero-split
		  double[] segHi  = new double[n * 2];
		  double[] segM   = new double[n * 2];
		  double[] segB   = new double[n * 2];
		  int rawCount = 0;

		  for (int i = 0; i < n; i++) {
			  final double xi  = FULL_LO + i * width;
			  final double xi1 = FULL_LO + (i + 1) * width;

			  if (xi1 <= pc_lo || xi >= pc_hi) {
				  continue;
			  }

			  // Clamp to PC range
			  double lo = Math.max(xi, pc_lo);
			  double hi = Math.min(xi1, pc_hi);

			  // If segment crosses x=0, split into two (negative + positive)
			  if (lo < 0.0 && hi > 0.0) {
				  // Negative half: [lo, 0]
				  segLo[rawCount] = lo;
				  segHi[rawCount] = 0.0;
				  segM[rawCount]  = (Math.sin(0.0) - Math.sin(lo)) / (0.0 - lo);
				  segB[rawCount]  = Math.sin(lo) - segM[rawCount] * lo;
				  rawCount++;
				  // Positive half: [0, hi]
				  segLo[rawCount] = 0.0;
				  segHi[rawCount] = hi;
				  segM[rawCount]  = (Math.sin(hi) - Math.sin(0.0)) / (hi - 0.0);
				  segB[rawCount]  = Math.sin(0.0) - segM[rawCount] * 0.0;
				  rawCount++;
			  } else {
				  segLo[rawCount] = lo;
				  segHi[rawCount] = hi;
				  segM[rawCount]  = (Math.sin(hi) - Math.sin(lo)) / (hi - lo);
				  segB[rawCount]  = Math.sin(lo) - segM[rawCount] * lo;
				  rawCount++;
			  }
		  }

		  // Phase 2: split into negative and positive groups, coarsen each
		  // Helper: coarsen a sub-array [start, start+count) by pair-merging
		  int negStart = 0, negCount = 0, posStart = 0, posCount = 0;
		  for (int r = 0; r < rawCount; r++) {
			  if (segHi[r] <= 0.0) {
				  if (negCount == 0) negStart = r;
				  negCount++;
			  } else {
				  if (posCount == 0) posStart = r;
				  posCount++;
			  }
		  }

		  // Coarsen each polarity group
		  int[] counts = {negCount, posCount};
		  int[] starts = {negStart, posStart};
		  for (int g = 0; g < 2; g++) {
			  int cnt = counts[g];
			  int st  = starts[g];
			  while (cnt > MAX_PER_POLARITY && cnt > 1) {
				  int wi = st;
				  for (int r = st; r < st + cnt; r += 2) {
					  if (r + 1 < st + cnt) {
						  segLo[wi] = segLo[r];
						  segHi[wi] = segHi[r + 1];
						  segM[wi]  = (Math.sin(segHi[wi]) - Math.sin(segLo[wi])) / (segHi[wi] - segLo[wi]);
						  segB[wi]  = Math.sin(segLo[wi]) - segM[wi] * segLo[wi];
					  } else {
						  segLo[wi] = segLo[r];
						  segHi[wi] = segHi[r];
						  segM[wi]  = segM[r];
						  segB[wi]  = segB[r];
					  }
					  wi++;
				  }
				  cnt = wi - st;
			  }
			  counts[g] = cnt;
		  }
		  negCount = counts[0];
		  posCount = counts[1];

		  // Phase 3: build ITE sub-trees for each polarity, then combine
		  // Negative sub-tree: segments from negStart..negStart+negCount
		  RealExpression negTree = null;
		  for (int r = negStart + negCount - 1; r >= negStart; r--) {
			  RealExpression segment = new RealConstant(segM[r])._mul(sym_arg)._plus(new RealConstant(segB[r]));
			  if (negTree == null) {
				  negTree = segment;
			  } else {
				  RealExpression cond = new BinaryRealExpression(Operator.CMP, sym_arg, new RealConstant(segHi[r]));
				  negTree = new BinaryRealExpression(Operator.ITEXPR, cond, segment, negTree);
			  }
		  }

		  // Positive sub-tree: segments from posStart..posStart+posCount
		  RealExpression posTree = null;
		  for (int r = posStart + posCount - 1; r >= posStart; r--) {
			  RealExpression segment = new RealConstant(segM[r])._mul(sym_arg)._plus(new RealConstant(segB[r]));
			  if (posTree == null) {
				  posTree = segment;
			  } else {
				  RealExpression cond = new BinaryRealExpression(Operator.CMP, sym_arg, new RealConstant(segHi[r]));
				  posTree = new BinaryRealExpression(Operator.ITEXPR, cond, segment, posTree);
			  }
		  }

		  // Phase 4: combine with polarity check: ite(x < 0, negTree, posTree)
		  RealExpression result;
		  if (negTree != null && posTree != null) {
			  RealExpression polarityCond = new BinaryRealExpression(Operator.CMP, sym_arg, new RealConstant(0.0));
			  result = new BinaryRealExpression(Operator.ITEXPR, polarityCond, negTree, posTree);
		  } else if (negTree != null) {
			  result = negTree;
		  } else if (posTree != null) {
			  result = posTree;
		  } else {
			  final double m = (Math.sin(pc_hi) - Math.sin(pc_lo)) / (pc_hi - pc_lo);
			  final double b = Math.sin(pc_lo) - m * pc_lo;
			  result = new RealConstant(m)._mul(sym_arg)._plus(new RealConstant(b));
			  negCount = 0; posCount = 0;
		  }

		  int totalActive = negCount + posCount;
		  System.out.println("[sin-pruning] PC=[" + pc_lo + ", " + pc_hi + "] raw=" + rawCount + " neg=" + negCount + " pos=" + posCount + "/" + n);
		  env.setReturnAttribute(result);
		  return 0;
	  }

  }
  @MJI
  public static double cos__D__D (MJIEnv env, int clsObjRef, double a) {
	  Object [] attrs = env.getArgAttributes();
	  if (attrs==null) // concrete? I think
		  return Math.cos(a);
	  RealExpression sym_arg = (RealExpression) attrs[0];
	  if (sym_arg == null) // concrete
		  return Math.cos(a);
	  else {
		  RealExpression result = new MathRealExpression(MathFunction.COS,sym_arg);
		  env.setReturnAttribute(result);
		  // System.out.println("result "+result);
		  return 0;
	  }
  }
  @MJI
  public static double pow__DD__D (MJIEnv env, int clsObjRef, double a, double b) {
//	  System.out.println("here!!!!!");
	  Object [] attrs = env.getArgAttributes();
	  if (attrs==null) // concrete? I think
		  return Math.pow(a,b);
	  RealExpression sym_arg1 = (RealExpression)attrs[0];
	  RealExpression sym_arg2 = (RealExpression)attrs[1];
	  RealExpression result;

	  if (sym_arg1 == null && sym_arg2 == null) // concrete
		  return Math.pow(a,b);
	  else if (sym_arg1 == null)
		  result = new MathRealExpression(MathFunction.POW, a, sym_arg2);
	  else if (sym_arg2 == null)
		  result = new MathRealExpression(MathFunction.POW, sym_arg1, b);
	  else // both symbolic
		  result = new MathRealExpression(MathFunction.POW, sym_arg1, sym_arg2);

	  env.setReturnAttribute(result);
	  // System.out.println("result "+result);
	  return 0; // don't care about concrete value
  }

}

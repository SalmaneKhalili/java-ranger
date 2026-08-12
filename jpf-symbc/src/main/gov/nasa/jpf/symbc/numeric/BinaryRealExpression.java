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
//Copyright (C) 2005 United States Government as represented by the
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

package gov.nasa.jpf.symbc.numeric;

import java.util.Map;

public class BinaryRealExpression extends RealExpression 
{
	RealExpression left;
	Operator   op;
	RealExpression right;
	// ITE only: holds the "else" branch (left=cond, right=then, extra=else)
	RealExpression extra;

	public BinaryRealExpression (RealExpression l, Operator o, RealExpression r) 
	{
		left = l;
		op = o;
		right = r;
	}

	// Operator-first ctor used by the piecewise Math.sin peer (CMP nodes).
	public BinaryRealExpression (Operator o, RealExpression l, RealExpression r)
	{
		op = o;
		left = l;
		right = r;
	}

	// Operator-first ctor for ITE: (cond ITEXPR then else).
	public BinaryRealExpression (Operator o, RealExpression cond, RealExpression thenExpr, RealExpression elseExpr)
	{
		op = o;
		left = cond;
		right = thenExpr;
		extra = elseExpr;
	}

	public double solution() 
	{
		double l = left.solution();
		double r = right.solution();
		switch(op){
		   case PLUS:  return l + r;
		   case MINUS: return l - r;
		   case MUL:   return l * r;
		   case DIV:   assert(r!=0);
			           return l/r;
		   case CMP:   return l < r ? 1.0 : 0.0;
		   case ITEXPR: return l != 0.0 ? r : (extra == null ? 0.0 : extra.solution());
           default:    throw new RuntimeException("## Error: BinaryRealSolution solution: l " + l + " op " + op + " r " + r);
		}
	}

	public RealExpression getExtra() {
		return extra;
	}

    public void getVarsVals(Map<String,Object> varsVals) {
    	left.getVarsVals(varsVals);
    	right.getVarsVals(varsVals);
	if (extra != null) extra.getVarsVals(varsVals);
    }

	public String stringPC() {
		if (extra != null)
			return "(ite " + left.stringPC() + " " + right.stringPC() + " " + extra.stringPC() + ")";
		return "(" + left.stringPC() + op.toString() + right.stringPC() + ")";
	}

	public String toString () 
	{
		if (extra != null)
			return "(ite " + left.toString() + " " + right.toString() + " " + extra.toString() + ")";
		return "(" + left.toString() + op.toString() + right.toString() + ")";
	}

	public String prefix_notation ()
	{
		if (extra != null)
			return "(ite " + left.prefix_notation() + " " + right.prefix_notation() + " " + extra.prefix_notation() + ")";
		return "(" + op.prefix_notation() + " "+left.prefix_notation()+" "  + right.prefix_notation() + ")";
	}
	
	public Operator getOp() {
		return op;
	}

	public RealExpression getLeft() {
		return left;
	}

	public RealExpression getRight() {
		return right;
	}

	// JacoGeldenhuys
	@Override
	public void accept(ConstraintExpressionVisitor visitor) {
		visitor.preVisit(this);
		left.accept(visitor);
		right.accept(visitor);
		if (extra != null) extra.accept(visitor);
		visitor.postVisit(this);
	}

	@Override
	public int compareTo(Expression expr) {
		if (expr instanceof BinaryRealExpression) {
			BinaryRealExpression e = (BinaryRealExpression) expr;
			int r = getOp().compareTo(e.getOp());
			if (r == 0) {
				r = getLeft().compareTo(e.getLeft());
			}
			if (r == 0) {
				r = getRight().compareTo(e.getRight());
			}
			return r;
		} else {
			return getClass().getCanonicalName().compareTo(expr.getClass().getCanonicalName());
		}
	}
}

/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.lang;

import r.lang.exception.EvalException;

/**
 * Expression representing a call to an R function, consisting of
 * a function reference and a list of arguments.
 *
 * Note that this type is called "language" in the R vocabulary.
 *
 */
public class FunctionCall extends PairList.Node {
  public static final int TYPE_CODE = 6;
  public static final String TYPE_NAME = "language";

  public FunctionCall(SEXP function, PairList arguments) {
    super(function, arguments);
  }

  public FunctionCall(SEXP function, PairList arguments, PairList attributes) {
    super(Null.INSTANCE, function, attributes, arguments);
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public EvalResult evaluate(Context context, Environment rho) {
    Function functionExpr = evaluateFunction(context, rho);
    return functionExpr.apply(context, rho, this, getArguments());
  }

  private Function evaluateFunction(Context context, Environment rho) {
    SEXP functionExp = getFunction();
    if(functionExp instanceof Symbol) {
      return findFunction(context, rho, (Symbol) functionExp);
    } else {
      SEXP evaluated = functionExp.evalToExp(context, rho);
      if(!(evaluated instanceof Function)) {
        throw new EvalException("'function' of lang expression is of unsupported type '%s'", functionExp.getTypeName());
      }
      return (Function)evaluated;
    }
  }

  private Function findFunction(Context context, Environment rho, Symbol symbol) {
    while(rho != Environment.EMPTY) {
      SEXP value = rho.getVariable(symbol);
      if(value instanceof Promise) {
        value = ((Promise) value).force().getExpression();
      }
      if(value instanceof Function) {
        return (Function) value;
      }
      rho = rho.getParent();
    }
    throw new EvalException("could not find function '%s'", symbol.getPrintName());
  }

  public static SEXP fromListExp(PairList.Node listExp) {
    return new FunctionCall(listExp.value, listExp.nextNode);
  }

  public SEXP getFunction() {
    return value;
  }
 
  public PairList getArguments() {
    return nextNode == null ? Null.INSTANCE : nextNode;
  }

  public <X extends SEXP> X getArgument(int index) {
    return getArguments().<X>getElementAsSEXP(index);
  }

  public SEXP evalArgument(Context context, Environment rho, int index) {
    return getArgument(index).evalToExp(context, rho);
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);

  }

  @Override
  public String toString() {
    StringBuilder sb= new StringBuilder();
    sb.append(getFunction()).append("(");
    boolean needsComma=false;
    for(PairList.Node node : getArguments().nodes()) {
      if(needsComma) {
        sb.append(", ");
      } else {
        needsComma = true;
      }
      if(node.hasTag()) {
        sb.append(node.getTag().getPrintName())
            .append("=");
      }
      sb.append(node.getValue());
    }
    return sb.append(")").toString();
  }

  public static FunctionCall newCall(SEXP function, SEXP... arguments) {
    if(arguments.length == 0) {
      return new FunctionCall(function, Null.INSTANCE);
    } else {
      return new FunctionCall(function, PairList.Node.fromArray(arguments));
    }
  }

  @Override
  protected SEXP cloneWithNewAttributes(PairList attributes) {
    return new FunctionCall(getFunction(), getArguments(), attributes);
  }

  @Override
  public FunctionCall clone() {
    return FunctionCall.newCall(getFunction(), getArguments().clone());
  }
}

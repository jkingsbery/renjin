package r.base.model;


import java.util.List;

import r.lang.FunctionCall;
import r.lang.SEXP;
import r.lang.Symbol;

import com.google.common.collect.Lists;

public class TermBuilder {
  
  private List<SEXP> expressions = Lists.newArrayList();
  public static final Symbol COLON = Symbol.get(":");
  public static final Symbol I = Symbol.get("I");
  
  public Term build(SEXP expr) {
    add(expr);
    return new Term(expressions);
  }

  private void add(SEXP expr) {
    if(expr instanceof FunctionCall) {
      call((FunctionCall)expr);
    } else {
      expressions.add(expr);
    }
  }

  private void call(FunctionCall call) {
    if(call.getFunction() == COLON) {
      add(call.getArgument(0));
      add(call.getArgument(1));
    } else {
      // treat all other calls as arithmetic
      expressions.add(call);
    }
  }
}

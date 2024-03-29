package r.compiler.ir.tac.functions;

import r.compiler.ir.exception.InvalidSyntaxException;
import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.statements.ExprStatement;
import r.lang.FunctionCall;
import r.lang.SEXP;
import r.lang.Symbol;

public class InternalCallTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get(".Internal");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call) {
    SEXP primitiveCall = call.getArgument(0);
    if(!(primitiveCall instanceof FunctionCall)) {
      throw new InvalidSyntaxException(".Internal() expects a language object as its only argument");
    }
    return builder.translatePrimitiveCall(context, (FunctionCall) primitiveCall);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
      FunctionCall call) {
    builder.addStatement(
        new ExprStatement(translateToExpression(builder, context, call)));
  }
}

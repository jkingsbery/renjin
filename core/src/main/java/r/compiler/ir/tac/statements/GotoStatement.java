package r.compiler.ir.tac.statements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.NullExpression;
import r.compiler.ir.tac.expressions.Variable;
import r.lang.Context;

public class GotoStatement implements Statement, BasicBlockEndingStatement {

  private final IRLabel target;

  public GotoStatement(IRLabel target) {
    this.target = target;
  }

  public IRLabel getTarget() {
    return target;
  }

  
  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Arrays.asList(target);
  }

  @Override
  public String toString() {
    return "goto " + target;
  }

  @Override
  public Object interpret(Context context, Object[] temp) {
    return target;
  }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public Expression getRHS() {
    return NullExpression.INSTANCE;
  }

  @Override
  public Statement withRHS(Expression newRHS) {
    if(newRHS != NullExpression.INSTANCE) {
      throw new IllegalArgumentException();
    }
    return this;
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException();
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitGoto(this);
  }
}

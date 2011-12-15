package r.compiler.ir.ssa;

import java.util.List;
import java.util.Set;

import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.Variable;
import r.lang.Context;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PhiFunction implements Operand {

  private List<Variable> arguments;
  
  public PhiFunction(Variable variable, int count) {
    if(count < 2) {
      throw new IllegalArgumentException("variable=" + variable + ", count=" + count + " (count must be >= 2)");
    }
    this.arguments = Lists.newArrayList();
    for(int i=0;i!=count;++i) {
      arguments.add(variable);
    }
  }

  public PhiFunction(List<Variable> arguments) {
    this.arguments = arguments;
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Variable> variables() {
    return Sets.newHashSet(arguments);
  }

  @Override
  public String toString() {
    return "\u03A6(" + Joiner.on(", ").join(arguments) + ")";
  }

  @Override
  public Operand renameVariable(Variable name, Variable newName) {
    List<Variable> newArguments = Lists.newArrayList();
    for(Variable arg : arguments) {
      newArguments.add(arg.equals(name) ? newName : arg);
    }
    return new PhiFunction(newArguments);
  }
  
  public PhiFunction replaceVariable(int j, int i) {
    List<Variable> newArguments = Lists.newArrayList(arguments);
    newArguments.set(j, new SsaVariable(newArguments.get(j), i));
    return new PhiFunction(newArguments);
  }

  public Variable getArgument(int j) {
    return arguments.get(j);
  }
} 

package r.compiler.ir.tac.expressions;

import java.util.Set;

import r.compiler.ir.tree.TreeNode;
import r.lang.Context;

public interface Expression extends TreeNode {

  /**
   * Retrieves the value of this expression (during interpretation)
   * 
   * @param context
   * @param temps
   * @return
   */
  Object retrieveValue(Context context, Object temps[]);
  
  /**
   * 
   * @return the set of all {@code Variable}s referenced by this {@code Expression}
   */
  Set<Variable> variables();

  /**
   * Recursively replaces all references to {@code variable} with 
   * {@code newVariable} in this {@code Expression}
   */
  Expression replaceVariable(Variable variable, Variable newVariable);
  
  
  void accept(ExpressionVisitor visitor);
  
}

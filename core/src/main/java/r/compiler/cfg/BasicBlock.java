package r.compiler.cfg;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import r.compiler.ir.ssa.PhiFunction;
import r.compiler.ir.tac.IRBody;
import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.PrimitiveCall;
import r.compiler.ir.tac.expressions.Variable;
import r.compiler.ir.tac.statements.Assignment;
import r.compiler.ir.tac.statements.GotoStatement;
import r.compiler.ir.tac.statements.IfStatement;
import r.compiler.ir.tac.statements.ReturnStatement;
import r.compiler.ir.tac.statements.Statement;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BasicBlock {
  private final IRBody parent;
  private String debugId;
  
  private IRLabel label;
  private List<Statement> statements = Lists.newArrayList();
  
  public BasicBlock(IRBody parent) {
    super();
    this.parent = parent;
  }
  
  public void addStatement(Statement statement) {
    statements.add(statement);
  }
  
  public void insertPhiFunction(Variable variable, int count) {
    statements.add(0, new Assignment(variable, new PhiFunction(variable, count)));
  }

  public Statement replaceStatement(Statement stmt, Statement newStmt) {
    int i = statements.indexOf(stmt);
    statements.set(i, newStmt);
    return newStmt;
  }
 
  public void replaceStatement(int i, Statement stmt) {
    statements.set(i, stmt);
  }

  
  public List<Statement> getStatements() {
    return statements;
  }
  
  public void setDebugId(int index) {
    this.debugId = "BB" + index;
  }

  public void setDebugId(String string) {
    this.debugId = string;
  }
  
  public static BasicBlock createWithStartAt(IRBody parent, int statementIndex) {
    BasicBlock block = new BasicBlock(parent);
    block.label = parent.getIntructionLabel(statementIndex);
    block.statements = Lists.newArrayList();
    block.statements.add(parent.getStatements().get(statementIndex));
    return block;
  }

  public IRLabel getLabel() {
    return label;
  }
  
  public boolean isLabeled() {
    return label != null;
  }
  
  public Statement getTerminal() {
    return statements.get(statements.size() - 1);
  }
  
  public boolean returns() {
    return getTerminal() instanceof ReturnStatement;
  }
  
  public boolean fallsThrough() {
    Statement terminal = getTerminal();
    return !( terminal instanceof GotoStatement ||
              terminal instanceof IfStatement ||
              terminal instanceof ReturnStatement);
  }
  
  public Iterable<IRLabel> targets() {
    return getTerminal().possibleTargets();
  }

  public String statementsToString() {
    StringBuilder sb = new StringBuilder();
    for(Statement statment : statements) {
      sb.append(statment).append("\n");
    }
    return sb.toString();
  }
  
  public Set<Variable> variables() {
    Set<Variable> variables = Sets.newHashSet();
    for(Statement statement : statements) {
      variables.addAll(statement.variables());
    }
    return Collections.unmodifiableSet(variables);
  }
  
  public Iterable<Assignment> assignments() {
    return (Iterable)Iterables.filter(statements, Predicates.instanceOf(Assignment.class));
  }
  
  public Iterable<Assignment> phiAssignments() {
    return (Iterable)Iterables.filter(statements, CfgPredicates.isPhiAssignment());
  }
  
  @Override
  public String toString() {
    return debugId;
  }

}

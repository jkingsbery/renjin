package r.compiler.ir.tac;

import java.io.IOException;

import r.lang.Context;
import r.lang.SEXP;

public class IRProgram {

  private IRBody main;
  private IRFunctionTable functionTable = new IRFunctionTable();
  
  public IRProgram(SEXP program) {
    IRBodyBuilder builder = new IRBodyBuilder(functionTable);
    main = builder.build(program);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(IRFunction function : functionTable) {
      sb.append("function@" + function.getId()).append("\n");
      sb.append(function.getScope()).append("\n");
    }
    
    sb.append("\nMAIN:\n");
    sb.append(main.toString());
    return sb.toString();
  }
  
  public SEXP evaluate() throws IOException {
    Context context = Context.newTopLevelContext();
    context.init();
    return main.evaluate(context);
  }

  public IRBody getMain() {
    return main;
  }

  public Iterable<IRFunction> getFunctions() {
    return functionTable.getFunctions();    
  }
}

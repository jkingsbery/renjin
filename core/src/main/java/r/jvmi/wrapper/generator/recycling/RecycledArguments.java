package r.jvmi.wrapper.generator.recycling;

import java.util.List;

import r.jvmi.wrapper.WrapperSourceWriter;

public class RecycledArguments {

  protected final WrapperSourceWriter s;
  private final List<RecycledArgument> args;
  
  public RecycledArguments(WrapperSourceWriter s,
      List<RecycledArgument> recycledArguments) {
    super();
    this.s = s;
    this.args = recycledArguments;
  }

  public void writeSetup() {
    storeArgLengths();
    computeLongestVector();
    declareAndInitializeCounterLocals();
  }

  private void storeArgLengths() {
    s.writeComment("component lengths");
    for(RecycledArgument arg : args) {
      s.writeStatementF("int %s_length = %s.length();", arg.getVectorLocal(), 
          arg.getVectorLocal());
    }
    s.writeBlankLine();
  }

  private void computeLongestVector() {
    s.writeComment("compute longest vector");
    s.writeStatement("Vector longest = Null.INSTANCE");
    s.writeStatement("int cycles = 0");
    for(RecycledArgument arg : args) {
      s.writeBeginBlock("if(" + arg.getVectorLocal() +"_length > cycles) {");
      s.writeStatementF("cycles = %s_length", arg.getVectorLocal());
      s.writeStatementF("longest = %s", arg.getVectorLocal());
      s.writeCloseBlock();
    }
    s.writeBlankLine();
  }

  private void declareAndInitializeCounterLocals() {
    s.writeComment("initialize counters");
    for(RecycledArgument arg : args) {
      s.writeStatementF("int %s_i = 0;", arg.getVectorLocal());
    }
    s.writeBlankLine();
  }
  
  public String composeAnyNACondition() {
    StringBuilder condition = new StringBuilder();
    for(RecycledArgument arg : args) {
      if(condition.length() > 0) {
        condition.append(" || ");
      }
      condition.append(String.format("%s.isElementNA(%s)", arg.getVectorLocal(), indexVariable(arg)));
    }    
    return condition.toString();
  }

  protected String indexVariable(RecycledArgument arg) {
    return arg.getVectorLocal() + "_i";
  }
  
  public void writeElementExtraction() {
    
    for(RecycledArgument arg : args) {
      
      s.writeStatement(new StringBuilder()
        .append(arg.getElementClassName())
        .append(" ")
        .append(arg.getElementLocal())
        .append(" = ")
        .append(arg.getAccessExpression(indexVariable(arg)))
        .toString());
           
    }
  }
  
  public void writeIncrementCounters() {
    for(RecycledArgument arg : args) {
      String index = indexVariable(arg);
      s.writeStatement( index  + "++" );
      s.writeStatement( "if(" + index + " >= " + arg.getLengthLocal() + ") " +
              index + " = 0; "); 
    }
    s.writeBlankLine();
  }
  
  public String getLongestLocal() {
    return "longest";
  }
}

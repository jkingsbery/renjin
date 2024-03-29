package r.jvmi.wrapper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility class for writing java source for generated wrappers 
 * for functions. 
 * 
 * @author alex
 *
 */
public class WrapperSourceWriter {
  private PrintWriter writer;
  private String className;
  private int indent = 0;
  
  public WrapperSourceWriter(File sourceFile, String className) throws IOException {
    writer = new PrintWriter( sourceFile );
    this.className = className;
  }
  
  public void writePackage(String packageName) {
    println("package " + packageName + ";");
    writeBlankLine();
  }
  
  public void writeImport(String className) {
    println("import " + className + ";");
  }
  
  public void writeImport(Class clazz) {
    writeImport(clazz.getName());
  }
  
  public void writeStaticImport(String spec) {
    println("import static " + spec + ";");
  }
    
  public void writeBeginClass(Class... interfacesImplemented) {
    StringBuilder classSig = new StringBuilder();
    classSig.append("public class " + className + " extends BuiltinFunction ");
    if(interfacesImplemented.length > 0) {
      classSig.append(" implements ");
      boolean commaNeeded = false;
      for(Class iface : interfacesImplemented) {
        if(commaNeeded) {
          classSig.append(", ");
        } else {
          commaNeeded = true;
        }
        classSig.append(iface.getName());
      }
    }
    classSig.append(" {");
    println(classSig.toString());
    indent++;
  }
  
  public void writeConstructor(String functionName) {
    println("public " + className + "() { super(" + quote(functionName) + "); }");
  }  
  
  public void writeBeginApplyMethod() {
    println("@Override");
    println("public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {");
    indent++;
  }
  
  public void writeComment(String comment) {
    println("// " + comment);
  }
  
  public void writeBeginBlock(String statement) {
    println(statement);
    indent();
  }
  
  public void writeBeginIf(String condition) {
    writeBeginBlock("if(" + condition + ") {");
  }
  
  
  public void writeElse() {
    outdent();
    writeBeginBlock("} else {");
  }
  
  public void writeBeginIfElse(String condition) {
    outdent();
    writeBeginBlock("} else if(" + condition + ") {");
  }

  
  public void writeStatement(String statement) {
    if(!statement.endsWith(";")) {
      statement += ";";
    }
    println(statement);
  }
  
  public void indent() {
    indent++;
  }
  
  public void outdent() {
    indent--;
  }
  

  public void writeStatementF(String statement, Object... args) {
    writeStatement(String.format(statement, args));
  }
  
  public void writeTempLocalDeclaration(Class clazz, String name) {
    writeStatementF("%s %s;", toJava(clazz), name);
  }
  
  public void writeCloseBlock() {
    indent--;
    println("}");
  }
  
  public void writeBlankLine() {
    writer.println();
  }
  
  public void println(String line) {
    for(int i=0;i<indent;++i) {
      writer.print("  ");
    }
    writer.println(line);
  }
  
  public void printlnf(String line, Object... args) {
    println(String.format(line, args));
  }
  
  public void printf(String line, Object... args) {
    writer.print(String.format(line, args));
  }
  

  public String quote(String name) {
    return "\"" + name + "\"";
  }
  
  public void flush() {
    writer.flush();
  }

  public void println() {
    writer.println();
  }

  public void close() {
    writer.close();
  }

  public void writeBeginTry() {
    writeStatement("try {");
    indent ++;
  }
  
  public void writeCatch(Class exceptionClass, String variableName) {
    indent--;
    println("} catch (" + exceptionClass.getName() + " " + variableName + ") { ");
    indent++;
  }

  public static final String toJava(Class<?> clazz) {
    if(clazz.isPrimitive()) {
      return clazz.getSimpleName();
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append(clazz.getPackage().getName());
      if(clazz.getEnclosingClass() != null) {
        sb.append(".");
        sb.append(clazz.getEnclosingClass().getSimpleName());
      }
      sb.append(".");
      sb.append(clazz.getSimpleName());
      return sb.toString();
    } 
  }  
}
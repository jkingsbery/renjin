/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
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

package r.interpreter;

import r.base.Warning;
import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.exception.FunctionCallException;
import r.lang.exception.ParseException;
import r.parser.ParseOptions;
import r.parser.ParseState;
import r.parser.RLexer;
import r.parser.RParser;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * Interactive interpreter for the R-language
 */
public class Interpreter implements Runnable {

  private final Console console;
  private Environment global;
  private Context topLevelContext;

  public Interpreter(Console console) {

    this.console = console;

    this.topLevelContext = Context.newTopLevelContext();
    this.topLevelContext.getGlobals().setStdOut(new PrintWriter(console.getOut()));
    this.global = topLevelContext.getEnvironment();

    if(console instanceof RichConsole) {
      ((RichConsole) console).setNameCompletion(new SymbolCompletion(global));
    }
  }

  /**
   * A decorator(?) around a standard reader that looks for special characters
   * (up, down, Ctrl-D) and catches those, and passes everything else along.
   * @author jamie
   *
   */
  public static class ConsoleReader extends Reader{
    private Reader x;

    public ConsoleReader(Reader x){
      this.x=x;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      int count = x.read(cbuf, off, len);
      
      return count;
    }

    @Override
    public void close() throws IOException {
      x.close();
      
    }
  }
  
  
  @Override
  public void run() {

    ParseOptions options = ParseOptions.defaults();
    ParseState state = new ParseState();
    Reader reader = console.getIn();
    RLexer lexer = new RLexer(options, state, reader);
    RParser parser = new RParser(options, state, lexer);

    printGreeting();

    try {
      topLevelContext.init();

    } catch (IOException e) {
      console.getOut().println("Error loading base package");
    }


    while(true) {

      console.getOut().print("> ");

      try {
        
        SEXP exp = parser.getResult();
        if(exp == null) {
          continue;
        }
        
        // clean up last warnings from any previous run
        clearWarnings();
        
        SEXP result = topLevelContext.evaluate(exp, global);

        if(!topLevelContext.getGlobals().isInvisible()) {
          topLevelContext.evaluate(FunctionCall.newCall(Symbol.get("print"), result));
          
        }
        
        printWarnings();

      } catch (ParseException e) {
        console.getErr().println(String.format("Error: %s", e.getMessage()));

      } catch (FunctionCallException e) {
        console.getErr().println(String.format("Error in '<fixme>': %s", e.getMessage()));

      } catch (EvalException e) {
        console.getErr().println(String.format("Error: %s", e.getMessage()));

      } catch (Exception e) {
        console.getErr().println(String.format("Java exception thrown: " + e.getMessage()));
        e.printStackTrace();
      }
    }
  }

  private void printWarnings() {
    SEXP warnings = topLevelContext.getEnvironment().getBaseEnvironment().getVariable(Warning.LAST_WARNING);
    if(warnings != Symbol.UNBOUND_VALUE) {
      topLevelContext.evaluate( FunctionCall.newCall(Symbol.get("print.warnings"), warnings),
        topLevelContext.getEnvironment().getBaseEnvironment());
    }
  }

  private void clearWarnings() {
    topLevelContext.getEnvironment().getBaseEnvironment().remove(Warning.LAST_WARNING);
  }

  private void printGreeting() {
    PrintStream out = console.getOut();
    out.print("Renjin 0.0.1-SNAPSHOT\n");
    out.print("Copyright (C) 2010 The R Foundation for Statistical Computing\n");
    out.print("Copyright (C) 2010 bedatadriven\n");
    out.print("ISBN 3-900051-07-0\n\n");

    out.print("R is free software and comes with ABSOLUTELY NO WARRANTY. " +
      "You are welcome to redistribute it under certain conditions. " +
      "Type 'license()' or 'licence()' for distribution details.\n\n");

    out.print("R is a collaborative project with many contributors. " +
      "Type 'contributors()' for more information and " +
      "'citation()' on how to cite R or R packages in publications.\n\n");

//    console.print("Type 'demo()' for some demos, 'help()' for on-line help, or " +
//      "'help.start()' for an HTML browser interface to help. " +
//      "Type 'q()' to quit R.\n\n");
  }

}

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

package r.base;

import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.lang.Context;
import r.lang.Environment;
import r.lang.FunctionCall;
import r.lang.ListVector;
import r.lang.Symbol;

public class Warning {

  public static final Symbol LAST_WARNING = Symbol.get("last.warning");
  
  public static void invokeWarning(@Current Context context, String message, Object... args) {
    emitWarning(context, null, false, String.format(message, args));
  }
  
  public static void invokeWarning(@Current Context context, FunctionCall call, String message, Object... args) {
    emitWarning(context, call, false, String.format(message, args));
  }

  @Primitive("warning")
  public static void warning(@Current Context context, boolean call, boolean immediate, String message) {
  
    if(call || !immediate) {
      emitWarning(context, Contexts.sysCall(context, 1), immediate, message);
    } else {
      context.getGlobals().stdout.println("Warning message:");
      context.getGlobals().stdout.println(message);
    }
  }

  private static void emitWarning(Context context, FunctionCall call,
      boolean immediate, String message) {
    int warnMode = context.getGlobals().options.getInt("warn", 0);
    if(warnMode == 1 || (warnMode <= 0 && immediate)) {
      context.getGlobals().stdout.println("Warning in " + call.toString() + " :");
      context.getGlobals().stdout.println("  " + message);
    } else if(warnMode == 0) {
      // store warnings until end of evaluation
      
      ListVector.NamedBuilder lastWarning = new ListVector.NamedBuilder();
     
      Environment baseEnv = context.getEnvironment().getBaseEnvironment();
      if(baseEnv.hasVariable(LAST_WARNING)) {
        lastWarning.addAll((ListVector)baseEnv.getVariable(LAST_WARNING));
      }
      
      lastWarning.add(message, call);
      baseEnv.setVariable(LAST_WARNING, lastWarning.build());
    }
  }
}

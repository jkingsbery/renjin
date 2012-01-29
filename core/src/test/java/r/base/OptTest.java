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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import r.EvalTestCase;
import r.lang.LogicalVector;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;

public class OptTest extends EvalTestCase{
  

  @Test
  public void overloadingWorks() {
    // this will fail if they are fed thru the string overload
    assertThat(eval("10>5"), equalTo(c(true)));
    assertThat(eval("10L>5L"), equalTo(c(true)));
    assertThat(eval("TRUE>FALSE"), equalTo(c(true)));
    assertThat(eval("'one' > 'zed'"), equalTo(c(false)));
  }
  
  @Test(expected=EvalException.class)
  public void vectorsAreChecked() {
    eval("sqrt('4')");
  }
  
  @Test
  public void integerDivision() {
    assertThat( eval("7 %/% 3"), equalTo(c(2)));
    assertThat( eval("7 %/% 3.9"), equalTo(c(1)));
    assertThat( eval("-7 %/% 3.9"), equalTo(c(-2)));
  }
  
  @Test
  public void dimAttribIsCopied() {
    eval(" y <- c(1,2) ");
    eval(" dj <- c(1,1) ");
    eval(" dim(dj) <- c(2,1)");
    
    assertThat(eval(" dim( y / dj )"), equalTo(c_i(2,1)));
  }
  
  @Test
  public void symbolEquality() {
    assertThat(eval("quote(x)==quote(y)"), equalTo(c(false)));
    assertThat(eval("quote(x)==quote(x)"), equalTo(c(true)));
  }
  
  @Test
  public void complexAdd() {
    assertThat(eval("3+1i"), equalTo(c(complex(3,1))));
  }
  
  @Test
  public void negatedLogical() {
    topLevelContext.getEnvironment().setVariable(Symbol.get("x"), LogicalVector.EMPTY);
    assertThat(eval("!x"), equalTo((SEXP)LogicalVector.EMPTY));
  }
  
}

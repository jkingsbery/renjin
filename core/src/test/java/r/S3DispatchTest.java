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

package r;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import r.lang.Logical;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class S3DispatchTest extends EvalTestCase {

  @Before
  public void setUpInternalWrappers() {
    eval("NextMethod <- function (generic = NULL, object = NULL, ...) .Internal(NextMethod(generic, object, ...))");
  }

  @Test
  public void genericPrimitive() {
    eval(" version <- list(platform='i386-pc', arch='i386', os='mingw32', major='2', minor='10.1') ");
    eval(" class(version) <- 'simple.list' ");
    eval(" `[.simple.list` <- function (x, i, ...) { y<-NextMethod('['); class(y) <- class(x); y }");

    eval(" v2 <- version[c('major', 'minor')]");

    assertThat( eval("class(v2)"), equalTo(c("simple.list")));
  }

  @Test
  public void groupGeneric() {
    eval(" Ops.numeric_version <- function(e1,e2) { e1<-e1$value; e2<-e2$value; NextMethod(.Generic) } ");
    eval(" o1 <- list(value=4) ");
    eval(" class(o1) <- 'numeric_version'");

    assertThat( eval("o1 < o1"), equalTo(c(Logical.FALSE)));
  }
  
  @Test
  public void argsAreEvaluatedWhenDispatchedFromPrimitive() {
    eval(" `[.foo` <- function(x, i, j) i == 1");
    eval(" x <- 1:9" );
    eval(" class(x) <- 'foo'");
   
    eval(" z <- 1" );
    
    assertThat( eval(" x[z] "), equalTo(c(true))); 
  }
  
  
  @Test
  public void missingInS3Generic() {
    eval("`[.foo` <- function(x, i, j, drop = TRUE) c(missing(i), missing(j), missing(drop)) ");
    eval("x <- 1:5");
    eval("class(x) <- 'foo'");
    
    assertThat(eval("x[1,2]"), equalTo(c(false,false,true)));
    assertThat(eval("x[,2]"), equalTo(c(true,false,true)));
  }
  
  @Test
  public void argumentsGoneMissing() {
    eval("g <- function(x, y, drop=FALSE) .Internal(assign('d', drop, globalenv(), FALSE)) ");
    eval("f.default <- function(x, y, drop=FALSE) g(x,y,drop) ");
    eval("f <- function(x,y,drop=FALSE) UseMethod('f')");
    eval("f(4,5)");
    assertThat(eval("d"), equalTo(c(false)));
  }

   
  @Test
  public void matchCallWithinGeneric() throws IOException {
    assumingBasePackagesLoad();
    
    eval("g <- { f <- function(a,b,...) { UseMethod('f') } ; " +
    		        "f.default <- function(a,b,...) { match.call() }; " +
    		        "f; } ");
    eval("call <- g(1,2)");
    
    assertThat(eval("call[[1]]"), equalTo(symbol("f.default")));
  }
  
  @Test
  public void nextMethodWithElipises() {
    
    eval("x <- 1:10 ");
    eval("class(x) <- 'foo' ");
    
    eval("`[.foo` <- function(...) NextMethod() ");
    
    assertThat( eval("x[9]"), equalTo(c_i(9)));
    
    
  }

}

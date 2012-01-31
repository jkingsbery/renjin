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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static r.lang.Logical.FALSE;
import static r.lang.Logical.TRUE;

import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;

import r.EvalTestCase;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.Logical;
import r.lang.Null;
import r.lang.PairList;
import r.lang.Raw;
import r.lang.RawVector;
import r.lang.SEXP;
import r.lang.StringVector;

public strictfp class TypesTest extends EvalTestCase {

  @Test
  public void asCharacter() {
    assertThat( eval("as.character(1)"), equalTo( c("1") ));
    assertThat( eval("as.character(\"foobar\")"), equalTo( c("foobar") ));
    assertThat( eval("as.character(1L)"), equalTo( c("1") ));
    assertThat( eval("as.character(1.3333333333333333333333333333333333)"),
        equalTo(c("1.33333333333333")));
    assertThat( eval("as.character(TRUE)"), equalTo( c("TRUE") ));
  }

  @Test
  public void asCharacterWithNA() {
    assertThat( eval("as.character(NA)"), equalTo( c( StringVector.NA )) );
  }

  @Test
  public void asCharacterFromStringObject(){ 
    eval("import(java.lang.String)");
    eval("x<-String$new(\"foo\")");
    assertThat(eval("as.character(x)"),equalTo(c("foo")));
  }
  
  @Test
  public void asCharacterFromList() {
    assertThat( eval("as.character(list(3, 'a', TRUE)) "), equalTo( c("3", "a", "TRUE" )));
    assertThat( eval("as.character(list(c(1,3), 'a', TRUE)) "), equalTo( c("c(1, 3)", "a", "TRUE" )));
  }

  @Test
  public void asCharacterFromSymbol() {
    assertThat( eval(" as.character(quote(x)) "), equalTo( c("x") ));
  }

  @Test
  public void asCharacterFromNull() {
    eval( " x<- NULL");
    eval( " g<-function(b) b");
    eval( " f<-function(a) g(as.character(a)) ");
    assertThat( eval("f(x)"), equalTo((SEXP)new StringVector()));
  }

  
  @Test
  public void asDoubleFromDoubleObject(){ 
    eval("import(java.lang.Double)");
    eval("x<-Double$new(1.5)");
    assertThat(eval("as.double(x)"),equalTo(c(1.5)));
  }
  
  @Test
  public void asDoubleFromDouble() {
    assertThat( eval("as.double(3.14)"), equalTo( c(3.14) ) );
    assertThat( eval("as.double(NA_real_)"), equalTo( c(DoubleVector.NA) ) );
  }

  @Test
  public void asDoubleFromInt() {
    assertThat( eval("as.double(3L)"), equalTo( c(3l) ));
  }

  @Test
  public void asLogicalFromBooleanObject(){ 
    eval("import(java.lang.Boolean)");
    eval("x<-Boolean$new(TRUE)");
    assertThat(eval("as.logical(x)"),equalTo(c(TRUE)));
  }
  
  @Test
  public void asLogicalFromList() {
    assertThat( eval("as.logical(list(1, 99.4, 0, 0L, FALSE, 'TRUE', 'FOO', 'T', 'F', 'FALSE')) "),
        equalTo( c(TRUE, TRUE, FALSE, FALSE, FALSE, TRUE, Logical.NA, TRUE, FALSE, FALSE) ));
  }

  @Test
  public void asLogical() {
    assertThat( eval("as.logical(c(1, 99.4, 0, NA_real_)) "),
        equalTo( c(TRUE, TRUE, FALSE, Logical.NA) ));
  }

  @Test
  public void asDoubleFromLogical() {
    assertThat( eval("as.double(TRUE)"), equalTo( c(1d) ));
    assertThat( eval("as.double(FALSE)"), equalTo( c(0d) ));
  }

  @Test
  public void asDoubleFromString() {
    assertThat( eval("as.double(\"42\")"), equalTo( c(42d) ));
    assertThat( eval("as.double(\"not an integer\")"), equalTo( c(DoubleVector.NA) ));
  }

  @Test
  public void asIntFromIntegerObject(){ 
    eval("import(java.lang.Integer)");
    eval("x<-Integer$new(2)");
    assertThat(eval("as.integer(x)"),equalTo(c_i(2)));
  }
  
  @Test
  public void asIntFromDouble() {
    assertThat( eval("as.integer(3.1)"), equalTo( c_i( 3 )));
    assertThat( eval("as.integer(3.9)"), equalTo( c_i( 3 )));
    assertThat( eval("as.integer(NA_real_)"), equalTo( c_i( IntVector.NA )));
  }

  @Test
  public void asIntFromRecycledDouble() {
    assertThat( eval("as.integer(c(1, 9.32, 9.9, 5.0))"), equalTo( c_i(1, 9, 9, 5 )));
  }

  @Test
  public void attributesSetting() {
    eval( " v <- .Internal(Version())");
    eval( " attributes(v) <- c( class='simpleList', attributes(v)) ");

    assertThat( eval("v$minor"), not(CoreMatchers.equalTo(NULL)));
  }
  
  @Test
  public void attributeReplace() {
    eval( "x <- 1:12");
    eval( "dim(x) <- c(3,4)");
    eval( "dim(x) <- c(3,4,1)");
    
    assertThat( eval("dim(x)"), equalTo(c_i(3,4,1)));
  }


  @Test
  public void na() {
    assertThat( eval(" is.na(TRUE) "), equalTo( c(FALSE)));
    assertThat( eval(" is.na(NA) "), equalTo( c(TRUE)));
    assertThat( eval(" is.na(c(1L, NA_integer_)) "), equalTo( c(FALSE, TRUE)));
    assertThat( eval(" is.na(c(NA_character_, '', 'foo')) "), equalTo( c(TRUE, FALSE, FALSE)));
  }

  @Test
  public void naList() {
    assertThat( eval(" is.na(list(NULL,  1,     FALSE, c(NA,4), NA_integer_, NA_real_)) "),
                       equalTo( c(FALSE, FALSE, FALSE, FALSE,   TRUE,        TRUE)) );
  }

  @Test
  public void naPreservesNames() {
    assertThat( eval(" names(is.na(c(x=1,y=2))) "), equalTo( c("x", "y")));
  }
  
//  @Test
//  public void unaryOpPreservesAllAttributes() {
//    eval("x <- 1:9");
//    eval("attr(x,'foo') <- 'bar' ");
//    assertThat( eval("attr(x, 'foo')"), equalTo()
//  }


  @Test
  public void naPreservesDimNames() {
    eval( " x <- .Internal(rbind(1, c(a=1,b=2))) ");
    eval( " x <- is.na(x) ");
    assertThat( eval(" dimnames(x)[[2]] "), equalTo( c("a", "b")));

    eval(" x <- !x ");
    assertThat( eval(" dimnames(x)[[2]] "), equalTo( c("a", "b")));
  }
  
  @Test
  public void nullDimNamePreservedOnAssignment() {
    eval(" x <- 1:12");
    eval(" dim(x) <- c(3,4) ");
    eval(" dimnames(x) <- list(NULL, c('a','b','c','d'))");
    
    assertThat(eval("dimnames(x)[[1]]"), equalTo(NULL));
  }


  @Test
  public void unaryPreservesNames() {
    assertThat( eval(" names(!is.na(c(x=1,y=2)))"), equalTo( c("x", "y")));
  }

  @Test
  public void vector() {
    assertThat( eval(" .Internal(vector('list', 3)) "), equalTo( list(NULL, NULL, NULL)));
    assertThat( eval(" .Internal(vector('numeric', 2)) "), equalTo( c(0, 0)));
    assertThat( eval(" .Internal(vector('character', 3)) "), equalTo( c("","","")) );
    assertThat( eval(" .Internal(vector('logical', 2)) "), equalTo( c(FALSE, FALSE)) );
  }

  @Test
  public void environment() {
    assertThat( eval(".Internal(environment())"), CoreMatchers.is((SEXP) topLevelContext.getGlobalEnvironment()));
  }
  
  @Test
  public void env2list() {
    eval(" env <- .Internal(new.env(TRUE, globalenv(), 29L))");
    eval(" env$a <- 1");
    eval(" env$.a <- 2");
    eval(" x <- .Internal(env2list(env,FALSE))");
    eval(" y <- .Internal(env2list(env,TRUE))");

    assertThat( eval("names(x)"), CoreMatchers.equalTo(c("a")));
    assertThat( eval("names(y)"), CoreMatchers.equalTo(c("a",".a")));
  }
  
  @Test
  public void env2list_hiddenFirst() {
    eval(" env <- .Internal(new.env(TRUE, globalenv(), 29L))");
    eval(" env$.a <- 1");
    eval(" env$a <- 2");
    eval(" x <- .Internal(env2list(env,FALSE))");
    eval(" y <- .Internal(env2list(env,TRUE))");

    assertThat( eval("names(x)"), CoreMatchers.equalTo(c("a")));
    assertThat( eval("names(y)"), CoreMatchers.equalTo(c("a",".a")));
  }
  
  @Test
  public void env2list_multipleNonHidden() {
    eval(" env <- .Internal(new.env(TRUE, globalenv(), 29L))");
    eval(" env$a <- 1");
    eval(" env$b <- 2");
    eval(" x <- .Internal(env2list(env,FALSE))");
    eval(" y <- .Internal(env2list(env,TRUE))");

    assertThat( eval("names(x)"), CoreMatchers.equalTo(c("a","b")));
    assertThat( eval("names(y)"), CoreMatchers.equalTo(c("a","b")));
  }
  
  @Test
  public void environmentName() {
    assertThat( eval(".Internal(environmentName(baseenv()))"), CoreMatchers.equalTo(c("base")));
    assertThat( eval(".Internal(environmentName(globalenv()))"), CoreMatchers.equalTo(c("R_GlobalEnv")));
  }

  @Test
  public void environmentOfRandomExp() {
    assertThat( eval(".Internal(environment(1))"), is((SEXP) Null.INSTANCE));
  }

  @Test
  public void environmentOfClosure() {
    eval("f <- function() { 1 } ");
    assertThat( eval(".Internal(environment( f ))"), is((SEXP) topLevelContext.getGlobalEnvironment()));
  }

  @Test
  public void list() {
    assertThat( eval("list(\"a\")"), equalTo( list("a") ));
  }

  @Test
  public void listOfNulls() {
    assertThat( eval("list(NULL, NULL)"), equalTo( list(NULL, NULL) ));
  }

  @Test
  public void listOfNull() {
    assertThat( eval("list(NULL)"), equalTo( list(NULL) ));
  }
  
  @Test
  public void closureBody() {
    eval(" f <- function(x) sqrt(x) ");
    
    assertThat( eval(" .Internal(body(f))[[1]] "), CoreMatchers.equalTo(symbol("sqrt")));
  }

  @Test
  public void setClassWithAttrFunction() {
    eval(" x<-c(1,2,3) ");
    eval(" attr(x, 'class') <- 'foo' ");

    assertThat( eval(" class(x) "), equalTo( c("foo")));
  }
  
  @Test
  public void atomicVectorsHaveImplicitClasses() {
    assertThat( eval("class(9)"), equalTo(c("numeric")));
    assertThat( eval("class(9L)"), equalTo(c("integer")));
    assertThat( eval("class('foo')"), equalTo(c("character")));
    assertThat( eval("class(TRUE)"), equalTo(c("logical")));
    assertThat( eval("class(NULL)"), equalTo(c("NULL")));
  }
  
  @Test
  @Ignore("to implement")
  public void someSpecialFunctionsHaveTheirOwnImplicitClass() {
    assertThat( eval("class(quote({1}))"), equalTo(c("{")));
    assertThat( eval("class(quote(if(TRUE) 1 else 0))"), equalTo(c("if")));
    assertThat( eval("class(quote(while(TRUE) 1))"), equalTo(c("while")));
    assertThat( eval("class(quote(for(x in 1:9) x))"), equalTo(c("for")));
 //   assertThat( eval("class(quote(x=1)"), equalTo(c("=")));
    assertThat( eval("class(quote(x<-1)"), equalTo(c("<-")));
    assertThat( eval("class(quote((1+1))"), equalTo(c("(")));
  }
  
  @Test
  public void implicitClassesAreOverridenByClassAttribute() {
    eval("m <- 1:12");
    eval("dim(m) <- c(3,4)");
    eval("class(m) <- c('foo','bar')");
    assertThat( eval("class(m)"), equalTo(c("foo", "bar")));        
  }

  @Test
  public void matricesHaveImplicitClass() {
    eval("m <- 1:12");
    eval("dim(m) <- c(3,4)");
    assertThat( eval("class(m)"), equalTo(c("matrix")));    
  }
  
  @Test
  public void matricesAreNotObjects() {
    eval("m <- 1:12");
    eval("dim(m) <- c(3,4)");
    assertThat( eval("is.object(m)"), equalTo(c(false)));
  }
  
  @Test
  public void arraysHaveImplicitClass() {
    eval("a <- 1:12");
    eval("dim(a) <- 12");
    assertThat( eval("class(a)"), equalTo(c("array")));
  }
  
  
  
  @Test
  public void unclass() {
    eval("x<-1");
    eval("class(x) <- 'foo'");
    eval("x <- unclass(x)");
    assertThat(eval("class(x)"), equalTo(c("numeric")));
  }
  
  @Test
  public void unclassPreservesOtherAttribs() {
    eval("x<-1");
    eval("attr(x,'zing')<-'bat'");
    eval("class(x) <- 'foo'");
    eval("x <- unclass(x)");
    assertThat(eval("class(x)"), equalTo(c("numeric")));
    assertThat(eval("attr(x,'zing')"), equalTo(c("bat")));

  }

  @Test
  public void setNamesWithNonStrVector() {
    eval(" x<-c(1,2,3) ");
    eval(" names(x) <- c(4,5,6) ");

    assertThat( eval("names(x)"), equalTo( c("4", "5","6")));
  }

  @Test
  public void setNamesWithNonVector() {
    eval(" x<-c(1,2,3) ");
    eval(" names(x) <- quote(quote(z)) ");

    assertThat( eval("names(x)"), equalTo( c("z", StringVector.NA, StringVector.NA)));
  }

  @Test
  public void setAttributes() {
    eval(" x <- 1:5");
    eval(" attributes(x) <- list(names=c('a','b', 'c'), foo='bar') ");

    assertThat( eval(" names(x) "), equalTo(c("a","b","c",StringVector.NA,StringVector.NA)));
    assertThat( eval(" attr(x, 'foo') "), equalTo( c("bar")));

  }

  @Test
  public void asEnvironment() {
    assertThat( eval("as.environment(1)"), sameInstance((SEXP)topLevelContext.getGlobalEnvironment()));
    assertThat( eval("as.environment(2)"), sameInstance((SEXP)topLevelContext.getGlobalEnvironment().getParent()));
  }

  @Test
  public void asVector() {
    eval(" as.vector <- function (x, mode = 'any') .Internal(as.vector(x, mode)) ");

    assertThat( eval("as.vector(1, 'character')"), equalTo( c("1" )));
    assertThat( eval("as.vector(c(4,5,0), mode='logical')"), equalTo( c(true, true, false)));
    assertThat( eval("as.vector(c(TRUE,FALSE,NA), mode='double')"), equalTo( c(1.0,0,DoubleVector.NA)));
}


  @Test
  public void asPairList() {
    eval(" as.vector <- function (x, mode = 'any') .Internal(as.vector(x, mode)) ");
    eval(" x <- as.vector( c(a=1,b=2), mode = 'pairlist') ");

    PairList.Node head = (PairList.Node) global.getVariable("x");
    assertThat( head.length(), equalTo(2));
    assertThat( head.getNode(0).getTag(), equalTo( symbol("a")));
    assertThat( head.getElementAsSEXP(0), equalTo( c(1) ));
    assertThat(head.getNode(1).getTag(), equalTo( symbol("b") ));
    assertThat( head.getElementAsSEXP(1), equalTo( c(2) ));
  }

  @Test
  public void options() {
    eval(" .Internal(options(foo=TRUE)) ");
  }

  @Test
  public void pairListToList() {

    eval(" x <- .Internal(as.vector(list(a=41, b=42), 'pairlist')) ");
    eval(" y <- .Internal(as.vector(x, 'list')) ");

    assertThat( eval("y"), equalTo( list(41d,42d)));
    assertThat( eval("names(x)"), equalTo( c("a", "b")));
    assertThat( eval(".Internal(typeof(x))"), equalTo( c("pairlist")));
    assertThat( eval("names(y)"), equalTo( c("a", "b")));
  }

  @Test
  public void functionCallToList() {

    eval(" x <- quote(~(0+births)) ");
    eval(" y <- .Internal(as.vector(x, 'list')) ");

    assertThat( eval("length(y)"), equalTo( c_i(2)));
    assertThat( eval("names(y)"), equalTo(  NULL ));
    assertThat( eval(".Internal(typeof(y[[2]]))"), equalTo( c("language")));
  }
  
  @Test
  public void isRawAndAsRaw(){
    Raw r1 = new Raw(1);
    Raw r2 = new Raw(20);
    Raw r3 = new Raw(30);
    assertThat( eval("is.raw(as.raw(c(123,124)))"), equalTo(c(Logical.TRUE)));
    assertThat( eval("as.raw(c(1,20,30))"), equalTo(c(r1,r2,r3)));
  }
  
  @Test
  public void rawToBits(){
    Raw r0 = new Raw(00);
    Raw r1 = new Raw(01);
    assertThat( eval(".Internal(rawToBits(as.raw(c(1,2))))"), equalTo(c(r0,r0,r0,r0,r0,r0,r0,r1,r0,r0,r0,r0,r0,r0,r1,r0)));
  }
  
  @Test
  public void charToRaw(){
    Raw r1 = new Raw('A');
    Raw r2 = new Raw('B');
    Raw r3 = new Raw('C');
    assertThat( eval(".Internal(charToRaw(\"ABC\"))"), equalTo(c(r1,r2,r3)));
  }
  
  @Test
  public void rawShift() {
    Raw r1 = new Raw(0x3a);Raw r2 = new Raw(0x3c);Raw r3 = new Raw(0x3e);
    assertThat(eval(".Internal(rawShift(as.raw(c(29:31)),1))"), equalTo(c(r1, r2, r3)));
    
    //r1 = new Raw(0x0e);r2 = new Raw(0x0f);r3 = new Raw(0x0f);
    //assertThat(eval(".Internal(rawShift(as.raw(c(29:31)),-1))"), equalTo(c(r1, r2, r3)));
  }
  
  @Test
  public void intToBits(){
    RawVector.Builder b = new RawVector.Builder();
    b.add(new Raw(01));
    for (int i=1;i<32;i++) {
      b.add(new Raw(0));
    }
    RawVector rv = b.build();
    assertThat(eval(".Internal(intToBits(1))"), equalTo(c(rv.getAsRawArray())));
  }

  @Test
  public void isNaGeneric() {
    
    eval("x<-1");
    eval("class(x) <- 'foo'");
    
    eval("is.na.foo <- function(x) 'FOO!!'");
    assertThat(eval("is.na(x)"), equalTo(c("FOO!!")));
  }
  
}

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

import org.junit.Ignore;
import org.junit.Test;
import r.EvalTestCase;

import r.lang.StringVector;
import r.lang.exception.EvalException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SystemTest extends EvalTestCase {

  @Test
  public void glob() {
//    java.lang.System.out.println(System.glob(topLevelContext, c("res:r"), false));
//    java.lang.System.out.println( System.glob( topLevelContext, c("c:\\*") , true) );
//    java.lang.System.out.println( System.glob( topLevelContext, c("c:\\.*") , true) );
  }

  @Ignore("platform dependent")
  @Test
  public void listFiles() {
    eval(" list.files <- function (path = '.'," +
        "pattern = NULL, all.files = FALSE, full.names = FALSE, " +
        "recursive = FALSE, ignore.case = FALSE) " +
          ".Internal(list.files(path, pattern, all.files, full.names, recursive, ignore.case))");

    assertThat( eval("list.files('classpath:/afolder')"), equalTo( c("file1.ext", "second.file")));
    assertThat( eval("list.files('classpath:/afolder', all.files=TRUE)"), equalTo( c(".", "..", ".secret", "file1.ext", "second.file")));
    assertThat( eval("list.files('classpath:/afolder', all.files=TRUE, full.names=TRUE)"),
        equalTo( c(fullPathPlus("."), fullPathPlus(".."), fullPath(".secret"), fullPath("file1.ext"), fullPath("second.file"))));

    assertThat( eval("list.files('classpath:/r/library', pattern='^survey$')"), equalTo( c("survey")) );
  }

  private String fullPath(String name) {
    return getClass().getResource("/afolder/" + name)
        .getFile().toString().replace("\\", "/");
  }

  private String fullPathPlus(String name) {
    return fullPath("") + name;
  }

  @Test
  public void dirname() {
    assertThat( eval(" .Internal(dirname(c('c:\\\\anyfolder\\\\file.txt', '/bin/bash', 'myfile'))) "),
        equalTo( c("c:\\anyfolder", "/bin", ".") ));
  }

  @Test
  public void basename() {
    assertThat( eval(" .Internal(basename(c('c:\\\\anyfolder\\\\file.txt', '/bin/bash', 'myfile'))) "),
        equalTo( c("file.txt", "bash", "myfile") ));
  }
  
   @Test
   public void date(){
    //assertThat( eval(" .Internal(date()) "), equalTo( c("Fri Sep 19 12:20:00 2011") ));
   }
   
   @Test
   public void SysSleep(){
     double delta = 100;
     try{
       topLevelContext.init();
     }catch(Exception e){
       
     }
     long start = java.lang.System.currentTimeMillis();
     eval("Sys.sleep(1)");
     long stop = java.lang.System.currentTimeMillis();
     assertThat((double)(stop-start), closeTo(1000.0, delta));
   }
   
   
}

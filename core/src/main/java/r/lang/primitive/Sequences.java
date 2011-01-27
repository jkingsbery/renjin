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

package r.lang.primitive;

import com.google.common.annotations.VisibleForTesting;
import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.Indices;
import r.lang.primitive.annotations.Primitive;


/**
 * Creates a sequential vector from an expression like "1:99"
 */
public class Sequences {

  public static SEXP colon(SEXP n1, SEXP n2) {
    if( n1.inherits("factor") && n2.inherits("factor")) {
      return crossColon(n1, n2);

    } else {
      return colonSequence(n1, n2);
    }
  }

  private static SEXP crossColon(SEXP n1, SEXP n2) {
    throw new UnsupportedOperationException("crossColon not yet implemented");
  }

  public static SEXP colonSequence(SEXP s1, SEXP s2 ) {
    checkArg(s1);
    checkArg(s2);

    double n1 = s1.asReal();
    double n2 = s2.asReal();

    checkValue(n1);
    checkValue(n2);

    return new Range(n1, n2).vector();
  }


  private static void checkValue(double r1) {
    if(DoubleVector.isNaN(r1)) {
      throw new EvalException("NA/NaN argument");
    }
  }

  private static void checkArg(SEXP exp) {
    if(exp.length() == 0) {
      throw new EvalException("argument of length 0");
    } else if(exp.length() > 1) {
      Warning.warning("numerical expression has %d elements: only the first used", exp.length());
    }
  }

  @Primitive("rep.int")
  public static Vector repeatInt(Vector x, @Indices int times) {
    EvalException.check(times >= 0, "invalid 'times' value");

    Vector.Builder result = x.newBuilder(x.length() * times);
    int count = 0;
    while(times > 0) {
      for(int i =0; i!=x.length();++i) {
        result.setFrom(count++, x, i);
      }
      times--;
    }
    return result.build();
  }

  @VisibleForTesting
  static class Range {
    boolean useInteger;
    private double range;
    double count;
    final double n1;
    final double n2;

    public Range(double n1, double n2) {
      this.n1 = n1;
      this.n2 = n2;
      range = Math.abs(n2 - n1);
      count = range + 1 + DoubleVector.EPSILON;

      determineType();
    }

    private void determineType() {
      int in1 = (int)(n1);
      useInteger = (n1 == in1);
      if(useInteger) {
        if(n1 <= Integer.MIN_VALUE || n1 > Integer.MAX_VALUE) {
          useInteger = false;
        } else {
          /* r := " the effective 'to' "  of  from:to */
          double upperBound = n1 + ((n1 <= n2) ? count -1 : -(count -1));
          if(upperBound <= Integer.MIN_VALUE || upperBound > Integer.MAX_VALUE)
            useInteger = false;
        }
      }
    }

    public SEXP vector() {
      return useInteger ? intVector() : realVector();
    }

    private SEXP intVector() {
      int values[] = new int[(int) count];
      int index = 0;

      if(n1 <= n2) {
        for(int n=(int)n1; n<=n2; n++) {
          values[index++] = n;
        }
      } else {
        for(int n=(int)n1; n>=n2; n--) {
          values[index++] = n;
        }
      }
      return new IntVector(values);
    }

    private SEXP realVector() {
      double[] values = new double[(int) count];
      int index = 0;

      if(n1 <= n2) {
        for(double n=n1; n<=n2; n+=1d) {
          values[index++] = n;
        }
      } else {
        for(double n=n1; n>=n2; n-=1d) {
          values[index++] = n;
        }
      }
      return new DoubleVector(values);
    }
  }

  public static int[] seqAlong(SEXP exp) {
    int indexes[] = new int[exp.length()];
    for(int i=0;i!=indexes.length;++i) {
      indexes[i] = i+1;
    }
    return indexes;
  }
}

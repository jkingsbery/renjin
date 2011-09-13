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

import java.text.ParseException;
import static org.netlib.lapack.Dgesdd.dgesdd;

import org.netlib.lapack.LAPACK;
import org.netlib.util.doubleW;
import org.netlib.util.intW;

import r.jvmi.annotations.Current;
import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.Vector;
import r.lang.exception.EvalException;

/**
 * Implementation of routines from the base dll.
 * 
 * <p>The functions implemented here are distinct from the collection of
 * primitives which make up most of the base package; these functions are
 * called from R code using the .Call("methodname", arg1, arg2, ... argn, PACKAGE="base") syntax.
 * 
 * <p>Also note that many methods use a convention whereby the values to be 
 * returned are allocated by the calling R code to save the C implementors the
 * trouble of dealing with memory management (I think), so many of these methods 
 * take what are, in renjin's context, unused arguments. 
 * 
 */
public class Base {

  private Base() { }

  public static boolean R_isMethodsDispatchOn(@Current Context context) {
    return false;
  }

  public static ListVector R_getSymbolInfo(String sname, SEXP spackage, boolean withRegistrationInfo) {

    ListVector.Builder result = new ListVector.Builder();
    result.setAttribute(Symbol.CLASS, new StringVector("CRoutine"));

    return result.build();

  }

  public static ListVector R_getRegisteredRoutines(String dll) {
    ListVector.Builder builder = new ListVector.Builder();
    return builder.build();
  }

  /** @return  n if the data frame 'vec' has c(NA, n) rownames;
   *         nrow(.) otherwise;  note that data frames with nrow(.) == 0
   *          have no row.names.
   * ==> is also used in dim.data.frame()
   *
   * AB Note: I have no idea what this function really does but it is
   * so opaque that it is first against the wall when the revolution comes.
   */
  public static SEXP R_shortRowNames(SEXP vector, int type) {
    SEXP s =  vector.getAttribute(Symbol.ROW_NAMES);
    SEXP ans = s;

    if( type < 0 || type > 2) {
      throw new EvalException("invalid 'type' argument");
    }

    if(type >= 1) {
      int n;
      if (s instanceof IntVector && s.length() == 2 && ((IntVector) s).isElementNA(0)) {
        n = ((IntVector) s).getElementAsInt(1);
      } else {
        if (s == Null.INSTANCE) {
          n = 0;
        } else {
          n = s.length();
        }
      }
      if (type == 1) {
        ans = new IntVector(n);
      } else {
        ans = new IntVector(Math.abs(n));
      }
    }
    return ans;
  }
  
  /**
   * 
   * "native" implementation called by tabulate(), which 
   * takes the integer-valued vector bin and counts the number of times each integer occurs in it.
   * 
   * <p>There is a bin for each of the values 1, ..., nbins; values outside that range 
   * and NAs are (silently) ignored.
   * 
   * @param bin the integer vector to bin
   * @param length the length of bin
   * @param nbins the number of bins
   * @param ans not used
   * @return 
   */
  public static PairList R_tabulate(IntVector bin, int length, int nbins, SEXP ans) {
    int counts[] = new int[nbins];
    for(int i=0;i!=length;++i) {
      if(!bin.isElementNA(i)) {
        int value = bin.getElementAsInt(i);
        if(value >= 1 && value <= nbins) {
          counts[value-1]++;
        }
      }
    }
    return PairList.Node.singleton("ans", new IntVector(counts));
  }
  
  public static SEXP Rrowsum_df(ListVector x, int ncol, Vector group, SEXP ugroup, boolean naRm) {
    throw new EvalException("nyi");
  }

  /**
   * Singular Value Decomposition implemented with Jlapack, a mechanical Fortran-to-java translation 
   * of the same lapack library that R uses.
   * 
   */
  public static SEXP La_svd(String jobu, String jobv, DoubleVector x, DoubleVector  sexp,
      DoubleVector uexp, DoubleVector vexp, String method ) {
    
    IntVector xdims = (IntVector) x.getAttribute(Symbol.DIM);
    int n = xdims.getElementAsInt(0);
    int p = xdims.getElementAsInt(1);

    double xvals[] = x.toDoubleArray();
    
    int ldu =  ((IntVector)uexp.getAttribute(Symbol.DIM)).getElementAsInt(0);
    int ldvt = ((IntVector)vexp.getAttribute(Symbol.DIM)).getElementAsInt(0);
    
    double s[] = sexp.toDoubleArray();
    double u[] = uexp.toDoubleArray();
    double v[] = vexp.toDoubleArray();
    double tmp[] = new double[1];
    
    int iwork[] = new int[8*(n<p ? n : p)];

    LAPACK lapack = LAPACK.getInstance();
    
    /* ask for optimal size of work array */
    int lwork = -1;
    intW info = new intW(0);
    lapack.dgesdd(jobu, n, p, xvals, n,  s, u, ldu, v, ldvt, tmp, lwork, iwork, info); 
    
    if (info.val != 0) {
      throw new EvalException("error code %d from Lapack routine '%s'", info.val, "dgesdd");
    }
     
    lwork = (int) tmp[0];
    double work[] = new double[lwork];
    
    lapack.dgesdd(jobu, n, p, xvals, n, s, u, ldu, v, ldvt, work, lwork, iwork, info);
    
    ListVector.Builder val = new ListVector.Builder();
    val.add("d", new DoubleVector(s, sexp.getAttributes()));
    val.add("u", new DoubleVector(u, uexp.getAttributes()));
    val.add("vt", new DoubleVector(v, vexp.getAttributes()));
    
    return val.build();
  }
 
  // nicer API but output does not match R-2.10's result at all--
  // do i misunderstand something??
//  public static SEXP La_svd(String jobu, String jobv, DoubleVector x, DoubleVector  sexp,
//      DoubleVector uexp, DoubleVector vexp, String method ) {
//    
//    SingularValueDecomposition svd = new SingularValueDecompositionImpl(CommonsMath.asRealMatrix(x));
//        
//    ListVector.Builder val = new ListVector.Builder();
//    val.add("d", new DoubleVector(svd.getSingularValues()));
//    val.add("u", CommonsMath.asDoubleVector(svd.getU()));
//    val.add("vt", CommonsMath.asDoubleVector(svd.getVT()));
//    
//    return val.build();
//  }
  
  
  
  /*
   * Converted directly from the C code, src/modules/lapack/Lapack.c
   */
  public static SEXP La_dgesv(DoubleVector A, DoubleVector Bin, DoubleVector tolin) {
    int n, p;
    IntVector ipiv, Adims, Bdims;
    IntVector.Builder ipivBuilder;
    DoubleVector avals;
    DoubleVector.Builder avalsBuilder;
    org.netlib.util.intW info;

    double anorm;
    doubleW rcond = new doubleW(0);

    DoubleVector tol = tolin, work;
    DoubleVector B;
    double[] Bcontent;

    if (!(Types.isMatrix(A) && Types.isDouble(A))) {
      throw new EvalException("'a' must be a numeric matrix");
    }
    if (!(Types.isMatrix(Bin) && Types.isDouble(Bin))) {
      throw new EvalException("'b' must be a numeric matrix");
    }
    //PROTECT(B = duplicate(Bin));
    DoubleVector.Builder bu = new DoubleVector.Builder();
    for (int i = 0; i < Bin.length(); i++) {
      bu.add(Bin.get(i));
    }    
    bu.copyAttributesFrom(Bin);
    B = bu.build();
      

    //Adims = INTEGER(coerceVector(getAttrib(A, R_DimSymbol), INTSXP));
    //Bdims = INTEGER(coerceVector(getAttrib(B, R_DimSymbol), INTSXP));
    Adims = (IntVector) A.getAttribute(Symbol.DIM);
    Bdims = (IntVector) B.getAttribute(Symbol.DIM);

    n = Adims.getElementAsInt(0);
    if (n == 0) {
      throw new EvalException("'a' is 0-diml");
    }
    p = Bdims.getElementAsInt(1);
    if (p == 0) {
      throw new EvalException("no right-hand side in 'b'");
    }
    if (Adims.getElementAsInt(1) != n) {
      throw new EvalException("'a' (" + n + " x " + Adims.getElementAsInt(1) + ") must be square");
    }
    if (Bdims.getElementAsInt(0) != n) {
      throw new EvalException("'b' (" + Bdims.getElementAsInt(0) + " x " + p + ") must be compatible with 'a' (" + n + " x " + n + ")");
    }

    //ipiv = new IntVector(); //Will be size of n
    ipivBuilder = new IntVector.Builder();
    for (int i = 0; i < n; i++) {
      ipivBuilder.add(0);
    }
    ipiv = ipivBuilder.build();

    //avals = (double *) R_alloc(n * n, sizeof(double));
    avalsBuilder = new DoubleVector.Builder();
    //Memcpy(avals, REAL(A), n * n);
    for (int i = 0; i < A.length(); i++) {
      avalsBuilder.add(A.get(i));
    }
    avalsBuilder.copyAttributesFrom(A);
    avals = avalsBuilder.build();

    //F77_CALL(dgesv)(&n, &p, avals, &n, ipiv, REAL(B), &n, &info);
    LAPACK lapack = LAPACK.getInstance();
    info = new intW(0);
    Bcontent = B.toDoubleArray();
    lapack.dgesv(n, p, avals.toDoubleArray(), n, ipiv.toIntArray(), Bcontent, n, info);

    if (info.val < 0) {
      throw new EvalException("argument -" + info.val + " of Lapack routine 'dgsv' had invalid value");
    }
    if (info.val > 0) {
      throw new EvalException("Lapack routine dgesv: system is exactly singular");
    }

    //anorm = F77_CALL(dlange)("1", &n, &n, REAL(A), &n, (double*) NULL);
    anorm = lapack.dlange("1", n, n, A.toDoubleArray(), n, null);
    //work = (double *) R_alloc(4*n, sizeof(double));
    double[] arrWork = new double[4 * n];
    //F77_CALL(dgecon)("1", &n, avals, &n, &anorm, &rcond, work, ipiv, &info);

    lapack.dgecon("1", n, avals.toDoubleArray(), n, anorm, rcond, arrWork, ipiv.toIntArray(), info);

    if (rcond.val < tol.get(0)) {
      throw new EvalException("system is computationally singular: reciprocal condition number = " + rcond.val);
    }
    return new DoubleVector(Bcontent);
  }
  
}

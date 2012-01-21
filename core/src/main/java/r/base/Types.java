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

import static r.lang.CollectionUtils.modePredicate;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.complex.Complex;

import r.jvmi.annotations.ArgumentList;
import r.jvmi.annotations.Current;
import r.jvmi.annotations.Generic;
import r.jvmi.annotations.InvokeAsCharacter;
import r.jvmi.annotations.Primitive;
import r.jvmi.annotations.Recycle;
import r.jvmi.annotations.Visible;
import r.jvmi.r2j.ClassFrame;
import r.jvmi.r2j.ObjectFrame;
import r.jvmi.r2j.converters.BooleanArrayConverter;
import r.jvmi.r2j.converters.BooleanConverter;
import r.jvmi.r2j.converters.DoubleArrayConverter;
import r.jvmi.r2j.converters.DoubleConverter;
import r.jvmi.r2j.converters.IntegerArrayConverter;
import r.jvmi.r2j.converters.IntegerConverter;
import r.jvmi.r2j.converters.StringArrayConverter;
import r.jvmi.r2j.converters.StringConverter;
import r.lang.AtomicVector;
import r.lang.Attributes;
import r.lang.Closure;
import r.lang.ComplexVector;
import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.Environment;
import r.lang.ExpressionVector;
import r.lang.Frame;
import r.lang.Function;
import r.lang.FunctionCall;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.LogicalVector;
import r.lang.NamedValue;
import r.lang.Null;
import r.lang.PairList;
import r.lang.Raw;
import r.lang.RawVector;
import r.lang.Recursive;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.Symbols;
import r.lang.Vector;
import r.lang.exception.EvalException;
import r.util.NamesBuilder;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Primitive type inspection and coercion functions
 */
public class Types {

  public static boolean isNull(SEXP exp) {
    return exp == Null.INSTANCE;
  }

  public static boolean isLogical(SEXP exp) {
    return exp instanceof LogicalVector;
  }

  public static boolean isInteger(SEXP exp) {
    return exp instanceof IntVector;
  }

  public static boolean isReal(SEXP exp) {
    return exp instanceof DoubleVector;
  }

  public static boolean isDouble(SEXP exp) {
    return exp instanceof DoubleVector;
  }

  public static boolean isComplex(SEXP exp) {
    return exp instanceof ComplexVector;
  }

  @Generic
  public static boolean isCharacter(SEXP exp) {
    return exp instanceof StringVector;
  }

  public static boolean isSymbol(SEXP exp) {
    return exp instanceof Symbol;
  }

  public static boolean isEnvironment(SEXP exp) {
    return exp instanceof Environment;
  }

  public static boolean isExpression(SEXP exp) {
    return exp instanceof Environment;
  }

  public static boolean isList(SEXP exp) {
    return exp instanceof ListVector || exp.getClass() == PairList.Node.class;
  }

  public static boolean isPairList(SEXP exp) {
    return exp instanceof PairList;
  }

  public static boolean isAtomic(SEXP exp) {
    return exp instanceof AtomicVector;
  }

  public static boolean isRecursive(SEXP exp) {
    return exp instanceof Recursive;
  }

  public static boolean isNumeric(SEXP exp) {
    return (exp instanceof IntVector && !exp.inherits("factor"))
        || exp instanceof LogicalVector || exp instanceof DoubleVector;
  }

  @Primitive("is.matrix")
  public static boolean isMatrix(SEXP exp) {
    return exp.getAttribute(Symbols.DIM).length() == 2;
  }

  @Primitive("is.array")
  public static boolean isArray(SEXP exp) {
    return exp.getAttribute(Symbols.DIM).length() > 0;
  }

  @Primitive("is.vector")
  public static boolean isVector(SEXP exp, String mode) {
    // first check for any attribute besides names
    for (PairList.Node node : exp.getAttributes().nodes()) {
      if (!node.getTag().equals(Symbols.NAMES)) {
        return false;
      }
    }

    // otherwise check
    if ("logical".equals(mode)) {
      return exp instanceof LogicalVector;
    } else if ("integer".equals(mode)) {
      return exp instanceof IntVector;
    } else if ("numeric".equals(mode)) {
      return exp instanceof DoubleVector;
    } else if ("complex".equals(mode)) {
      return exp instanceof ComplexVector;
    } else if ("character".equals(mode)) {
      return exp instanceof StringVector;
    } else if ("any".equals(mode)) {
      return exp instanceof AtomicVector || exp instanceof ListVector;
    } else if ("list".equals(mode)) {
      return exp instanceof ListVector;
    } else {
      return false;
    }
  }

  @Primitive("is.object")
  public static boolean isObject(SEXP exp) {
    return exp.isObject();
  }

  public static boolean isCall(SEXP exp) {
    return exp instanceof FunctionCall;
  }

  public static boolean isLanguage(SEXP exp) {
    return exp instanceof Symbol || exp instanceof FunctionCall
        || exp instanceof ExpressionVector;

  }

  public static boolean isFunction(SEXP exp) {
    return exp instanceof Function;
  }

  public static boolean isSingle(SEXP exp) {
    throw new EvalException("type \"single\" unimplemented in R");
  }

  @Primitive("is.na")
  public static LogicalVector isNA(Vector list) {
    LogicalVector.Builder result = new LogicalVector.Builder(list.length());
    for (int i = 0; i != list.length(); ++i) {
      result.set(i, list.isElementNA(i));
    }
    result.setAttribute(Symbols.DIM, list.getAttribute(Symbols.DIM));
    result.setAttribute(Symbols.NAMES, list.getAttribute(Symbols.NAMES));
    result.setAttribute(Symbols.DIMNAMES, list.getAttribute(Symbols.DIMNAMES));

    return result.build();
  }

  public static boolean isNaN(@Recycle double value) {
    return DoubleVector.isNaN(value);
  }

  public static boolean isFinite(@Recycle double value) {
    return !Double.isInfinite(value);
  }

  public static boolean isInfinite(@Recycle double value) {
    return Double.isInfinite(value);
  }

  @Primitive("as.raw")
  public static RawVector asRaw(Vector source) {
    /*
     * Vector iv = (Vector) values; RawVector.Builder b = new
     * RawVector.Builder(); int value; Raw raw; for (int i = 0; i <
     * values.length(); i++) { value = iv.getElementAsInt(i); if (value < 0 ||
     * value > 255) { throw new
     * EvalException("out-of-range values treated as 0 in coercion to raw"); }
     * raw = new Raw(iv.getElementAsInt(i)); b.add(raw); } return (b.build());
     */
    return (RawVector) convertVector(new RawVector.Builder(), source);
  }

  @Primitive("is.raw")
  public static SEXP isRaw(Vector v) {
    return (new LogicalVector(v.getVectorType() == RawVector.VECTOR_TYPE));
  }

  @Primitive("rawToBits")
  public static RawVector rawToBits(RawVector rv) {
    RawVector.Builder b = new RawVector.Builder();
    Raw[] raws;
    for (int i = 0; i < rv.length(); i++) {
      raws = rv.getElement(i).getAsZerosAndOnes();
      for (int j = 0; j < Raw.NUM_BITS; j++) {
        b.add(raws[j]);
      }
    }
    return (b.build());
  }

  @Primitive("charToRaw")
  public static RawVector charToRaw(StringVector sv) {
    RawVector.Builder b = new RawVector.Builder();
    if (sv.length() != 1) {
      throw new EvalException(
          "argument should be a character vector of length 1");
    }
    for (int i = 0; i < sv.getElement(0).length(); i++) {
      b.add(new Raw(sv.getElement(0).charAt(i)));
    }
    return (b.build());
  }

  @Primitive("rawShift")
  public static RawVector rawShift(RawVector rv, int n) {
    if (n > Raw.NUM_BITS || n < (-1 * Raw.NUM_BITS)) {
      throw new EvalException("argument 'shift' must be a small integer");
    }
    RawVector.Builder b = new RawVector.Builder();
    Raw r;
    for (int i = 0; i < rv.length(); i++) {
      if (n >= 0) {
        r = new Raw((byte) (rv.getElement(i).getAsByte() << Math.abs(n)));
      } else {
        r = new Raw((byte) (rv.getElement(i).getAsByte() >> Math.abs(n)));
      }
      b.add(r);
    }
    return (b.build());
  }

  /*
   * !!Weird It is supposed to be an integer has four bytes! It is supposed to
   * be integer's byte order is big-endian!
   */
  @Primitive("intToBits")
  public static RawVector intToBits(Vector rv) {
    RawVector.Builder b = new RawVector.Builder();
    RawVector.Builder reverseb = new RawVector.Builder();
    byte[] intbytes = new byte[4];
    int currvalue;
    for (int i = 0; i < rv.length(); i++) {
      currvalue = rv.getElementAsInt(i);
      intbytes[0] = (byte) (currvalue >> 24);
      intbytes[1] = (byte) ((currvalue << 8) >> 24);
      intbytes[2] = (byte) ((currvalue << 16) >> 24);
      intbytes[3] = (byte) ((currvalue << 24) >> 24);
      for (int j = 0; j < 4; j++) {
        Raw[] r = (new Raw(intbytes[j])).getAsZerosAndOnes();
        for (int h = 0; h < r.length; h++) {
          b.add(r[h]);
        }
      }
    }
    RawVector temp = b.build();
    for (int i = 0; i < temp.length(); i++) {
      reverseb.addFrom(temp, temp.length() - i - 1);
    }
    return (reverseb.build());
  }

  @Generic
  public static StringVector asCharacter(Vector source) {
    return (StringVector) convertVector(new StringVector.Builder(), source);
  }

  @Generic
  public static StringVector asCharacter(Symbol symbol) {
    return new StringVector(symbol.getPrintName());
  }

  @Generic
  public static StringVector asCharacter(Environment env) {
    Frame frame = env.getFrame();
    if (frame instanceof ObjectFrame) {
      ObjectFrame oframe = (ObjectFrame) frame;
      Object instance = oframe.getInstance();
      if (StringConverter.accept(instance.getClass())) {
        return (StringVector) StringConverter.INSTANCE
            .convertToR((String) instance);
      } else if (StringArrayConverter.accept(instance.getClass())) {
        return (StringVector) StringArrayConverter.INSTANCE
            .convertToR((String[]) instance);
      }
    }
    throw new EvalException(
        "unsurpported converter, onlyfor environment with  String or String[] ObjectFrame");
  }

  @Generic
  public static LogicalVector asLogical(Environment env) {
    Frame frame = env.getFrame();
    if (frame instanceof ObjectFrame) {
      ObjectFrame oframe = (ObjectFrame) frame;
      Object instance = oframe.getInstance();
      Class clazz = instance.getClass();
      if (BooleanConverter.accept(clazz)) {
        return (LogicalVector) BooleanConverter.INSTANCE
            .convertToR((Boolean) instance);
      } else if (BooleanArrayConverter.accept(clazz)) {
        return (LogicalVector) BooleanArrayConverter.INSTANCE
            .convertToR((Boolean[]) instance);
      }
    }
    throw new EvalException(
        "unsurpported converter,only environment with boolean\\boolean[] or Boolean\\Boolean[] ObjectFrame is implemented");
  }

  public static LogicalVector asLogical(Vector vector) {
    return (LogicalVector) convertVector(new LogicalVector.Builder(), vector);
  }

  @Generic
  public static IntVector asInteger(Environment env) {
    Frame frame = env.getFrame();
    if (frame instanceof ObjectFrame) {
      ObjectFrame oframe = (ObjectFrame) frame;
      Object instance = oframe.getInstance();
      Class clazz = instance.getClass();
      if (IntegerConverter.accept(clazz)) {
        return (IntVector) IntegerConverter.INSTANCE
            .convertToR((Integer) instance);
      } else if (IntegerArrayConverter.accept(clazz)) {
        return (IntVector) IntegerArrayConverter.INSTANCE
            .convertToR((Integer[]) instance);
      }
    }
    throw new EvalException(
        "unsurpported converter,only environment with one element or array of int\\short\\Integer\\Short ObjectFrame is implemented");
  }

  @Generic
  public static IntVector asInteger(Vector source) {
    return (IntVector) convertVector(new IntVector.Builder(), source);
  }

  @Generic
  public static DoubleVector asDouble(Environment env) {
    Frame frame = env.getFrame();
    if (frame instanceof ObjectFrame) {
      ObjectFrame oframe = (ObjectFrame) frame;
      Object instance = oframe.getInstance();
      Class clazz = instance.getClass();
      if (DoubleConverter.accept(clazz)) {
        return (DoubleVector) DoubleConverter.INSTANCE
            .convertToR((Double) instance);
      } else if (DoubleArrayConverter.accept(clazz)) {
        return (DoubleVector) DoubleArrayConverter.INSTANCE
            .convertToR((Double[]) instance);
      }
    }
    throw new EvalException(
        "unsurpported converter,only environment with double[] ObjectFrame is implemented");
  }

  @Generic
  public static DoubleVector asDouble(Vector source) {
    return (DoubleVector) convertVector(new DoubleVector.Builder(), source);
  }

  private static Vector convertVector(Vector.Builder builder, Vector source) {
    for (int i = 0; i != source.length(); ++i) {
      builder.addFrom(source, i);
    }
    return builder.build();
  }
  
  @Primitive("as.complex")
  public static Complex asComplex(double x){
    return new Complex(x,0);
  }
  
  @Primitive("as.vector")
  public static SEXP asVector(Vector x, String mode) {
    Vector.Builder result;
    if ("any".equals(mode)) {
      result = x.getVectorType().newBuilder();
    } else if ("character".equals(mode)) {
      result = new StringVector.Builder();
    } else if ("logical".equals(mode)) {
      result = new LogicalVector.Builder(x.length());
    } else if ("integer".equals(mode)) {
      result = new IntVector.Builder(x.length());
    } else if ("numeric".equals(mode) || "double".equals(mode)) {
      result = new DoubleVector.Builder(x.length());
    } else if ("list".equals(mode)) {
      result = new ListVector.Builder();
    } else if ("pairlist".equals(mode)) {
      // a pairlist is actually not a vector, so bail from here
      // as a special case
      return asPairList(x);
    } else if ("symbol".equals(mode)) {
      // weird but seen in the base package
      if (x.length() == 0) {
        throw new EvalException(
            "invalid type/length (symbol/0) in vector allocation");
      }
      return Symbol.get(x.getElementAsString(0));
    } else if ("raw".equals(mode)) {
      result = new RawVector.Builder();
    } else {
      throw new EvalException("invalid 'mode' argument: " + mode);
    }

    for (int i = 0; i != x.length(); ++i) {
      result.setFrom(i, x, i);
    }
    SEXP names = x.getNames();
    if (names.length() > 0) {
      result.setAttribute(Symbols.NAMES, names);
    }
    return result.build();
  }

  @Primitive("as.vector")
  public static SEXP asVector(PairList x, String mode) {
    Vector.Builder result;
    NamesBuilder names = NamesBuilder.withInitialCapacity(x.length());
    if ("character".equals(mode)) {
      result = new StringVector.Builder();
    } else if ("logical".equals(mode)) {
      result = new LogicalVector.Builder(x.length());
    } else if ("numeric".equals(mode)) {
      result = new DoubleVector.Builder(x.length());
    } else if ("list".equals(mode)) {
      result = new ListVector.Builder();
    } else if ("raw".equals(mode)) {
      result = new RawVector.Builder();
    } else {
      throw new EvalException("invalid 'mode' argument");
    }

    for (PairList.Node node : x.nodes()) {
      if (node.hasTag()) {
        names.add(node.getTag().getPrintName());
      } else {
        names.addNA();
      }
      result.add(node.getValue());
    }
    result.setAttribute(Symbols.NAMES.getPrintName(),
        names.build(result.length()));
    return result.build();
  }

  private static PairList asPairList(Vector x) {
    PairList.Builder builder = new PairList.Builder();
    for (int i = 0; i != x.length(); ++i) {
      builder.add(x.getName(i), x.getElementAsSEXP(i));
    }
    return builder.build();
  }

  /**
   * Creates a new, unevaluated FunctionCall expression from a list vector.
   * 
   * @param list
   *          a list containing the function as the first element, followed by
   *          arguments
   * @return an unevaluated FunctionCall expression
   */
  @Primitive("as.call")
  public static FunctionCall asCall(ListVector list) {
    EvalException.check(list.length() > 0, "invalid length 0 argument");

    PairList.Builder arguments = new PairList.Builder();
    for (int i = 1; i != list.length(); ++i) {
      arguments.add(list.getName(i), list.getElementAsSEXP(i));
    }
    return new FunctionCall(list.getElementAsSEXP(0), arguments.build());
  }

  public static Environment asEnvironment(Environment arg) {
    return arg;
  }

  @Primitive("list2env")
  public static ListVector env2list(Environment env, boolean allNames) {
    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
    for(Symbol name : env.getSymbolNames()) {
      if(allNames || !name.getPrintName().startsWith(".")) {
        list.add(name, env.getVariable(name));
      }
    }
    return list.build();
  }

  @Primitive("as.environment")
  public static Environment asEnvironment(@Current Context context, double index) {
    Environment result = context.getGlobalEnvironment();
    for (int i = 1; i < index; ++i) {
      if (result == Environment.EMPTY) {
        throw new EvalException("invalid 'pos' argument");
      }
      result = result.getParent();
    }
    return result;
  }
  
  @Primitive("environmentName")
  public static String environmentName(Environment env) {
    return env.getName();
  }

  @Primitive("parent.env")
  public static Environment getParentEnv(Environment environment) {
    return environment.getParent();
  }

  @Primitive("parent.env<-")
  public static Environment setParentEnv(Environment environment,
      Environment newParent) {
    environment.setParent(newParent);
    return environment;
  }

  public static StringVector ls(Environment environment, boolean allNames) {
    StringVector.Builder names = new StringVector.Builder();
    if (environment.getFrame() instanceof ClassFrame) {
      ClassFrame of = (ClassFrame)environment.getFrame();
      of.getSymbols();
      for (Symbol name : of.getSymbols()) {
        if (allNames || !name.getPrintName().startsWith(".")) {
          names.add(name.getPrintName());
        }
      }
    } else {
      for (Symbol name : environment.getSymbolNames()) {
        if (allNames || !name.getPrintName().startsWith(".")) {
          names.add(name.getPrintName());
        }
      }
    }
    return names.build();
  }

  public static void lockEnvironment(Environment env, boolean bindings) {
    env.lock(bindings);
  }

  public static void lockBinding(Symbol name, Environment env) {
    env.lockBinding(name);
  }

  public static void unlockBinding(Symbol name, Environment env) {
    env.unlockBinding(name);
  }

  public static boolean environmentIsLocked(Environment env) {
    return env.isLocked();
  }

  public static boolean identical(SEXP x, SEXP y, boolean numericallyEqual,
      boolean singleNA, boolean attributesAsSet) {
    if (!numericallyEqual || !singleNA || !attributesAsSet) {
      throw new EvalException(
          "identical implementation only supports num.eq = TRUE, single.NA = TRUE, attrib.as.set = TRUE");
    }
    return x.equals(y);
  }

  /*----------------------------------------------------------------------
  
  do_libfixup
  
  This function copies the bindings in the loading environment to the
  library environment frame (the one that gets put in the search path)
  and removes the bindings from the loading environment.  Values that
  contain promises (created by delayedAssign, for example) are not forced.
  Values that are closures with environments equal to the loading
  environment are reparented to .GlobalEnv.  Finally, all bindings are
  removed from the loading environment.
  
  This routine can die if we automatically create a name space when
  loading a package.
   */
  @Primitive("lib.fixup")
  public static Environment libfixup(Environment loadEnv, Environment libEnv) {
    for (Symbol name : loadEnv.getSymbolNames()) {
      SEXP value = loadEnv.getVariable(name);
      if (value instanceof Closure) {
        Closure closure = (Closure) value;
        if (closure.getEnclosingEnvironment() == loadEnv) {
          value = closure.setEnclosingEnvironment(libEnv);
        }
      }
      loadEnv.setVariable(name, value);
    }
    return libEnv;
  }

  @Primitive("dim")
  @Generic
  public static SEXP getDimensions(SEXP sexp) {
    return sexp.getAttribute(Symbols.DIM);
  }

  @Primitive("dim<-")
  public static SEXP setDimensions(SEXP exp, AtomicVector vector) {
    int dim[] = new int[vector.length()];
    int prod = 1;
    for (int i = 0; i != vector.length(); ++i) {
      dim[i] = vector.getElementAsInt(i);
      prod *= dim[i];
    }

    if (prod != exp.length()) {
      throw new EvalException(
          "dims [product %d] do not match the length of object [%d]", prod,
          exp.length());
    }

    return exp.setAttribute(Symbols.DIM, new IntVector(dim));
  }

  @Generic
  @Primitive("dimnames")
  public static SEXP getDimensionNames(SEXP exp) {
    return exp.getAttribute(Symbols.DIMNAMES);
  }

  @Primitive("dimnames<-")
  public static SEXP setDimensionNames(@Current Context context, SEXP exp, ListVector dimnames) {
    Vector dim = (Vector) exp.getAttribute(Symbols.DIM);
    if(dim.length() != dimnames.length()) {
      throw new EvalException("length of 'dimnames' [%d] not equal to array extent [%d]", 
          dimnames.length(), dim.length());
          
    }
    ListVector.Builder dn = new ListVector.Builder();
    for(SEXP names : dimnames) {
      if(!(names instanceof StringVector)) {
        names = context.evaluate(FunctionCall.newCall(Symbol.get("as.character"), names));
      }
      dn.add(names);
    }
    return exp.setAttribute(Symbols.DIMNAMES, dn.build());
  }

  public static PairList attributes(SEXP sexp) {
    return sexp.getAttributes();
  }

  @Primitive("attr")
  public static SEXP getAttribute(SEXP exp, String which) {
    PairList.Node partialMatch = null;
    int partialMatchCount = 0;

    for (PairList.Node node : exp.getAttributes().nodes()) {
      String name = node.getTag().getPrintName();
      if (name.equals(which)) {
        return Attributes.postProcessAttributeValue(node);
      } else if (name.startsWith(which)) {
        partialMatch = node;
        partialMatchCount++;
      }
    }
    return partialMatchCount == 1 ? Attributes
        .postProcessAttributeValue(partialMatch) : Null.INSTANCE;
  }

  @Primitive("attributes<-")
  public static SEXP setAttributes(SEXP exp, ListVector attributes) {
    return exp.setAttributes(attributes);
  }
  
  @Primitive("attributes<-")
  public static SEXP setAttributes(SEXP exp, PairList list) {
    if(list == Null.INSTANCE) {
      return exp.setAttributes(ListVector.EMPTY);
    } else {
      return exp.setAttributes(((PairList.Node)list).toVector());
    }
  }

  public static ListVector list(@ArgumentList ListVector arguments) {
    return arguments;
  }

  public static Environment environment(@Current Context context) {
    return context.getGlobalEnvironment();
  }

  public static SEXP environment(@Current Environment rho, SEXP exp) {
    if (exp == Null.INSTANCE) {
      // if the user passes null, we return the current exp
      return rho;
    } else if (exp instanceof Closure) {
      return ((Closure) exp).getEnclosingEnvironment();
    } else {
      return exp.getAttribute(Symbols.DOT_ENVIRONMENT);
    }
  }

  @Primitive("environment<-")
  public static SEXP setEnvironment(SEXP exp, Environment newRho) {
    if (exp instanceof Closure) {
      return ((Closure) exp).setEnclosingEnvironment(newRho);
    } else {
      return exp.setAttribute(Symbols.DOT_ENVIRONMENT.getPrintName(), newRho);
    }
  }

  public static PairList formals(Closure closure) {
    return closure.getFormals();
  }

  @Primitive("body")
  public static SEXP body(Closure closure) {
    return closure.getBody();
  }

  public static Environment newEnv(boolean hash, Environment parent, int size) {
    return Environment.createChildEnvironment(parent);
  }

  public static Environment baseenv(@Current Environment rho) {
    return rho.getBaseEnvironment();
  }

  public static Environment emptyenv(@Current Environment rho) {
    return rho.getBaseEnvironment().getParent();
  }

  public static Environment globalenv(@Current Context context) {
    return context.getGlobalEnvironment();
  }

  public static boolean exists(@Current Context context, String x,
      Environment environment, String mode, boolean inherits) {
    return environment.findVariable(Symbol.get(x), modePredicate(mode),
        inherits) != Symbol.UNBOUND_VALUE;
  }

  public static SEXP get(@Current Context context, String x,
      Environment environment, String mode, boolean inherits) {
    return environment.findVariable(Symbol.get(x), modePredicate(mode),
        inherits);
  }

  public static int length(SEXP exp) {
    return exp.length();
  }

  public static SEXP vector(String mode, int length) {
    if ("logical".equals(mode)) {
      return new LogicalVector(new int[length]);

    } else if ("integer".equals(mode)) {
      return new IntVector(new int[length]);

    } else if ("numeric".equals(mode)) {
      return new DoubleVector(new double[length]);

    } else if ("double".equals(mode)) {
      return new DoubleVector(new double[length]);

    } else if ("complex".equals(mode)) {
      throw new UnsupportedOperationException("implement me!");

    } else if ("character".equals(mode)) {
      String values[] = new String[length];
      Arrays.fill(values, "");
      return new StringVector(values);

    } else if ("list".equals(mode)) {
      SEXP values[] = new SEXP[length];
      Arrays.fill(values, Null.INSTANCE);
      return new ListVector(values);

    } else if ("pairlist".equals(mode)) {
      SEXP values[] = new SEXP[length];
      Arrays.fill(values, Null.INSTANCE);
      return PairList.Node.fromArray(values);

    } else if ("raw".equals(mode)) {
      Raw values[] = new Raw[length];
      Arrays.fill(values, Null.INSTANCE);
      return new RawVector(values);
    } else {
      throw new EvalException(String.format(
          "vector: cannot make a vector of mode '%s'.", mode));
    }
  }

  @Primitive("storage.mode<-")
  public static SEXP setStorageMode(Vector source, String newMode) {
    Vector.Builder builder;
    if (newMode.equals("logical")) {
      builder = new LogicalVector.Builder();
    } else if (newMode.equals("double")) {
      builder = new DoubleVector.Builder();
    } else if (newMode.equals("integer")) {
      builder = new IntVector.Builder();
    } else if (newMode.equals("character")) {
      builder = new StringVector.Builder();
    } else {
      throw new UnsupportedOperationException("storage.mode with new mode '"
          + newMode + "' invalid or not implemented");
    }

    for (int i = 0; i != source.length(); ++i) {
      builder.setFrom(i, source, i);
    }
    return builder.copyAttributesFrom(source).build();
  }

  public static String typeof(SEXP exp) {
    return exp.getTypeName();
  }

  @Generic
  @Primitive("names")
  public static SEXP getNames(SEXP exp) {
    return exp.getNames();
  }

  @Generic
  @Primitive("names<-")
  public static SEXP setNames(SEXP exp, @InvokeAsCharacter Vector names) {
    return exp.setAttribute("names", names);
  }

  @Generic
  @Primitive("levels<-")
  public static SEXP setLabels(SEXP exp, SEXP levels) {
    return exp.setAttribute(Symbols.LEVELS, levels);
  }

  /**
   * 
   * This implements the 'class' builtin. The R docs mention this function in
   * the context of S3 dispatch, but it appears that the logic has diverged:
   * class(9) for example will return 'numeric', but the class list used for
   * dispatch by UseMethod is actually c('double', 'numeric')
   * 
   * @param exp
   * @return
   */
  @Primitive("class")
  public static StringVector getClass(SEXP exp) {

    SEXP classAttribute = exp.getAttribute(Symbols.CLASS);
    if (classAttribute.length() > 0) {
      return (StringVector) classAttribute;
    }

    SEXP dim = exp.getAttribute(Symbols.DIM);
    if (dim.length() == 2) {
      return new StringVector("matrix");
    } else if (dim.length() > 0) {
      return new StringVector("array");
    }

    return new StringVector(exp.getImplicitClass());
  }

  @Primitive("comment")
  public static SEXP getComment(SEXP exp) {
    return exp.getAttribute(Symbols.COMMENT);
  }
  
  @Primitive("comment<-")
  public static SEXP setComment(StringVector exp) {
    return exp.setAttribute(Symbols.COMMENT, exp);
  }

  @Primitive("class<-")
  public static SEXP setClass(SEXP exp, Vector classes) {
    return exp.setAttribute("class", classes);

    // TODO:
    // this is apparently more complicated then implemented above:
    // int nProtect = 0;
    // if(isNull(value)) {
    // setAttrib(obj, R_ClassSymbol, value);
    // if(IS_S4_OBJECT(obj)) /* NULL class is only valid for S3 objects */
    // do_unsetS4(obj, value);
    // return obj;
    // }
    // if(TYPEOF(value) != STRSXP) {
    // /* Beware: assumes value is protected, which it is
    // in the only use below */
    // PROTECT(value = coerceVector(duplicate(value), STRSXP));
    // nProtect++;
    // }
    // if(length(value) > 1) {
    // setAttrib(obj, R_ClassSymbol, value);
    // if(IS_S4_OBJECT(obj)) /* multiple strings only valid for S3 objects */
    // do_unsetS4(obj, value);
    // }
    // else if(length(value) == 0) {
    // UNPROTECT(nProtect); nProtect = 0;
    // error(_("invalid replacement object to be a class string"));
    // }
    // else {
    // const char *valueString, *classString; int whichType;
    // SEXP cur_class; SEXPTYPE valueType;
    // valueString = CHAR(asChar(value)); /* ASCII */
    // whichType = class2type(valueString);
    // valueType = (whichType == -1) ? -1 : classTable[whichType].sexp;
    // PROTECT(cur_class = R_data_class(obj, FALSE)); nProtect++;
    // classString = CHAR(asChar(cur_class)); /* ASCII */
    // /* assigning type as a class deletes an explicit class attribute. */
    // if(valueType != -1) {
    // setAttrib(obj, R_ClassSymbol, R_NilValue);
    // if(IS_S4_OBJECT(obj)) /* NULL class is only valid for S3 objects */
    // do_unsetS4(obj, value);
    // if(classTable[whichType].canChange) {
    // PROTECT(obj = ascommon(call, obj, valueType));
    // nProtect++;
    // }
    // else if(valueType != TYPEOF(obj))
    // error(_("\"%s\" can only be set as the class if the object has this type; found \"%s\""),
    // valueString, type2char(TYPEOF(obj)));
    // /* else, leave alone */
    // }
    // else if(!strcmp("numeric", valueString)) {
    // setAttrib(obj, R_ClassSymbol, R_NilValue);
    // if(IS_S4_OBJECT(obj)) /* NULL class is only valid for S3 objects */
    // do_unsetS4(obj, value);
    // switch(TYPEOF(obj)) {
    // case INTSXP: case REALSXP: break;
    // default: PROTECT(obj = coerceVector(obj, REALSXP));
    // nProtect++;
    // }
    // }
    // /* the next 2 special cases mirror the special code in
    // * R_data_class */
    // else if(!strcmp("matrix", valueString)) {
    // if(length(getAttrib(obj, R_DimSymbol)) != 2)
    // error(_("invalid to set the class to matrix unless the dimension attribute is of length 2 (was %d)"),
    // length(getAttrib(obj, R_DimSymbol)));
    // setAttrib(obj, R_ClassSymbol, R_NilValue);
    // if(IS_S4_OBJECT(obj))
    // do_unsetS4(obj, value);
    // }
    // else if(!strcmp("array", valueString)) {
    // if(length(getAttrib(obj, R_DimSymbol))<= 0)
    // error(_("cannot set class to \"array\" unless the dimension attribute has length > 0"));
    // setAttrib(obj, R_ClassSymbol, R_NilValue);
    // if(IS_S4_OBJECT(obj)) /* NULL class is only valid for S3 objects */
    // UNSET_S4_OBJECT(obj);
    // }
    // else { /* set the class but don't do the coercion; that's
    // supposed to be done by an as() method */
    // setAttrib(obj, R_ClassSymbol, value);
    // }
    // }
    // UNPROTECT(nProtect);
    // return obj;

  }

  @Primitive("oldClass<-")
  public static SEXP setOldClass(SEXP exp, Vector classes) {
    /*
     * checkArity(op, args); if (NAMED(CAR(args)) == 2) SETCAR(args,
     * duplicate(CAR(args))); if (length(CADR(args)) == 0) SETCADR(args,
     * R_NilValue); if(IS_S4_OBJECT(CAR(args))) UNSET_S4_OBJECT(CAR(args));
     * setAttrib(CAR(args), R_ClassSymbol, CADR(args)); return CAR(args);
     */
    return exp.setAttribute(Symbols.CLASS, classes);
  }

  public static SEXP unclass(SEXP exp) {
    return exp.setAttribute("class", Null.INSTANCE);
  }

  @Primitive("attr<-")
  public static SEXP setAttribute(SEXP exp, String which, SEXP value) {
    return exp.setAttribute(which, value);
  }

  public static SEXP oldClass(SEXP exp) {
    if (!exp.hasAttributes()) {
      return Null.INSTANCE;
    }
    return exp.getAttribute(Symbols.CLASS);
  }

  public static boolean inherits(SEXP exp, StringVector what) {
    StringVector classes = getClass(exp);
    for (String whatClass : what) {
      if (Iterables.contains(classes, whatClass)) {
        return true;
      }
    }
    return false;
  }

  public static boolean inherits(SEXP exp, String what) {
    return Iterables.contains(getClass(exp), what);
  }

  public static SEXP inherits(SEXP exp, StringVector what, boolean which) {
    if (!which) {
      return new LogicalVector(inherits(exp, what));
    }
    StringVector classes = getClass(exp);
    int result[] = new int[what.length()];

    for (int i = 0; i != what.length(); ++i) {
      result[i] = Iterables.indexOf(classes,
          Predicates.equalTo(what.getElement(i))) + 1;
    }
    return new IntVector(result);
  }

  public static SEXP invisible(@Current Context context, SEXP value) {
    context.setInvisibleFlag();
    return value;
  }

  public static SEXP invisible(@Current Context context) {
    context.setInvisibleFlag();
    return Null.INSTANCE;
  }

  public static StringVector search(@Current Context context) {
    List<String> names = Lists.newArrayList();
    Environment env = context.getGlobalEnvironment();
    while (env != Environment.EMPTY) {
      names.add(env.getName());
      env = env.getParent();
    }
    // special cased:
    names.set(0, ".GlobalEnv");
    names.set(names.size() - 1, "package:base");

    return new StringVector(names);
  }

  @Visible(false)
  public static Environment attach(@Current Context context, SEXP what,
      int pos, String name) {

    // By default the database is attached in position 2 in the search path,
    // immediately after the user's workspace and before all previously loaded
    // packages and
    // previously attached databases. This can be altered to attach later in the
    // search
    // path with the pos option, but you cannot attach at pos=1.

    if (pos < 2) {
      throw new EvalException("Attachment position must be 2 or greater");
    }

    Environment child = context.getGlobalEnvironment();
    for (int i = 2; i != pos; ++i) {
      child = child.getParent();
    }

    Environment newEnv = Environment.createChildEnvironment(child.getParent());
    child.setParent(newEnv);

    newEnv.setAttribute(Symbols.NAME.getPrintName(), new StringVector(name));

    // copy all values from the provided environment into the
    // new environment
    if (what instanceof Environment) {
      Environment source = (Environment) what;
      for (Symbol symbol : source.getSymbolNames()) {
        newEnv.setVariable(symbol, source.getVariable(symbol));
      }
    }
    return newEnv;
  }

  public static String Encoding(StringVector vector) {
    return "UTF-8";
  }

  public static StringVector setEncoding(StringVector vector,
      String encodingName) {
    if (encodingName.equals("UTF-8") || encodingName.equals("unknown")) {
      return vector;
    } else {
      throw new EvalException(
          "Only UTF-8 and unknown encoding are supported at this point");
    }
  }

  public static boolean isFactor(SEXP exp) {
    return exp instanceof IntVector && exp.inherits("factor");
  }

  private static boolean isListFactor(ListVector list) {
    for (SEXP element : list) {
      if (element instanceof ListVector && !isListFactor((ListVector) element)) {
        return false;
      } else if (!isFactor(element)) {
        return false;
      }
    }
    return true;
  }

  @Primitive("islistfactor")
  public static boolean isListFactor(SEXP exp, boolean recursive) {

    if (!(exp instanceof ListVector)) {
      return false;
    }
    if (exp.length() == 0) {
      return false;
    }

    ListVector vector = (ListVector) exp;
    for (SEXP element : vector) {
      if (element instanceof ListVector) {
        if (!recursive || !isListFactor((ListVector) element)) {
          return false;
        }
      } else if (!isFactor(exp)) {
        return false;
      }
    }
    return true;
  }

  public static ListVector options(@Current Context context,
      @ArgumentList ListVector arguments) {
    Context.Options options = context.getGlobals().options;
    ListVector.NamedBuilder results = ListVector.newNamedBuilder();

    if (arguments.length() == 0) {
      // return all options as a list
      for (String name : options.names()) {
        results.add(name, options.get(name));
      }

    } else if (arguments.length() == 1
        && arguments.getElementAsSEXP(0) instanceof ListVector
        && StringVector.isNA(arguments.getName(0))) {
      ListVector list = (ListVector) arguments.getElementAsSEXP(0);
      if (list.getAttribute(Symbols.NAMES) == Null.INSTANCE) {
        throw new EvalException("list argument has no valid names");
      }
      for (NamedValue argument : list.namedValues()) {
        if (!argument.hasName()) {
          throw new EvalException("invalid argument");
        }
        String name = argument.getName();
        results.add(name, options.set(name, argument.getValue()));
      }

    } else {
      for (NamedValue argument : arguments.namedValues()) {
        if (argument.hasName()) {
          String name = argument.getName();
          results.add(name, options.set(name, argument.getValue()));

        } else if (argument.getValue() instanceof StringVector) {
          String name = ((StringVector) argument.getValue())
              .getElementAsString(0);
          results.add(name, options.get(name));

        } else {
          throw new EvalException("invalid argument");
        }
      }
    }
    return results.build();
  }

  /**
   * returns a vector of type "expression" containing its arguments
   * (unevaluated).
   */
  public static ExpressionVector expression(@ArgumentList ListVector arguments) {
    return new ExpressionVector(arguments);
  }
}

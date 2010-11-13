/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package r.lang;

import com.google.common.base.Preconditions;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * The Environment data type.
 *
 * <p>
 * Environments can be thought of as consisting of two things:
 * <ul>
 * <li>A <strong>frame</strong>, consisting of a set of symbol-value pairs, and
 * <li>an enclosure, a pointer to an enclosing environment.</li>
 * </ul>
 *
 * <p>
 * When R looks up the value for a symbol the frame is examined and if a
 * matching symbol is found its value will be returned. If not, the enclosing environment
 *  is then accessed and the process repeated.
 * Environments form a tree structure in which the enclosures play the role of parents.
 *  The tree of environments is rooted in an empty environment,
 * available through emptyenv(), which has no parent.
 * It is the direct parent of the environment of the base package
 * (available through the baseenv() function). Formerly baseenv() 
 * had the special value {@code NULL}, but as from version 2.4.0, the
 *  use of {@code NULL} as an environment is defunct.
 *
 */
public class EnvExp extends SEXP implements RecursiveExp {

  public static final EnvExp EMPTY = new EnvExp();

  public static final int TYPE_CODE = 4;
  public static final String TYPE_NAME = "environment";

  private GlobalContext globalContext;
  private EnvExp parent;

  public interface Frame {
    Set<SymbolExp> getSymbols();
    SEXP getVariable(SymbolExp name);
    void setVariable(SymbolExp name, SEXP value);
  }

  public static class HashFrame implements Frame{
    private HashMap<SymbolExp, SEXP> values = new HashMap<SymbolExp, SEXP>();

    @Override
    public Set<SymbolExp> getSymbols() {
      return values.keySet();
    }

    @Override
    public SEXP getVariable(SymbolExp name) {
      SEXP value = values.get(name);
      return value == null ? SymbolExp.UNBOUND_VALUE : value;
    }

    @Override
    public void setVariable(SymbolExp name, SEXP value) {
      values.put(name, value);
    }
  }


  protected Frame frame;

  /**
   * Creates a new environment, with no enclosing environment,
   * initialized with an empty HashFrame.
   */
  public EnvExp() {
    this.frame = new HashFrame();
  }

  /**
   * Creates a new environment with the given parent
   * and an empty HashFrame
   *
   * @param parent
   */
  public EnvExp(EnvExp parent) {
    Preconditions.checkNotNull(parent);

    this.parent = parent;
    this.globalContext = parent.getGlobalContext();
    this.frame = new HashFrame();
  }

  protected EnvExp(GlobalContext globalContext) {
    this.globalContext = globalContext;
    this.parent = null;
    this.frame = new HashFrame();
  }

  public EnvExp getParent() {
    return parent;
  }

  public void setParent(EnvExp parent) {
    this.parent = parent;
  }

  public GlobalContext getGlobalContext() {
    return globalContext;
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  public Collection<SymbolExp> getSymbolNames() {
    return frame.getSymbols();
  }

  public void setVariable(SymbolExp symbol, SEXP value) {
    frame.setVariable(symbol, value);
  }

  public SEXP findVariable(SymbolExp symbol) {
    SEXP value = frame.getVariable(symbol);
    if(value != SymbolExp.UNBOUND_VALUE) {
      return value;
    }
    return parent.findVariable(symbol);
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  public Iterable<EnvExp> selfAndParents() {
    return new Iterable<EnvExp>() {
      @Override
      public Iterator<EnvExp> iterator() {
        return new EnvIterator(EnvExp.this);
      }
    };
  }

  public SEXP getVariable(SymbolExp symbol) {
    return frame.getVariable(symbol);
  }

  private static class EnvIterator extends UnmodifiableIterator<EnvExp> {
    private EnvExp next;

    private EnvIterator(EnvExp next) {
      this.next = next;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public EnvExp next() {
      EnvExp toReturn = next;
      next = next.parent;
      return toReturn;
    }
  }
}


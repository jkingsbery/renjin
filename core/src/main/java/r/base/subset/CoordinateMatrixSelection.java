package r.base.subset;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.UnmodifiableIterator;

import r.base.matrix.Matrix;
import r.lang.AtomicVector;
import r.lang.DoubleVector;
import r.lang.Indexes;
import r.lang.IntVector;
import r.lang.SEXP;
import r.lang.Symbols;
import r.lang.Vector;
import r.lang.exception.EvalException;

/**
 * In the rarest of cases, the single subscript provided 
 * can be a matrix that contains cell coordinates in rows.
 */
public class CoordinateMatrixSelection extends Selection {

  private Matrix coordinateMatrix;
  private int sourceDim[];
  
  public static boolean isCoordinateMatrix(SEXP source, SEXP subscript) {
    
    if(!(subscript instanceof IntVector) &&
       !(subscript instanceof DoubleVector)) {
      return false;
    }
    
    Vector subscriptDim = (Vector)subscript.getAttribute(Symbols.DIM);
    if(subscriptDim.length() != 2) {
      return false;
    }
    
    // now check that the columns in the subscript match the number of
    // dimensions in the source.
    
    SEXP sourceDim = source.getAttribute(Symbols.DIM);
    return sourceDim.length() == subscriptDim.getElementAsInt(1);
  }
  
  public CoordinateMatrixSelection(SEXP source, SEXP subscript) {
    this.coordinateMatrix = new Matrix((Vector)subscript);
    this.sourceDim = dimAsIntArray(source);
    
    if(sourceDim.length != coordinateMatrix.getNumCols()) {
      throw new EvalException("The number of dimensions in the source (%d) does not " +
      		"match the number of columns in the provided coordinate matrix (%d)", 
      		sourceDim.length,
      		coordinateMatrix.getNumCols());
    }    
  }

  @Override
  public int getSourceDimensions() {
    return sourceDim.length;
  }

  @Override
  public int getElementCount() {
    return coordinateMatrix.getNumRows();
  }
  
  @Override
  protected AtomicVector getNames(int dimensionIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] getSubscriptDimensions() {
    return new int[] { getElementCount() };
  }
  
  private int[] getCoordinate(int row) {
    int[] coord = new int[sourceDim.length];
    for(int i=0;i!=coord.length;++i) {
      coord[i] = coordinateMatrix.getElementAsInt(row, i) - 1;
    }
    return coord;
  }

  @Override
  public Iterator<Integer> iterator() {
    
    return new UnmodifiableIterator<Integer>() {
      private int row = 0;
      
      @Override
      public boolean hasNext() {
        return row < coordinateMatrix.getNumRows();
      }

      @Override
      public Integer next() {
        return Indexes.arrayIndexToVectorIndex(getCoordinate(row++), sourceDim);
      }
    }; 
  }
}

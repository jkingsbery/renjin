package r.base.matrix;

import r.lang.DoubleVector;

public class DoubleMatrixBuilder extends AbstractMatrixBuilder<DoubleVector.Builder, DoubleVector> 
    implements MatrixBuilder {

  public DoubleMatrixBuilder(int nrows, int ncols) {
    super(DoubleVector.VECTOR_TYPE, nrows, ncols);
  }
  
  public void set(int row, int col, double value) {
    builder.set(computeIndex(row, col), value);
  }

  @Override
  public void setValue(int row, int col, double value) {
    builder.set(computeIndex(row, col), value);
  }

  @Override
  public void setValue(int row, int col, int value) {
    builder.set(computeIndex(row, col), value);  
  }

}

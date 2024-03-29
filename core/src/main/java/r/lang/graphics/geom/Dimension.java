package r.lang.graphics.geom;

import r.lang.DoubleVector;

public class Dimension {
  private final double width;
  private final double height;
  
  public Dimension(double width, double height) {
    super();
    this.width = width;
    this.height = height;
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }
  
  public double getAspectRatio() {
    return height / width;
  }

  public Dimension multiplyBy(Dimension b) {
    return new Dimension(width * b.getWidth(), height * b.getHeight());
  }

  public DoubleVector toVector() {
    return new DoubleVector(width, height);
  }
}

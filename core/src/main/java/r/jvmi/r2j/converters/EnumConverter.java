package r.jvmi.r2j.converters;

import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Vector;

public class EnumConverter extends PrimitiveScalarConverter<Enum> {

  private Class enumType;
  
  public EnumConverter(Class enumType) {
    super();
    this.enumType = enumType;
  }

  @Override
  public SEXP convertToR(Enum value) {
    return new StringVector(value.name());
  }

  @Override
  protected Object getFirstElement(Vector value) {
    return Enum.valueOf(enumType, value.getElementAsString(0));
  }

  
  public static boolean accept(Class clazz) {
    // not sure what is preferable here:
    // do we convert enums with extra methods to objects or to strings?
    // Enum.class.isAssignableFrom will catch all enums, while clazz.isEnum
    // will only apply to those with no special methods 
    return Enum.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof StringVector;
  }

  @Override
  public int getSpecificity() {
    return 300;
  }
}

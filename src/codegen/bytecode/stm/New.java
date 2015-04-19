package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

//new is used to create object instances. 
//new takes a single parameter, 
//<class>, the name of the class of object you want to create. 
//<class> is resolved into a Java class (see Chapter 7 for a discussion of how classes are resolved).
//Then new determines the size in bytes of instances of the given class and allocates memory 
//for the new instance from the garbage collected heap. 
//The fields of the instance are set to the initial value 0 (for numeric and boolean fields), 
//or null (for reference fields). Next, a reference to the new object is pushed onto the operand stack.


public class New extends T
{
  public String c;

  public New(String c)
  {
    this.c = c;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
  }
}

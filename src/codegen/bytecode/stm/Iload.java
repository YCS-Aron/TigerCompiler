package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

//retrieve integer from local variable
public class Iload extends T
{
  public int index;

  public Iload(int index)
  {
    this.index = index;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
  }
}

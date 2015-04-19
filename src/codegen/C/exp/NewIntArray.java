package codegen.C.exp;

import codegen.C.Visitor;

public class NewIntArray extends T	//new int[exp]
{
  public T exp;

  public NewIntArray(T exp)
  {
    this.exp = exp;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
    return;
  }
}

package codegen.C.exp;

import codegen.C.Visitor;

public class True extends T
{
  public True()
  {

  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
    return;
  }
}
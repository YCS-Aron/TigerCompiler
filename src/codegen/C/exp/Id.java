package codegen.C.exp;

import codegen.C.Visitor;

public class Id extends T
{
  public String id;
  public int isref;

  public Id(String id,int isref)
  {
    this.id = id;
    this.isref = isref;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
    return;
  }
}

package codegen.C.exp;

import codegen.C.Visitor;

public class NewObject extends T	//id = new id()
{
  public String id;
  public codegen.C.type.T type;

  public NewObject(codegen.C.type.T type,String id)
  {
    this.id = id;
    this.type = type;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
    return;
  }
}

package codegen.C.exp;

import codegen.C.Visitor;

public class Call extends T
{
  public codegen.C.type.T rt;
  public String assign;			
  public T exp;
  public String id;
  public java.util.LinkedList<T> args;

  public Call(codegen.C.type.T rt ,String assign, T exp, String id, java.util.LinkedList<T> args)
  {
	this.rt = rt;
    this.assign = assign;
    this.exp = exp;
    this.id = id;
    this.args = args;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
    return;
  }
}

package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

public class Putfield extends T
{
  public String path;
  public String descriptor;

  public Putfield(String path,String descriptor)
  {
    this.path = path;
    this.descriptor = descriptor;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
  }
}

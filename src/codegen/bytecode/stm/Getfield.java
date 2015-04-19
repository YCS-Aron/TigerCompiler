package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

public class Getfield extends T
{
  public String path;
  public String descriptor;

  public Getfield(String path,String descriptor)
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

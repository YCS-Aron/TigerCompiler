package codegen.C.vtable;

import codegen.C.Visitor;

public class Vtable extends T
{
  public String id; // name of the class
  public java.util.ArrayList<codegen.C.Ftuple> ms; // all methods
  public String gc_map;

  public Vtable(String id, java.util.ArrayList<codegen.C.Ftuple> ms,String map)
  {
    this.id = id;
    this.ms = ms;
    gc_map = map;
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
  }
}

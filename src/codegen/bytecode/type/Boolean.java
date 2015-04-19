package codegen.bytecode.type;

import codegen.bytecode.Visitor;

public class Boolean extends T{
	  public Boolean()
	  {
	  }

	  @Override
	  public String toString()
	  {
	    return "@boolean";
	  }
	  
	  @Override
	  public void accept(Visitor v)
	  {
	    v.visit(this);
	  }
}

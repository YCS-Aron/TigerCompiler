package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

//discard top word on stack
public class pop extends T{
	  public pop()
	  {
	  }

	  @Override
	  public void accept(Visitor v)
	  {
	    v.visit(this);
	  }

}
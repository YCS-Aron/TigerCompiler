package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

//Computes the bitwise and of value1 and value2 (which must be ints).
//The int result replaces value1 and value2 on the stack.
public class Iand extends T{
	  public Iand()
	  {
	  }

	  @Override
	  public void accept(Visitor v)
	  {
	    v.visit(this);
	  }

}

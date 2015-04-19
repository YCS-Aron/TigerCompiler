package codegen.bytecode.stm;

import codegen.bytecode.Visitor;

public class Arraylength extends T{
	public Arraylength()
	{
		
	}
	
	public void accept(Visitor v)
	{
		v.visit(this);
	}
}

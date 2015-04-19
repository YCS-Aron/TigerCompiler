package codegen.bytecode.stm;

//Retrieves an entry from a int array and places it on the stack. 
//arrayref is a reference to an array of ints. 
//index is an int.
//The arrayref and index are removed from the stack, 
//and the int value at the given index in the array is pushed onto the stack.
import codegen.bytecode.Visitor;

public class IaLoad extends T
{
  public String c;

  public IaLoad()
  {
  }

  @Override
  public void accept(Visitor v)
  {
    v.visit(this);
  }
}

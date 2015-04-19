package ast.exp;

public class NewIntArray extends T
{
  public T exp; 

  public NewIntArray(T exp)
  {
    this.exp = exp;
  }
  
  public NewIntArray(T exp,int lineNum)
  {
    this.exp = exp;
    this.lineNum=lineNum;
  }

  @Override
  public void accept(ast.Visitor v)
  {
    v.visit(this);
    return;
  }
}

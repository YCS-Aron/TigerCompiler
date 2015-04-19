package ast.exp;

public class Not extends T
{
  public T exp;

  public Not(T exp)
  {
    this.exp = exp;
  }
  
  public Not(T exp,int lineNum)
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

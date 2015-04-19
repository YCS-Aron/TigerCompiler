package ast.exp;

public class Add extends T {
	public T left;
	public T right;


	public Add(T left, T right) {
		this.left = left;
		this.right = right;
	}

	public Add(T left, T right, int lineNum) {
		this.left = left;
		this.right = right;
		this.lineNum = lineNum;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}

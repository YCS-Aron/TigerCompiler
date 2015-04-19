package ast.exp;

public class Length extends T {
	public T array;

	public Length(T array) {
		this.array = array;
	}

	public Length(T array, int lineNum) {
		this.array = array;
		this.lineNum = lineNum;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}

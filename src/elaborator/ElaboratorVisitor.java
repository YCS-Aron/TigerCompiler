package elaborator;

public class ElaboratorVisitor implements ast.Visitor {
	public ClassTable classTable; // symbol table for class
	public MethodTable methodTable; // symbol table for each method
	public String currentClass; // the class name being elaborated
	public ast.type.T type; // type of the expression being elaborated

	public int errLine; // mark the error's lineNum
	public String errID;// mark the error identify

	public ElaboratorVisitor() {
		this.classTable = new ClassTable();
		this.methodTable = new MethodTable();
		this.currentClass = null;
		this.type = null;

	}

	private void error(String error) {
		System.err.print("Error: ");
		switch (error) {
		case "NotDeclare":
			System.err.println("the identify ' " + this.errID
					+ " ' has not been declared!" + " --- at line "
					+ this.errLine);

			break;
		case "NotSame":
			System.err.println("the type of the expression"
					+ " between the operator ' " + this.errID
					+ " ' must be same! --- at line " + this.errLine);
			break;
		case "ArrayIndex":
			System.err.println("the index of array must be the type of int!"
					+ " --- at line " + this.errLine);

			break;
		case "IntArray":
			System.err.println("the type of the array must be int[]!"
					+ " --- at line " + this.errLine);

			break;
		case "NotClass":
			System.err.println("the method must be called by a class!"
					+ " --- at line " + this.errLine);

			break;
		case "Parameters":
			System.err.println("the parameters of the method ' " + this.errID
					+ "() ' don't match" + " --- at line " + this.errLine);

			break;
		case "NotBoolean":
			System.err.println("the type of the expression must be boolean!"
					+ " --- at line " + this.errLine);
			break;
		case "PrintInt":
			System.err.println("the method print() can only print Integer now!"
					+ " --- at line " + this.errLine);
			break;
		default:
			System.out.println(error);
		}
		System.exit(1);
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(ast.exp.Add e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!leftty.toString().equals(this.type.toString())) {
			this.errID = "+";
			this.errLine = e.lineNum;
			error("NotSame");
		}
		this.type = new ast.type.Int();
	}

	@Override
	public void visit(ast.exp.And e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!leftty.toString().equals(this.type.toString())) {
			this.errID = "&&";
			this.errLine = e.lineNum;
			error("NotSame");
		}
		this.type = new ast.type.Boolean();
	}

	@Override
	public void visit(ast.exp.ArraySelect e) {
		e.index.accept(this);
		if (!this.type.toString().equals("@int")) {
			this.errLine = e.lineNum;
			error("ArrayIndex");
		}
		//
		// check index out of bounds?
		e.array.accept(this);
		if (!this.type.toString().equals("@int[]")) {
			this.errLine = e.lineNum;
			error("IntArray");
		}
		this.type = new ast.type.Int();
	}

	@Override
	public void visit(ast.exp.Call e) {
		ast.type.T leftty;
		ast.type.Class ty = null;

		e.exp.accept(this);
		leftty = this.type;
		if (leftty instanceof ast.type.Class) {
			ty = (ast.type.Class) leftty;
			e.type = ty.id; // the class name
		} else {
			this.errLine = e.lineNum;
			error("NotClass");
		}
		// get the args type of this method in classTable
		MethodType mty = this.classTable.getm(ty.id, e.id);
		// get the args type of this method being called
		java.util.LinkedList<ast.type.T> argsty = new java.util.LinkedList<ast.type.T>();
		if (e.args != null) {
			for (ast.exp.T a : e.args) {
				a.accept(this);
				argsty.addLast(this.type);
			}
		}
		// contrast them
		if (mty.argsType.size() != argsty.size()) {
			this.errLine = e.lineNum;
			this.errID = e.id;
			error("Parameters");
		}

		// Bug : such as "Visitor : MyVisitor(extends Visitor)" don't match
		for (int i = 0; i < argsty.size(); i++) {
			ast.dec.Dec dec = (ast.dec.Dec) mty.argsType.get(i);
			// System.out.println(dec.type + ":" + argsty.get(i));

			if (dec.type.toString().equals(argsty.get(i).toString()))
				;
			else {
				if (argsty.get(i) instanceof ast.type.Class) {
					ClassBinding cb = this.classTable.get(argsty.get(i)
							.toString());
					boolean matches = false;
					while (cb.extendss != null) {
						if (dec.type.toString().equals(cb.extendss)) {
							matches = true;
							break;
						} else
							cb = this.classTable.get(cb.extendss);
					}
					if (!matches) {
						this.errLine = e.lineNum;
						this.errID = e.id;
						error("Parameters");
					}
				} else {
					this.errLine = e.lineNum;
					this.errID = e.id;
					error("Parameters");
				}
			}
		}

		this.type = mty.retType;
		e.at = argsty;
		e.rt = this.type;
		return;
	}

	@Override
	public void visit(ast.exp.False e) {
		this.type = new ast.type.Boolean();
	}

	@Override
	public void visit(ast.exp.Id e) {
		// first look up the id in method table
		ast.type.T type = this.methodTable.get(e.id);
		// if search failed, then s.id must be a class field.
		if (type == null) {
			type = this.classTable.get(this.currentClass, e.id);
			// mark this id as a field id, this fact will be
			// useful in later phase.
			e.isField = true;
		}
		if (type == null) {
			this.errLine = e.lineNum;
			this.errID = e.id;
			error("NotDeclare");
		}
		this.type = type;
		// record this type on this node for future use.
		e.type = type;
		return;
	}

	@Override
	public void visit(ast.exp.Length e) {
		e.array.accept(this);
		if (!this.type.toString().equals("@int[]")) {
			this.errLine = e.lineNum;
			error("IntArray");
		}
		this.type = new ast.type.Int();
	}

	@Override
	public void visit(ast.exp.Lt e) {
		e.left.accept(this);
		ast.type.T ty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(ty.toString())) {
			this.errID = "<";
			this.errLine = e.lineNum;
			error("NotSame");
		}
		this.type = new ast.type.Boolean();
		return;
	}

	@Override
	public void visit(ast.exp.NewIntArray e) {
		e.exp.accept(this);
		if (!this.type.toString().equals("@int")) {
			this.errLine = e.lineNum;
			error("ArrayIndex");
		}
		this.type = new ast.type.IntArray();
	}

	@Override
	public void visit(ast.exp.NewObject e) {
		this.type = new ast.type.Class(e.id);
		return;
	}

	@Override
	public void visit(ast.exp.Not e) {
		e.exp.accept(this);
		if (!this.type.toString().equals("@boolean")) {
			this.errLine = e.lineNum;
			error("NotBoolean");
		}
		this.type = new ast.type.Boolean();
	}

	@Override
	public void visit(ast.exp.Num e) {
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.Sub e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(leftty.toString())) {
			this.errID = "-";
			this.errLine = e.lineNum;
			error("NotSame");
		}
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.This e) {
		this.type = new ast.type.Class(this.currentClass);
		return;
	}

	@Override
	public void visit(ast.exp.Times e) {
		e.left.accept(this);
		ast.type.T leftty = this.type;
		e.right.accept(this);
		if (!this.type.toString().equals(leftty.toString())) {// Not must int??
			this.errID = "*";
			this.errLine = e.lineNum;
			error("NotSame");
		}
		this.type = new ast.type.Int();
		return;
	}

	@Override
	public void visit(ast.exp.True e) {
		this.type = new ast.type.Boolean();
	}

	// statements
	@Override
	public void visit(ast.stm.Assign s) {
		// first look up the id in method table
		ast.type.T type = this.methodTable.get(s.id);
		// if failed,then search the class table
		if (type == null)
			type = this.classTable.get(this.currentClass, s.id);
		// if search failed, then s.id must have not been declared
		if (type == null) {
			this.errLine = s.exp.lineNum;
			this.errID = s.id;
			error("NotDeclare");
		}
		s.exp.accept(this);
		s.type = type; // not this.type ,this.type now is the exp type
		if (!this.type.toString().equals(type.toString())) {
			this.errLine = s.exp.lineNum;
			this.errID = "=";
			error("NotSame");
		}
		return;
	}

	@Override
	public void visit(ast.stm.AssignArray s) {
		ast.type.T type = this.methodTable.get(s.id);
		if (type == null)
			type = this.classTable.get(this.currentClass, s.id);
		if (type == null) {
			this.errID = s.id;
			this.errLine = s.index.lineNum;
			error("NotDeclare");
		}

		s.index.accept(this);
		if (!this.type.toString().equals("@int")) {
			this.errLine = s.index.lineNum;
			error("ArrayIndex");
		}
		//
		// Is it necessary to check whether the index out of bounds now?
		s.exp.accept(this);
		if (!this.type.toString().equals("@int")) {// only support int[]
			this.errLine = s.exp.lineNum;
			error("IntArray");
		}
		return;

	}

	@Override
	public void visit(ast.stm.Block s) {
		for (ast.stm.T stm : s.stms) {
			stm.accept(this);
		}
	}

	@Override
	public void visit(ast.stm.If s) {
		s.condition.accept(this);
		if (!this.type.toString().equals("@boolean")) {
			this.errLine = s.condition.lineNum;
			error("NotBoolean");
		}
		s.thenn.accept(this);
		s.elsee.accept(this);
		return;
	}

	@Override
	public void visit(ast.stm.Print s) {
		s.exp.accept(this);
		if (!this.type.toString().equals("@int")) {
			this.errLine = s.exp.lineNum;
			error("PrintInt");
		}
		return;
	}

	@Override
	public void visit(ast.stm.While s) {
		s.condition.accept(this);
		if (!this.type.toString().equals("@boolean")) {
			this.errLine = s.condition.lineNum;
			error("NotBoolean");
		}
		s.body.accept(this);
	}

	// type not use now
	@Override
	public void visit(ast.type.Boolean t) {
		System.out.println("boolean@");
	}

	@Override
	public void visit(ast.type.Class t) {
		System.out.println("class@");
	}

	@Override
	public void visit(ast.type.Int t) {
		System.out.println("int@");
	}

	@Override
	public void visit(ast.type.IntArray t) {
		System.out.println("intArray@");
	}

	// dec
	@Override
	// Because when we build the classTable and the methodTable,we have checked
	// the validity of the vars,so we don't need to check again!
	public void visit(ast.dec.Dec d) {
	}

	// method
	@Override
	public void visit(ast.method.Method m) {
		// construct the method table
		this.methodTable = new MethodTable();// methodTable don't need to be
												// stored!so we don't need to
												// make a Linkedlist
		// Up to now,I have just known that in Java a field and a local
		// variable can have a same name,so we don't need to check the local
		// variable's name here!
		this.methodTable.put(m.formals, m.locals, m.id);

		if (control.Control.elabMethodTable)
			this.methodTable.dump();

		for (ast.stm.T s : m.stms)
			s.accept(this);
		m.retExp.accept(this);

		// check whether all the local variable have been used
		this.methodTable.isVariableUsed();
		return;
	}

	// class
	@Override
	public void visit(ast.classs.Class c) {
		this.currentClass = c.id;

		for (ast.method.T m : c.methods) {
			m.accept(this);
		}

		// check whether all the fields have been used
		this.classTable.isFieldUsed(this.currentClass);

		return;
	}

	// main class
	@Override
	public void visit(ast.mainClass.MainClass c) {
		this.currentClass = c.id;
		// "main" has an argument "arg" of type "String[]", but
		// one has no chance to use it. So it's safe to skip it...

		c.stm.accept(this);
		return;
	}

	// ////////////////////////////////////////////////////////
	// step 1: build class table
	// class table for Main class
	private void buildMainClass(ast.mainClass.MainClass main) {
		this.classTable.put(main.id, new ClassBinding(null));
	}

	// class table for normal classes
	private void buildClass(ast.classs.Class c) {
		this.classTable.put(c.id, new ClassBinding(c.extendss));
		for (ast.dec.T dec : c.decs) {
			ast.dec.Dec d = (ast.dec.Dec) dec;
			this.classTable.put(c.id, d.id, d.type, d.lineNum);
		}
		for (ast.method.T method : c.methods) {
			ast.method.Method m = (ast.method.Method) method;
			this.classTable.put(c.id, m.id,
					new MethodType(m.retType, m.formals));
		}
	}

	// step 1: end
	// ///////////////////////////////////////////////////

	// program
	@Override
	public void visit(ast.program.Program p) {
		// ////////////////////////////////////////////////
		// step 1: build a symbol table for class (the class table)
		// a class table is a mapping from class names to class bindings
		// classTable: className -> ClassBinding{extends, fields, methods}
		buildMainClass((ast.mainClass.MainClass) p.mainClass);
		for (ast.classs.T c : p.classes) {
			buildClass((ast.classs.Class) c);
		}

		// we can double check that the class table is OK!
		if (control.Control.elabClassTable) {
			this.classTable.dump();
		}

		// ////////////////////////////////////////////////
		// step 2: elaborate each class in turn, under the class table
		// built above.
		p.mainClass.accept(this);
		for (ast.classs.T c : p.classes) {
			c.accept(this);
		}
	}
}

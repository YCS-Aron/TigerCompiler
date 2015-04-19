package elaborator;

import java.util.LinkedHashMap;

public class ClassBinding {
	public String extendss; // null for non-existing extends
	public LinkedHashMap<String, ast.type.T> fields;
	public LinkedHashMap<String, MethodType> methods;

	// whether the fields have been used
	public LinkedHashMap<String, Boolean> isUsed;
	// mark the field lineNum when it is declared
	public LinkedHashMap<String, Integer> fieldLines;

	public ClassBinding(String extendss) {
		this.extendss = extendss;
		this.fields = new LinkedHashMap<String, ast.type.T>();
		this.methods = new LinkedHashMap<String, MethodType>();
		this.isUsed = new LinkedHashMap<String, Boolean>();
		this.fieldLines = new LinkedHashMap<String, Integer>();
	}

	public ClassBinding(String extendss,
			java.util.LinkedHashMap<String, ast.type.T> fields,
			java.util.LinkedHashMap<String, MethodType> methods) {
		this.extendss = extendss;
		this.fields = fields;
		this.methods = methods;
	}

	public void put(String xid, ast.type.T type, int lineNum) {
		if (this.fields.get(xid) != null) {
			System.out.println("duplicated class field: " + xid);
			System.exit(1);
		}
		this.fields.put(xid, type);
		this.isUsed.put(xid, false);
		this.fieldLines.put(xid, lineNum);
	}

	public void put(String mid, MethodType mt) {
		if (this.methods.get(mid) != null) {
			System.out.println("duplicated class method: " + mid);
			System.exit(1);
		}
		this.methods.put(mid, mt);
	}

	@Override
	public String toString() {
		System.out.print("extends: ");
		if (this.extendss != null)
			System.out.println(this.extendss);
		else
			System.out.println("<>");
		System.out.println("fields:  ");
		System.out.println(fields.toString());
		System.out.println("methods:  ");
		System.out.println(methods.toString());

		return "";
	}
}

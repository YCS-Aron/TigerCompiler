package elaborator;


import java.util.Iterator;
import java.util.Map.Entry;

public class ClassTable {
	// map each class name (a string), to the class bindings.
	/* the next to do....................... */
	// we used hashtable here before,but it has a problem that it doesn't output
	// in the input order,so we change it to LinkedHashMap.

	// private java.util.Hashtable<String, ClassBinding> table;
	private java.util.LinkedHashMap<String, ClassBinding> table;

	public ClassTable() {
		// this.table = new java.util.Hashtable<String, ClassBinding>();
		this.table = new java.util.LinkedHashMap<String, ClassBinding>();
	}

	// Duplication is not allowed
	public void put(String c, ClassBinding cb) {
		if (this.table.get(c) != null) {
			System.out.println("duplicated class: " + c);
			System.exit(1);
		}
		this.table.put(c, cb);
	}

	// put a field into this table
	// Duplication is not allowed
	public void put(String c, String id, ast.type.T type, int lineNum) {
		ClassBinding cb = this.table.get(c);
		cb.put(id, type, lineNum);
		return;
	}

	// put a method into this table
	// Duplication is not allowed.
	// Also note that MiniJava does NOT allow overloading.
	public void put(String c, String id, MethodType type) {
		ClassBinding cb = this.table.get(c);
		cb.put(id, type);
		return;
	}

	// return null for non-existing class
	public ClassBinding get(String className) {
		return this.table.get(className);
	}

	// get type of some field
	// return null for non-existing field.
	public ast.type.T get(String className, String xid) {
		ClassBinding cb = this.table.get(className);
		ast.type.T type = cb.fields.get(xid);
		cb.isUsed.put(xid, true); // xid has been used
		while (type == null) { // search all parent classes until found or fail
			if (cb.extendss == null)
				return type;

			cb = this.table.get(cb.extendss);
			type = cb.fields.get(xid);
		}
		return type;
	}

	// get type of some method
	// return null for non-existing method
	public MethodType getm(String className, String mid) {
		ClassBinding cb = this.table.get(className);
		MethodType type = cb.methods.get(mid);
		while (type == null) { // search all parent classes until found or fail
			if (cb.extendss == null)
				return type;

			cb = this.table.get(cb.extendss);
			type = cb.methods.get(mid);
		}
		return type;
	}

	public void dump() {
		System.out.println("=============Class Table Start=============");
		// Enumeration<String> id = this.table.keys();
		// Enumeration<ClassBinding> classes = this.table.elements();

		Iterator<Entry<String, ClassBinding>> iterator = this.table.entrySet()
				.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			Entry<String, ClassBinding> entry = iterator.next();
			String id = entry.getKey();
			ClassBinding cb = entry.getValue();
			System.out.print(i + " : " + id + "  ");
			System.out.println(cb.toString());
			i++;
		}
		System.out.println("=============Class Table End===============\n");
		// this.table.toString();
	}

	@Override
	public String toString() {
		return this.table.toString();
	}

	public void isFieldUsed(String className) {
		ClassBinding cb = this.table.get(className);
		Iterator<Entry<String, Boolean>> iterator = cb.isUsed.entrySet()
				.iterator();
		// Enumeration<String> id = cb.isUsed.keys();
		// Enumeration<Boolean> use = cb.isUsed.elements();
		while (iterator.hasNext()) {
			Entry<String, Boolean> entry = iterator.next();
			String nowId = entry.getKey();
			boolean nowUse = entry.getValue();
			if (!nowUse)
				System.out.println("Warning: the field ' " + nowId
						+ " ' at line " + cb.fieldLines.get(nowId)
						+ " is never used ! ---- in Class " + className + " ;");
		}
	}
}

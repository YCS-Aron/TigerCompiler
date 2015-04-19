package elaborator;

import java.util.Iterator;
import java.util.Map.Entry;

import ast.type.T;

public class MethodTable {
	// private java.util.Hashtable<String, ast.type.T> table;
	private java.util.LinkedHashMap<String, ast.type.T> table;
	private java.util.LinkedHashMap<String, Boolean> isUsed;// exercise 9

	// mark the variable lineNum when it is declared
	private java.util.LinkedHashMap<String, Integer> varLines;
	String mid; // mark the name of the method

	public MethodTable() {
		// this.table = new java.util.Hashtable<String, ast.type.T>();
		this.table = new java.util.LinkedHashMap<String, ast.type.T>();
		this.isUsed = new java.util.LinkedHashMap<String, Boolean>();
		this.varLines = new java.util.LinkedHashMap<String, Integer>();
	}

	// Duplication is not allowed
	public void put(java.util.LinkedList<ast.dec.T> formals,
			java.util.LinkedList<ast.dec.T> locals, String mid) {
		this.mid = mid;
		for (ast.dec.T dec : formals) {
			ast.dec.Dec decc = (ast.dec.Dec) dec;
			if (this.table.get(decc.id) != null) {
				System.out.println("duplicated parameter: " + decc.id);
				System.exit(1);
			}
			this.table.put(decc.id, decc.type);
			// the parameter don't need to check isUsed
		}

		for (ast.dec.T dec : locals) {
			ast.dec.Dec decc = (ast.dec.Dec) dec;
			if (this.table.get(decc.id) != null) {
				System.out.println("duplicated variable: " + decc.id);
				System.exit(1);
			}
			this.table.put(decc.id, decc.type);
			this.isUsed.put(decc.id, false);
			this.varLines.put(decc.id, decc.lineNum);
		}

	}

	// return null for non-existing keys
	public ast.type.T get(String id) {
		ast.type.T type = this.table.get(id);
		if (type != null)
			this.isUsed.put(id, true);
		return type;
	}

	public void dump() {
		System.out.println("====== " + mid + " Method ======");
		// Enumeration<String> id = this.table.keys();
		// Enumeration<ast.type.T> type = this.table.elements();
		Iterator<Entry<String, T>> iterator = this.table.entrySet().iterator();
		int i = 0;
		while (iterator.hasNext()) {
			Entry<String, ast.type.T> entry = iterator.next();
			System.out.print(i + " : " + entry.getKey() + "---");
			System.out.println(entry.getValue().toString());
			i++;
		}
		System.out.println("========== End =========\n");
	}

	@Override
	public String toString() {
		return this.table.toString();
	}

	public void isVariableUsed() {
		Iterator<Entry<String, Boolean>> iterator = this.isUsed.entrySet()
				.iterator();
		// Enumeration<String> id = this.isUsed.keys();
		// Enumeration<Boolean> use = this.isUsed.elements();
		while (iterator.hasNext()) {
			Entry<String, Boolean> entry = iterator.next();
			String nowId = entry.getKey();
			boolean nowUse = entry.getValue();
			if (!nowUse)
				System.out.println("Warning: the variable ' " + nowId
						+ " ' at line " + this.varLines.get(nowId)
						+ " is never used ! ---- in Method " + mid + "();");
		}
	}
}

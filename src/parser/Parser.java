package parser;

import java.util.LinkedList;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

public class Parser {
	Lexer lexer;
	Token current;

	// String syntax;
	// String preSyntax;

	public Parser(String fname, java.io.InputStream fstream) {
		lexer = new Lexer(fname, fstream);
		current = lexer.nextToken();
	}

	// /////////////////////////////////////////////
	// utility methods to connect the lexer
	// and the parser.

	private void advance() {
		current = lexer.nextToken();
	}

	private void eatToken(Kind kind) {
		if (kind == current.kind)
			advance();
		else {
			System.err.println("Syntax Error: expects: " + kind.toString()
					+ " , ");
			String errKind = current.kind.toString()
					+ (current.kind == Token.Kind.TOKEN_ID ? (": " + current.lexeme)
							: "");
			String errPos = " ---" + current.printPos() + " , " + lexer.fname;
			System.err.println("but got: " + errKind + errPos);
			System.exit(1);
		}
	}

	private void error() {
		String errKind = current.kind.toString()
				+ (current.kind == Token.Kind.TOKEN_ID ? (": " + current.lexeme)
						: "");
		String errPos = " ---" + current.printPos() + " , " + lexer.fname;
		System.out.println("Syntax Error: compilation aborting..." + errKind
				+ errPos);
		System.exit(1);
	}

	// ////////////////////////////////////////////////////////////
	// below are method for parsing.

	// A bunch of parsing methods to parse expressions. The messy
	// parts are to deal with precedence and associativity.

	// ExpList -> Exp ExpRest*
	// ->
	// ExpRest -> , Exp
	private LinkedList<ast.exp.T> parseExpList() {
		if (current.kind == Kind.TOKEN_RPAREN)
			return null;
		LinkedList<ast.exp.T> list = new LinkedList<>();
		ast.exp.T exp = parseExp();
		list.add(exp);
		while (current.kind == Kind.TOKEN_COMMER) {
			advance();
			exp = parseExp();
			list.add(exp);
		}
		return list;
	}

	// AtomExp -> (exp)
	// -> INTEGER_LITERAL
	// -> true
	// -> false
	// -> this
	// -> id
	// -> new int [exp]
	// -> new id ()
	private ast.exp.T parseAtomExp() {
		switch (current.kind) {
		case TOKEN_LPAREN:
			advance();
			ast.exp.T exp = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			return exp;
		case TOKEN_NUM:
			ast.exp.Num num = new ast.exp.Num(Integer.parseInt(current.lexeme),
					current.getLineNum());
			advance();
			return num;
		case TOKEN_TRUE:
			advance();
			return new ast.exp.True(current.getLineNum());
		case TOKEN_FALSE: // My add
			advance();
			return new ast.exp.False(current.getLineNum());
		case TOKEN_THIS:
			advance();
			return new ast.exp.This(current.getLineNum());
		case TOKEN_ID:
			ast.exp.Id id = new ast.exp.Id(current.lexeme, current.getLineNum());
			advance();
			return id;
		case TOKEN_NEW: {
			advance();
			switch (current.kind) {
			case TOKEN_INT:
				advance();
				eatToken(Kind.TOKEN_LBRACK);
				ast.exp.T exp1 = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				return new ast.exp.NewIntArray(exp1, current.getLineNum());
			case TOKEN_ID:
				ast.exp.NewObject no = new ast.exp.NewObject(current.lexeme,
						current.getLineNum());
				advance();
				eatToken(Kind.TOKEN_LPAREN);
				eatToken(Kind.TOKEN_RPAREN);
				return no;
			default:
				error();
				return null;
			}
		}
		default:
			error();
			return null;
		}
	}

	// NotExp -> AtomExp
	// -> AtomExp .id (expList)
	// -> AtomExp [exp]
	// -> AtomExp .length
	private ast.exp.T parseNotExp() {
		ast.exp.T exp = parseAtomExp();
		// Be careful,once use "while",the production is infinite!
		while (current.kind == Kind.TOKEN_DOT
				|| current.kind == Kind.TOKEN_LBRACK) {
			if (current.kind == Kind.TOKEN_DOT) {
				advance();
				if (current.kind == Kind.TOKEN_LENGTH) {
					exp = new ast.exp.Length(exp, current.getLineNum());
					advance();
				} else {
					String id = current.lexeme;
					eatToken(Kind.TOKEN_ID);
					eatToken(Kind.TOKEN_LPAREN);
					LinkedList<ast.exp.T> args = parseExpList();
					eatToken(Kind.TOKEN_RPAREN);
					exp = new ast.exp.Call(exp, id, args, current.getLineNum());
				}
			} else {
				advance();
				ast.exp.T exp1 = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				exp = new ast.exp.ArraySelect(exp, exp1, current.getLineNum());
			}
		}
		return exp;
	}

	// TimesExp -> ! TimesExp
	// -> NotExp
	private ast.exp.T parseTimesExp() {
		int notTimes = 0; // judge the times of "!",such as"!!!!!!!!!!true"
		while (current.kind == Kind.TOKEN_NOT) {
			notTimes++;
			advance();
		}
		ast.exp.T exp = parseNotExp(); // distinguish the "NotExp" and "exp.Not"

		if (notTimes != 0)
			for (int i = 0; i < notTimes; i++)
				exp = new ast.exp.Not(exp, current.getLineNum());

		return exp;
	}

	// AddSubExp -> TimesExp * TimesExp
	// -> TimesExp

	// Note that,there is a 'Exp->Exp op Exp' production in the MiniJava
	// specification,so we use 'while' to match it
	private ast.exp.T parseAddSubExp() {
		ast.exp.T left = parseTimesExp();
		while (current.kind == Kind.TOKEN_TIMES) {
			advance();
			ast.exp.T right = parseTimesExp();
			left = new ast.exp.Times(left, right, current.getLineNum());
		}
		return left;
	}

	// LtExp -> AddSubExp + AddSubExp
	// -> AddSubExp - AddSubExp
	// -> AddSubExp
	private ast.exp.T parseLtExp() {
		ast.exp.T left = parseAddSubExp();
		while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
			Kind kind = current.kind;
			advance();
			ast.exp.T right = parseAddSubExp();
			if (kind == Kind.TOKEN_ADD)
				left = new ast.exp.Add(left, right, current.getLineNum());
			if (kind == Kind.TOKEN_SUB)
				left = new ast.exp.Sub(left, right, current.getLineNum());
		}
		return left;
	}

	// AndExp -> LtExp < LtExp
	// -> LtExp
	private ast.exp.T parseAndExp() {
		ast.exp.T left = parseLtExp();
		while (current.kind == Kind.TOKEN_LT) {
			advance();
			ast.exp.T right = parseLtExp();
			left = new ast.exp.Lt(left, right, current.getLineNum());
		}
		return left;
	}

	// Exp -> AndExp && AndExp
	// -> AndExp
	private ast.exp.T parseExp() {
		ast.exp.T left = parseAndExp();
		while (current.kind == Kind.TOKEN_AND) {
			advance();
			ast.exp.T right = parseAndExp();
			left = new ast.exp.And(left, right, current.getLineNum());
		}
		return left;
	}

	// Statement -> { Statement* }
	// -> if ( Exp ) Statement else Statement
	// -> while ( Exp ) Statement
	// -> System.out.println ( Exp ) ;
	// -> id = Exp ;
	// -> id [ Exp ]= Exp ;

	boolean comeFromVarDecls = false;// define this var to judge whether this
										// statement is after the VarDecls or
										// not,base on it,we can distinguish the
										// token "id"

	private ast.stm.T parseStatement() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a statement.
		/************ My Code *****************/
		switch (current.kind) {
		case TOKEN_LBRACE:
			eatToken(Kind.TOKEN_LBRACE);
			LinkedList<ast.stm.T> list = this.parseStatements();
			eatToken(Kind.TOKEN_RBRACE);
			return new ast.stm.Block(list);
		case TOKEN_IF:
			eatToken(Kind.TOKEN_IF);
			eatToken(Kind.TOKEN_LPAREN);
			ast.exp.T condition = this.parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			ast.stm.T thenn = this.parseStatement();
			eatToken(Kind.TOKEN_ELSE);
			ast.stm.T elsee = this.parseStatement();
			return new ast.stm.If(condition, thenn, elsee);
		case TOKEN_WHILE:
			eatToken(Kind.TOKEN_WHILE);
			eatToken(Kind.TOKEN_LPAREN);
			ast.exp.T condition1 = this.parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			ast.stm.T body = this.parseStatement();
			return new ast.stm.While(condition1, body);
		case TOKEN_SYSTEM:
			eatToken(Kind.TOKEN_SYSTEM);
			eatToken(Kind.TOKEN_DOT);
			eatToken(Kind.TOKEN_OUT);
			eatToken(Kind.TOKEN_DOT);
			eatToken(Kind.TOKEN_PRINTLN);
			eatToken(Kind.TOKEN_LPAREN);
			ast.exp.T exp = this.parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			eatToken(Kind.TOKEN_SEMI);
			return new ast.stm.Print(exp);
		case TOKEN_ID:
			String id = current.lexeme;
			if (comeFromVarDecls) {
				// if this is true , that means we have advanced the next token,
				// so we can't advance again!
				current = idAfter;
				comeFromVarDecls = false;
			} else {
				eatToken(Kind.TOKEN_ID);
			}
			boolean isAssignArray = false;
			ast.exp.T index = null;
			if (current.kind == Kind.TOKEN_LBRACK) {
				isAssignArray = true;
				eatToken(Kind.TOKEN_LBRACK);
				index = this.parseExp();
				eatToken(Kind.TOKEN_RBRACK);
			}
			eatToken(Kind.TOKEN_ASSIGN);
			ast.exp.T exp1 = this.parseExp();
			eatToken(Kind.TOKEN_SEMI);
			if (isAssignArray)
				return new ast.stm.AssignArray(id, index, exp1);
			else
				return new ast.stm.Assign(id, exp1);
		default:
			error();
		}
		return null;
	}

	// Statements -> Statement Statements
	// ->
	private LinkedList<ast.stm.T> parseStatements() {
		LinkedList<ast.stm.T> list = new LinkedList<>();
		while (current.kind == Kind.TOKEN_LBRACE
				|| current.kind == Kind.TOKEN_IF
				|| current.kind == Kind.TOKEN_WHILE
				|| current.kind == Kind.TOKEN_SYSTEM
				|| current.kind == Kind.TOKEN_ID) {
			ast.stm.T stm = parseStatement();
			list.add(stm);
		}
		return list;
	}

	// Type -> int []
	// -> boolean
	// -> int
	// -> id
	Token idAfter;
	boolean comeFromVarDecls2 = false;// define this var to distinguish whether
										// we have advance the next token or not

	private ast.type.T parseType() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a type.
		/************** My Code ****************/
		switch (current.kind) {
		case TOKEN_INT:
			eatToken(Kind.TOKEN_INT);
			boolean isIntArray = false;
			if (current.kind == Kind.TOKEN_LBRACK) {
				isIntArray = true;
				advance();
				eatToken(Kind.TOKEN_RBRACK);
			}
			if (isIntArray)
				return new ast.type.IntArray();
			else
				return new ast.type.Int();
		case TOKEN_BOOLEAN:
			eatToken(Kind.TOKEN_BOOLEAN);
			return new ast.type.Boolean();
		case TOKEN_ID:
			String id = current.lexeme;
			if (comeFromVarDecls2) {
				current = idAfter;
				comeFromVarDecls2 = false;
			} else
				eatToken(Kind.TOKEN_ID);
			return new ast.type.Class(id);
		default:
			error();
		}
		return null;
	}

	// VarDecl -> Type id ;
	private ast.dec.T parseVarDecl() {
		// to parse the "Type" nonterminal in this method, instead of writing
		// a fresh one.
		ast.type.T type = parseType();
		String id = current.lexeme;
		int lineNum = current.getLineNum();
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_SEMI);
		return new ast.dec.Dec(type, id, lineNum);
	}

	// VarDecls -> VarDecl VarDecls
	// ->
	private LinkedList<ast.dec.T> parseVarDecls() {
		// VarDecl* and Statement* can both start with "id",so it is ambiguous.
		// To distinguish it,we can read the next token.
		LinkedList<ast.dec.T> decs = new LinkedList<>();
		while (current.kind == Kind.TOKEN_INT
				|| current.kind == Kind.TOKEN_BOOLEAN
				|| current.kind == Kind.TOKEN_ID) {
			if (current.kind == Kind.TOKEN_ID) {
				idAfter = lexer.nextToken();
				if (idAfter.kind == Kind.TOKEN_ASSIGN
						|| idAfter.kind == Kind.TOKEN_LBRACK) {
					comeFromVarDecls = true;
					// if this is true,that means the VarDecls have been over,
					// so we exit the cycle
					break;
				}
				comeFromVarDecls2 = true;
			}
			ast.dec.T dec = parseVarDecl();
			decs.add(dec);
		}
		return decs;
	}

	// FormalList -> Type id FormalRest*
	// ->
	// FormalRest -> , Type id
	private LinkedList<ast.dec.T> parseFormalList() {
		LinkedList<ast.dec.T> formals = new LinkedList<>();
		if (current.kind == Kind.TOKEN_INT
				|| current.kind == Kind.TOKEN_BOOLEAN
				|| current.kind == Kind.TOKEN_ID) {
			ast.type.T type = parseType();
			String id = current.lexeme;
			int lineNum = current.getLineNum();
			eatToken(Kind.TOKEN_ID);
			formals.add(new ast.dec.Dec(type, id, lineNum));
			while (current.kind == Kind.TOKEN_COMMER) {
				advance();
				type = parseType();
				id = current.lexeme;
				lineNum = current.getLineNum();
				eatToken(Kind.TOKEN_ID);
				formals.add(new ast.dec.Dec(type, id, lineNum));
			}
		}
		return formals;
	}

	// Method -> public Type id ( FormalList )
	// { VarDecl* Statement* return Exp ;}
	private ast.method.T parseMethod() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a method.
		/************** My Code ****************/
		this.comeFromVarDecls = false;
		eatToken(Kind.TOKEN_PUBLIC);
		ast.type.T retType = this.parseType();
		String id = current.lexeme;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_LPAREN);
		LinkedList<ast.dec.T> formals = this.parseFormalList();
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_LBRACE);
		LinkedList<ast.dec.T> locals = this.parseVarDecls();
		LinkedList<ast.stm.T> stms = this.parseStatements();
		eatToken(Kind.TOKEN_RETURN);
		ast.exp.T retExp = this.parseExp();
		eatToken(Kind.TOKEN_SEMI);
		eatToken(Kind.TOKEN_RBRACE);
		return new ast.method.Method(retType, id, formals, locals, stms, retExp);
	}

	// MethodDecls -> MethodDecl MethodDecls
	// ->
	private LinkedList<ast.method.T> parseMethodDecls() {
		LinkedList<ast.method.T> list = new LinkedList<>();
		while (current.kind == Kind.TOKEN_PUBLIC) {
			ast.method.T method = parseMethod();
			list.add(method);
		}
		return list;
	}

	// ClassDecl -> class id { VarDecl* MethodDecl* }
	// -> class id extends id { VarDecl* MethodDecl* }
	private ast.classs.T parseClassDecl() {
		eatToken(Kind.TOKEN_CLASS);
		String id = current.lexeme;
		eatToken(Kind.TOKEN_ID);
		boolean hasExtends = false;
		String extendsId = null;
		if (current.kind == Kind.TOKEN_EXTENDS) {
			hasExtends = true;
			eatToken(Kind.TOKEN_EXTENDS);
			extendsId = current.lexeme;
			eatToken(Kind.TOKEN_ID);
		}
		eatToken(Kind.TOKEN_LBRACE);
		LinkedList<ast.dec.T> decs = parseVarDecls();
		LinkedList<ast.method.T> methods = parseMethodDecls();
		eatToken(Kind.TOKEN_RBRACE);
		if (hasExtends)
			return new ast.classs.Class(id, extendsId, decs, methods);
		else
			return new ast.classs.Class(id, null, decs, methods);
	}

	// ClassDecls -> ClassDecl ClassDecls
	// ->
	private LinkedList<ast.classs.T> parseClassDecls() {
		LinkedList<ast.classs.T> classes = new LinkedList<>();
		while (current.kind == Kind.TOKEN_CLASS) {
			ast.classs.T clas = parseClassDecl();
			classes.add(clas);
		}
		return classes;
	}

	// MainClass -> class id
	// {
	// public static void main ( String [] id )
	// {
	// Statement
	// }
	// }
	private ast.mainClass.T parseMainClass() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a main class as described by the
		// grammar above.
		/**************** My Code ******************/
		eatToken(Kind.TOKEN_CLASS);
		String id = current.lexeme;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_LBRACE);
		eatToken(Kind.TOKEN_PUBLIC);
		eatToken(Kind.TOKEN_STATIC);
		eatToken(Kind.TOKEN_VOID);
		eatToken(Kind.TOKEN_MAIN);
		eatToken(Kind.TOKEN_LPAREN);
		eatToken(Kind.TOKEN_STRING);
		eatToken(Kind.TOKEN_LBRACK);
		eatToken(Kind.TOKEN_RBRACK);
		String arg = current.lexeme;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_LBRACE);
		ast.stm.T stm = this.parseStatement();
		eatToken(Kind.TOKEN_RBRACE);
		eatToken(Kind.TOKEN_RBRACE);
		return new ast.mainClass.MainClass(id, arg, stm);
	}

	// Program -> MainClass ClassDecl*
	private ast.program.T parseProgram() {
		ast.mainClass.T mainClass = parseMainClass();
		LinkedList<ast.classs.T> classes = parseClassDecls();
		eatToken(Kind.TOKEN_EOF);
		return new ast.program.Program(mainClass, classes);
	}

	public ast.program.T parse() {
		ast.program.T prog = parseProgram();
		// System.out.println("Your Program is Valid!!");
		return prog;
	}
}

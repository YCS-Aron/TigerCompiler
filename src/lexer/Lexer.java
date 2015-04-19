package lexer;

import java.io.InputStream;

import lexer.Token.Kind;

public class Lexer {

	public String fname; // the input file name to be compiled
	InputStream fstream; // input stream for the above file
	int lineNum = 1;
	int colNum = 1;

	public Lexer(String fname, InputStream fstream) {
		this.fname = fname;
		this.fstream = fstream;
	}

	String[] keyWords = { "boolean", "class", "else", "extends", "false", "if",
			"int", "length", "main", "new", "out", "println", "public",
			"return", "static", "String", "System", "this", "true", "void",
			"while" };

	public void posChange(int c) {
		if (c == '\t')
			colNum += 4;
		else if ((c != '\n') && (c != '\r'))
			colNum++;
		else if (c == '\n') {
			lineNum++;
			colNum = 1;
		}
	}

	// When called, return the next token (refer to the code "Token.java")
	// from the input stream.
	// Return TOKEN_EOF when reaching the end of the input stream.
	private Token nextTokenInternal() throws Exception {
		int c = this.fstream.read();
		if (-1 == c)
			// The value for "lineNum" is now "lineNum",
			// you should modify this to an appropriate
			// line number for the "EOF" token.
			return new Token(Kind.TOKEN_EOF, lineNum, colNum);

		// skip all kinds of "blanks"
		while (' ' == c || '\t' == c || '\n' == c || '\r' == c) {
			this.posChange(c);
			c = this.fstream.read();
		}

		if (-1 == c)
			return new Token(Kind.TOKEN_EOF, lineNum, colNum++);

		switch (c) {
		case '+':
			return new Token(Kind.TOKEN_ADD, lineNum, colNum++);

			/******************* My Code ***********************/
		case '=':
			return new Token(Kind.TOKEN_ASSIGN, lineNum, colNum++);
		case ',':
			return new Token(Kind.TOKEN_COMMER, lineNum, colNum++);
		case '.':
			return new Token(Kind.TOKEN_DOT, lineNum, colNum++);
		case '{':
			return new Token(Kind.TOKEN_LBRACE, lineNum, colNum++);
		case '[':
			return new Token(Kind.TOKEN_LBRACK, lineNum, colNum++);
		case '(':
			return new Token(Kind.TOKEN_LPAREN, lineNum, colNum++);
		case '<':
			return new Token(Kind.TOKEN_LT, lineNum, colNum++);
		case '!':
			return new Token(Kind.TOKEN_NOT, lineNum, colNum++);
		case '}':
			return new Token(Kind.TOKEN_RBRACE, lineNum, colNum++);
		case ']':
			return new Token(Kind.TOKEN_RBRACK, lineNum, colNum++);
		case ')':
			return new Token(Kind.TOKEN_RPAREN, lineNum, colNum++);
		case ';':
			return new Token(Kind.TOKEN_SEMI, lineNum, colNum++);
		case '-':
			return new Token(Kind.TOKEN_SUB, lineNum, colNum++);
		case '*':
			return new Token(Kind.TOKEN_TIMES, lineNum, colNum++);
		case '&':
			c = this.fstream.read();
			if (c == '&') {
				int sColNum = colNum;
				colNum += 2;
				return new Token(Kind.TOKEN_AND, lineNum, sColNum);
			} else {
				// error
				String errInfo = "Lexical Error : invalid operator '&' , '&&' is expected";
				String posInfo = " --- at line " + lineNum + " , column "
						+ colNum + " , " + fname;
				System.err.println(errInfo + posInfo);
				return this.nextTokenInternal();
			}
		default:
			// Lab 1, exercise 2: supply missing code to
			// lex other kinds of tokens.
			// Hint: think carefully about the basic
			// data structure and algorithms. The code
			// is not that much and may be less than 50 lines. If you
			// find you are writing a lot of code, you
			// are on the wrong way.
			/******************* My Code ***********************/
			// a. Is an Integer?
			String s = (char) c + "";
			this.fstream.mark(0);
			if (c >= '0' && c <= '9') {
				c = this.fstream.read();
				while (c >= '0' && c <= '9') {
					s += (char) c;
					this.fstream.mark(0);
					c = this.fstream.read();
				}
				this.fstream.reset();
				int sColNum = colNum;
				colNum = colNum + s.length();
				return new Token(Kind.TOKEN_NUM, lineNum, sColNum, s);
			}

			// b. Is an Identifier?
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
				c = this.fstream.read();
				while (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0'
						&& c <= '9' || c == '_') {
					s += (char) c;
					this.fstream.mark(0);
					c = this.fstream.read();
				}
				this.fstream.reset();
				int sColNum = colNum;
				colNum = colNum + s.length();
				// b.1 Is a KeyWord
				for (int i = 0; i < keyWords.length; i++) {
					if (s.equals(keyWords[i])) {
						s = "TOKEN_" + s.toUpperCase();
						return new Token(Kind.valueOf(s), lineNum, sColNum);
					}
				}
				// b.2 Not a KeyWord
				return new Token(Kind.TOKEN_ID, lineNum, sColNum, s);
			}
			// c. Comments?
			if (c == '/') {
				c = this.fstream.read();
				// c.1 Comments"//"
				if (c == '/') {
					c = this.fstream.read();
					while (c != '\n') {
						c = this.fstream.read();
					}
					this.posChange(c);
					return this.nextTokenInternal();
				}
				// c.2 Comments"/*"
				if (c == '*') {
					colNum += 2;
					while (true) {
						c = this.fstream.read();
						this.posChange(c);
						if (c == '*') {
							c = this.fstream.read();
							this.posChange(c);
							if (c == '/')
								break;
						}
					}
					return this.nextTokenInternal();
				}
			}
			System.err.println("Lexical Error : invalid identifier : " + s
					+ " --- at line " + lineNum + " , column " + (colNum++)
					+ " , " + fname);
			return this.nextTokenInternal();
		}
	}

	public Token nextToken() {
		Token t = null;

		try {
			t = this.nextTokenInternal();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (control.Control.lex)
			System.out.println(t.toString());
		return t;
	}
}

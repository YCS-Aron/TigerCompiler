package codegen.bytecode;

import java.util.Hashtable;
import java.util.LinkedList;

import codegen.bytecode.stm.Aload;
import codegen.bytecode.stm.Getfield;
import codegen.bytecode.stm.Iload;
import codegen.bytecode.stm.Ldc;
import codegen.bytecode.stm.pop;
import codegen.bytecode.type.Boolean;
import codegen.bytecode.type.Class;
import ast.stm.T;
import util.Label;

// Given a Java ast, translate it into Java bytecode.

public class TranslateVisitor implements ast.Visitor
{
  private String classId;
  private int index;
  private Hashtable<String, Integer> indexTable;
  private codegen.bytecode.type.T type; // type after translation
  private codegen.bytecode.dec.T dec;
  private LinkedList<codegen.bytecode.stm.T> stms;
  private codegen.bytecode.method.T method;
  private codegen.bytecode.classs.T classs;
  private codegen.bytecode.mainClass.T mainClass;
  public codegen.bytecode.program.T program;	//mainclass classes
  
  public java.util.LinkedList<codegen.bytecode.classs.T> classes;
  public java.util.LinkedList<codegen.bytecode.dec.T> deces;
  
  private String findcls;
  private boolean isfield;

  public TranslateVisitor()
  {
    this.classId = null;
    this.indexTable = null;
    this.type = null;
    this.dec = null;
    this.stms = new java.util.LinkedList<codegen.bytecode.stm.T>();
    this.method = null;
    this.classs = null;
    this.mainClass = null;
    this.program = null;
    this.findcls = new String();
    
    classes = new java.util.LinkedList<codegen.bytecode.classs.T>();
    deces = new java.util.LinkedList<codegen.bytecode.dec.T>();
    
    isfield = false;
  }

  private void emit(codegen.bytecode.stm.T s)
  {
    this.stms.add(s);
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(ast.exp.Add e)
  {
	  e.left.accept(this);
	  e.right.accept(this);
	  emit(new codegen.bytecode.stm.Iadd());
	  return;
  }

  @Override
  public void visit(ast.exp.And e)
  {
	  e.left.accept(this);		//the result will push
	  e.right.accept(this);
	  emit(new codegen.bytecode.stm.Iand());
  }

  @Override
  public void visit(ast.exp.ArraySelect e)
  {  
	  	if( this.indexTable.get( ((ast.exp.Id)(e.array)).id ) != null)
	  	{
	  	  int indexx = this.indexTable.get( ((ast.exp.Id)(e.array)).id );
		  
		  //push reference
		  emit(new codegen.bytecode.stm.Aload(indexx));
		  //push subscript
		  e.index.accept(this);
		  //push array[index]
		  emit(new codegen.bytecode.stm.IaLoad());
		  return;
	  	}
	  	else 
	  	{
	    	emit(new codegen.bytecode.stm.Aload(0));
	    	switch(checkid(((ast.exp.Id)(e.array)).id))
	    	{
	    		case 0:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + ((ast.exp.Id)(e.array)).id,"I"));
	    			break;
	    		case -1:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + ((ast.exp.Id)(e.array)).id,"B"));
	    			break;
	    		case 1:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + ((ast.exp.Id)(e.array)).id,"[I"));
	    			break;
	    			
	    		case 2:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + ((ast.exp.Id)(e.array)).id,"L"+this.findcls+";"));
	    			break;
	    	}
			  //push subsript
			  e.index.accept(this);
			  emit(new codegen.bytecode.stm.IaLoad());
	  	}    
  }

  @Override
  public void visit(ast.exp.Call e)
  {
    e.exp.accept(this);
    if(e.args != null)
    for(ast.exp.T x : e.args) {
      x.accept(this);
    }   	
    e.rt.accept(this);
    codegen.bytecode.type.T rt = this.type;
    java.util.LinkedList<codegen.bytecode.type.T> at = new java.util.LinkedList<codegen.bytecode.type.T>();
    for (ast.type.T t : e.at) {
      t.accept(this);
      at.add(this.type);
    }
    emit(new codegen.bytecode.stm.Invokevirtual(e.id, e.type, at, rt));	//at is paramenters,type is the type of exp
    return;
  }

  @Override
  public void visit(ast.exp.False e)
  {
	  emit(new codegen.bytecode.stm.Ldc(0));
	  return;
  }

  @Override
//  ..
  public void visit(ast.exp.Id e)
  {
	  if(this.indexTable.get(e.id) != null)
	  {
	    int index = this.indexTable.get(e.id);	//indexTable is the map from id to index
	    ast.type.T type = e.type;
	    if (type.getNum() > 0)			// a reference
	      emit(new codegen.bytecode.stm.Aload(index));
	    else
	      emit(new codegen.bytecode.stm.Iload(index));
	    // but what about this is a field?
	    return;
	  }
	  else
	  {
	    	emit(new codegen.bytecode.stm.Aload(0));
	    	switch(checkid(e.id))
	    	{
	    		case 0:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + e.id,"I"));
	    			break;
	    		case -1:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + e.id,"B"));
	    			break;
	    		case 1:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + e.id,"[I"));
	    			break;
	    		case 2:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + e.id,"L"+this.findcls+";"));
	    			break;
	    	}
	  }
  }

  @Override
  public void visit(ast.exp.Length e)
  {
	  	if( this.indexTable.get( ((ast.exp.Id)(e.array)).id ) != null)
	  	{
	  		int index = this.indexTable.get( ((ast.exp.Id)(e.array)).id );
	  	    //push reference
	  	    emit(new codegen.bytecode.stm.Aload(index));
	  	    //push subscript
	  	    emit(new codegen.bytecode.stm.Ldc(0));
	  	    //push array[0]
	  	    emit(new codegen.bytecode.stm.IaLoad()); 
	  	    return;	  		
	  	}
	  	else
	  	{
	    	emit(new codegen.bytecode.stm.Aload(0));
	    	switch(checkid(((ast.exp.Id)(e.array)).id))
	    	{
	    		case 0:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + ((ast.exp.Id)(e.array)).id,"I"));
	    			break;
	    		case -1:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + ((ast.exp.Id)(e.array)).id,"B"));
	    			break;
	    		case 1:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + ((ast.exp.Id)(e.array)).id,"[I"));
	    			break;
	    		case 2:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + ((ast.exp.Id)(e.array)).id,"L"+this.findcls+";"));
	    			break;
	    	}
	    	emit(new codegen.bytecode.stm.Arraylength());
	  	}
  }

  @Override
  public void visit(ast.exp.Lt e)
  {
    Label tl = new Label(), fl = new Label(), el = new Label();				
    e.left.accept(this);
    e.right.accept(this);
    emit(new codegen.bytecode.stm.Ificmplt(tl));
    
    //not large than
    emit(new codegen.bytecode.stm.Label(fl));	//output   L_XX :
    emit(new codegen.bytecode.stm.Ldc(0));
    emit(new codegen.bytecode.stm.Goto(el));
    
    //large than
    emit(new codegen.bytecode.stm.Label(tl));
    emit(new codegen.bytecode.stm.Ldc(1));
    emit(new codegen.bytecode.stm.Goto(el));
    emit(new codegen.bytecode.stm.Label(el));
    return;
  }

  @Override
  public void visit(ast.exp.NewIntArray e)
  {
	  e.exp.accept(this);
	  emit(new codegen.bytecode.stm.Newarray());
	  return;
  }

  @Override
  public void visit(ast.exp.NewObject e)
  {
	  emit(new codegen.bytecode.stm.New(e.id));
	  return;
  }

  @Override
  public void visit(ast.exp.Not e)
  {
	    Label tl = new Label(), fl = new Label(), el = new Label();

	    e.exp.accept(this);
	    //keng BBBBBBBBBBBBBBBBB
	    
	    emit(new codegen.bytecode.stm.Ifne(tl));
	    
	    //if top equals zero
	    emit(new codegen.bytecode.stm.Label(fl));
	    emit(new codegen.bytecode.stm.Ldc(1));
	    emit(new codegen.bytecode.stm.Goto(el));
	    
	    //if top larger than zero
	    emit(new codegen.bytecode.stm.Label(tl));
	    emit(new codegen.bytecode.stm.Ldc(0));
	    emit(new codegen.bytecode.stm.Goto(el));
	    emit(new codegen.bytecode.stm.Label(el));
	    return;
  }

  @Override
  public void visit(ast.exp.Num e)
  {
    emit(new codegen.bytecode.stm.Ldc(e.num));
    return;
  }

  @Override
  public void visit(ast.exp.Sub e)
  {
    e.left.accept(this);
    e.right.accept(this);
    emit(new codegen.bytecode.stm.Isub());			
    return;
  }

  @Override
  public void visit(ast.exp.This e)
  {
    emit(new codegen.bytecode.stm.Aload(0));
    return;
  }

  @Override
  public void visit(ast.exp.Times e)
  {
    e.left.accept(this);
    e.right.accept(this);
    emit(new codegen.bytecode.stm.Imul());
    return;
  }

  @Override
  public void visit(ast.exp.True e)
  {
	  emit(new codegen.bytecode.stm.Ldc(1));
	  return;
  }

  // statements
  @Override
  public void visit(ast.stm.Assign s)
  { 
    if(this.indexTable.get(s.id) != null)
    {
    	s.exp.accept(this);
	    int index = this.indexTable.get(s.id);
	    ast.type.T type = s.type;
	    if (type.getNum() > 0)
	      emit(new codegen.bytecode.stm.Astore(index));
	    else
	      emit(new codegen.bytecode.stm.Istore(index));
	
	    return;
    }
    else
    {
    	emit(new codegen.bytecode.stm.Aload(0));
    	s.exp.accept(this);
    	switch(checkid(s.id))
    	{
    		case 0:
    			emit(new codegen.bytecode.stm.Putfield(this.classId+ "/" + s.id,"I"));
    			break;
    		case -1:
    			emit(new codegen.bytecode.stm.Putfield(this.classId+ "/" + s.id,"B"));
    			break;
    		case 1:
    			emit(new codegen.bytecode.stm.Putfield(this.classId+ "/" + s.id,"[I"));
    			break;
    		case 2:
    			emit(new codegen.bytecode.stm.Putfield(this.classId+ "/" + s.id,"L"+this.findcls+";"));
    			break;
    	}
    }
  }

  @Override
//  ..
  public void visit(ast.stm.AssignArray s)
  {
	  if(this.indexTable.get(s.id) != null)
	  {
		  int indexx = this.indexTable.get(s.id);
	
		  //push reference
		  emit(new codegen.bytecode.stm.Aload(indexx));
		  //push subsript
		  s.index.accept(this);
		  //push right value
		  s.exp.accept(this);	
		  //assign
		  emit(new codegen.bytecode.stm.IaStore());
		  return ;
	  }
	  else
	  {
	    	emit(new codegen.bytecode.stm.Aload(0));
	    	switch(checkid(s.id))
	    	{
	    		case 0:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + s.id,"I"));
	    			break;
	    		case -1:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + s.id,"B"));
	    			break;
	    		case 1:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + s.id,"[I"));
	    			break;
	    		case 2:
	    			emit(new codegen.bytecode.stm.Getfield(this.classId+ "/" + s.id,"L"+this.findcls+";"));
	    			break;
	    	}
			  //push subsript
			  s.index.accept(this);
			  //push right value
			  s.exp.accept(this);
			  emit(new codegen.bytecode.stm.IaStore());
	   } 
		  
  }

  @Override
  public void visit(ast.stm.Block s)
  {
	  int len = s.stms.size();
	  
	  for(int i=0;i<len;i++)
	  {
		  s.stms.get(i).accept(this);
	  }
	  return;
  }

  @Override
  public void visit(ast.stm.If s)
  {
    Label tl = new Label(), fl = new Label(), el = new Label();
    s.condition.accept(this);
    emit(new codegen.bytecode.stm.Ifne(tl));	
    
    emit(new codegen.bytecode.stm.Label(fl));
    s.elsee.accept(this);
    emit(new codegen.bytecode.stm.Goto(el));
    
    emit(new codegen.bytecode.stm.Label(tl));
    s.thenn.accept(this);
    emit(new codegen.bytecode.stm.Goto(el));
    emit(new codegen.bytecode.stm.Label(el));
    return;
  }

  @Override
  public void visit(ast.stm.Print s)
  {
    s.exp.accept(this);
    emit(new codegen.bytecode.stm.Print());
    return;
  }

  @Override
  public void visit(ast.stm.While s)
  {
	    Label tl = new Label(),el = new Label();

	    //goto condition statement
	    emit(new codegen.bytecode.stm.Goto(el));
	    
	    //body block
	    emit(new codegen.bytecode.stm.Label(tl));
	    s.body.accept(this);
	    
	    //conditon statement
	    emit(new codegen.bytecode.stm.Label(el));
	    s.condition.accept(this);
	    emit(new codegen.bytecode.stm.Ifne(tl));
	        
	    return;
  }

  // type
  @Override
  public void visit(ast.type.Boolean t)
  {
	  this.type = new codegen.bytecode.type.Boolean();
	  return;
  }

  @Override
  public void visit(ast.type.Class t)
  {
	  this.type = new codegen.bytecode.type.Class(t.id);
	  return;
  }

  @Override
  public void visit(ast.type.Int t)
  {
	  this.type = new codegen.bytecode.type.Int();
	  return;    
  }

  @Override
  public void visit(ast.type.IntArray t)
  {
	  this.type = new codegen.bytecode.type.IntArray();
	  return;
  }

  // dec
  @Override
  public void visit(ast.dec.Dec d)
  {
    d.type.accept(this);
    this.dec = new codegen.bytecode.dec.Dec(this.type, d.id);
    if(this.isfield)
    {
    	return;
    }
    this.indexTable.put(d.id, index++);
    return;
  }

  // method
  @Override
  public void visit(ast.method.Method m)
  {
    // record, in a hash table, each var's index
    // this index will be used in the load store operation
    this.index = 1;
    this.indexTable = new java.util.Hashtable<String, Integer>();

    m.retType.accept(this);
    codegen.bytecode.type.T newRetType = this.type;
    java.util.LinkedList<codegen.bytecode.dec.T> newFormals = new java.util.LinkedList<codegen.bytecode.dec.T>();
    for (ast.dec.T d : m.formals) {
      d.accept(this);
      newFormals.add(this.dec);
    }

    java.util.LinkedList<codegen.bytecode.dec.T> newDeces = new java.util.LinkedList<codegen.bytecode.dec.T>();
    for (ast.dec.T d : m.locals) {
      d.accept(this);		//accept = add to indexTable
      newDeces.add(this.dec);
    }
    this.stms = new java.util.LinkedList<codegen.bytecode.stm.T>();
    for (ast.stm.T s : m.stms) {
      s.accept(this);
    }

    // return statement is specially treated
    m.retExp.accept(this);

    if (m.retType.getNum() > 0)
      emit(new codegen.bytecode.stm.Areturn());
    else
      emit(new codegen.bytecode.stm.Ireturn());

    this.method = new codegen.bytecode.method.Method(newRetType, m.id,
        this.classId, newFormals, newDeces, this.stms, 0, this.index);

    return;
  }

  // class
  @Override
  public void visit(ast.classs.Class c)
  {
    this.classId = c.id;
    this.indexTable = new Hashtable<String, Integer>();
    
    //Decs
    this.deces = new java.util.LinkedList<codegen.bytecode.dec.T>();
    
    //enter field
    this.isfield = true;
    
    for (ast.dec.T dec : c.decs) {
      dec.accept(this);
      this.deces.add(this.dec);
    }
    
  //get father's decs
    if(c.extendss != null)
    {
//    	System.out.println("get father"+c.extendss+"'s decs");
    	codegen.bytecode.classs.Class father = null;
    	
    	if(this.classes != null)
    	{
	    	int len =this.classes.size();
	    	
	    	for(int i=0;i<len;i++)
	    	{
	    		if( ((codegen.bytecode.classs.Class)(classes.get(i))).id.equals(c.extendss) )
	    		{
	    			father = ((codegen.bytecode.classs.Class)(classes.get(i)));
	    		}
	    	}
	    	
	    	//get father's decs
	    	len = father.decs.size();
	    	for(int i = 0;i<len;i++)
	    	{
	    		codegen.bytecode.dec.Dec d = new codegen.bytecode.dec.Dec(((codegen.bytecode.dec.Dec)(father.decs.get(i))).type ,((codegen.bytecode.dec.Dec)(father.decs.get(i))).id); 
	    		this.deces.add(d);
	    	}
    	}
    }
    
    //exit field
    this.isfield = false;
    
    //methods
    java.util.LinkedList<codegen.bytecode.method.T> newMethods = new java.util.LinkedList<codegen.bytecode.method.T>();
    for (ast.method.T m : c.methods) {
      m.accept(this);
      newMethods.add(this.method);
    }
    
    //new class
    this.classs = new codegen.bytecode.classs.Class(c.id, c.extendss, this.deces,
        newMethods);
    return;
  }

  // main class
  @Override
  public void visit(ast.mainClass.MainClass c)
  {
    c.stm.accept(this);
    this.mainClass = new codegen.bytecode.mainClass.MainClass(c.id, c.arg,
        this.stms);
    this.stms = new java.util.LinkedList<codegen.bytecode.stm.T>();
    return;
  }

  // program
  @Override
  public void visit(ast.program.Program p)
  {
	  
    // do translations
    p.mainClass.accept(this);

    //class list
 //   java.util.LinkedList<codegen.bytecode.classs.T> newClasses = new java.util.LinkedList<codegen.bytecode.classs.T>();
    for (ast.classs.T classs : p.classes) {
      classs.accept(this);
//      System.out.println( ((ast.classs.Class)(classs)).id + " added");
      this.classes.add(this.classs);
    }
    
    //generate program
    this.program = new codegen.bytecode.program.Program(this.mainClass,classes);
    return;
  }
	
	private int checkid(String e)
	{
		codegen.bytecode.type.T type=null;
		  

		int len = deces.size();
			  
		for(int i=0;i<len;i++)
			if( ((codegen.bytecode.dec.Dec)(deces.get(i))).id.equals(e)) 
			{
				type = ((codegen.bytecode.dec.Dec)(deces.get(i))).type;
				break;
			}
			  
		if(type instanceof codegen.bytecode.type.Int)
			return 0;
		else if(type instanceof codegen.bytecode.type.Boolean)
			return -1;
		else if(type instanceof codegen.bytecode.type.IntArray)
			return 1;
		else
		{
			//get the class of the id
			this.findcls = ((codegen.bytecode.type.Class)(type)).id;
			return 2;
		}
	}
}
package codegen.C;

import codegen.C.dec.Dec;
import control.Control;

public class PrettyPrintVisitor implements Visitor
{
  private int indentLevel;
  private java.io.BufferedWriter writer;
  String gc_map;

  public PrettyPrintVisitor()
  {
    this.indentLevel = 2;
  }

  private void indent()
  {
    this.indentLevel += 2;
  }

  private void unIndent()
  {
    this.indentLevel -= 2;
  }

  private void printSpaces()
  {
    int i = this.indentLevel;
    while (i-- != 0)
      this.say(" ");
  }

  private void sayln(String s)
  {
    say(s);
    try {
      this.writer.write("\n");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void say(String s)
  {
    try {
      this.writer.write(s);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(codegen.C.exp.Add e)
  {
		if (!(e.left instanceof codegen.C.exp.Num) && !(e.left instanceof codegen.C.exp.Id))
			this.say("(");
		e.left.accept(this);
		if (!(e.left instanceof codegen.C.exp.Num) && !(e.left instanceof codegen.C.exp.Id))
			this.say(")");
		this.say(" + ");
		if (!(e.right instanceof codegen.C.exp.Num) && !(e.right instanceof codegen.C.exp.Id))
			this.say("(");
		e.right.accept(this);
		if (!(e.right instanceof codegen.C.exp.Num) && !(e.right instanceof codegen.C.exp.Id))
			this.say(")");
  }

  @Override
  public void visit(codegen.C.exp.And e)
  {
	  this.say("(");
	  e.left.accept(this);
	  this.say(")");
	  this.say(" && ");
	  this.say("(");
	  e.right.accept(this);
	  this.say(")");
  }

  @Override
  public void visit(codegen.C.exp.ArraySelect e)
  {
	  e.array.accept(this);
	  this.say("[");
	  e.index.accept(this);
	  this.say("]");
  }

  @Override
  public void visit(codegen.C.exp.Call e)
  {
    this.say("(frame." + e.assign + "=");
    e.exp.accept(this);
    this.say(", ");
    this.say("frame."+e.assign + "->vptr->" + e.id + "(");
    int size = e.args.size();
    
    for(int i=0;i<size;i++) 
    {
      (e.args.get(i)).accept(this);;
      if(i != size-1)
    	  this.say(", ");
    }

    this.say("))");
    return;
  }

  @Override
  public void visit(codegen.C.exp.Id e)
  {
    this.say(e.id);
  }

  @Override
  public void visit(codegen.C.exp.Length e)
  {
	  this.say("*(("+((codegen.C.exp.Id)(e.array)).id+")+2)");
	  return;
  }

  @Override
  public void visit(codegen.C.exp.Lt e)
  {
	  this.say("(");
    e.left.accept(this);
    this.say(")");
    this.say(" < ");
    this.say("(");
    e.right.accept(this);
    this.say(")");
    return;
  }

  @Override
  public void visit(codegen.C.exp.NewIntArray e)
  {
	  this.say("(int"  +"*)Tiger_new_array (");
	  e.exp.accept(this);
	  this.say(")");
	  return;
  }

  @Override
  public void visit(codegen.C.exp.NewObject e)
  {  
	    this.say("((struct " + e.id + "_class" + "*)(Tiger_new (&" + e.id
	        + "_vtable_, sizeof(struct " + e.id + "_class" +")/4 )))");
	    return;
  }

  @Override
  public void visit(codegen.C.exp.Not e)
  {
	  this.say("!(");
	  e.exp.accept(this);
	  this.say(")");
  }

  @Override
  public void visit(codegen.C.exp.Num e)
  {
    this.say(Integer.toString(e.num));
    return;
  }

  @Override
  public void visit(codegen.C.exp.Sub e)
  {
		if (!(e.left instanceof codegen.C.exp.Num) && !(e.left instanceof codegen.C.exp.Id))
			this.say("(");
		e.left.accept(this);
		if (!(e.left instanceof codegen.C.exp.Num) && !(e.left instanceof codegen.C.exp.Id))
			this.say(")");
		this.say(" - ");
		if (!(e.right instanceof codegen.C.exp.Num) && !(e.right instanceof codegen.C.exp.Id))
			this.say("(");
		e.right.accept(this);
		if (!(e.right instanceof codegen.C.exp.Num) && !(e.right instanceof codegen.C.exp.Id))
			this.say(")");
  }

  @Override
  public void visit(codegen.C.exp.This e)
  {
    this.say("this");
  }

  @Override
  public void visit(codegen.C.exp.Times e)
  {
    e.left.accept(this);
    this.say(" * ");
    e.right.accept(this);
    return;
  }

  // statements
  @Override
  public void visit(codegen.C.stm.Assign s)
  {
    if(s.exp instanceof codegen.C.exp.Id && ((codegen.C.exp.Id)(s.exp)).isref==1 )
    {
        this.printSpaces();
    	this.sayln("Interceptor(" + s.id + " , " + ((codegen.C.exp.Id)(s.exp)).id + ");");
        this.printSpaces();
        this.say(s.id + " = ");
        s.exp.accept(this);
        this.sayln(";");   	
    }
    else if(s.exp instanceof codegen.C.exp.Call && ( ((codegen.C.exp.Call)(s.exp)).rt instanceof codegen.C.type.Class || ((codegen.C.exp.Call)(s.exp)).rt instanceof codegen.C.type.IntArray ) )
    {
    	codegen.C.exp.Call temp =  ((codegen.C.exp.Call)(s.exp)); 
    	codegen.C.type.Class ret = ((codegen.C.type.Class)(temp.rt));
    	
        this.printSpaces();   	
    	this.sayln("struct " + ret.id + " *" + "temp_" + temp.assign + ";");
    	
        this.printSpaces();
    	this.say("temp_" + temp.assign + " = ");  	
        this.say("(frame." + temp.assign + "=");
        temp.exp.accept(this);
        this.say(", ");
        this.say("frame."+temp.assign + "->vptr->" + temp.id + "(");
        int size = temp.args.size();       
        for(int i=0;i<size;i++) 
        {
          (temp.args.get(i)).accept(this);
          if(i != size-1)
        	  this.say(", ");
        }
        this.sayln("));");

        this.printSpaces();
    	this.sayln("Interceptor(" + s.id + " , " + "temp_" + temp.assign + ");"); 
        
        this.printSpaces();
        this.sayln(s.id + " = " + "temp_" + temp.assign + ";");	
    }
    else if(s.exp instanceof codegen.C.exp.NewIntArray)
    {
    	this.printSpaces();
    	this.say("int *tempttt = ");
    	s.exp.accept(this);
        this.sayln(";"); 
        this.printSpaces();
        this.sayln(s.id + " = tempttt;");
    }
    else 
    {
    	
        this.printSpaces();
        this.say(s.id + " = ");
        s.exp.accept(this);
        this.sayln(";");   
    	
    }
    return;
  }
  
  public void visit(codegen.C.exp.False f)
  {
	  this.say("0");
	  return;
  }	
  
  public void visit(codegen.C.exp.True t)
  {
	  this.say("1");
	  return;
  }

  @Override
  public void visit(codegen.C.stm.AssignArray s)
  {
	  this.say("  "+s.id+"[");
	  s.index.accept(this);
	  this.say("] = ");
	  s.exp.accept(this);
	  this.sayln(";");
  }

  @Override
  public void visit(codegen.C.stm.Block s)
  {
	  this.sayln("{");
	  int len = s.stms.size();

	  for(int i = 0;i<len;i++)
	  {
		  s.stms.get(i).accept(this);
	  }

	  this.sayln("}");
  }

  @Override
  public void visit(codegen.C.stm.If s)
  {
    this.printSpaces();
    this.say("if (");
    s.condition.accept(this);
    this.sayln(")");
    this.indent();
    this.printSpaces();
    this.sayln("{");
    s.thenn.accept(this);
    this.printSpaces();
    this.sayln("}");
    this.unIndent();
    this.sayln("");
    this.printSpaces();
    this.sayln("else");
    this.indent();
    this.printSpaces();
    this.sayln("{");
    s.elsee.accept(this);
    this.printSpaces();
    this.sayln("}");
    this.sayln("");
    this.unIndent();
    return;
  }

  @Override
  public void visit(codegen.C.stm.Print s)
  {
    this.printSpaces();
    this.say("System_out_println (");
    s.exp.accept(this);
    this.sayln(");");
    return;
  }

  @Override
  public void visit(codegen.C.stm.While s)
  {
	  this.say("  while(");
	  s.condition.accept(this);
	  this.sayln(")"+"\n"+"  {");
	  s.body.accept(this);
	  this.sayln("  }");
  }

  // type
  @Override
  public void visit(codegen.C.type.Class t)
  {
    this.say("struct " + t.id + "_class" + " *");
  }

  @Override
  public void visit(codegen.C.type.Int t)
  {
    this.say("int ");
  }

  @Override
  public void visit(codegen.C.type.IntArray t)
  {
	  this.say("int * ");
  }
  
  public void visit(codegen.C.type.Boolean t)
  {
	  this.say("int ");
  }

  // dec
  @Override
  public void visit(codegen.C.dec.Dec d)
  {	
	d.type.accept(this);
	this.say(" "+d.id);
	return;
  }

  // method
  //AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
  @Override
  public void visit(codegen.C.method.Method m) 
  {
	    //GC maps declaration
	    String arguments_gc_map = new String();
	    int locals_gc_count = 0;
	
	    //struct gc_frame declare
		this.sayln("  struct "+m.classId+"_"+m.id+"_gc_frame");
		this.sayln("{");
		this.sayln("  void *prev;");
		this.sayln("  char *arguments_gc_map;");
		this.sayln("  int *arguments_base_address;");
		this.sayln("  int locals_gc_count;");
		
		int len = m.locals.size();
		for(int i=0;i<len;i++)
		{
			if(((codegen.C.dec.Dec)(m.locals.get(i))).type instanceof codegen.C.type.IntArray)
			{
				this.sayln("  int * " + ((codegen.C.dec.Dec)(m.locals.get(i))).id+";");
				locals_gc_count++;
			}
			else if(((codegen.C.dec.Dec)(m.locals.get(i))).type instanceof codegen.C.type.Class)
			{
				this.sayln("  struct " + ((codegen.C.type.Class)(((codegen.C.dec.Dec)(m.locals.get(i))).type)).id+"_class "+"* "+ ((codegen.C.dec.Dec)(m.locals.get(i))).id+";");
				locals_gc_count++;
			}			
		}
		
		this.sayln("};");
		this.sayln("");
	    
		m.retType.accept(this);
		this.say(" " + m.classId + "_" + m.id + "(");
		
		//formals
		int size = m.formals.size();
		for (int i = 0;i<size;i++) 
		{
			codegen.C.dec.Dec dec = (codegen.C.dec.Dec)(m.formals.get(i));
		    
		    //print arguments
			dec.type.accept(this);
			
			this.say(" " + dec.id);
			if (i != size-1)
				this.say(", ");
			
		    //arguments gc map
			if(i == 0)
				continue;
		    if(((codegen.C.dec.Dec)(dec)).type instanceof codegen.C.type.Boolean)
		    	arguments_gc_map = arguments_gc_map+"0";
		    else if(((codegen.C.dec.Dec)(dec)).type instanceof codegen.C.type.Int)
		    	arguments_gc_map = arguments_gc_map+"0";
		    else if(((codegen.C.dec.Dec)(dec)).type instanceof codegen.C.type.IntArray)
		    	arguments_gc_map = arguments_gc_map+"1";
		    else 
		    	arguments_gc_map = arguments_gc_map+"1";
		}
		this.sayln(")");
		this.sayln("{");
		
		//print gc-map declare
		this.sayln("  struct "+m.classId+"_"+m.id+"_gc_frame"+" frame;");
		this.sayln("");
		
		//locals
		len = m.locals.size();
		for(int i=0;i<len;i++)
		{
			if(((codegen.C.dec.Dec)(m.locals.get(i))).type instanceof codegen.C.type.Int)
			{
				this.sayln("  int " + ((codegen.C.dec.Dec)(m.locals.get(i))).id+";");
			}
			else if(((codegen.C.dec.Dec)(m.locals.get(i))).type instanceof codegen.C.type.Boolean)
			{
				this.sayln("  int " + ((codegen.C.dec.Dec)(m.locals.get(i))).id+";");
			}
		}
		
		//print gc-map assgin
		this.sayln("  frame.prev = prev;");
		this.sayln("  prev = &frame;");
		this.sayln("  frame.arguments_gc_map = "+'"'+arguments_gc_map+'"'+';');
		this.sayln("  frame.arguments_base_address = "+"(int*)&this;");
		this.sayln("  frame.locals_gc_count = "+locals_gc_count+';');	
		len = m.locals.size();
		for(int i=0;i<len;i++)
			if(((codegen.C.dec.Dec)(m.locals.get(i))).type instanceof codegen.C.type.IntArray)
				this.sayln("frame."+((codegen.C.dec.Dec)(m.locals.get(i))).id+" = 0;");
			else if(((codegen.C.dec.Dec)(m.locals.get(i))).type instanceof codegen.C.type.Class)
				this.sayln("frame."+((codegen.C.dec.Dec)(m.locals.get(i))).id+" = 0;");
		
		this.sayln("");
		
		//stms
		for (codegen.C.stm.T s : m.stms)
			s.accept(this);
		this.sayln("");
		
		//before return
		//pop off the GC stack frame 

		this.sayln("  prev = frame.prev;");
		this.sayln("");
				
		//return
		this.say("  return ");
		if(m.retType instanceof codegen.C.type.Boolean)
		{
			this.say("(int)(");
			m.retExp.accept(this);
			this.sayln(");");
		}
		else
		{
			m.retExp.accept(this);
			this.sayln(";");
		}
		this.sayln("}");
	}

//AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
  @Override
  public void visit(codegen.C.mainMethod.MainMethod m)
  {
	  int locals_gc_count=0;
	  String arguments_gc_map =  new String();

	    //struct gc_frame declare
		this.sayln("  struct main_gc_frame");
		this.sayln("{");
		this.sayln("  void *prev;");
		this.sayln("  char *arguments_gc_map;");
		this.sayln("  int *arguments_base_address;");
		this.sayln("  int locals_gc_count;");
		
		int len = m.locals.size();
		for(int i=0;i<len;i++)
		{
			if(((codegen.C.dec.Dec)(m.locals.get(i))).type instanceof codegen.C.type.IntArray)
			{
				this.sayln("  int * " + ((codegen.C.dec.Dec)(m.locals.get(i))).id+";");
				locals_gc_count++;
			}
			else if(((codegen.C.dec.Dec)(m.locals.get(i))).type instanceof codegen.C.type.Class)
			{
				this.sayln("  struct " + ((codegen.C.type.Class)(((codegen.C.dec.Dec)(m.locals.get(i))).type)).id+"_class"+"* "+ ((codegen.C.dec.Dec)(m.locals.get(i))).id+";");
				locals_gc_count++;
			}			
		}
		
		this.sayln("};");	  
	  
    this.sayln("int Tiger_main ()");
    this.sayln("{");
	
	//print gc-map declare
	this.sayln("  struct main_gc_frame"+" frame;");
	this.sayln("");

	//print gc-map assgin
	this.sayln("  prev = 0;");
	this.sayln("  frame.prev = prev;");
	this.sayln("  prev = &frame;");
	this.sayln("  frame.arguments_gc_map = "+'"'+arguments_gc_map+'"'+';');
	this.sayln("  frame.arguments_base_address = 0;");
	this.sayln("  frame.locals_gc_count = "+locals_gc_count+';');	
	
    m.stm.accept(this);
    this.sayln("}\n");
    return;
  }

  // vtables
  //AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
  @Override
  public void visit(codegen.C.vtable.Vtable v)
  {
	int sizeofargu;
    this.sayln("struct " + v.id + "_vtable");
    this.sayln("{");
    
    //declare gc_map
    this.sayln("char* "+v.id+"_gc_map;");
    
    for (codegen.C.Ftuple t : v.ms) 
    {
      this.say("  ");
      t.ret.accept(this);
      this.say(" (*" + t.id + ")(");
      sizeofargu = t.args.size();
      this.say("struct "+v.id+"_class"+" *this");
      if(sizeofargu != 0)
    	  this.say(",");
      for (int i = 0;i<sizeofargu;i++)
      {
    	  t.args.get(i).accept(this);
    	  if(i != sizeofargu-1)
    		  this.say(",");
      }
      this.sayln(");");
    }
    this.sayln("};\n");
    return;
  }

  private void outputVtable(codegen.C.vtable.Vtable v)
  {
	  int size = v.ms.size();
    this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = ");
    this.sayln("{");
    this.say("  "+'"'+v.gc_map+'"');
    if(size == 0)
    {
    	this.sayln("\n};\n");
    	return;
    }
    this.sayln(",");
    for (int i = 0;i<size;i++) {
      this.say("  ");
      this.say(v.ms.get(i).classs + "_" + v.ms.get(i).id);
      if(i != size-1)
    	  this.sayln(",");
    }
    this.sayln("\n};\n");
    return;
  }

  // class
  @Override
  public void visit(codegen.C.classs.Class c)
  {  
    this.sayln("struct " + c.id + "_class");
    this.sayln("{");
    this.sayln("  struct " + c.id + "_vtable *vptr;");
    this.sayln("int isObjOrArray;");			
    this.sayln("void *forwarding;");
    this.sayln("unsigned mark;");			
    for (codegen.C.Tuple t : c.decs) 
    {    	
    		t.type.accept(this);
    		this.sayln(t.id + ";");
    }
    this.sayln("};");
    return;
  }

  // program
  @Override
  public void visit(codegen.C.program.Program p)
  {
    // we'd like to output to a file, rather than the "stdout".
    try {
      String outputName = null;
      if (Control.outputName != null)
        outputName = Control.outputName;
      else if (Control.fileName != null)
        outputName = Control.fileName + ".c";
      else
        outputName = "a.c";

      this.writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
          new java.io.FileOutputStream(outputName)));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    this.sayln("// This is automatically generated by the Tiger compiler.");
    this.sayln("// Do NOT modify!\n");
    this.sayln("extern void* prev;");

    this.sayln("//vtable structures declaration");
    sayvdec(p.vtables);
    
    this.sayln("// structures");
    for (codegen.C.classs.T c : p.classes) {
      c.accept(this);
    }
    
    this.sayln("// vtables structures");
    for (codegen.C.vtable.T v : p.vtables) {
      v.accept(this);
    }
    this.sayln("");  
    
    this.sayln("// forward declare vtable");
    sayvdec2(p.vtables);

    this.sayln("// methods");
    for (codegen.C.method.T m : p.methods) {
      m.accept(this);
    }
    this.sayln("");   
    
    this.sayln("// vtables");
    for (codegen.C.vtable.T v : p.vtables) {
      outputVtable((codegen.C.vtable.Vtable) v);
    }
    this.sayln(""); 

    this.sayln("// main method");
    p.mainMethod.accept(this);
    this.sayln("");

    this.say("\n\n");

    try 
    {
      this.writer.close();
    } catch (Exception e) 
    {
      e.printStackTrace();
      System.exit(1);
    }

  }
  
  void sayvdec(java.util.LinkedList<codegen.C.vtable.T> vtables)
  {
	  int len = vtables.size();
	  for(int i=0;i<len;i++)
		  this.sayln("struct " + ((codegen.C.vtable.Vtable)(vtables.get(i))).id + "_vtable;");	  
  }
  
  void sayvdec2(java.util.LinkedList<codegen.C.vtable.T> vtables)
  {
	  int len = vtables.size();
	  for(int i=0;i<len;i++)
		  this.sayln("struct " + ((codegen.C.vtable.Vtable)(vtables.get(i))).id + "_vtable " + ((codegen.C.vtable.Vtable)(vtables.get(i))).id + "_vtable_; ");	  
  }
  
}

class Factorial {
	public static void main(String[] a) {
		System.out.println(new Fac2().ComputeFac(10));
	}
}

class Memery220{ 		//20B
	int[] c;
	public int fun()	//if call this function,it's 20B+200B
	{
		c= new int[50];
		return 919191;
	}
}

class Fac{		//16B
	public int ComputeFac(int num) 
	{
		int c;
		int i;
		int k;
		Memery220 a;		//generate youngth objects
		k=42;
		i=0;
		while(i < k)		//send Fac2.ComputeFac.a to old_gen
		{
			a = new Memery220(); 	//every 12 times alloc Memery will call gc
			c=a.fun();
			System.out.println(c);
			i=i+1;
		}
		return 9999;
	}
}

class Fac2
{
	public int ComputeFac(int num) 
	{
		int c;
		Fac a;			//generate old objects
		while(true)
		{
			a = new Fac(); 	//
			c=a.ComputeFac(90);
			System.out.println(c);
		}
		return 9999;
	}	
}






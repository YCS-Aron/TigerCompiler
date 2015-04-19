class Factorial {
	public static void main(String[] a) {
		System.out.println(new Fac().ComputeFac(10));
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


class Fac{
	public int ComputeFac(int num) 
	{
		int c;
		Memery220 a;
		while(true)
		{
			a = new Memery220(); 	//
			c=a.fun();
			System.out.println(c);
		}
		System.out.println(42);
		return 9999;
	}
}

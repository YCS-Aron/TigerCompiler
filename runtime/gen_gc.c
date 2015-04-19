#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// The Generation Garbage Collector.


//===============================================================//
// The Java Heap data structure.

/*   
      ----------------------------------------------------
      |                        |                         |
      ----------------------------------------------------
      ^\                      /^
      | \<~~~~~~~ size ~~~~~>/ |
    from                       to
 */
 
struct JavaHeap
{
  int Heapsize;         // in bytes
  double youth_new_rate;		//12%
  double youth_senior_rate; 	//4%
  double old_rate;				//80%
  
  int youth_new_size;
  int youth_senior_size;
  int old_size;
  
  int *youth_gen;
  int *old_gen;
  int *youth_new;
  int *youth_senior;
  int *from;       // the "from" space pointer
  int *to;         // the "to" space pointer
  int *toStart;    // "start" address in the "to" space
  int *toNext;     // "next" free space pointer in the to space
  
  int* old_Alloc;
  int* youth_Alloc;

  int *List;
  int *LP;
  int Listsize;
  
  int *two_bound;
  int *one_bound;
  
  char* bitmap;		//bitmap用来标记年老代
  int bmsize;		//1000B * 80% = 800B  800bit/8=100B
  char* bitmapbase;	//bitmap 标记的空间的始址
  
  int new_Alloced;
  int from_Alloced;
  int to_Alloced;
  int old_Alloced;


/*
 //list里存的是间代指针值
 //bitmap_XXX用来标识被修改的引用的位置
  int *List_new;
  int List_new_size; 	//根据new的大小算出来	5%
  int *List_new_LP;		
  int *bitmap_new;	
  int bitmap_new_size;	//根据new的大小算出来  heap.youth_new_size/32(+1)
  int *bitmap_new_base;	//heap.youth_new
  
  int *List_senior;
  int List_senior_size; //根据senior的大小算出来
  int *List_senior_LP;
  int *bitmap_senior;
  int bitmap_senior_size;	//根据new的大小算出来  heap.youth_new_size/32(+1)
  int *bitmap_senior_base;
  
  int *List_old;
  int List_old_size; 	//根据old的大小算出来
  int *List_old_LP;
  int *bitmap_old;
  int bitmap_old_size;		//根据new的大小算出来  heap.youth_new_size/32(+1)
  int *bitmap_old_base;
  
  int seg_count;		//统计一共有多少段
  int *seg_base;
  int *seg_tag;
*/
};

// The Java heap, which is initialized by the following
// "heap_init" function.
struct JavaHeap heap;
int segment_size = 12800;
int List_size = 200;

// Lab 4, exercise 10:
// Given the heap size (in bytes), allocate a Java heap
// in the C heap, initialize the relevant fields.

void Tiger_heap_init(int heapSize,int ls)
{
  int temp;
//  char *heapstart = (char *)malloc(sizeof(char)*heapSize);   //逻辑错误，唉唉唉唉唉唉
  int *heapstart = (int *)malloc(sizeof(char)*heapSize);
  
  heap.Heapsize = heapSize;     	//in bytes
  heap.youth_new_rate = 0.12;		//12%
  heap.youth_senior_rate = 0.04; 	//4%
  heap.old_rate = 0.8;				//80% 
  heap.youth_new_size = heapSize * heap.youth_new_rate/4;
  heap.youth_senior_size = heapSize * heap.youth_senior_rate/4;  //half of senior section
  heap.old_size = heapSize * heap.old_rate/4;

  heap.youth_gen = heapstart;
  heap.old_gen = heapstart + heap.youth_new_size + heap.youth_senior_size*2;
  heap.youth_new = heapstart;
  heap.youth_senior = heapstart + heap.youth_new_size;
  heap.from = heapstart + heap.youth_new_size;  
  heap.to = heapstart + heap.youth_new_size + heap.youth_senior_size;  
  
  heap.youth_Alloc = heap.youth_gen;     
  heap.old_Alloc = heap.old_gen;
  
  heap.toStart = heapstart + heap.youth_new_size + heap.youth_senior_size;   
  heap.toNext = heapstart + heap.youth_new_size + heap.youth_senior_size;     
  
  heap.two_bound = heapstart + heap.youth_new_size;
  heap.one_bound = heapstart + heap.youth_new_size;

  heap.Listsize = ls; 
  heap.List = (int*)malloc(sizeof(int)*ls);
  heap.LP = heap.List;
  
  temp = heap.old_size%4 == 0 ? heap.old_size/4 : heap.old_size/4+1 ;	//需要的位数
  if(temp%8 == 0)
	heap.bmsize = temp/8;
  else
	heap.bmsize = temp/8+1;
  heap.bitmap = (char*)malloc(sizeof(char)*heap.bmsize);
  memset(heap.bitmap,0,heap.bmsize);
  heap.bitmapbase = (char*)heap.old_gen;
  
  heap.new_Alloced=0;
  heap.from_Alloced=0;
  heap.to_Alloced=0;
  heap.old_Alloced=0;

 /* 
  //size指的字节数
  heap.List_new_size = ((int)(heap.youth_new_size*0.05)); 	//根据new的大小算出来	5%
  heap.List_new = (int *)malloc(sizeof(char)*heap.List_new_size);
  heap.List_new_LP = heap.List_new;		

  heap.bitmap_new_size = (heap.youth_new_size/4)%8==0?(heap.youth_new_size/4)%8:(heap.youth_new_size/4)%8+1;	//根据new的大小算出来  heap.youth_new_size/32(+1)
  heap.bitmap_new = (int *)malloc(sizeof(char)*heap.bitmap_new_size);	
  heap.bitmap_new_base = heap.bitmap_new;	
  
  heap.List_senior_size = ((int)(heap.youth_senior_size*2*0.05));  //根据senior的大小算出来
  heap.List_senior = (int*)malloc(sizeof(char)*heap.List_senior_size);
  heap.List_senior_LP = heap.List_senior;
  
  heap.bitmap_senior_size = (heap.youth_senior_size*2/4)%8==0?(heap.youth_senior_size*2/4)%8:(heap.youth_senior_size*2/4)%8+1;	
  heap.bitmap_senior = (int*)malloc(sizeof(char)*heap.bitmap_senior_size);
  heap.bitmap_senior_base = heap.bitmap_senior;
  
  int *List_old;
  int List_old_size; 	//根据old的大小算出来
  int *List_old_LP;
  int *bitmap_old;
  int bitmap_old_size;		//根据new的大小算出来  heap.youth_new_size/32(+1)
  int *bitmap_old_base;
  
  int seg_count;		//统计一共有多少段
  int *seg_base;
  int *seg_tag;
*/
  return;
  }

// The "prev" pointer, pointing to the top frame on the GC stack. 
// (see part A of Lab 4)
void *prev = 0;

void exchange()
{
	int* tmp;

	tmp = heap.from;
	heap.from = heap.to;
	heap.to = tmp;
		
	heap.toStart = heap.to;
	heap.toNext = heap.to;
	
	heap.from_Alloced = heap.to_Alloced;
	heap.to_Alloced = 0;
}

int isinfrom(void* add)
{
	if((int)add>=(int)heap.from && (int)add<(int)heap.from+heap.youth_senior_size)
		return 1;
	else 
		return 0;
}

int isinto(void* add)
{
	if((int)add>=(int)heap.to && (int)add<(int)heap.to+heap.youth_senior_size)
		return 1;
	else 
		return 0;
}

int isinnew(void* add)
{
	if((int)add>=(int)heap.youth_gen && (int)add<=(int)heap.youth_senior)
		return 1;
	else 
		return 0;
}

int isinold(void* add)
{
	if((int)add>=(int)heap.old_gen && (int)add<(int)heap.old_gen + heap.old_size)
		return 1;
	else 
		return 0;
}

int isobject(void* ob)
{
	if(*((int*)ob+1) == 1)
		return 1;
	else 
		return 0;
}

int isfwa(void* ob)
{
	if(isobject(ob))
		return *((int*)ob+2) == 0?0:1;
	else
		return *((int*)ob+3) == 0?0:1;
}

int isintwo(void* add)
{
	if((int)add>=(int)heap.from && (int)add<(int)heap.two_bound)
		return 1;
	else 
		return 0;
}

int isinone(void* add)
{
	if((int)add>=(int)heap.two_bound && (int)add<(int)heap.one_bound)
		return 1;
	else 
		return 0;	
}

unsigned getdone(void* v)
{
	return (unsigned)((*((unsigned*)v)) & 0x3fffffff);
}

void setdone(void* v,unsigned key)
{
	unsigned temp = (*((unsigned*)v));
	temp = temp & 0xc0000000;		//保留最高两位,其它位清零
	temp = temp | (key & 0x3fffffff);
	(*((unsigned*)v)) = temp;
}

void mark(void* pc)
{
	char* map;
	int size;
	int i;
	int* p;
	int* t;		
	int* y;		
	
	if(isobject(pc) == 1 && isinold(pc) == 1)
	{
		//int* markaddr = (int*)(*((int*)pc+3;
		int* markaddr = (int*)pc+3;
		if(bitmap_get(pc) == 0)	
		{
			t = NULL;
			bitmap_set1(pc);		
			setdone((void*)markaddr,0);		
			while(1)
			{
				i=getdone((void*)markaddr);     
				map = *((char**)(*(int*)pc));
				size = strlen(map);
				
				if(i < size)
				{
					if(map[i]==1)
					//进入第一个没有标记的孩子，此处，t用来保存其父节点，y用来保存即将访问的孩子
					{
						y=(int*)(*((int*)pc+4+i));	//获得第i个field的指针
						if(isobject(y)==1 && bitmap_get(y)==0 && isinold(y)==1) 	//y为对象，且没有标记
						{
							(*((int*)pc+4+i)) = (int)t;	//原来的域指针存父亲地址，直接覆盖掉
							t = pc;		//赋值t为pc，准备开始标记pc的孩子
							pc = y;		//将pc的第i个孩子复制给pc
						}
						//如果是数组，也作为一个孩子处理，只是一进去就会发生回溯
						//此时，pc已经设置成了要标记的孩子结点
					//	markaddr = (int*)(*((int*)pc+3));     ai ai ai ai ai ai ai ai ai ai ai ai ai ai ai ai ai
						markaddr = (int*)pc+3;
						bitmap_set1(pc);  	//标记新的pc/////////////////////////////////////////
						setdone((void*)markaddr,0);		//从pc的第一个field开始标记
					}
					else
						setdone((void*)markaddr,i+1);
				}
				else
				//已经全部标记，或者，是个数组
				//调整到上一个节点的下一个未标记的孩子
				{
					y = pc;	//y专门用来存该结点之前的标记的结点？
					//!!!!
					pc = t;	//父亲节点赋值给pc，回溯
					markaddr = (int*)pc+3;
					if(pc == NULL)
						return;
					i = getdone((void*)markaddr);	
					t = (int*)(*((int*)pc+4+i));	//将之前存储的上一个节点取出，相当于“出栈”
					//发生回溯，则前一个结点必然是一个对象
					//!!!!
					*((int*)pc+4+i) = (int)y;	//交换指针，还原
					setdone((void*)markaddr,i+1);
				}
			}
		}
	}
	else if(isobject(pc) == 1 && isinold(pc) == 0)
		return;
	else if(isobject(pc) == 0 && isinold(pc) == 1)
		bitmap_set1(pc);
	else 
		return;
}

void Lisp2(void** pre,void** nex,void** scn)
{
	int free = 0,size,i,j;
	void* cp;
	char* map;
	int classsize;
	int* pc,*newaddr,*base,*list;
	char *arguments_map;
	int* p;
	
	//STEP 1 mark   reversal pointor******************************************************
	p=(int*)prev;
	
	/*root in frame*/
	while(p)
	{
		//arguments
		base = (int*)(*((int*)p+2));
	
		if(base != 0)		//非Main函数
		{
			arguments_map = (char*)(*((int*)p+1));
			size = strlen(arguments_map);
			
			if(isinold(base)==1)
				mark((void*)base);
			
			for(i=0;i<size;i++)
			{
				if(arguments_map[i] == 1)
				//为在old_gen中的对象，则标记
				{
					cp = (int*)(*((int*)base+1+i));
					if(isinold(cp))
					{
						if(isobject(cp))
							mark((void*)cp);
						else
							bitmap_set1((void*)(*((int*)cp+4)));
					}
				}
			}
		}
				
		//locals
		size = *((int*)p+3);
		for(i=0;i<size;i++)
		{
			cp = (int*)(*((int*)p+4+i));
			if(isinold(cp))
			//为在old_gen中的对象，则标记
			{
				if(isobject(cp))
					mark(cp);
				else
					bitmap_set1((void*)(*((int*)cp+4)));
			}	
		}				
		p = (int*)(*(int*)p);	//取上一层的frame
	}		
	
	/*root in list*/
	list = heap.List;
	while(list < heap.LP)
	{
		cp = (int*)(*((int*)list));
		if(isinold(cp))
		//为在old_gen中的对象，则标记
		{
			if(isobject(cp))
				mark(cp);
			else
				bitmap_set1((void*)(*((int*)cp+4)));
		}		
		list+=1;
	} 
	
	//STEP 2 store new address in field forwarding*********************************
	size = heap.bmsize;	

	for(i=0;i<size;i++)
	{
		if(bitmap_sub(i) == 1)
		{
			pc = (int*)heap.old_gen+i;
			if(isobject(pc))
			{
				*((int*)pc+2) = (int)heap.old_gen + free;				//给forwarding赋值
				classsize = strlen(*(char**)(*((int*)pc)))*4+16;	//size of bytes
				free+=classsize;		//update size
			}
			else
			{
				*((int*)pc+3) = (int)heap.old_gen + free;				//给forwarding赋值
				classsize = (*((int*)pc+2))*4+20;	//size of bytes
				free+=classsize;		//update size				
			}
		}
	}
	//***********************************************************************************
	//更新next和pre,scan
//	*((int**)nex) = (int*)free;
	*((int**)nex) = heap.old_gen+free;
	heap.old_Alloc = heap.old_gen+free;
/*	int* ppp = *((int**)pre);
	int* pp = *((int**)nex);
	
	if(isobject(ppp) == 1)
	{
		*((int**)pre) = (int*)(*(ppp+2));
		*((int**)nex) = (int*)(*(ppp+2))+(pp-ppp);
		*((int**)scn) = (int*)(*(ppp+2))+(pp-ppp) - ((char*)pp - (char*)(*((int**)scn)))/4;
	}
	else
	{
		*((int**)pre) = (int*)(*(ppp+3));
		*((int**)nex) = (int*)(*(ppp+3))+(pp-ppp);
		*((int**)scn) = (int*)(*(ppp+3))+(pp-ppp) - ((char*)pp - (char*)(*((int**)scn)))/4;
	}
*/
	
	//STEP 3 modify pointor,begin with root**********************************************
	//先修改所有root中的引用的地址
	p=(int*)prev;
	
	/*root in frame*/
	while(p)
	{
		//arguments
		base = (int*)(*((int*)p+2));
		arguments_map = (char*)(*((int*)p+1));
		size = strlen(arguments_map);
		
		for(i=0;i<size;i++)
		{
			if(arguments_map[i] == 1)
			//为在old_gen中的对象
			{
				cp = (int*)(*((int*)base+1+i));
				if(isinold(cp))
				{
					if(isobject(cp))
						*((int*)base+1+i) = *((int*)cp+2);		//r = r.forwarding，更新指向root的指针
					else
						*((int*)base+1+i) = *((int*)cp+3);		//r = r.forwarding，更新指向root的指针
				}
			}
		}
				
		//locals
		size = *((int*)p+3);
		for(i=0;i<size;i++)
		{
			cp = (int*)(*((int*)p+4+i));
			if(isinold(cp))
			//为在old_gen中的对象，则标记
			{
				if(isobject(cp))
					*((int*)p+4+i) = *((int*)cp+2);		//r = r.forwarding，更新指向root的指针
				else
					*((int*)p+4+i) = *((int*)cp+3);		//r = r.forwarding，更新指向root的指针
			}	
		}				
		p = (int*)(*(int*)p);	//取上一层的frame
	}		
	
	/*root in list*/
	list = heap.List;
	while(list < heap.LP)
	{
		cp = (int*)(*((int*)list));
		if(isinold(cp))
		//为在old_gen中的对象
		{
			if(isobject(cp))
				*((int*)list) = *((int*)cp+2);		//r = r.forwarding，更新指向root的指针
			else
				*((int*)list) = *((int*)cp+3);		//r = r.forwarding，更新指向root的指针
		}		
		list+=1;
	} 	
	
	//再修改非root结点的引用成员，数组因为没有成员则不处理
	for(i=0;i<size;i++)
	{
		if(bitmap_sub(i) == 1)
		{
			pc = (int*)heap.old_gen+i;
			if(isobject(pc) ==1 && *((int*)pc+2) != 0)
			//如果遇到一个活的对象结点
			{
				map = *(char**)(*((int*)pc));
				size = strlen(map);		
				for(i=0;i<size;i++)
				{
					if(map[i] == 1)
					{
						int* field = (int*)(*((int*)pc+4+i));
						if(isobject(field) == 1)
							*((int*)pc+4+i) = *(field+2);
						else
							*((int*)pc+4+i) = *(field+3);
					}
				}
			}
			//else 数组不用处理它的域
		}
	}
	
	//STEP 4 move******************************************************************
	for(i=0;i<size;i++)
	{
		if(bitmap_sub(i) == 1)
		{
			pc = (int*)heap.old_gen+i;
			map = *(char**)(*((int*)pc));
			size = strlen(map);		//对象的size，不是数组的size	
			if(isobject(pc) == 1)
			{
				newaddr = (int*)(*((int*)pc+2));
				classsize = (size+4)*4;
				for(j=0;j<classsize;i++)			//从低地址向高地址拷贝
					*((char*)newaddr+j) = *((char*)pc+j);
			}
			else
			{
				newaddr = (int*)(*((int*)pc+3));
				classsize = (*(pc+2)+5)*4;
				for(j=0;j<classsize;i++)			//从低地址向高地址拷贝
					*((char*)newaddr+j) = *((char*)pc+j);
			}
		}
	}
	//清空bitmap
	memset(heap.bitmap,0,heap.bmsize);
//	heap.old_Alloc = (int*)free;
	heap.old_Alloc = heap.old_gen+free;
	
	printf("old_gen collected!~~~~\n");
}

void* Forward(void* p,void** pre,void** nex,void** scn,int stage)
{
	int object_size,i,size;
	int* next = *((int**)nex);
	int* temp = next;
	
	if(isobject(p) == 1)
	{
		size = strlen(*(char**)(*(int*)p));	//size of fields
		object_size = 4+size;
	}
	else
		object_size = 5+*((int*)p+2);
		
	switch(stage)
	{
	case 1:
		if(object_size+next >= heap.old_gen+heap.old_size)
			Lisp2(pre,nex,scn);
		next = *((int**)nex);	
		if(isintwo(p) == 1)
		{
			if(isfwa(p) == 0)
			{
				for(i=0;i<object_size;i++)	
					*((int*)next+i) = *((int*)((int*)p+i));
				next = (int*)next + object_size;
				*((int**)nex) = next;		
				*((int**)pre) = temp;		
				if(isobject(p))
					*((int*)p+2) = (int)temp;
				else
					*((int*)p+3) = (int)temp;
				//返回拷贝到的首地址
				return (void*)temp;
			}
			else 
			//已经拷贝过了，则只需要返回forwarding
			{
				if(isobject(p)==1)
					return (void*)(*((int*)p+2));
				else
					return (void*)(*((int*)p+3));
			}
		}
		else
			return p;
		break;
	case 2:
		if(isinone(p) == 1)
		{
			if(isfwa(p) == 0)
			{
				//拷贝
				for(i=0;i<object_size;i++)	
					*((int*)next+i) = *((int*)((int*)p+i));
				next = (int*)next + object_size;
				*((int**)nex) = next;		//
				*((int**)pre) = temp;		//
				//更新被拷贝对象的forwarding
				if(isobject(p))
					*((int*)p+2) = (int)temp;
				else
					*((int*)p+3) = (int)temp;
				//返回拷贝到的首地址
				return (void*)temp;
			}
			else 
			{
				if(isobject(p))
					return (void*)(*((int*)p+2));
				else
					return (void*)(*((int*)p+3));
			}
		}
		else
			return p;
		break;
	case 3:
		if(isinnew(p) == 1)
		{
			if(isfwa(p) == 0)
			{
				//拷贝
				for(i=0;i<object_size;i++)	
					*((int*)next+i) = *((int*)p+i);
				next = (int*)next + object_size;	
				*((int**)nex) = next;		//
				*((int**)pre) = temp;		//
				//更新被拷贝对象的forwarding//
				if(isobject(p))
					*((int*)p+2) = (int)temp;
				else
					*((int*)p+3) = (int)temp;
				//返回拷贝到的首地址
				return (void*)temp;
			}
			else 
			{
				if(isobject(p))
					return (void*)(*((int*)p+2));
				else
					return (void*)(*((int*)p+3));
			}
		}
		else
			return p;
		break;	
	}
	*next = (int)nex;
}
//===============================================================//
// The Gimple Garbage Collector

int isfinal(void* pc)
{
	char* map = *((char**)(*(int*)pc));
	int size = strlen(map);
	int i;
	
	for(i=0;i<size;i++)
	{
		if(map[i] == 1 && *(((int*)(*((int*)pc+4+i)))+2) == 0 )
			return 0;
	}
	return 1;
}  

void setisinlist1(void* v)
{
	(*((unsigned*)v)) |= 0x80000000;
}

void setisinlist0(void* v)
{
	(*((unsigned*)v)) &= 0x7FFFFFFF;
}

unsigned getisinlist(void* v)
{
	return ((*((unsigned*)v)) & 0x80000000)>0? 1 : 0 ;
}

int bitmap_set1(void* index)
//index为地址值，设置index对应的位图位为1
{
	int bitcount = ((int)((char*)index-(char*)heap.bitmapbase))/4;		// 字节数/4  为整数个数   即为位数
    int charcount = bitcount/8;
	int spare = bitcount%8;
    unsigned char x = (0x1<<(7-spare));		//在字节内是从左到右标记
    if(charcount > heap.bmsize)
        return 0;
    heap.bitmap[charcount] |= x;

	int i;
	for(i=0;i<320;i++)
		printf("%d",bitmap_sub(i));
//		getchar();
	
    return 1; 
}

int bitmap_set0(void* index)
//index为地址值，设置index对应的位图位为0
{
	int bitcount = ((int)((char*)index-(char*)heap.bitmapbase))/4;		// 字节数/4  为整数个数   即为位数
    int charcount = bitcount/8;
	int spare = bitcount%8;
    unsigned char x = (0x1<<(7-spare));
	x=~x;
    if(charcount > heap.bmsize)
        return 0;
    heap.bitmap[charcount] &= x;
    return 1; 
	
}

int bitmap_get(void* index)
{
	int bitcount = ((int)((char*)index-(char*)heap.bitmapbase))/4;		// 字节数/4  为整数个数   即为位数
    int charcount = bitcount/8;
	int spare = bitcount%8;
    unsigned char x = (0x1<<(7-spare));
    unsigned char res;
    if( charcount > heap.bmsize)
        return -1;
    res = heap.bitmap[charcount] & x;
    return res > 0 ? 1 : 0; 
}

int bitmap_sub(int index)
{
    int charcount = index/8;
    int spare = index%8;
    unsigned char x = (0x1<<(7-spare));
    unsigned char res;
    if( charcount > heap.bmsize)
        return -1;
    res = heap.bitmap[charcount] & x;
    return res > 0 ? 1 : 0; 	
}

void extendlist()
{	
	int i,newsize;
	int *newlist;
	newsize = 2*heap.Listsize;
	newlist = (int*)malloc(sizeof(int)*newsize);
	for(i=0;i<heap.Listsize;i++)
		*(newlist+i) = *((int*)heap.List+i);
		
	heap.List = newlist;
	heap.Listsize = newsize;
	heap.LP = newlist + i;
}

void Interceptor(void* opa,void* opb)
{
	if(isinnew(opa) == 1)
	{
//		if(isinnew(opb) == 0)  //这样写是不安全的，逻辑错误
		if(isinfrom(opb) == 1 || isinto(opb) == 1 || isinold(opb) == 1)
		{
			if(getisinlist(opb) == 0)
			{
				if(heap.LP == heap.List+heap.Listsize)
					extendlist();
				setisinlist1(opb);
				*(heap.LP) = (int)opb;
				heap.LP++;
			}
		}
	}
	else if(isinfrom(opa) == 1)
	{
//		if(isinfrom(opb) == 0)
		if(isinnew(opb) == 1 || isinto(opb) == 1 || isinold(opb) == 1)
		{
			if(getisinlist(opb) == 0)
			{
				if(heap.LP == heap.List+heap.Listsize)
					extendlist();
				setisinlist1(opb);
				*(heap.LP) = (int)opb;
				heap.LP++;
			}
		}		
	}
	else if(isinto(opa) == 1)
	{
//		if(isinto(opb) == 0)
		if(isinfrom(opb) == 1 || isinnew(opb) == 1 || isinold(opb) == 1)
		{
			if(getisinlist(opb) == 0)
			{
				if(heap.LP == heap.List+heap.Listsize)
					extendlist();
				setisinlist1(opb);
				*(heap.LP) = (int)opb;
				heap.LP++;
			}
		}	
	}
	else if(isinold(opa) == 1)
	{
//		if(isinold(opb) == 0)
		if(isinfrom(opb) == 1 || isinto(opb) == 1 || isinnew(opb) == 1)
		{
			if(getisinlist(opb) == 0)
			{
				if(heap.LP == heap.List+heap.Listsize)
					extendlist();
				setisinlist1(opb);
				*(heap.LP) = (int)opb;
				heap.LP++;
			}
		}		
	}	
}

// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
void Tiger_gen_gc()
{
	// Your code here:
	int* scan,*next,*p;
	int size,i,gg;
	int* base,*list,*begin,*end,*preclass,*temp;
	char* arguments_map;
	int* b_nex;
	int stage;
	
	heap.new_Alloced=0;
	heap.from_Alloced=0;
	heap.to_Alloced=0;
	
	printf("\n garbage collect......\n");
	printf("now,collect objects with 2 age:......\n");
	printf("before collect,size of objects with 2 age:%dB \n",(heap.two_bound-heap.from)*4);

	//STEP 1 :拷贝年龄为2的对象到老化区
	stage = 1;
	p=(int*)prev;
	scan = (int*)heap.old_Alloc;
	next = (int*)heap.old_Alloc;
	b_nex = next;
	preclass = NULL;		//通过preclass指针和对象的大小计算移动后的next//preclass==null代表拷贝的是第一个对象
	
	/*root in frame*/
	while(p)
	{	
		//arguments
		base = (int*)(*((int*)p+2));	
		arguments_map = (char*)(*((int*)p+1));
		size = strlen(arguments_map);
		if(base != 0)		//防止回溯到main函数的base，此时base为空
		{
			*((int*)base) = (int)Forward((void*)(*((int*)base)),(void**)(&preclass),(void**)(&next),NULL,stage);
			for(i=0;i<size;i++)
				if(arguments_map[i] == 1 && isintwo((void*)(*((int*)base+1+i))) == 1)	
				//gc只保证指针指向的位置是对的
					*((int*)base+1+i) = (int)Forward((void*)(*((int*)base+1+i)),(void**)(&preclass),(void**)(&next),NULL,stage);	//????
		}			
	
		//locals
		size = *((int*)p+3);
		for(i=0;i<size;i++)
			if(isintwo((void*)(*((int*)p+4+i))))
				*((int*)p+4+i) = (int)Forward((void*)(*((int*)p+4+i)),(void**)(&preclass),(void**)(&next),NULL,stage);		//????
				
		p = (int*)(*(int*)p);	//取上一层的frame
	}		
	
	/*root in list*/
	list = heap.List;
	while(list < heap.LP)
	//list中本身就存的是对象或者数组的指针
	{
		if(isintwo((void*)(*((int*)list))) == 1)
		//在two中
			*(int*)list = (int)Forward((void*)(*((int*)list)),(void**)(&preclass),(void**)(&next),0,stage);		//??
		
		list++;
	} 

	while((int*)scan < (int*)next)
	{
		if(isobject(scan))
		//scan指向的成员为对象
		{
			char* fields_map = *(char**)(*(int*)scan);
			size = strlen(fields_map);
			scan+=4;
			for(i=0;i<size;i++)
			{
				if(fields_map[i] == 1 && isintwo((void*)(*scan)))
				//filed为对象，且在two中
				{
					*(int*)scan = (int)Forward((void*)(*(int*)scan),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);
					scan+=1;	
				}
				else
					scan+=1;
			}		
		}
		else
		//没有引用域，则跳过
			scan+=(5 + *((int*)scan+2));
	}
	
	//更改old_gen中的指针
	gg = (heap.two_bound-heap.from)*4;
	
	heap.old_Alloc = next;
	heap.old_Alloced = (heap.old_Alloc - heap.old_gen)*4;
	
	printf("collect garbage:%dB\n",gg-(next-b_nex)*4);
	printf("after collect,living 2 objects  / alloced in old :%dB / %dB\n",(next-b_nex)*4,(heap.old_Alloc-heap.old_gen)*4);
	
	//*******************************************************************************************
	//STEP 2 :拷贝年龄为1的结点到to
	stage = 2;
	p=(int*)prev;
	scan = (int*)heap.toStart;
	next = (int*)heap.toNext;
	b_nex = next;
	
	printf("now,collect objects with 1 age:......\n");
	printf("before collect,size of objects with 1 age:%dB \n",(heap.one_bound-heap.two_bound)*4);
	
	/*root in frame*/
	while(p)
	{
		//arguments
		base = (int*)(*((int*)p+2));	
		arguments_map = (char*)(*((int*)p+1));
		size = strlen(arguments_map);
		if(base != 0)
		{
			*((int*)base) = (int)Forward((void*)(*((int*)base)),(void**)(&preclass),(void**)(&next),NULL,stage);
			for(i=0;i<size;i++)
				if(arguments_map[i] == 1 && isinone((void*)(*((int*)base+1+i))))
					*((int*)base+1+i) = (int)Forward((void*)(*((int*)base+1+i)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);
		}

		//locals
		size = *((int*)p+3);
		for(i=0;i<size;i++)
			if(isinone((void*)(*((int*)p+4+i))))
			//指向one
				*((int*)p+4+i) = (int)Forward((void*)(*((int*)p+4+i)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);	
				
		p = (int*)(*(int*)p);	//取上一层的frame
	}		
	
	/*root in list*/
	list = heap.List;
	while(list < heap.LP)
	{
		if(isinone((void*)(*((int*)list))))
		//指向two，并且没有拷贝到old_gen中
			*(int*)list = (int)Forward((void*)(*((int*)list)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);
			
		list+=1;
	} 
	
	while((int*)scan < (int*)next)
	{
		if(isobject(scan))
		//为对象，且没拷贝
		{
			char* fields_map = *(char**)(*(int*)scan);
			size = strlen(fields_map);
			scan+=4;
			for(i=0;i<size;i++)
			{
				if(fields_map[i] == 1 && isinone((void*)(*scan)))
				//filed为对象,且在one中
				{
					*(int*)scan = (int)Forward((void*)(*(int*)scan),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);
					scan+=1;		
				}
				else
					scan+=1;
			}		
		}
		else
			scan+=(5 + *((int*)scan+2));
	}
	
	gg = (heap.one_bound - heap.two_bound)*4;
	
	heap.two_bound=next;
	
	printf("collect garbage:%dB\n",gg-(heap.two_bound - heap.to)*4);
	printf("after collect,living objects with 1 age:%dB \n",(heap.two_bound - heap.to)*4);
	
	//*****************************************************************************************
	//STEP 3 :拷贝新建区的幸存对象到to
	stage = 3;
	p=(int*)prev;
	scan = (int*)heap.two_bound;
	next = (int*)heap.two_bound;
	b_nex = next;
	
	printf("now,collect objects in new:......\n");
	printf("before collect(size of new objects / size of new section):%dB / %dB\n",(heap.youth_Alloc-heap.youth_new)*4,heap.youth_new_size*4);
	
	/*root in frame*/
	while(p)
	{
		//arguments
		base = (int*)(*((int*)p+2));	
		arguments_map = (char*)(*((int*)p+1));
		size = strlen(arguments_map);
		if(base != 0)
		{
			*((int*)base) = (int)Forward((void*)(*((int*)base)),(void**)(&preclass),(void**)(&next),NULL,stage);
			for(i=0;i<size;i++)
				if(arguments_map[i] == 1 && isinnew((void*)(*((int*)base+1+i))))	
					*((int*)base+1+i) = (int)Forward((void*)(*((int*)base+1+i)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);
		}		
		//locals
		size = *((int*)p+3);
		for(i=0;i<size;i++)
			if(isinnew((void*)(*((int*)p+4+i))))
			//在new中的对象
				*((int*)p+4+i) = (int)Forward((void*)(*((int*)p+4+i)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);	
		
		p = (int*)(*(int*)p);	//取上一层的frame
	}		
	
	/*root in list*/
	list = heap.List;
	while(list < heap.LP)
	{
		if(isinnew((void*)(*((int*)list))))
		//在new中的对象
			*(int*)list = (int)Forward((void*)(*((int*)list)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);
			
		list+=1;
	} 
	
	while((int*)scan < (int*)next)
	{
		if(isobject(scan))
		//为对象
		{
			char* fields_map = *(char**)(*(int*)scan);
			size = strlen(fields_map);
			scan+=4;
			for(i=0;i<size;i++)
			{
				if(fields_map[i] == 1 && isinnew((void*)(*scan)))
				//field为对象
				{
					*(int*)scan = (int)Forward((void*)(*(int*)scan),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);
					scan+=1;		
				}
				else
					scan+=1;
			}		
		}
		else
			scan+=(5 + *((int*)scan+2));
	}
	heap.one_bound=next;
	heap.to_Alloced = (heap.one_bound-heap.to)*4;
	heap.from_Alloced = 0;
	heap.new_Alloced = 0;
	printf("collect garbage:%dB\n",(heap.youth_Alloc-heap.youth_new)*4-(next-b_nex)*4);
	printf("after collect(living new objects / size of new):%dB  / %dB\n",(next-b_nex)*4,heap.youth_new_size*4);
//	getchar();
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	
	memset(heap.List,0,heap.Listsize);
	heap.LP = heap.List;
	begin = heap.to;
	end = heap.one_bound;
	for(p = begin;p<end;p++)
	{
		if(isobject(p) == 1)
		//处理对象中的引用值
		{
			char* fields_map = *(char**)(*(int*)p);
			size = strlen(fields_map);
			p+=4;
			for(i=0;i<size;i++)
			{
				if(fields_map[i] == 1)
				//其指针指向的必定是被移动清空的区域
				{
					if(isobject((int*)(*(p+i))) == 1)
					{
						//收集后，处理间接指针
						Interceptor((void*)(*(p+i)),(void*)(*((int*)(*(p+i))+2)));//比较的是指向的对象的地址和该对象的forwarding值
						*(p+i) = *((int*)(*(p+i))+2);
					}
					else
					{
						//收集后，处理间接指针
						Interceptor((void*)(*(p+i)),(void*)(*((int*)(*(p+i))+3)));
						*(p+i) = *((int*)(*(p+i))+3);
					}		
				}
			}
			p+=size;
		}
	}

	//处理old_gen中的活对象
	begin = heap.old_gen;
	end = heap.old_Alloc;
	for(p = begin;p<end;p++)
	{
		if(isobject(p) == 1)
		//处理对象中的引用值
		{
			char* fields_map = *(char**)(*(int*)p);
			size = strlen(fields_map);
			p+=4;
			for(i=0;i<size;i++)
			{
				if(fields_map[i] == 1)
				//其指针指向的必定是被移动清空的区域
				{
					if(isobject((int*)(*(p+i))) == 1)
					{
						//收集后，处理间接指针
						Interceptor((void*)(*(p+i)),(void*)(*((int*)(*(p+i))+2)));		//将域的对象地址和对象的forwarding进行比较
						*(p+i) = *((int*)(*(p+i))+2);
					}
					else
					{
						//收集后，处理间接指针
						Interceptor((void*)(*(p+i)),(void*)(*((int*)(*(p+i))+3)));
						*(p+i) = *((int*)(*(p+i))+3);
					}		
				}
			}
		}
		p+=size;
	}
	
//	memset(heap.youth_gen,0,heap.youth_new_size + 2*heap.youth_senior_size);
	memset(heap.youth_gen,0,heap.youth_new_size*4);  		//ai ai ai ai ai ai ai ai ai ai ai ai 
	heap.youth_Alloc = heap.youth_new;
	exchange();
}

void *Tiger_new (void *vtable, int size)	//size 为字节数
{
  // Your code here:
	if( isinnew(heap.youth_Alloc + size) == 0 )
	{
		Tiger_gen_gc();
		if(size >= (int)(heap.youth_gen+heap.youth_new_size - heap.youth_Alloc))
		{
			printf("Out Of Memory!!\n");
			exit(1);
		}
	}
	int *addr = heap.youth_Alloc;
	heap.youth_Alloc += size;
	heap.new_Alloced += size*4;

	*(int*)addr = (int)vtable;
	*((int*)addr+1) = 1;
	*((int*)addr+2) = 0;			//initiate forwarding
	*((int*)addr+3) = 0x0000;		//initiate mark

	return (void*)addr;
}

void *Tiger_new_array (int length)
{
  // Your code here:
	if(isinnew(heap.youth_Alloc+length+5) == 0)
	{		
		Tiger_gen_gc();
		if((4*length+20) >= (int)(heap.youth_gen+heap.youth_new_size - heap.youth_Alloc))
		//invalid operands to Binary
		{
			printf("Out Of Memory!!\n");
			exit(1);
		}
	}

	int* addr = heap.youth_Alloc;
//	heap.youth_Alloc += (4*length + 20);   //ai ai ai ai ai ai ai ai 
	heap.youth_Alloc += (length + 5);
	heap.new_Alloced += (length + 5)*4;

	*((int*)addr) = 0;
	*((int*)addr+1) = 0;
	*((unsigned*)addr+2) = length;
	*((int*)addr+3) = 0;		//initiate forwarding
	*((int*)addr+4) = 0x0000;		//initiate mark
	
//	return addr;  // ai ai ai ai ai ai ai ai ai ai ai ai ai ai ai ai
	return addr+5;
}

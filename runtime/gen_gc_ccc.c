#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct JavaHeap
{
  int Heapsize;         
  double youth_new_rate;		
  double youth_senior_rate; 	
  double old_rate;				
  
  int youth_new_size;//119B
  int youth_senior_size;//40
  int old_size;//800
  
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
  
  char* bitmap;		
  int bmsize;		//1000B * 80% = 800B  800bit/8=100B
  int* bitmapbase;	
};

// The Java heap, which is initialized by the following
// "heap_init" function.
struct JavaHeap heap;
int segment_size = 1024;

// Lab 4, exercise 10:
// Given the heap size (in bytes), allocate a Java heap
// in the C heap, initialize the relevant fields.
void Tiger_heap_init (int heapSize,int ls)
{
  int temp;
  int *heapstart = (int *)malloc(sizeof(char)*heapSize);
  
  
  heap.Heapsize = heapSize;     	//in bytes
  heap.youth_new_rate = 0.12;		//12%
  heap.youth_senior_rate = 0.04; 	//4%
  heap.old_rate = 0.8;				//80% 
  heap.youth_new_size = heapSize * heap.youth_new_rate;
  heap.youth_senior_size = heapSize * heap.youth_senior_rate;  //half of senior section
  heap.old_size = heapSize * heap.old_rate;

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
  
  temp = heap.old_size%4==0? heap.old_size/4 : heap.old_size/4+1;	
  if(temp%8 == 0)
	heap.bmsize = temp/8;
  else
	heap.bmsize = temp/8+1;
  heap.bitmap = (char*)malloc(sizeof(char)*heap.bmsize);
  memset(heap.bitmap,0,heap.bmsize);
  heap.bitmapbase = heap.old_gen;

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
}

int isinfrom(void* add)
{
	if((int)add>=(int)heap.from && (int)add<heap.from+heap.youth_senior_size)
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
	if((int)add>=(int)heap.youth_gen && (int)add<(int)heap.youth_senior)
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

void* Forward(void* p,void** pre,void** nex,void** scn,int stage)
{
	int object_size,i;
	int size = strlen(*(char**)(*(int*)p));	//size of fields
	int* next = *((int**)nex);
	int* temp = next;
	
	if(isobject(p) == 1)
		object_size = 4+size;
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
				//¿½±´
				for(i=0;i<object_size;i++)	
					*((int*)next+i) = *((int*)((int*)p+i));
				next = (int*)next + object_size;
				*((int**)nex) = next;		
				*((int**)pre) = temp;		
				if(isobject(p))
					*((int*)p+2) = (int)temp;
				else
					*((int*)p+3) = (int)temp;
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
	case 2:
		if(isinone(p) == 1)
		{
			if(isfwa(p) == 0)
			{
				for(i=0;i<object_size;i++)	
					*((int*)next+i) = *((int*)((int*)p+i));
				next = (int*)next + object_size;
				*((int**)nex) = next;		//
				*((int**)pre) = temp;		//
				if(isobject(p))
					*((int*)p+2) = (int)temp;
				else
					*((int*)p+3) = (int)temp;
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
				for(i=0;i<object_size;i++)	
					*((int*)next+i) = *((int*)((int*)p+i));
				next = (int*)next + object_size;	
				*((int**)nex) = next;		//
				*((int**)pre) = temp;		//
				if(isobject(p))
					*((int*)p+2) = (int)temp;
				else
					*((int*)p+3) = (int)temp;
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
{
	int charcount = ((int)((char*)index-(char*)heap.bitmapbase))/8;
    int spare = ((int)((char*)index-(char*)heap.bitmapbase))%8;
    unsigned char x = (0x1<<(7-spare));
    if(charcount > heap.bmsize)
        return 0;
    heap.bitmap[charcount] |= x;
    return 1; 
}

int bitmap_set0(void* index)
{
	int charcount = ((int)((char*)index-(char*)heap.bitmapbase))/8;
    int spare = ((int)((char*)index-(char*)heap.bitmapbase))%8;
    unsigned char x = (0x1<<(7-spare));
	x=~x;
    if(charcount > heap.bmsize)
        return 0;
    heap.bitmap[charcount] &= x;
    return 1; 
}

int bitmap_get(void* index)
{
    int charcount = ((int)((char*)index-(char*)heap.bitmapbase))/8;
    int spare = ((int)((char*)index-(char*)heap.bitmapbase))%8;
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
		if(isinnew(opb) == 0)
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
		if(isinfrom(opb) == 0)
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
		if(isinto(opb) == 0)
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
		if(isinold(opb) == 0)
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

unsigned getdone(void* v)
{
	return (unsigned)((*((unsigned*)v)) & 0x3fffffff);
}

void setdone(void* v,unsigned key)
{
	unsigned temp = (*((unsigned*)v));
	temp = temp & 0xc0000000;		
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
	
	if(isobject(pc) == 1)
	{
		int* markaddr = (int*)(*((int*)pc+3));
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
					{
						y=(int*)(*((int*)pc+4+i));	
						if(isobject(y)==1 && bitmap_get(y)==0) 	
						{
							(*((int*)pc+4+i)) = (int)t;	
							t = pc;		
							pc = y;		
						}
						markaddr = (int*)(*((int*)pc+3));
						bitmap_set1(pc);  
						setdone((void*)markaddr,0);		
					}
					else
						setdone((void*)markaddr,i+1);
				}
				else
				{
					y = pc;
					//!!!!
					pc = t;	
					markaddr = (int*)(*((int*)pc+3));
					if(pc == NULL)
						return;
					i = getdone((void*)markaddr);	
					t = (int*)(*((int*)pc+4+i));	
					//!!!!
					(int*)(*((int*)pc+4+i)) = (int)y;	
					//!!!!
					setdone((void*)markaddr,i+1);
				}
			}
		}
	}
	else 
		bitmap_set1(pc);
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
		base = (int)(*((int*)p+2));
		arguments_map = (char*)(*((int*)p+1));
		size = strlen(arguments_map);
		
		for(i=0;i<size;i++)
		{
			if(arguments_map[i] == 1)
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
				
		//locals
		size = *((int*)p+3);
		for(i=0;i<size;i++)
		{
			cp = (int*)(*((int*)p+4+i));
			if(isinold(cp))
			{
				if(isobject(cp))
					mark(cp);
				else
					bitmap_set1((void*)(*((int*)cp+4)));
			}	
		}				
		p = (int*)(*(int*)p);	
	}		
	
	/*root in list*/
	list = heap.List;
	while(list < heap.LP)
	{
		cp = (int*)(*((int*)list));
		if(isinold(cp))
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
				*((int*)pc+2) = (int)heap.old_gen + free;				
				classsize = strlen(*(char**)(*((int*)pc)))*4+16;	//size of bytes
				free+=classsize;		//update size
			}
			else
			{
				*((int*)pc+3) = (int)heap.old_gen + free;				
				classsize = (*((int*)pc+2))*4+20;	//size of bytes
				free+=classsize;		//update size				
			}
		}
	}
	//***********************************************************************************

	int* ppp = *((int**)pre);
	int* pp = *((int**)nex);
	
	if(isobject(ppp) == 1)
	{
		*((int**)pre) = (int*)(*(ppp+2));
		*((int**)nex) = (int*)(*(ppp+2))+((int)(pp-ppp))/4;
		*((int**)scn) = (int*)(*(ppp+2))+((int)(pp-ppp))/4 - ((int)((char*)pp - (char*)(*((int**)scn))))/4;
	}
	else
	{
		*((int**)pre) = (int*)(*(ppp+3));
		*((int**)nex) = (int*)(*(ppp+3))+((int)(pp-ppp))/4;
		*((int**)scn) = (int*)(*(ppp+3))+((int)(pp-ppp))/4 - ((int)((char*)pp - (char*)(*((int**)scn))))/4;
	}
	
	//STEP 3 modify pointor,begin with root**********************************************

	p=(int*)prev;
	
	/*root in frame*/
	while(p)
	{
		//arguments
		base = (int)(*((int*)p+2));
		arguments_map = (char*)(*((int*)p+1));
		size = strlen(arguments_map);
		
		for(i=0;i<size;i++)
		{
			if(arguments_map[i] == 1)

			{
				cp = (int*)(*((int*)base+1+i));
				if(isinold(cp))
				{
					if(isobject(cp))
						*((int*)base+1+i) = *((int*)cp+2);		
					else
						*((int*)base+1+i) = *((int*)cp+3);	
				}
			}
		}
				
		//locals
		size = *((int*)p+3);
		for(i=0;i<size;i++)
		{
			cp = (int*)(*((int*)p+4+i));
			if(isinold(cp))
			{
				if(isobject(cp))
					*((int*)p+4+i) = *((int*)cp+2);	
				else
					*((int*)p+4+i) = *((int*)cp+3);	
			}	
		}				
		p = (int*)(*(int*)p);	
	}		
	
	/*root in list*/
	list = heap.List;
	while(list < heap.LP)
	{
		cp = (int*)(*((int*)list));
		if(isinold(cp))
		{
			if(isobject(cp))
				*((int*)list) = *((int*)cp+2);	
			else
				*((int*)list) = *((int*)cp+3);	
		}		
		list+=1;
	} 	
	
	for(i=0;i<size;i++)
	{
		if(bitmap_sub(i) == 1)
		{
			pc = (int*)heap.old_gen+i;
			if(isobject(pc) ==1 && *((int*)pc+2) != 0)
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
		}
	}
	
	//STEP 4 move******************************************************************
	for(i=0;i<size;i++)
	{
		if(bitmap_sub(i) == 1)
		{
			pc = (int*)heap.old_gen+i;
			map = *(char**)(*((int*)pc));
			size = strlen(map);		
			if(isobject(pc) == 1)
			{
				newaddr = (int*)(*((int*)pc+2));
				classsize = (size+4)*4;
				for(j=0;j<classsize;i++)			
					*((char*)newaddr+j) = *((char*)pc+j);
			}
			else
			{
				newaddr = (int*)(*((int*)pc+3));
				classsize = (*(pc+2)+5)*4;
				for(j=0;j<classsize;i++)		
					*((char*)newaddr+j) = *((char*)pc+j);
			}
		}
	}
	memset(heap.bitmap,0,heap.bmsize);
	heap.old_Alloc = (int*)free;
}

// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
void Tiger_gen_gc()
{
	// Your code here:
	int* scan,*next,*p;
	int size,i;
	int* base,*list,*begin,*end,*preclass,*temp;
	char* arguments_map;
	
	int stage;

	//STEP 1 :
	stage = 1;
	p=(int*)prev;
	scan = (int*)heap.old_Alloc;
	next = (int*)heap.old_Alloc;
	preclass = NULL;		
	
	/*root in frame*/
	while(p)
	{	
		//arguments
		base = (int*)(*((int*)p+2));	
		arguments_map = (char*)(*((int*)p+1));
		size = strlen(arguments_map);
		*((int*)base) = (int)Forward((void*)(*((int*)base)),(void**)(&preclass),(void**)(&next),NULL,stage);
		for(i=0;i<size;i++)
			if(arguments_map[i] == 1 && isintwo((void*)(*((int*)base+1+i))) == 1)	
			{
				*((int*)base+1+i) = (int)Forward((void*)(*((int*)base+1+i)),(void**)(&preclass),(void**)(&next),NULL,stage);	
				bitmap_set1(preclass);
			}				

		//locals
		size = *((int*)p+3);
		for(i=0;i<size;i++)
			if(isintwo((void*)(*((int*)p+4+i))))
			{
				*((int*)p+4+i) = (int)Forward((void*)(*((int*)p+4+i)),(void**)(&preclass),(void**)(&next),NULL,stage);
				bitmap_set1(preclass);
			}
				
		p = (int*)(*(int*)p);	
	}		
	
	/*root in list*/
	list = heap.List;
	while(list < heap.LP)
	{
		if(isintwo((void*)(*((int*)list))))
		//ÔÚtwoÖĞ
		{
			*(int*)list = (int)Forward((void*)(*((int*)list)),(void**)(&preclass),(void**)(&next),0,stage);
			bitmap_set1(preclass);
		}
		
		list++;
	} 

	while((int*)scan < (int*)next)
	{
		if(isobject(scan))
		{
			char* fields_map = *(char**)(*(int*)scan);
			size = strlen(fields_map);
			scan+=4;
			for(i=0;i<size;i++)
			{
				if(fields_map[i] == 1 && isintwo((void*)(*scan)))
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
	
	heap.old_Alloc = next;
	
	//*******************************************************************************************
	//STEP 2 :
	stage = 2;
	p=(int*)prev;
	scan = (int*)heap.toStart;
	next = (int*)heap.toNext;
	
	/*root in frame*/
	while(p)
	{
		//arguments
		base = (int*)(*((int*)p+2));	
		arguments_map = (char*)(*((int*)p+1));
		size = strlen(arguments_map);
		*((int*)base) = (int)Forward((void*)(*((int*)base)),(void**)(&preclass),(void**)(&next),NULL,stage);
		for(i=0;i<size;i++)
			if(arguments_map[i] == 1 && isinone((void*)(*((int*)base+1+i))))
				*((int*)base+1+i) = (int)Forward((void*)(*((int*)base+1+i)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);


		//locals
		size = *((int*)p+3);
		for(i=0;i<size;i++)
			if(isinone((void*)(*((int*)p+4+i))))
				*((int*)p+4+i) = (int)Forward((void*)(*((int*)p+4+i)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);	
				
		p = (int*)(*(int*)p);	
	}		
	
	/*root in list*/
	list = heap.List;
	while(list < heap.LP)
	{
		if(isinone((void*)(*((int*)list))))
			*(int*)list = (int)Forward((void*)(*((int*)list)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);
			
		list+=1;
	} 
	
	while((int*)scan < (int*)next)
	{
		if(isobject(scan))
		{
			char* fields_map = *(char**)(*(int*)scan);
			size = strlen(fields_map);
			scan+=4;
			for(i=0;i<size;i++)
			{
				if(fields_map[i] == 1 && isinone((void*)(*scan)))
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
	
	heap.two_bound=next;
	
	//*****************************************************************************************
	//STEP 3 :
	stage = 3;
	p=(int*)prev;
	scan = (int*)heap.two_bound;
	next = (int*)heap.two_bound;
	
	/*root in frame*/
	while(p)
	{
		//arguments
		base = (int*)(*((int*)p+2));	
		arguments_map = (char*)(*((int*)p+1));
		size = strlen(arguments_map);
		*((int*)base) = (int)Forward((void*)(*((int*)base)),(void**)(&preclass),(void**)(&next),NULL,stage);
		for(i=0;i<size;i++)
			if(arguments_map[i] == 1 && isinnew((void*)(*((int*)base+1+i))))	
				*((int*)base+1+i) = (int)Forward((void*)(*((int*)base+1+i)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);
				
		//locals
		size = *((int*)p+3);
		for(i=0;i<size;i++)
			if(isinnew((void*)(*((int*)p+4+i))))
				*((int*)p+4+i) = (int)Forward((void*)(*((int*)p+4+i)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);	
		
		p = (int*)(*(int*)p);	
	}		
	
	/*root in list*/
	list = heap.List;
	while(list < heap.LP)
	{
		if(isinnew((void*)(*((int*)list))))
			*(int*)list = (int)Forward((void*)(*((int*)list)),(void**)(&preclass),(void**)(&next),(void**)(&scan),stage);
			
		list+=1;
	} 
	
	while((int*)scan < (int*)next)
	{
		if(isobject(scan))
		
		{
			char* fields_map = *(char**)(*(int*)scan);
			size = strlen(fields_map);
			scan+=4;
			for(i=0;i<size;i++)
			{
				if(fields_map[i] == 1 && isinnew((void*)(*scan)))
				
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
//**********************************************************************************

	memset(heap.List,0,heap.Listsize);
	heap.LP = heap.List;

//**********************************************************************************

	begin = (int*)heap.to;
	end = (int*)heap.one_bound;
	for(p = begin;p<end;p++)
	{
		if(isobject(p) == 1)

		{
			char* fields_map = *(char**)(*(int*)p);
			size = strlen(locals_map);
			p+=4;
			for(i=0;i<size;i++)
			{
				if(fields_map[i] == 1)
				{
					if(isobject((int*)(*(p+i))) == 1)
					{
						
						Interceptor(*(p+i),*((int*)(*(p+i))+2));
						*(p+i) = *((int*)(*(p+i))+2);
					}
					else
					{
						
						Interceptor(*(p+i),*((int*)(*(p+i))+3));
						*(p+i) = *((int*)(*(p+i))+3);
					}		
				}
			}
			p+=size;
		}
	}
	

	begin = heap.old_gen;
	end = heap.old_Alloc;
	for(p = begin;p<end;p++)
	{
		if(isobject(p) == 1)
		{
			char* fields_map = *(char**)(*(int*)p);
			size = strlen(locals_map);
			p+=4;
			for(i=0;i<size;i++)
			{
				if(fields_map[i] == 1)
				{
					if(isobject((int*)(*(p+i))) == 1)
					{
						Interceptor(*(p+i),*((int*)(*(p+i))+2));		
						*(p+i) = *((int*)(*(p+i))+2);
					}
					else
					{
						Interceptor(*(p+i),*((int*)(*(p+i))+3));
						*(p+i) = *((int*)(*(p+i))+3);
					}		
				}
			}
		}
		p+=size;
	}
	

//**********************************************************************************
	memset(heap.youth_gen,0,heap.youth_new_size + 2*heap.youth_senior_size);
	heap.youth_Alloc = heap.youth_new;
	exchange();
}


void *Tiger_new (void *vtable, int size)
{
  // Your code here:
	if( isinnew(heap.youth_Alloc + size) == 1 )
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

	*(int*)addr = (int)vtable;
	*((int*)addr+1) = 1;
	*((int*)addr+2) = 0;			//initiate forwarding
	*((int*)addr+3) = 0x0000;		//initiate mark

	return (void*)addr;
}


void *Tiger_new_array (int length)
{
  // Your code here:
	if(isinnew(heap.youth_Alloc+length+5) == 1)
	{		
		Tiger_gen_gc();
		if((4*length+20) >= (int)(heap.youth_gen+heap.youth_new_size - heap.youth_Alloc))
		{
			printf("Out Of Memory!!\n");
			exit(1);
		}
	}

	int* addr = heap.youth_Alloc;
	heap.youth_Alloc += (4*length + 20);

	*((int*)addr) = NULL;
	*((int*)addr+1) = 0;
	*((unsigned*)addr+2) = length;
	*((int*)addr+3) = 0;		//initiate forwarding
	*((int*)addr+4) = 0x0000;		//initiate mark
	
	return addr;
}

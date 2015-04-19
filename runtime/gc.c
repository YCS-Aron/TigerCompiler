#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// The Gimple Garbage Collector.


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
  int size;         // in bytes, note that this if for semi-heap size
  char *from;       // the "from" space pointer
  char *fromFree;   // the next "free" space in the from space
  char *to;         // the "to" space pointer
  char *toStart;    // "start" address in the "to" space
  char *toNext;     // "next" free space pointer in the to space
};

// The Java heap, which is initialized by the following
// "heap_init" function.
struct JavaHeap heap;

// Lab 4, exercise 10:
// Given the heap size (in bytes), allocate a Java heap
// in the C heap, initialize the relevant fields.
void Tiger_heap_init (int heapSize)
{
  // You should write 7 statement here:
  // #1: allocate a chunk of memory of size "heapSize" using "malloc"
  char *heapstart = (char *)malloc(sizeof(char)*heapSize);

  // #2: initialize the "size" field, note that "size" field
  // is for semi-heap, but "heapSize" is for the whole heap.
  heap.size = heapSize/2;

  // #3: initialize the "from" field (with what value?)
  heap.from = heapstart;

  // #4: initialize the "fromFree" field (with what value?)
  heap.fromFree = heapstart;

  // #5: initialize the "to" field (with what value?)
  heap.to = heapstart+heap.size;

  // #6: initizlize the "toStart" field with NULL;
  heap.toStart = heap.to;

  // #7: initialize the "toNext" field with NULL;
  heap.toNext = heap.to;

  return;
}

// The "prev" pointer, pointing to the top frame on the GC stack. 
// (see part A of Lab 4)
void *prev = 0;

void exchange()
{
	char* tmp;
	tmp = heap.from;
	heap.from = heap.to;
	heap.to = tmp;
	heap.fromFree = heap.toNext;
	heap.toStart = heap.to;
	heap.toNext = heap.to;
}

int isinfrom(void* add)
{
	if(add>=heap.from && add<heap.from+heap.size)
		return 1;
	else 
		return 0;
}

int isinto(void* add)
{
	if(add>=heap.to && add<heap.to+heap.size)
		return 1;
	else 
		return 0;
}

void* Forward(void* p,void* next)
{
	int object_size,i;
	int size = strlen(*(char**)(*(int*)p));
	int* temp = next;

	if(*((int*)(*(int*)p)+1) == 1)
		object_size = 3+size;
	else
		object_size = 4+*((int*)(*(int*)p)+2);

	if( isinfrom((void*)(*(int*)p)) )
	{
		if((*((int*)(*(int*)p)+1) == 1) && isinto(*((int*)(*(int*)p)+2)))
			return *((int*)(*(int*)p)+2);
		else if((*((int*)(*(int*)p)+1) == 0) && isinto(*((int*)(*(int*)p)+3)))
			return *((int*)(*(int*)p)+3);
		else
			for(i=0;i<object_size;i++)	
				*((int*)next+i) = *((int*)(*(int*)p)+i);

		if(*((int*)(*(int*)p)+1) == 1)
			*((int*)(*(int*)p)+2) = next;
		else if(*((int*)(*(int*)p)+1) == 0)
			*((int*)(*(int*)p)+3) = next;

		next = (int*)next + object_size;
		return temp;
	}
	else 
		return p;
}
//===============================================================//
// The Gimple Garbage Collector

// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
void Tiger_gc()
{
	// Your code here:
	int* scan,next,p;
	int size,i;
	int* base;
	char* arguments_map;
	
	p=(int*)prev;
	scan = (int*)heap.toStart;
	next = (int*)heap.toNext;

	while(p)
	{
		//arguments
		base = *((int*)(*((int*)p+2)));	//base = &this
		arguments_map = (char*)(*((int*)p+1));
		size = strlen(arguments_map);
		for(i=0;i<size;i++)
			if(arguments_map[i] == 1)
				*((int*)base+1+i) = Forward((void*)(*((int*)base+1+i)),(void*)next);
			else
				continue;
		//locals
		size = *((int*)p+3);
		for(i=0;i<size;i++)
			*((int*)p+4+i) = Forward((void*)(*((int*)p+4+i)),(void*)next);	

		p = (int*)(*(int*)p);
	}

	while(scan < (int*)next)
	{
		if(*((int*)scan+1) == 1)
		{
			char* locals_map = *(char**)(*(int*)scan);
			size = strlen(locals_map);
			scan+=12;
			for(i=0;i<size;i++)
			{
				if(locals_map[i] == 1)
				{
					*(int*)scan = Forward((void*)(*(int*)scan),(void*)next);
					scan+=1;		//(int*)scan++ ²»ÐÐ
				}
				else
					scan+=1;
			}		
		}
		else
			scan+=1;
	}

	heap.toNext = next;

	exchange();

}

//===============================================================//
// Object Model And allocation


// Lab 4: exercise 11:
// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    ----------------
      | vptr      ---|----> (points to the virtual method table)
      |--------------|
      | isObjOrArray | (0: for normal objects)
      |--------------|
      | length       | (this field should be empty for normal objects)
      |--------------|
      | forwarding   | 
      |--------------|\
p---->| v_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | v_{size-1}   | /e
      ----------------/
*/
// Try to allocate an object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new (void *vtable, int size)
{
  // Your code here:

	if( !isinfrom(heap.fromFree+size) )
	{
		Tiger_gc();
		if(size >= (heap.fromFree + size - heap.toNext))
		{
			printf("Out Of Memory!!\n");
			exit(1);
		}
	}
	char *addr = heap.fromFree;
	heap.fromFree += size;

	*(int*)addr = vtable;
	*(int*)(addr+4) = 1;

	return (void*)addr;
}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length and other information.
/*    ----------------
      | vptr         | (this field should be empty for an array)
      |--------------|
      | isObjOrArray | (1: for array)
      |--------------|
      | length       |
      |--------------|
      | forwarding   | 
      |--------------|\
p---->| e_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | e_{length-1} | /e
      ----------------/
*/
// Try to allocate an array object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this array object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new_array (int length)
{
  // Your code here:
	if(!isinfrom(heap.fromFree+4*length+16))
	{		
		Tiger_gc();
		if((4*length+16) >= (heap.to +heap.size - heap.toNext))
		{
			printf("Out Of Memory!!\n");
			exit(1);
		}
	}

	char* addr = heap.fromFree;
	heap.fromFree += (4*length + 16);

	*((unsigned*)addr+2) = length;
	*((int*)addr+1) = 0;
	
	return addr;
}





/*
struct JavaHeap
{
  int size;         // in bytes, note that this if for semi-heap size
  char *from;       // the "from" space pointer
  char *fromFree;   // the next "free" space in the from space
  char *to;         // the "to" space pointer
  char *toStart;    // "start" address in the "to" space
  char *toNext;     // "next" free space pointer in the to space
};

struct A_class{
  void *vptr;       // virtual method table pointer
  int isObjOrArray; // is this a normal object or an (integer) array object?
  unsigned length;  // array length
  void *forwarding; // forwarding pointer, will be used by your Gimple GC
  ...;              // remainings are normal class or array fields
};

struct f_gc_frame{
  void *prev;                     // dynamic chain, pointing to f's caller's GC frame
  char *arguments_gc_map;         // should be assigned the value of "f_arguments_gc_map"
  int *arguments_base_address;    // address of the first argument
  int locals_gc_count;            // should be assigned the value of "f_locals_gc_map"
  struct A *local1;               // remaining fields are method locals
  struct C *local3;
};

*/
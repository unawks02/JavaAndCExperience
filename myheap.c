////////////////////////////////////////////////////////////////////////////////
// Main File:        myHeap.c
// This File:        myHeap.c
// Other Files:      N/A
// Semester:         CS 354 Lecture 003 Spring 2022
// Instructor:       deppeler
// 
// Author:           Sage Fritz
// Email:            sgfritz2@wisc.edu
// CS Login:         sfritz
//
/////////////////////////// OTHER SOURCES OF HELP //////////////////////////////
//                   Fully acknowledge and credit all sources of help,
//                         other than Instructors and TAs.
//                   Do credit and acknowledge peer mentors, 
//                         tutors, friends, family, etc.
//
// Persons:          N/A
//
// Online sources:   N/A
//                   
//////////////////////////// 80 columns wide ///////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
//
// Copyright 2020-2022 Deb Deppeler based on work by Jim Skrentny
// Posting or sharing this file is prohibited, including any changes/additions.
// Used by permission Spring 2022, CS354-deppeler
//
///////////////////////////////////////////////////////////////////////////////

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <stdio.h>
#include <string.h>
#include "myHeap.h"
 
/*
 * This structure serves as the header for each allocated and free block.
 * It also serves as the footer for each free block but only containing size.
 */
typedef struct blockHeader {           

    int size_status;
    /*
     * Size of the block is always a multiple of 8.
     * Size is stored in all block headers and in free block footers.
     *
     * Status is stored only in headers using the two least significant bits.
     *   Bit0 => least significant bit, last bit
     *   Bit0 == 0 => free block
     *   Bit0 == 1 => allocated block
     *
     *   Bit1 => second last bit 
     *   Bit1 == 0 => previous block is free
     *   Bit1 == 1 => previous block is allocated
     * 
     * End Mark: 
     *  The end of the available memory is indicated using a size_status of 1.
     * 
     * Examples:
     * 
     * 1. Allocated block of size 24 bytes:
     *    Allocated Block Header:
     *      If the previous block is free      p-bit=0 size_status would be 25
     *      If the previous block is allocated p-bit=1 size_status would be 27
     * 
     * 2. Free block of size 24 bytes:
     *    Free Block Header:
     *      If the previous block is free      p-bit=0 size_status would be 24
     *      If the previous block is allocated p-bit=1 size_status would be 26
     *    Free Block Footer:
     *      size_status should be 24
     */
} blockHeader;         

/* Global variable - DO NOT CHANGE. It should always point to the first block,
 * i.e., the block at the lowest address.
 */
blockHeader *heapStart = NULL;     

/* Size of heap allocation padded to round to nearest page size.
 */
int allocsize;

/*
 * Additional global variables may be added as needed below
 */

/*
 * Function to check if the block being scanned is a better fit than the
 * previous best fit block
 * A helper function for myAlloc()
 * Argument int prevBest: represents previous best fit, -1 if no current fit
 * Argument int thisSize: size to test if better than previous fit
 * Argument int allocSize: size to be allocated in heap
 * Return 1 if better fit, 0 if not
 */
int isBetterFit(int prevBest, int thisSize,  int allocSize) {
        if (prevBest == -1){ //no current fit
                return 1;
        }
        if ((thisSize - allocSize) < (prevBest - allocSize)){
                //indicates less difference, better fit
                return 1;
        }
        else {
                return 0;
        }
}

 
/* 
 * Function for allocating 'size' bytes of heap memory.
 * Argument size: requested size for the payload
 * Returns address of allocated block (payload) on success.
 * Returns NULL on failure.
 *
 * This function must:
 * - Check size - Return NULL if not positive or if larger than heap space.
 * - Determine block size rounding up to a multiple of 8 
 *   and possibly adding padding as a result.
 *
 * - Use BEST-FIT PLACEMENT POLICY to chose a free block
 *
 * - If the BEST-FIT block that is found is exact size match
 *   - 1. Update all heap blocks as needed for any affected blocks
 *   - 2. Return the address of the allocated block payload
 *
 * - If the BEST-FIT block that is found is large enough to split 
 *   - 1. SPLIT the free block into two valid heap blocks:
 *         1. an allocated block
 *         2. a free block
 *         NOTE: both blocks must meet heap block requirements 
 *       - Update all heap block header(s) and footer(s) 
 *              as needed for any affected blocks.
 *   - 2. Return the address of the allocated block payload
 *
 * - If a BEST-FIT block found is NOT found, return NULL
 *   Return NULL unable to find and allocate block for desired size
 *
 * Note: payload address that is returned is NOT the address of the
 *       block header.  It is the address of the start of the 
 *       available memory for the requesterr.
 *
 * Tips: Be careful with pointer arithmetic and scale factors.
 * This calls a helper function isBetterFit
 */
void* myAlloc(int size) {     
    if (size < 1){
	    return NULL; //size too small
    }
    if (size > allocsize){
            return NULL; //size too big
    }


    int blockSize = size; //add payload to total block size
    blockSize = blockSize + sizeof(blockHeader); //add room for header
   
    if (blockSize % 8 != 0){ //add padding if not divisible by 8
	    blockSize = ((blockSize / 8) + 1) * 8; //round up to next multiple
    }

    blockHeader *curBlock; //counter pointer to loop through array from start
    curBlock =  heapStart;
    //a pointer to the current block header being checked for a fit
    int bestSize = -1; //size of best fit block (no a or p bit)
    int curSize = curBlock -> size_status;
    blockHeader *bestFit = NULL; //a ptr to the best fit block
    //while size of blocks being scanned is not 1 (end point)
    while (curSize != 1){
	    //start at first heap block, see if alloced or not
	    //if heap block is free, check to see if it is best fit
	    if ((curSize - 1)  % 8 == 0){
		    //curBlock alloc, prev block free
		    curSize = curSize - 1;
	    }
	    else if ((curSize - 3) % 8 == 0){
		    //curBlock alloc, prev block alloc
		    curSize = curSize - 3;
	    }
	    else if((curSize - 2) % 8 == 0){
		    //curBlock free; prev block alloc
		    curSize = curSize - 2;
		    if (curSize >= blockSize){
			    if (isBetterFit(bestSize,  curSize, blockSize) == 1){
				    //if better fit, update best fit block
				    bestSize = curSize;
				    bestFit = curBlock;
			    }
		    }

	    }
	    else if(curSize % 8 == 0){
		    //curBlock free, prev block free
		    if (curSize >= blockSize){
			    if(isBetterFit(bestSize, curSize, blockSize) == 1){
				    //update best fit block if better fit
				    bestSize = curSize;
				    bestFit = curBlock;
			    }
		    }
	    }
	    //continue to next block
	    curBlock =  (curBlock + (curSize/4)); //curSize /4  for scaling
	    curSize = curBlock -> size_status;

    }
    
    //return null if unable to find best fit block
    if (bestSize == -1){
	    //indicates no match found
	    return NULL;
    }
    //split if best fit size is big enough for two blocks
    int extraSize = bestSize - blockSize; //extra space in current block
    if(extraSize >= 8){ //8 is minimum block size
	    //split best fit block
	    bestSize = bestSize - extraSize; //size for alloc block after fit
	    //update header size of block to be allocated
	    bestFit -> size_status = (bestFit -> size_status) - extraSize;
	    //create header for new free block
	    blockHeader *nextBlock = (blockHeader*) (bestFit + (bestSize /4));
	    nextBlock -> size_status = extraSize;
	    // divided bestSize by four to account for void scaling
	    
	    //update footer of free block
	    blockHeader *footer = (blockHeader*) (nextBlock + (extraSize/4) - 1);
	    // divide size by four to account for scaling
            footer -> size_status = extraSize;
    }

    //only do the following if next heap block is not end block
    if ((bestFit + (bestSize/4)) -> size_status != 1){
            (bestFit + (bestSize/4)) -> size_status += 2;
    }
    //update p bit of next block to 1 since this block is now alloc'd

    //update  heap block header
    bestFit -> size_status += 1; //add 1 (a bit) alloc'd
   
    //return address to PAYLOAD  of best fitting heap block
    
    return (bestFit + 1);
} 
 
/* 
 * Function for freeing up a previously allocated block.
 * Argument ptr: address of the block to be freed up.
 * Rieturns 0 on success.
 * Returns -1 on failure.
 * This function should:
 * - Return -1 if ptr is NULL.
 * - Return -1 if ptr is not a multiple of 8.
 * - Return -1 if ptr is outside of the heap space.
 * - Return -1 if ptr block is already freed.
 * - Update header(s) and footer as needed.
 */                    
int myFree(void *ptr) {    
    if (ptr == NULL){
	    return -1;
    }
    if ((int)ptr % 8 != 0){ //not a multiple of 8
	    return -1;
    }
    if (ptr < (void*)heapStart){ //ptr before heap space
	    return -1;
    }
    if (ptr > ((void*)heapStart + (allocsize - 4))){
	    return -1; //ptr after valid  heap space
	    //heap space ends at end mark (heapStart + allocSize)
	    //last ptr value is 8 bits before end mark
    }

    blockHeader *toFree = ((blockHeader*) ptr) - 1;

    //return -1 if ptr already freed
    if ((toFree -> size_status & 1) == 0){
	    return -1;
    }
    //else free ptr
    //update header
    (toFree -> size_status) -= 1; //subtract 1 to indicate free
    //update footer

    int ptrSize;
    if ((toFree -> size_status) % 8 == 0){
	    ptrSize = toFree -> size_status; //size is equal to status
    }
    else { //since freed, p bit must be full
	    ptrSize = toFree -> size_status - 2;
    }
    int nextSize = (toFree + (ptrSize / 4)) -> size_status;

    blockHeader *footer = (blockHeader*) (toFree + ((ptrSize - 4)/4));
    footer -> size_status = ptrSize;
    //update header p-bit of next block if not end mark
    if (nextSize != 1){
            (toFree + (ptrSize/4)) -> size_status = nextSize - 2;
    }

    return 0;
} 

/*
 * Function for traversing heap block list and coalescing all adjacent 
 * free blocks.
 *
 * This function is used for delayed coalescing.
 * Updated header size_status and footer size_status as needed.
 *
 * Returns 0 if there were no adjacent free blocks coalesced 
 * Returns any positive integer if there were any adjacent free blocks that coalesced
 * */
int coalesce() {
    int coalBlocks = 0; //number of blocks coalesced
    blockHeader *curBlock; //counter pointer to loop through array from start
    curBlock =  heapStart;
    int curSize = curBlock -> size_status;
    //while size of blocks being scanned is not 1 (end point)
    while (curSize != 1){
            //start at first heap block, see if alloced or not
            //if heap block is free, check to see if it can be coalensced
            if ((curSize - 1)  % 8 == 0){
                    //curBlock alloc, prev block free
                    curSize = curSize - 1;
            }
            else if ((curSize - 3) % 8 == 0){
                    //curBlock alloc, prev block alloc
                    curSize = curSize - 3;
            }
            else if((curSize - 2) % 8 == 0){
                    //curBlock free; prev block alloc
                    curSize = curSize - 2;
	    }
	    else if (curSize % 8 == 0 ){
		    //curBlock free, prev block free
		    //this is the block we want to merge
		    // curSize is the size of this block

		    //go to footer of previous and store its size
		    int prevSize = (curBlock - 1) -> size_status;
		    //go to header of previous and edit size to equal size of both
		    blockHeader *newBlock = (blockHeader*)(curBlock - (prevSize/4));
		    newBlock -> size_status += curSize;

		    //make footer of new, coalesced block and set its size
		    int newSize = prevSize + curSize;
		    blockHeader *footer = (blockHeader*) (newBlock + (newSize/4) - 1);
                    // divide size by four to account for scaling
                    footer -> size_status = newSize;
		    coalBlocks += 1;
            }
            //continue to next block
            curBlock =  (curBlock + (curSize/4)); //curSize /4  for scaling
            curSize = curBlock -> size_status;

    }
    return coalBlocks;
}

 
/* 
 * Function used to initialize the memory allocator.
 * Intended to be called ONLY once by a program.
 * Argument sizeOfRegion: the size of the heap space to be allocated.
 * Returns 0 on success.
 * Returns -1 on failure.
 */                    
int myInit(int sizeOfRegion) {    
 
    static int allocated_once = 0; //prevent multiple myInit calls
 
    int pagesize;   // page size
    int padsize;    // size of padding when heap size not a multiple of page size
    void* mmap_ptr; // pointer to memory mapped area
    int fd;

    blockHeader* endMark;
  
    if (0 != allocated_once) {
        fprintf(stderr, 
        "Error:mem.c: InitHeap has allocated space during a previous call\n");
        return -1;
    }

    if (sizeOfRegion <= 0) {
        fprintf(stderr, "Error:mem.c: Requested block size is not positive\n");
        return -1;
    }

    // Get the pagesize
    pagesize = getpagesize();

    // Calculate padsize as the padding required to round up sizeOfRegion 
    // to a multiple of pagesize
    padsize = sizeOfRegion % pagesize;
    padsize = (pagesize - padsize) % pagesize;

    allocsize = sizeOfRegion + padsize;

    // Using mmap to allocate memory
    fd = open("/dev/zero", O_RDWR);
    if (-1 == fd) {
        fprintf(stderr, "Error:mem.c: Cannot open /dev/zero\n");
        return -1;
    }
    mmap_ptr = mmap(NULL, allocsize, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);
    if (MAP_FAILED == mmap_ptr) {
        fprintf(stderr, "Error:mem.c: mmap cannot allocate space\n");
        allocated_once = 0;
        return -1;
    }
  
    allocated_once = 1;

    // for double word alignment and end mark
    allocsize -= 8;

    // Initially there is only one big free block in the heap.
    // Skip first 4 bytes for double word alignment requirement.
    heapStart = (blockHeader*) mmap_ptr + 1;

    // Set the end mark
    endMark = (blockHeader*)((void*)heapStart + allocsize);
    endMark->size_status = 1;

    // Set size in header
    heapStart->size_status = allocsize;

    // Set p-bit as allocated in header
    // note a-bit left at 0 for free
    heapStart->size_status += 2;

    // Set the footer
    blockHeader *footer = (blockHeader*) ((void*)heapStart + allocsize - 4);
    footer->size_status = allocsize;
  
    return 0;
} 
                  
/* 
 * Function to be used for DEBUGGING to help you visualize your heap structure.
 * Prints out a list of all the blocks including this information:
 * No.      : serial number of the block 
 * Status   : free/used (allocated)
 * Prev     : status of previous block free/used (allocated)
 * t_Begin  : address of the first byte in the block (where the header starts) 
 * t_End    : address of the last byte in the block 
 * t_Size   : size of the block as stored in the block header
 */                     
void dispMem() {     
 
    int counter;
    char status[6];
    char p_status[6];
    char *t_begin = NULL;
    char *t_end   = NULL;
    int t_size;

    blockHeader *current = heapStart;
    counter = 1;

    int used_size = 0;
    int free_size = 0;
    int is_used   = -1;

    fprintf(stdout, 
	"*********************************** Block List **********************************\n");
    fprintf(stdout, "No.\tStatus\tPrev\tt_Begin\t\tt_End\t\tt_Size\n");
    fprintf(stdout, 
	"---------------------------------------------------------------------------------\n");
  
    while (current->size_status != 1) {
        t_begin = (char*)current;
        t_size = current->size_status;
    
        if (t_size & 1) {
            // LSB = 1 => used block
            strcpy(status, "alloc");
            is_used = 1;
            t_size = t_size - 1;
        } else {
            strcpy(status, "FREE ");
            is_used = 0;
        }

        if (t_size & 2) {
            strcpy(p_status, "alloc");
            t_size = t_size - 2;
        } else {
            strcpy(p_status, "FREE ");
        }

        if (is_used) 
            used_size += t_size;
        else 
            free_size += t_size;

        t_end = t_begin + t_size - 1;
    
        fprintf(stdout, "%d\t%s\t%s\t0x%08lx\t0x%08lx\t%4i\n", counter, status, 
        p_status, (unsigned long int)t_begin, (unsigned long int)t_end, t_size);
    
        current = (blockHeader*)((char*)current + t_size);
        counter = counter + 1;
    }

    fprintf(stdout, 
	"---------------------------------------------------------------------------------\n");
    fprintf(stdout, 
	"*********************************************************************************\n");
    fprintf(stdout, "Total used size = %4d\n", used_size);
    fprintf(stdout, "Total free size = %4d\n", free_size);
    fprintf(stdout, "Total size      = %4d\n", used_size + free_size);
    fprintf(stdout, 
	"*********************************************************************************\n");
    fflush(stdout);

    return;  
} 


// end of myHeap.c (Spring 2022)                                         


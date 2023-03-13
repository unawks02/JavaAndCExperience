//for hw 5 for CS 577 Sage Fritz

import java.util.Scanner;

public class InversionCount {

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    int numInstances = sc.nextInt();
    for (int i = 0; i < numInstances; i++) {
      makeList(sc);
    }
    sc.close();
    System.exit(0);
  }
  
  public static void makeList(Scanner sc) {
    int sizeList = sc.nextInt();
    int[] list = new int[sizeList];
    for (int i = 0; i < sizeList; i++) {
      list[i] = sc.nextInt();
    }
    int inversions = invCount(list, 0, (list.length - 1));
    System.out.println(inversions);
  }
  
  public static int invCount(int[] list, int start, int end) {
    if (end - start <= 0) {
      return 0;
    }
    int middleValue = (start + end) / 2;
    return (invCount(list, start, middleValue) + invCount(list, middleValue + 1, end)
        + mergeCount(list, start, middleValue, end));
  }
  
  public static int mergeCount(int[] list, int start, int middle, int end) {
    //first make two sub-arrays
    int frontSize = 1 + middle - start; //+1 because front includes middle
    int backSize = end - middle;
    int[] front = new int[frontSize];
    int[] back = new int[backSize];
    for (int i = 0; i < frontSize; i++) {
      front[i] = list[start + i];
    }
    for (int i = 0; i < backSize; i++) {
      back[i] = list[(middle + 1) + i]; //+1 because front includes middle
    }
    
    //while either list is not empty, append
    //if next is from backend, inversions increase by size A remaining
    
    int curF = 0;
    int curB = 0;
    int toMerge = start;
    int numInversions = 0;
    while ((curF < frontSize)&&(curB < backSize)) {
      if(front[curF] <= back[curB]) {
        list[toMerge] = front[curF];
        curF++;
        toMerge++;
      }
      else {
        numInversions = numInversions + (frontSize - curF);
        list[toMerge] = back[curB];
        curB++;
        toMerge++;
      }
    }
    while (curB < backSize) {
      list[toMerge] = back[curB];
      curB++;
      toMerge++;
    }
    while (curF < frontSize) {
      list[toMerge] = front[curF];
      curF++;
      toMerge++;
    }

    return numInversions;
  }
}

import java.util.Scanner;

//Sage Fritz for CS 577

public class FurthestFuture {

  /**
   * main method
   */
  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    int numInstances = sc.nextInt();
    for (int i = 0; i < numInstances; i++) {
      cache(sc);
    }
    sc.close();
    System.exit(0);
  }
  
  public static void cache(Scanner sc) {
    int sizeCache = sc.nextInt();
    int numRequests = sc.nextInt();
    int[] cache = new int[sizeCache];
    int[] requests = new int[numRequests];
    for (int i = 0; i < numRequests; i++) {
      requests[i] = sc.nextInt();
    }
    int faults = 0;
    int numInserted = 0;
    for (int i = 0; i < numRequests; i++) {
      int current = requests[i];
       
      if (!(found(cache, current))) {
        faults++;
        // special case where cache isn't filled yet
        if (numInserted < sizeCache) {
          cache[numInserted] = current;
          numInserted++;
        } else {
          int[] requestsLeft = new int[numRequests - (i + 1)];
          for (int j = 0; j < requestsLeft.length; j++) {
            requestsLeft[j] = requests[i + 1 + j];
          }
          find(cache, requestsLeft, current);
        }
      }
    }
    System.out.println(faults);
  }
  
  public static boolean found(int[] cache, int page) {
    for (int i = 0; i < cache.length; i++) {
      if (cache[i]==page) {
        return true;
      }
    }
    return false;
  }
  
  public static void find(int[] cache, int[] requests, int page) {
    int[] distance = new int[cache.length];
    for (int i = 0; i < distance.length; i++) {
      distance[i] = 1000000000;
    }
    for (int i = 0; i < cache.length; i++) {
      for (int j = 0; j < requests.length; j++) {
        if (requests[j] == cache[i]) {
          distance[i] = j;
          break;
        }
      }
    }

    //find index furthest in future and replace it
    int max = 0;
    int maxIndex = 0;
    for (int i = 0; i < distance.length; i++) {
      if (distance[i] > max) {
        max = distance[i];
        maxIndex = i;
      }
    }
    cache[maxIndex] = page;
  }
  
}
// --== CS400 Project One File Header ==--
// Name: Sage Fritz
// Email: sgfritz2@wisc.edu
// Team: Blue
// Group: BL
// TA: Surabhi Gupta
// Lecturer: Gary Dahl
// Notes to Grader: <optional extra notes>
// To code this class I referenced
// https://docs.oracle.com/javase/7/docs/api/java/util/LinkedList.html


import java.util.NoSuchElementException;
import java.util.LinkedList;


/**
 * This class creates a HashTable array that maps its value into linked lists in order to handle
 * collisions. It calls on the KeyValuePair.java class.
 * 
 * @author Sage Fritz
 *
 */
public class HashtableMap<KeyType, ValueType> implements MapADT<KeyType, ValueType> {

  @SuppressWarnings("rawtypes")
  private LinkedList[] hashTableArray; // an array of Linked Lists to store KeyValuePairs in

  /**
   * This constructor makes a new HashTableMap array with the given capacity. Capacity at or below 0
   * is not allowed, so it is replaced with the default capacity 20.
   * 
   * @param capacity is user provided length of the array
   */
  public HashtableMap(int capacity) {
    if (capacity <= 0) {
      capacity = 20; // default capacity
    }
    hashTableArray = new LinkedList[capacity];
  }

  /**
   * This constructor makes a new HashTableMap array with the default capacity, 20.
   */
  public HashtableMap() {
    hashTableArray = new LinkedList[20];
    // with default capacity = 20
  }

  /**
   * This method keeps an eye on the load factor and resizes the array if the load factor is greater
   * than or equal to 80%. It rehashes the old, smaller array into a bigger array with twice the
   * capacity.
   */
  private void resizeArray() {
    int newCapacity = (2 * hashTableArray.length); // double capacity for new array
    @SuppressWarnings("rawtypes")
    LinkedList[] tempCopy = new LinkedList[hashTableArray.length];
    for (int i = 0; i < hashTableArray.length; i++) { // makes a copy to use in rehashing
      tempCopy[i] = hashTableArray[i];
    }
    hashTableArray = new LinkedList[newCapacity];// new array double capacity but empty

    for (int i = 0; i < tempCopy.length; i++) {
      // fills array with rehashed values from old array
      if ((tempCopy[i] == null) || !(tempCopy[i] instanceof LinkedList)) {
        continue;
      } else {
        @SuppressWarnings("unchecked")
        LinkedList<KeyValuePair<KeyType, ValueType>> indexList = tempCopy[i];
        for (int j = 0; j < indexList.size(); j++) {
          KeyValuePair<KeyType, ValueType> inList = indexList.get(j);
          put(inList.getKey(), inList.getValue());
        }
      }
    }
  }

  /**
   * Returns the number of key value pairs stored in this collection.
   */
  @Override
  public int size() {
    int size = 0;
    for (int i = 0; i < hashTableArray.length; i++) {
      if (hashTableArray[i] != null) {
        size = size + hashTableArray[i].size(); // iterates through lists, adds # elements in list
      }
    }
    return size;
  }

  /**
   * This private helper method assigns indexes using hashCode modulus the array capacity.
   * 
   * @param key the item's key for getting the hash code
   * @return an index based on the item's key
   */
  private int assignIndex(KeyType key) {
    int thisHash = key.hashCode();
    int index = java.lang.Math.abs(thisHash % hashTableArray.length);
    return index;
  }

  /**
   * This private helper method calculates the current Load Factor, using Load Factor = amount of
   * elements stored / the hash table capacity.
   * 
   * @return the current load factor
   */
  private double getLoadFactor() {
    double loadFactor;
    // below is formula for load factor
    // get size in double so no int division
    double size = (double)size();
    loadFactor = (size / hashTableArray.length);
    return loadFactor;
  }

  /**
   * This hash table needs to resize itself if the Load Factor is greater or equal to 80%. This
   * helper method checks if the load factor is up to that value and calls to resize the hash table
   * if so.
   */
  private void checkFactor() {
    if (getLoadFactor() >= 0.80) {
      resizeArray();
    }
  }

  /**
   * This method puts a new value at an index corresponding to the absolute value of the key's Hash
   * Code modulus the array capacity. If a key input is null or equal to one in a hash table, it
   * returns false and the item is not added and the hash table is unchanged. It only returns true
   * if a new key-value pair is successfully stored. It uses chaining to handle hash collisions.
   * 
   * @param key   is the item's key to hash
   * @param value is the value to be stored
   * @return boolean returns true if key and value stored successfully, false otherwise
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean put(KeyType key, ValueType value) {
    if (key == null) {
      return false;
    }
    int index = assignIndex(key);
    if (containsKey(key)) {
      return false;
    }
    // the following makes a pair of the key and value and stores it to a linked list
    KeyValuePair<KeyType, ValueType> toStore = new KeyValuePair<KeyType, ValueType>(key, value);
    if ((hashTableArray[index] == null) || !(hashTableArray[index] instanceof LinkedList)) {
      hashTableArray[index] = new LinkedList<KeyValuePair<KeyType, ValueType>>();
      // used if there is no linked list at this index, makes new one
    }
    hashTableArray[index].add(toStore); // adds to existing linked list
    checkFactor(); // checks if addition of a new item requires resizing
    return true;
  }

  /**
   * This method retrieves a value from the hash table given its key. It throws a no such element
   * exception if the key is null, nothing is at the index, or if the key searched for is not found.
   * 
   * @param key the user provided key of the value to get
   * @return a value corresponding to the key given as a parameter
   * @throws NoSuchElementException if key is null, index null, or key match not found at index
   */
  @Override
  public ValueType get(KeyType key) throws NoSuchElementException {
    // throws No Such Element exception
    if (key == null) {
      throw new NoSuchElementException("key is null");
    }
    int index = assignIndex(key);
    if (hashTableArray[index] == null) {
      throw new NoSuchElementException("Nothing in array here");
    }
    @SuppressWarnings("unchecked")
    LinkedList<KeyValuePair<KeyType, ValueType>> indexList = hashTableArray[index];
    // the following loop searches the index's linked list for a matching key
    for (int i = 0; i < indexList.size(); i++) {
      KeyValuePair<KeyType, ValueType> inList = indexList.get(i);
      if (key.equals(inList.getKey())) {
        return inList.getValue();
      }
    }
    // if reached, shows matching key not found
    throw new NoSuchElementException("no match found");
  }

  /**
   * This method sees if the hash table contains an item with the user provided key.
   * 
   * @param key the user provided key
   * @returns true if key found in array, false otherwise
   */
  @Override
  public boolean containsKey(KeyType key) {
    if (key == null) {
      return false;
    }
    int index = assignIndex(key);
    if ((hashTableArray[index] == null) || !(hashTableArray[index] instanceof LinkedList)) {
      return false;
    }
    @SuppressWarnings("unchecked")
    LinkedList<KeyValuePair<KeyType, ValueType>> indexList = hashTableArray[index];
    for (int i = 0; i < indexList.size(); i++) {
      KeyValuePair<KeyType, ValueType> inList = indexList.get(i);
      if (key.equals(inList.getKey())) {
        return true;
      }
    }
    // cycle through linked lists, no match
    return false;
  }

  /**
   * This method removes a key-value pair from an array given its key. It returns the value of the
   * item removed.
   * 
   * @param key the user provided key given
   * @return a value type that has been removed from the array
   */
  @Override
  public ValueType remove(KeyType key) {
    // returns reference to value removed from table
    // if key being removed is not found, return null
    if (key == null) {
      return null;
    }
    if (!(containsKey(key))) {
      return null;
    }

    int index = assignIndex(key); // index of the key
    @SuppressWarnings("unchecked")
    LinkedList<KeyValuePair<KeyType, ValueType>> indexList = hashTableArray[index];
    ValueType toReturn = null; // stores return value

    for (int i = 0; i < indexList.size(); i++) { // the linked list at this index
      KeyValuePair<KeyType, ValueType> inList = indexList.get(i); // item in list at index i
      if (key.equals(inList.getKey())) { // found matching key at index i of linked list
        if (indexList.size() == 1) {
          // list only has one item so will be empty once removed, so clear list
          toReturn = inList.getValue();
          hashTableArray[index] = null;
          break;
        }
        toReturn = inList.getValue();
        indexList.remove(i);
        break;
      }
    }
    return toReturn;
  }

  /**
   * This method clears all key value pairs from the collection without changing the underlying
   * array capacity.
   */
  @Override
  public void clear() {
    // removes all key-values from collection
    for (int i = 0; i < hashTableArray.length; i++) {
      hashTableArray[i] = null;
    }
  }

  /*
   * Used for testing. Returns hash table capacity.
   * 
   * @return hash table capacity
  public int capacity() {
    return hashTableArray.length;
  }
  */

}
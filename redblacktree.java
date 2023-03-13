// --== CS400 Header ==--
// Name: Sage Fritz
// Email: sgfritz2@wisc.edu
//
// TA: Surabhi Gupta
// Lecturer: Gary Dahl
// Notes to Grader: <optional extra notes>

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Stack;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Red-Black Tree implementation with a Node inner class for representing the nodes of the tree.
 * Currently, this implements a Binary Search Tree that we will turn into a red black tree by
 * modifying the insert functionality. In this activity, we will start with implementing rotations
 * for the binary search tree insert algorithm. You can use this class' insert method to build a
 * regular binary search tree, and its toString method to display a level-order traversal of the
 * tree.
 */
public class RedBlackTree<T extends Comparable<T>> implements SortedCollectionInterface<T> {

  /**
   * This class represents a node holding a single value within a binary tree the parent, left, and
   * right child references are always maintained.
   */
  protected static class Node<T> {
    public T data;
    public Node<T> parent; // null for root node
    public Node<T> leftChild;
    public Node<T> rightChild;

    //a new variable isBlack that is used for balancing Red Black Trees
    //initialized to false so all newly inserted nodes are Red
    public boolean isBlack = false;
    
    public Node(T data) {
      this.data = data;
    }

    /**
     * @return true when this node has a parent and is the left child of that parent, otherwise
     *         return false
     */
    public boolean isLeftChild() {
      return parent != null && parent.leftChild == this;
    }

    /*
     * /**
     * 
     * @return true when this node has a parent and is the right child of that parent, otherwise
     * return false
     */
    /*
     * public boolean isRightChild() { return parent != null && parent.rightChild == this; }
     */

    /**
     * This method performs a level order traversal of the tree rooted at the current node. The
     * string representations of each data value within this tree are assembled into a comma
     * separated string within brackets (similar to many implementations of java.util.Collection).
     * Note that the Node's implementation of toString generates a level order traversal. The
     * toString of the RedBlackTree class below produces an inorder traversal of the nodes / values
     * of the tree. This method will be helpful as a helper for the debugging and testing of your
     * rotation implementation.
     * 
     * @return string containing the values of this tree in level order
     */
    @Override
    public String toString() {
      String output = "[";
      LinkedList<Node<T>> q = new LinkedList<>();
      q.add(this);
      while (!q.isEmpty()) {
        Node<T> next = q.removeFirst();
        if (next.leftChild != null)
          q.add(next.leftChild);
        if (next.rightChild != null)
          q.add(next.rightChild);
        output += next.data.toString();
        if (!q.isEmpty())
          output += ", ";
      }
      return output + "]";
    }
  }

  protected Node<T> root; // reference to root node of tree, null when empty
  protected int size = 0; // the number of values in the tree

  /**
   * Performs a naive insertion into a binary search tree: adding the input data value to a new node
   * in a leaf position within the tree. After this insertion, no attempt is made to restructure or
   * balance the tree. This tree will not hold null references, nor duplicate data values.
   * 
   * @param data to be added into this binary search tree
   * @return true if the value was inserted, false if not
   * @throws NullPointerException     when the provided data argument is null
   * @throws IllegalArgumentException when the newNode and subtree contain equal data references
   */
  @Override
  public boolean insert(T data) throws NullPointerException, IllegalArgumentException {
    // null references cannot be stored within this tree
    if (data == null)
      throw new NullPointerException("This RedBlackTree cannot store null references.");

    Node<T> newNode = new Node<>(data);
    if (root == null) {
      root = newNode;
      size++;
      root.isBlack = true; //sets root node to black
      return true;
    } // add first node to an empty tree
    else {
      boolean returnValue = insertHelper(newNode, root); // recursively insert into subtree
      if (returnValue)
        size++;
      else
        throw new IllegalArgumentException("This RedBlackTree already contains that value.");
      root.isBlack = true; //sets root node to black 
      return returnValue;
    }
  }

  /**
   * Recursive helper method to find the subtree with a null reference in the position that the
   * newNode should be inserted, and then extend this tree by the newNode in that position.
   * 
   * @param newNode is the new node that is being added to this tree
   * @param subtree is the reference to a node within this tree which the newNode should be inserted
   *                as a descenedent beneath
   * @return true is the value was inserted in subtree, false if not
   */
  private boolean insertHelper(Node<T> newNode, Node<T> subtree) {
    int compare = newNode.data.compareTo(subtree.data);
    // do not allow duplicate values to be stored within this tree
    if (compare == 0)
      return false;

    // store newNode within left subtree of subtree
    else if (compare < 0) {
      if (subtree.leftChild == null) { // left subtree empty, add here
        subtree.leftChild = newNode;
        newNode.parent = subtree;
        enforceRBTreePropertiesAfterInsert(newNode); 
        return true;
        // otherwise continue recursive search for location to insert
      } else
        return insertHelper(newNode, subtree.leftChild);
    }

    // store newNode within the right subtree of subtree
    else {
      if (subtree.rightChild == null) { // right subtree empty, add here
        subtree.rightChild = newNode;
        newNode.parent = subtree;
        enforceRBTreePropertiesAfterInsert(newNode);
        return true;
        // otherwise continue recursive search for location to insert
      } else
        return insertHelper(newNode, subtree.rightChild);
    }
  }
  
  /**
   * This ensures that Red Black Tree properties are followed
   * when nodes are inserted. 
   * The Red Black Tree properties are:
   * 1) Red nodes can only have 0 or 2 Black children
   * 2) Red nodes cannot have Red children.
   * 3) All paths from any leaf to any root in trees or subtree must have the same number
   *    of Black nodes along each path.
   * It calls on the checkRedNodeChildren and checkBlackHeight methods to check for violations.
   * It calls on the caseOne, caseTwo, and caseThree methods to resolve violations.
   * @param newRedNode is a newly inserted Red node
   */
  private void enforceRBTreePropertiesAfterInsert(Node<T> newRedNode) {
    if (newRedNode == null) {
      return; //checks for invalid input
    }
    boolean childViolation;
    boolean heightViolation;
    //checks for rule violates in red parent/child relationships
    childViolation = checkRedNodeChildren(newRedNode.parent, newRedNode);
    //checks for violates in the Black node height
    heightViolation = (checkBlackHeight(root) == -1);
    
    if (!(childViolation || heightViolation)){
      return; //ends the method if no violations found
    }
    
    // if there are violations, sees what case the node is in
    // root cases
    if (newRedNode.parent == null) { //indicates node is root
      newRedNode.isBlack = true;
      return;
    }
    if (newRedNode.parent.parent == null) { //indicates the parent has no parent thus no siblings
     //parent must be root
      newRedNode.parent.isBlack = true;
      enforceRBTreePropertiesAfterInsert(newRedNode); //checks that violation solved
    }
    
    Node<T> parent = newRedNode.parent;
    Node<T> grandparent = newRedNode.parent.parent;
    Node<T> siblingOfParent = getSibling(grandparent, parent);
    
    if ((siblingOfParent == null) || (siblingOfParent.isBlack)) {
      //checks which child inserted node is
      boolean isRightChild = isRightChild(parent, newRedNode);
      boolean isLeftChild = isLeftChild(parent, newRedNode);
      //if parent is right child, sibling is left, and vice versa
      boolean siblingIsRightChild = isLeftChild(grandparent, parent);
      boolean siblingIsLeftChild = isRightChild(grandparent, parent);
      
      //compares to parent's sibling
      // on same sides: case 2
      // case 2: parent's sibling is black and on same side of R-R child
      if ((siblingIsLeftChild && isLeftChild) || (siblingIsRightChild && isRightChild)) {
        caseTwo(newRedNode); //transform into a case one
        enforceRBTreePropertiesAfterInsert(parent); //parent is now in R-R child position
      }
      // on different sides: case 1
      // case 1: parent's sibling is black and on opposite side of R-R child
      else {
        caseOne(newRedNode);  //case 1 solved in one step
      }
      
    }
    else {
      caseThree(newRedNode); // case 3: parent's sibling is red
      enforceRBTreePropertiesAfterInsert(grandparent); //check if problem moved up tree
    }
  }
  
  /** Checks for right child node relationship.
   * 
   * @param parent the parent node
   * @param child the child node
   * @return true if right child, false otherwise
   */
  private boolean isRightChild(Node<T> parent, Node<T> child) {
    if ((parent.rightChild == null)||(child == null)) {
      return false;
    }
    return (parent.rightChild.equals(child));
  }
  
  /** Checks for left child node relationship.
   * 
   * @param parent the parent node
   * @param child the child node
   * @return true if left child, false otherwise
   */
  private boolean isLeftChild(Node<T> parent, Node<T> child) {
    if ((parent.leftChild == null)||(child == null)) {
      return false;
    }
    return (parent.leftChild.equals(child));
  }
  
  /**
   * Returns the sibling for a given node.
   * 
   * @param child the node to find a sibling for
   * @param parent the parent of the child
   * @return the sibling
   */
  private Node<T> getSibling(Node<T> parent, Node<T> child){
    if (isRightChild(parent, child)) {
      return parent.leftChild;
    }
    else if (isLeftChild(parent, child)){
      return parent.rightChild;
    }
    else {
      return null;
    }
  }
  
  /**
   * Checks two parent and child nodes for the following violations:
   * 1) a red parent has a red child
   * 2) a red parent has one child
   * 
   * @param parent the parent of the given child node
   * @param child the child node
   * @return true if violation found, false otherwise
   */
  private boolean checkRedNodeChildren(Node<T> parent, Node<T> child) {
    if (parent == null) {
      return false;
      //indicates root, return false
    }
    if (parent.isBlack) {
      return false; //this type of violation does not occur with a black node
    } //else parent is red
    
    Node<T> sibling = getSibling(parent, child);
    if (sibling == null) {
      return true; //Red nodes cannot have just one child
    }
    if (!(child.isBlack)) {
      return true; //Red nodes cannot have red children
    }
    return false;
  }
  
  /**
   * The following recursive method returns an value for the left black height of a tree
   * and can be used to see if a tree is following the Black Height red black tree property
   * 
   * @param subRoot the node to start from
   * @return the blackHeight of the tree or -1 if an error is found
   */
  private int checkBlackHeight(Node<T> subRoot) {
    int totalBlackHeight = 0;
    if (subRoot == null) {
      return totalBlackHeight;
    }
    if (subRoot.isBlack) {
      totalBlackHeight++;
    }
    int leftBlackHeight = totalBlackHeight;
    int rightBlackHeight = totalBlackHeight;
    if (subRoot.leftChild != null) {
    leftBlackHeight = leftBlackHeight + checkBlackHeight(subRoot.leftChild);
    }
    if (subRoot.rightChild != null) {
    rightBlackHeight =  rightBlackHeight + checkBlackHeight(subRoot.rightChild);
    }
    if (leftBlackHeight != rightBlackHeight) {
      return -1;
    }
    totalBlackHeight = leftBlackHeight;
    return totalBlackHeight;
  }
  
  /**
   * Resolves case one violations in the Red Black Tree
   * Case one: parent's sibling is black and on opposite side of newRedNode
   * Algorithm: rotate the parent's parent and parent
   *    and color swap the two
   * 
   * @param newRedNode the red node that caused the violation to solve
   */
  private void caseOne(Node<T> newRedNode) {
    boolean parentWasBlack = newRedNode.parent.isBlack;
    boolean grandparentWasBlack = newRedNode.parent.parent.isBlack;
    //color swap first
    newRedNode.parent.parent.isBlack = parentWasBlack;
    newRedNode.parent.isBlack = grandparentWasBlack;
    //rotate
    rotate(newRedNode.parent, newRedNode.parent.parent);
  }
  
  /**
   * Resolves case two violations in the Red Black Tree
   * Case two: parent's sibling is black and on same side of NewRedNode
   * Algorithm: rotate the parent node and the newRedNode
   *    then the tree is ready to go through Case 1 with old parent
   * 
   * @param newRedNode the red node that caused the violation to solve
   */
  private void caseTwo(Node<T> newRedNode) {
    rotate(newRedNode, newRedNode.parent);
  }
  
  /**
   * Resolves case three violations in the Red Black Tree
   * Case three: parent's sibling is red
   * Algorithm: change parent's red row to black
   *    black height is now +1 in this subtree compared to rest
   *    change grandparent node to red (if not root: if root, done)
   *    now ready to solve for other cases
   * 
   * @param newRedNode the red node that caused the violation to solve
   */
  private void caseThree(Node<T> newRedNode) {
    Node <T> parent = newRedNode.parent;
    Node<T> parentSibling = getSibling(parent.parent, parent);
    
    parent.isBlack = true;
    parentSibling.isBlack = true;
    if (parent.parent == root) {
      return;
    }
    parent.parent.isBlack = false;
  }

  /**
   * Performs the rotation operation on the provided nodes within this tree. When the provided child
   * is a leftChild of the provided parent, this method will perform a right rotation. When the
   * provided child is a rightChild of the provided parent, this method will perform a left
   * rotation. When the provided nodes are not related in one of these ways, this method will throw
   * an IllegalArgumentException.
   * 
   * @param child  is the node being rotated from child to parent position (between these two node
   *               arguments)
   * @param parent is the node being rotated from parent to child position (between these two node
   *               arguments)
   * @throws IllegalArgumentException when the provided child and parent node references are not
   *                                  initially (pre-rotation) related that way
   */
  private void rotate(Node<T> child, Node<T> parent) throws IllegalArgumentException {
    if ((child == null) || (parent == null)) {
      throw new IllegalArgumentException("Parameters should not be null");
    }
    if (parent.leftChild != null) {
      if (parent.leftChild.equals(child)) {
        // rotate right
        Node<T> storeRightChild = child.rightChild;
        Node<T> storeParent = parent.parent;
        child.rightChild = parent;
        parent.parent = child;
        parent.leftChild = storeRightChild;
        if (storeParent == null) { // indicates root node
          root = child;
        } else {
          if (parent.equals(storeParent.leftChild)) {
            storeParent.leftChild = child;
          } else if (parent.equals(storeParent.rightChild)) {
            storeParent.rightChild = child;
          }
        }
        child.parent = storeParent;

        // all references changed, rotation finished
        return;
      }
    }
    if (parent.rightChild != null) {
      if (parent.rightChild.equals(child)) {
        // rotate left
        Node<T> storeleftChild = child.leftChild;
        Node<T> storeParent = parent.parent;
        child.leftChild = parent;
        parent.parent = child;
        parent.rightChild = storeleftChild;
        if (storeParent == null) { // indicates root node
          root = child;
        } else {
          if (parent.equals(storeParent.leftChild)) {
            storeParent.leftChild = child;
          } else if (parent.equals(storeParent.rightChild)) {
            storeParent.rightChild = child;
          }
        }
        child.parent = storeParent;
        // all references changed, rotation finished
        return;
      }
    }
    throw new IllegalArgumentException("Nodes do not have child-parent relationship.");
  }


  /**
   * Get the size of the tree (its number of nodes).
   * 
   * @return the number of nodes in the tree
   */
  @Override
  public int size() {
    return size;
  }

  /**
   * Method to check if the tree is empty (does not contain any node).
   * 
   * @return true of this.size() return 0, false if this.size() > 0
   */
  @Override
  public boolean isEmpty() {
    return this.size() == 0;
  }

  /**
   * Checks whether the tree contains the value *data*.
   * 
   * @param data the data value to test for
   * @return true if *data* is in the tree, false if it is not in the tree
   */
  @Override
  public boolean contains(T data) {
    // null references will not be stored within this tree
    if (data == null)
      throw new NullPointerException("This RedBlackTree cannot store null references.");
    return this.containsHelper(data, root);
  }

  /**
   * Recursive helper method that recurses through the tree and looks for the value *data*.
   * 
   * @param data    the data value to look for
   * @param subtree the subtree to search through
   * @return true of the value is in the subtree, false if not
   */
  private boolean containsHelper(T data, Node<T> subtree) {
    if (subtree == null) {
      // we are at a null child, value is not in tree
      return false;
    } else {
      int compare = data.compareTo(subtree.data);
      if (compare < 0) {
        // go left in the tree
        return containsHelper(data, subtree.leftChild);
      } else if (compare > 0) {
        // go right in the tree
        return containsHelper(data, subtree.rightChild);
      } else {
        // we found it :)
        return true;
      }
    }
  }

  /**
   * Returns an iterator over the values in in-order (sorted) order.
   * 
   * @return iterator object that traverses the tree in in-order sequence
   */
  @Override
  public Iterator<T> iterator() {
    // use an anonymous class here that implements the Iterator interface
    // we create a new on-off object of this class everytime the iterator
    // method is called
    return new Iterator<T>() {
      // a stack and current reference store the progress of the traversal
      // so that we can return one value at a time with the Iterator
      Stack<Node<T>> stack = null;
      Node<T> current = root;

      /**
       * The next method is called for each value in the traversal sequence. It returns one value at
       * a time.
       * 
       * @return next value in the sequence of the traversal
       * @throws NoSuchElementException if there is no more elements in the sequence
       */
      public T next() {
        // if stack == null, we need to initialize the stack and current element
        if (stack == null) {
          stack = new Stack<Node<T>>();
          current = root;
        }
        // go left as far as possible in the sub tree we are in until we hit a null
        // leaf (current is null), pushing all the nodes we fund on our way onto the
        // stack to process later
        while (current != null) {
          stack.push(current);
          current = current.leftChild;
        }
        // as long as the stack is not empty, we haven't finished the traversal yet;
        // take the next element from the stack and return it, then start to step down
        // its right subtree (set its right sub tree to current)
        if (!stack.isEmpty()) {
          Node<T> processedNode = stack.pop();
          current = processedNode.rightChild;
          return processedNode.data;
        } else {
          // if the stack is empty, we are done with our traversal
          throw new NoSuchElementException("There are no more elements in the tree");
        }

      }

      /**
       * Returns a boolean that indicates if the iterator has more elements (true), or if the
       * traversal has finished (false)
       * 
       * @return boolean indicating whether there are more elements / steps for the traversal
       */
      public boolean hasNext() {
        // return true if we either still have a current reference, or the stack
        // is not empty yet
        return !(current == null && (stack == null || stack.isEmpty()));
      }

    };
  }

  /**
   * This method performs an inorder traversal of the tree. The string representations of each data
   * value within this tree are assembled into a comma separated string within brackets (similar to
   * many implementations of java.util.Collection, like java.util.ArrayList, LinkedList, etc). Note
   * that this RedBlackTree class implementation of toString generates an inorder traversal. The
   * toString of the Node class class above produces a level order traversal of the nodes / values
   * of the tree.
   * 
   * @return string containing the ordered values of this tree (in-order traversal)
   */
  @Override
  public String toString() {
    // use the inorder Iterator that we get by calling the iterator method above
    // to generate a string of all values of the tree in (ordered) in-order
    // traversal sequence
    Iterator<T> treeNodeIterator = this.iterator();
    StringBuffer sb = new StringBuffer();
    sb.append("[ ");
    if (treeNodeIterator.hasNext())
      sb.append(treeNodeIterator.next());
    while (treeNodeIterator.hasNext()) {
      T data = treeNodeIterator.next();
      sb.append(", ");
      sb.append(data.toString());
    }
    sb.append(" ]");
    return sb.toString();
  }

}
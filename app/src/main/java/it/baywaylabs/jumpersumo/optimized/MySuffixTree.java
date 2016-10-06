 /**
 * A class for Suffix Trees.
 * 
 * @author Paul Chew, Feb 2004.
 * 
 * A Suffix Tree is built from a single string and contains all suffixes of
 * that string.  For this implementation, only letters and the separator ($) 
 * can appear in the string.
 * 
 * The separator ($) is treated specially: it terminate suffixes.
 * In other words, each suffix is stored only up to its first $.  This implies
 * that identical suffixes can be stored in the tree.  To handle this situation
 * this implementation allows multiple leaf nodes.
 */

 package it.baywaylabs.jumpersumo.optimized;

import java.util.Vector;

public class MySuffixTree extends SuffixTree {
  
  /**
   * Convert a String into all lowercase and ensure that only letters
   * and the separator ($) appear in the String.  Add a final separator
   * if necessary.
   * @param string string to be converted
   * @return a converted version of the string
   * @exception IllegalArgumentException if a nonletter appears in String
   */
  public static String convert (String string) {
    string = string.toLowerCase();
  
    // Check for illegal characters.
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if (c != '$' && (c < 'a' || c > 'z'))
        throw new IllegalArgumentException("Illegal character");
    }
    // Append $ if necessary.
    if (string.charAt(string.length()-1) != '$') string = string + '$';
    return string;
  }
  
  /**
   * Determine integer that corresponds to given character.
   * $ is 0 and a thru z are 1 thru 26.
   * @param ch 
   * @return the corresponding int.
   */
  public static int index (char ch) {
    return (ch == '$')? 0 : ch - 'a' + 1;
  }
  
  /**
   * Constructor; creates a SuffixTree.
   * The input string should consist only of letters and the separator ($).
   */
  public MySuffixTree (String inputString) {
    string = convert(inputString);
    root = new InternalNode();
    // This technique is inefficient; there is a linear time algorithm.
    for (int i = 0; i < string.length(); i++) {
      LeafNode leaf = insert(i, root);
      leaf.beginIndex = i;
      // System.out.println(this.toString());
    }
  }
  
  /**
   * Insert string starting at loc into the tree rooted at node.
   * @param loc the location with string at which to start.
   * @param node the tree node at which the insertion starts.
   * @return the leaf node where the inserted string ends.
   */
  private LeafNode insert (int loc, InternalNode node) {
    LeafNode leaf;
    int endLoc = 1 + string.indexOf('$', loc);    // First '$' after loc
    int subscript = index(string.charAt(loc));
    Edge edge = node.child[subscript];
    if (edge == null) {
      // Brand new edge
      edge = new Edge();
      edge.beginIndex = loc;
      edge.endIndex = endLoc;
      edge.node = new LeafNode();
      node.child[subscript] = edge;
      return (LeafNode) edge.node;
    }
    String edgeString = string.substring(edge.beginIndex, edge.endIndex);
    if (string.substring(loc, endLoc).equals(edgeString)) {
      // Shared leaf node
      leaf = new LeafNode();
      leaf.more = (LeafNode) edge.node;
      edge.node = leaf;
      return leaf;
    }
    // Edge has to be 'broken' at first disagreement
    int i, j;
    for (i = edge.beginIndex, j = loc;
         (i < edge.endIndex) && (string.charAt(i) == string.charAt(j)); 
         i++, j++) {}
    if (i >= edge.endIndex) {
      // Recursively insert the remaining string in the edge's node.
      return insert(j, (InternalNode) edge.node);
    }
    InternalNode newNode = new InternalNode();
    Edge newEdge = new Edge();
    newEdge.beginIndex = i;
    newEdge.endIndex = edge.endIndex;
    newEdge.node = edge.node;
    edge.endIndex = i;
    edge.node = newNode;
    newNode.child[index(string.charAt(i))] = newEdge;
    leaf = insert(j, newNode);
    return leaf;
  }
  
  /**
   * Locate all longest prefixes of a query string.
   * @param query a String
   * @param root an InternalNode at which to start
   * @param prefixOK true iff prefix match is OK.
   * @return an array of indices showing all the places where query starts.
   */
  private int[] locate (String query, Node node, boolean prefixOK) {
    if (query.length() == 0) return collect(node);
    if (node instanceof LeafNode) {
      if (prefixOK) return collect(node);
      return new int[0];
    }
    Edge edge = ((InternalNode) node).child[index(query.charAt(0))];
    if (edge == null) {
      if (prefixOK) return collect(node);
      return new int[0];
    }
    int queryLength = query.length();
    int i, q;
    for (i = edge.beginIndex, q = 0;
         (q < queryLength) && (i < edge.endIndex) && 
         (query.charAt(q) == string.charAt(i)); i++, q++) {}
    if (q >= queryLength) return collect(edge.node);
    else if (i >= edge.endIndex) 
      return locate(query.substring(q), edge.node, prefixOK);
    else if (prefixOK) return collect(edge.node);
    else return new int[0];
  }
  
  /**
   * Collect all the beginnings in all descendent leaf nodes.
   * @param node Node to start at.
   */
  private int[] collect (Node node) {
    Vector v = new Vector();
    collect(node, v);
    Object[] elements = v.toArray();
    int[] result = new int[elements.length];
    for (int i = 0; i < result.length; i++) 
      result[i] = ((Integer) elements[i]).intValue();
    return result;
  }
  private void collect (Node node, Vector v) {
    if (node instanceof LeafNode) {
      LeafNode leaf = (LeafNode) node;
      do {
        v.add(new Integer(leaf.beginIndex));
        leaf = leaf.more;
      } while (leaf != null);
    }
    else {
      InternalNode iNode = (InternalNode) node;
      for (int i = 0; i < 27; i++) {
        if (iNode.child[i] != null) collect(iNode.child[i].node, v);
      }
    }
    return;
  }
 
  /**
   * Find all occurrences of a query string.
   * @return an array of integer indices showing all the places in
   *  the original string where the query string starts; note that
   *  the size of the array matches the number of occurrences
   */
  public int[] findAll (String query) {
    return locate(query, root, false);
  }

  /**
   * Locate the longest prefix of the given query string that appears as a
   * suffix in the SuffixTree.
   * @param query the query string
   * @return an array of integer indices showing all the places in
   *  the original string where the longest prefix starts
   */
  public int[] longestPrefix (String query) {
    return locate(query, root, true);
  }
}
    
    
    
    
    
    
    
  
  
 
 
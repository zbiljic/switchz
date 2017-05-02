package com.zbiljic.switchz;

/**
 * Radix tree utility functions.
 */
public final class TreeNodeUtil {

  private TreeNodeUtil() { /* No instance methods */ }

  static short countParams(final String path) {
    if (path == null) {
      throw new NullPointerException();
    }
    int n = 0;
    for (char c : path.toCharArray()) {
      if (c != ':' && c != '*') {
        continue;
      }
      n++;
    }
    if (n >= Short.MAX_VALUE) {
      return Short.MAX_VALUE;
    }
    return (short) n;
  }

  /**
   * Prints a radix tree to <code>System.out</code>.
   *
   * @param tree the tree
   */
  public static <V> void dumpTree(TreeNode<V> tree) {
    dumpTree(tree, "");
  }

  /**
   * Prints a subtree to <code>System.out</code>.
   *
   * @param node         the subtree
   * @param outputPrefix prefix to be printed to output
   */
  static <V> void dumpTree(TreeNode<V> node, String outputPrefix) {
    if (node.value != null) {
      System.out.format(" %02d:%02d %s%s[%d] %s %s %s \r\n",
        node.priority, node.maxParams, outputPrefix, String.valueOf(node.path),
        node.children.length, node.value, node.wildChild, node.nodeType);
    } else {
      System.out.format(" %02d:%02d %s%s[%d] <> %s %s \r\n",
        node.priority, node.maxParams, outputPrefix, String.valueOf(node.path),
        node.children.length, node.wildChild, node.nodeType);
    }
    for (TreeNode<V> child : node.children) {
      dumpTree(child, outputPrefix + ". ");
    }
  }
}

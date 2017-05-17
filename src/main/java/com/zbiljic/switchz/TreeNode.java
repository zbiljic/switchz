package com.zbiljic.switchz;

import java.util.Arrays;

/**
 * A node in a radix tree.
 *
 * @param <T> the value type
 */
public final class TreeNode<T> {

  /**
   * An empty immutable {@code TreeNode} array.
   */
  private static final TreeNode[] EMPTY_TREE_NODE_ARRAY = new TreeNode[0];
  /**
   * An empty immutable {@code TreeNode} array.
   */
  private static final Param[] EMPTY_PARAM_ARRAY = new Param[0];

  /** The path at this node. */
  String path;

  /** The type of this node. */
  NodeType nodeType;

  /** Whether this node has wildcard child. */
  boolean wildChild;

  /** Maximum possible number of parameters at this node. */
  short maxParams;

  /** The children lookup reference array. */
  char[] indices;

  /** The children for this node. */
  TreeNode<T>[] children;

  /** The value stored at this node. */
  T value;

  /** The priority of this node. */
  int priority;

  public TreeNode() {
    this.path = "";
    this.nodeType = NodeType.STATIC;
    this.indices = new char[0];
    this.children = EMPTY_TREE_NODE_ARRAY;
  }

  public void add(String path, final T value) {
    if (path == null) {
      throw new NullPointerException("path cannot be null");
    }

    String fullPath = path;
    this.priority++;
    short numParams = TreeNodeUtil.countParams(path);

    // non-empty tree
    if (this.path.length() > 0 || this.children.length > 0) {
      TreeNode<T> n = this; // local pointer
      walk:
      for (; ; ) {
        // Update maxParams of the current node
        if (numParams > n.maxParams) {
          n.maxParams = numParams;
        }

        // Find the longest common prefix.
        // This also implies that the common prefix contains no ':' or '*'
        // since the existing key can't contain those chars.
        int i = 0;
        int max = Math.min(path.length(), n.path.length());
        while (i < max && path.charAt(i) == n.path.charAt(i)) {
          i++;
        }

        // Split edge
        if (i < n.path.length()) {
          final TreeNode<T> child = new TreeNode<>();
          child.path = n.path.substring(i, n.path.length());
          child.wildChild = n.wildChild;
          child.nodeType = NodeType.STATIC;
          child.indices = n.indices;
          child.children = n.children;
          child.value = n.value;
          child.priority = n.priority - 1;

          // Update maxParams (max of all children)
          for (TreeNode node : child.children) {
            if (node.maxParams > child.maxParams) {
              child.maxParams = node.maxParams;
            }
          }

          n.children = new TreeNode[]{child};
          n.indices = new char[]{n.path.charAt(i)};
          n.path = path.substring(0, i);
          n.value = null;
          n.wildChild = false;
        }

        // Make new node a child of this node
        if (i < path.length()) {
          path = path.substring(i);

          if (n.wildChild) {
            n = n.children[0];
            n.priority++;

            // Update maxParams of the child node
            if (numParams > n.maxParams) {
              n.maxParams = numParams;
            }
            numParams--;

            // Check if the wildcard matches
            if (path.length() >= n.path.length() &&
              n.path.compareTo(path.substring(0, n.path.length())) == 0 &&
              // Check for longer wildcard, e.g. :name and :names
              (n.path.length() >= path.length() || path.charAt(n.path.length()) == '/')) {
              continue walk;
            } else {
              // Wildcard conflict
              String pathSeg;
              if (NodeType.CATCH_ALL == n.nodeType) {
                pathSeg = path;
              } else {
                pathSeg = path.split("/", 2)[0];
              }
              String prefix = fullPath.substring(0, fullPath.indexOf(pathSeg)) + String.valueOf(n.path);
              String msg = String.format("'%s' in new path '%s' conflicts with existing wildcard '%s'"
                  + " in existing prefix '%s'",
                pathSeg, fullPath, n.path, prefix);
              throw new IllegalArgumentException(msg);
            }
          }

          char c = path.charAt(0);

          // slash after param
          if (NodeType.PARAM == n.nodeType && c == '/' && n.children.length == 1) {
            n = n.children[0];
            n.priority++;
            continue walk;
          }

          // Check if a child with the next path byte exists
          for (int j = 0; j < n.indices.length; j++) {
            if (c == n.indices[j]) {
              j = n.incrementChildPriority(j);
              n = n.children[j];
              continue walk;
            }
          }

          // Otherwise insert it
          if (c != ':' && c != '*') {
            n.indices = ArrayUtils.add(n.indices, c);
            final TreeNode<T> child = new TreeNode<>();
            child.maxParams = numParams;
            n.children = ArrayUtils.add(n.children, child);
            n.incrementChildPriority(n.indices.length - 1);
            n = child;
          }
          n.insertChild(numParams, path, fullPath, value);

          return;

        } else if (i == path.length()) { // Make node a (in-path) leaf
          if (n.value != null) {
            String msg = String.format("a handle is already registered for path '%s'", fullPath);
            throw new IllegalArgumentException(msg);
          }
          n.value = value;
        }

        return;
      }
    } else {
      // Empty tree
      this.insertChild(numParams, path, fullPath, value);
      this.nodeType = NodeType.ROOT;
    }
  }

  /**
   * Increments priority of the given child and reorders if necessary.
   */
  private int incrementChildPriority(int pos) {
    this.children[pos].priority++;
    int priority = this.children[pos].priority;

    // adjust position (move to front)
    int newPos = pos;
    while (newPos > 0 && this.children[newPos - 1].priority < priority) {
      // swap node positions
      final TreeNode<T> tmp = this.children[newPos - 1];
      this.children[newPos - 1] = this.children[newPos];
      this.children[newPos] = tmp;

      newPos--;
    }

    // build new index char string
    if (newPos != pos) {
      char[] indices;
      indices = Arrays.copyOf(this.indices, newPos); // unchanged prefix, might be empty
      indices = ArrayUtils.addAll(indices, ArrayUtils.subarray(this.indices, pos, pos + 1)); // the index char we move
      // rest without char at 'pos'
      indices = ArrayUtils.addAll(indices, ArrayUtils.subarray(this.indices, newPos, pos));
      indices = ArrayUtils.addAll(indices, ArrayUtils.subarray(this.indices, pos + 1, this.indices.length));
      this.indices = indices;
    }

    return newPos;
  }

  private void insertChild(short numParams, String path, String fullPath, T value) {
    TreeNode<T> n = this; // local pointer

    int offset = 0; // already handled bytes of the path

    // find prefix until first wildcard (beginning with ':'' or '*'')
    for (int i = 0, max = path.length(); numParams > 0; i++) {
      char c = path.charAt(i);
      if (c != ':' && c != '*') {
        continue;
      }

      // find wildcard end (either '/' or path end)
      int end = i + 1;
      while (end < max && path.charAt(end) != '/') {
        switch (path.charAt(end)) {
          // the wildcard name must not contain ':' and '*'
          case ':':
          case '*':
            String subPath = path.substring(i);
            String msg = String.format("only one wildcard per path segment is allowed, has: '%s' in path '%s'",
              subPath, fullPath);
            throw new IllegalArgumentException(msg);
          default:
            end++;
            break;
        }
      }

      // check if this TreeNode existing children which would be
      // unreachable if we insert the wildcard here
      if (n.children.length > 0) {
        String subPath = path.substring(i);
        String msg = String.format("wildcard route '%s' conflicts with existing children in path '%s'",
          subPath, fullPath);
        throw new IllegalArgumentException(msg);
      }

      // check if the wildcard has a name
      if ((end - i) < 2) {
        String msg = String.format("wildcards must be named with a non-empty name in path '%s'", fullPath);
        throw new IllegalArgumentException(msg);
      }

      if (c == ':') { // param
        // split path at the beginning of the wildcard
        if (i > 0) {
          n.path = path.substring(offset, i);
          offset = i;
        }

        final TreeNode<T> child = new TreeNode<>();
        child.nodeType = NodeType.PARAM;
        child.maxParams = numParams;

        n.children = new TreeNode[]{child};
        n.wildChild = true;
        n = child;
        n.priority++;
        numParams--;

        // if the path doesn't end with the wildcard, then there
        // will be another non-wildcard subpath starting with '/'
        if (end < max) {
          n.path = path.substring(offset, end);
          offset = end;

          final TreeNode child2 = new TreeNode();
          child2.maxParams = numParams;
          child2.priority = 1;

          n.children = new TreeNode[]{child2};
          n = child2;
        }

      } else { // catchAll
        if (end != max || numParams > 1) {
          String msg = String.format("catch-all routes are only allowed at the end of the path in path '%s'", fullPath);
          throw new IllegalArgumentException(msg);
        }

        if (n.path.length() > 0 && n.path.charAt(n.path.length() - 1) == '/') {
          String msg = String.format("catch-all conflicts with existing handle for the path segment root in path '%s'", fullPath);
          throw new IllegalArgumentException(msg);
        }

        // currently fixed width 1 for '/'
        i--;
        if (path.charAt(i) != '/') {
          String msg = String.format("no / before catch-all in path '%s'", fullPath);
          throw new IllegalArgumentException(msg);
        }

        n.path = path.substring(offset, i);

        // first node: catchAll node with empty path
        final TreeNode<T> child = new TreeNode<>();
        child.wildChild = true;
        child.nodeType = NodeType.CATCH_ALL;
        child.maxParams = 1;

        n.children = new TreeNode[]{child};
        n.indices = new char[]{path.charAt(i)};
        n = child;
        n.priority++;

        // second node: node holding the variable
        final TreeNode<T> child2 = new TreeNode<>();
        child2.path = path.substring(i);
        child2.nodeType = NodeType.CATCH_ALL;
        child2.maxParams = 1;
        child2.value = value;
        child2.priority = 1;

        n.children = new TreeNode[]{child2};

        return;
      }
    }

    // insert remaining path part and handle to the leaf
    n.path = path.substring(offset);
    n.value = value;
  }

  /**
   * Returns the value registered with the given path (key). The values of wildcards are saved to a
   * map.
   * <p>
   * If no handle can be found, a TSR (trailing slash redirect) recommendation is made if a handle
   * exists with an extra (without the) trailing slash for the given path.
   */
  public NodeMatch<T> get(String path) {
    if (path == null) {
      throw new NullPointerException("path cannot be null");
    }

    TreeNode<T> n = this; // local pointer
    Param[] params = EMPTY_PARAM_ARRAY;
    boolean tsr = false;

    // outer loop for walking the tree
    walk:
    for (; ; ) {
      if (path.length() > n.path.length()) {
        if (path.substring(0, n.path.length()).compareTo(n.path) == 0) {
          path = path.substring(n.path.length());
          // If this node does not have a wildcard (param or catchAll)
          // child,  we can just look up the next child node and continue
          // to walk down the tree
          if (!n.wildChild) {
            char c = path.charAt(0);
            for (int i = 0; i < n.indices.length; i++) {
              if (c == n.indices[i]) {
                n = n.children[i];
                continue walk;
              }
            }

            // Nothing found.
            // We can recommend to redirect to the same URL without a
            // trailing slash if a leaf exists for that path.
            tsr = (path.equals("/") && n.value != null);
            return new NodeMatch<>(tsr);
          }

          // handle wildcard child
          n = n.children[0];
          switch (n.nodeType) {
            case PARAM: {
              // find param end (either '/' or path end)
              int end = 0;
              while (end < path.length() && path.charAt(end) != '/') {
                end++;
              }

              // save param value
              if (params.length == 0) {
                // lazy allocation
                params = new Param[0];
              }
              int i = params.length;
              params = Arrays.copyOf(params, i + 1); // expand within pre-allocated capacity
              params[i] = new Param(n.path.substring(1, n.path.length()), path.substring(0, end));

              // we need to go deeper!
              if (end < path.length()) {
                if (n.children.length > 0) {
                  path = path.substring(end);
                  n = n.children[0];
                  continue walk;
                }

                // ... but we can't
                tsr = (path.length() == end + 1);
                return new NodeMatch<>(tsr);
              }

              if (n.value != null) {
                return new NodeMatch<>(n.path, n.value, params);
              } else if (n.children.length == 1) {
                // No handle found. Check if a handle for this path + a
                // trailing slash exists for TSR recommendation
                n = n.children[0];
                tsr = (path.equals("/") && n.value != null);
              }

              return new NodeMatch<>(n.path, null, params, tsr);
            }

            case CATCH_ALL: {
              // save param value
              if (params.length == 0) {
                // lazy allocation
                params = new Param[0];
              }
              int i = params.length;
              params = Arrays.copyOf(params, i + 1); // expand within pre-allocated capacity
              params[i] = new Param(n.path.substring(2, n.path.length()), path);

              return new NodeMatch<>(n.path, n.value, params);
            }

            default:
              throw new IllegalStateException("invalid node type");
          }
        }

      } else if (path.compareTo(n.path) == 0) {
        // We should have reached the node containing the handle.
        // Check if this node has a handle registered.
        if (n.value != null) {
          return new NodeMatch<>(n.path, n.value, params);
        }

        if (path.equals("/") && n.wildChild && NodeType.ROOT != n.nodeType) {
          return new NodeMatch<>(n.path, null, params, true);
        }

        // No handle found. Check if a handle for this path + a
        // trailing slash exists for trailing slash recommendation
        for (int i = 0; i < n.indices.length; i++) {
          if (n.indices[i] == '/') {
            n = n.children[i];
            tsr = (n.path.length() == 1 && n.value != null) ||
              (NodeType.CATCH_ALL == n.nodeType && n.children[0].value != null);
            return new NodeMatch<>(tsr);
          }
        }

        return new NodeMatch<>(n.path, null, params, false);
      }

      // Nothing found. We can recommend to redirect to the same URL with an
      // extra trailing slash if a leaf exists for that path
      tsr = (path.equals("/")) ||
        (n.path.length() == path.length() + 1 &&
          n.path.charAt(path.length()) == '/' &&
          path.compareTo(n.path.substring(0, n.path.length() - 1)) == 0 &&
          n.value != null);

      return new NodeMatch<>(tsr);
    }
  }

  @Override
  public String toString() {
    return "TreeNode{" +
      "path='" + path + '\'' +
      ", nodeType=" + nodeType +
      ", wildChild=" + wildChild +
      ", maxParams=" + maxParams +
      ", indices=" + Arrays.toString(indices) +
      ", children=" + Arrays.toString(children) +
      ", value=" + value +
      ", priority=" + priority +
      '}';
  }
}

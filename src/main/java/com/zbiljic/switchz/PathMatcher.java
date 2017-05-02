package com.zbiljic.switchz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathMatcher<T> {

  private static final Logger log = LoggerFactory.getLogger(PathMatcher.class);

  private static final String STRING_PATH_SEPARATOR = "/";

  private volatile T defaultHandler;

  /** The root node in this path matcher. */
  private final TreeNode<T> root;

  public PathMatcher(final T defaultHandler) {
    this();
    this.defaultHandler = defaultHandler;
  }

  /**
   * Default constructor.
   */
  public PathMatcher() {
    root = new TreeNode<>();
  }

  /**
   * Matches a path against the registered handlers.
   *
   * @param path The relative path to match
   * @return The match match. This will never be null, however if none matched its value field will
   * be
   */
  public NodeMatch<T> match(final String path) {

    NodeMatch<T> match = root.get(path);
    if (match.getValue() != null) {
      if (log.isDebugEnabled()) {
        log.debug("Matched path: %s", path);
      }
      return match;
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Attempting normalized path: %s", URLUtils.normalizeSlashes(path));
      }
      match = root.get(URLUtils.normalizeSlashes(path));
      if (match.getValue() != null) {
        return match;
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("Matched default handler path: %s", path);
    }
    return new NodeMatch<>("", defaultHandler);
  }

  public synchronized PathMatcher addPath(final String path, final T handler) {
    if (path.isEmpty()) {
      throw new IllegalArgumentException("Path must be specified");
    }

    final String normalizedPath = URLUtils.normalizeSlashes(path);

    if (PathMatcher.STRING_PATH_SEPARATOR.equals(normalizedPath)) {
      this.defaultHandler = handler;
      return this;
    }

    root.add(path, handler);

    return this;
  }
}

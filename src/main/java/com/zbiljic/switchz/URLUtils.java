package com.zbiljic.switchz;

/**
 * Utilities for dealing with URLs.
 */
public final class URLUtils {

  private URLUtils() { /* No instance methods */ }

  private static final char PATH_SEPARATOR = '/';

  /**
   * Adds a '/' prefix to the beginning of a path if one isn't present and removes trailing slashes
   * if any are present.
   *
   * @param path the path to normalize
   * @return a normalized (with respect to slashes) result
   */
  public static String normalizeSlashes(final String path) {
    // prepare
    final StringBuilder builder = new StringBuilder(path);
    boolean modified = false;

    // remove all trailing '/'s except the first one
    while (builder.length() > 0 && builder.length() != 1 && PATH_SEPARATOR == builder.charAt(builder.length() - 1)) {
      builder.deleteCharAt(builder.length() - 1);
      modified = true;
    }

    // add a slash at the beginning if one isn't present
    if (builder.length() == 0 || PATH_SEPARATOR != builder.charAt(0)) {
      builder.insert(0, PATH_SEPARATOR);
      modified = true;
    }

    // only create string when it was modified
    if (modified) {
      return builder.toString();
    }

    return path;
  }
}

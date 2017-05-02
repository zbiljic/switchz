package com.zbiljic.switchz;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public final class NodeMatch<T> {

  private final String matched;
  private final T value;
  private final SimpleEntry<String, String>[] params;
  private final boolean trailingSlashRedirect;

  private transient Map<String, String> parameters;

  public NodeMatch(boolean trailingSlashRedirect) {
    this.matched = null;
    this.value = null;
    this.params = null;
    this.trailingSlashRedirect = trailingSlashRedirect;
  }

  public NodeMatch(String matched, T value) {
    this.matched = matched;
    this.value = value;
    this.params = null;
    this.trailingSlashRedirect = false;
  }

  public NodeMatch(String matched, T value, SimpleEntry<String, String>[] params) {
    this.matched = matched;
    this.value = value;
    this.params = params;
    this.trailingSlashRedirect = false;
  }

  public NodeMatch(String matched, T value, SimpleEntry<String, String>[] params, boolean trailingSlashRedirect) {
    this.matched = matched;
    this.value = value;
    this.params = params;
    this.trailingSlashRedirect = trailingSlashRedirect;
  }

  public String getMatched() {
    return matched;
  }

  public T getValue() {
    return value;
  }

  public Map<String, String> getParameters() {
    if (parameters == null) {
      if (params == null || params.length == 0) {
        parameters = Collections.emptyMap();
      } else {
        parameters = new SimpleStringStringMap(params);
      }
    }
    return parameters;
  }

  public boolean isTrailingSlashRedirect() {
    return trailingSlashRedirect;
  }

  /**
   * Returns a string representation of this object; useful for testing and debugging.
   *
   * @return A string representation of this object.
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "NodeMatch{" +
      "matched='" + matched + '\'' +
      ", value=" + value +
      ", params=" + Arrays.toString(params) +
      ", trailingSlashRedirect=" + trailingSlashRedirect +
      '}';
  }
}

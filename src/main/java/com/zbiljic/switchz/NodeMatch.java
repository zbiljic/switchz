package com.zbiljic.switchz;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public final class NodeMatch<T> {

  private static final Param[] EMPTY_PARAM_ARRAY = new Param[0];

  private final String matched;
  private final T value;
  private final Param[] params;
  private final boolean trailingSlashRedirect;

  private transient Map<String, String> parameters;

  public NodeMatch(boolean trailingSlashRedirect) {
    this.matched = null;
    this.value = null;
    this.params = EMPTY_PARAM_ARRAY;
    this.trailingSlashRedirect = trailingSlashRedirect;
  }

  public NodeMatch(String matched, T value) {
    this.matched = matched;
    this.value = value;
    this.params = EMPTY_PARAM_ARRAY;
    this.trailingSlashRedirect = false;
  }

  public NodeMatch(String matched, T value, Param[] params) {
    this.matched = matched;
    this.value = value;
    this.params = params;
    this.trailingSlashRedirect = false;
  }

  public NodeMatch(String matched, T value, Param[] params, boolean trailingSlashRedirect) {
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

  public Param[] getParameters() {
    return params.clone();
  }

  public Map<String, String> getParametersAsMap() {
    if (parameters == null) {
      if (params == null || params.length == 0) {
        parameters = Collections.emptyMap();
      } else {
        parameters = Collections.unmodifiableMap(Arrays.stream(params)
          .collect(Collectors.toMap(Param::getKey, Param::getValue)));
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

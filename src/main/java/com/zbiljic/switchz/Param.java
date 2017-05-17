package com.zbiljic.switchz;

import java.util.Objects;

/**
 * Param is a single URL parameter, consisting of a key and a value.
 */
public class Param {

  private final String key;

  private final String value;

  public Param(String key, String value) {
    this.key = key;
    this.value = value;
  }

  /**
   * Returns the key corresponding to this param.
   *
   * @return the key corresponding to this param
   */
  public String getKey() {
    return key;
  }

  /**
   * Returns the value corresponding to this param.
   *
   * @return the value corresponding to this param
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns a String representation of this param.  This implementation returns the string
   * representation of this param's key followed by the equals character ("<tt>=</tt>") followed by
   * the string representation of this param's value.
   *
   * @return a String representation of this map entry
   */
  public String toString() {
    return key + "=" + value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Param param = (Param) o;
    return Objects.equals(key, param.key) &&
      Objects.equals(value, param.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }
}

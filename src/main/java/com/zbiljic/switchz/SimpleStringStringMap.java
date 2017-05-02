package com.zbiljic.switchz;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Simple {@link Map} where keys and values are of type {@code String}.
 * <p>
 * Not really conforming to the {@link Map} interface because it is possible to add multiple entries
 * with the same key, but on retrieval only one will be returned.
 *
 * @see Map
 * @see AbstractMap
 */
@NotThreadSafe
class SimpleStringStringMap extends AbstractMap<String, String> implements Map<String, String> {

  private final SimpleEntry<String, String>[] entries;

  private transient Set<String> keySet;
  private transient Collection<String> values;
  private transient Set<Map.Entry<String, String>> entrySet;

  SimpleStringStringMap(SimpleEntry<String, String>... entries) {
    if (entries == null) {
      throw new NullPointerException();
    }
    this.entries = entries;
  }

  @Override
  public int size() {
    return entries.length;
  }

  @Override
  public String put(String key, String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> keySet() {
    if (keySet == null) {
      keySet = Collections.unmodifiableSet(Arrays.stream(entries)
        .map(SimpleEntry::getKey).collect(Collectors.toSet()));
    }
    return keySet;
  }

  @Override
  public Collection<String> values() {
    if (values == null) {
      values = Collections.unmodifiableSet(Arrays.stream(entries)
        .map(SimpleEntry::getValue).collect(Collectors.toSet()));
    }
    return values;
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    if (entrySet == null) {
      entrySet = Collections.unmodifiableSet(Arrays.stream(entries)
        .collect(Collectors.toSet()));
    }
    return entrySet;
  }

  @Override
  public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String putIfAbsent(String key, String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object key, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean replace(String key, String oldValue, String newValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String replace(String key, String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String computeIfAbsent(String key, Function<? super String, ? extends String> mappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String computeIfPresent(String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String compute(String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String merge(String key, String value, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
    throw new UnsupportedOperationException();
  }
}

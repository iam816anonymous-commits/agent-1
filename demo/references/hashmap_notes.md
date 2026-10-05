# Java Collections Framework - HashMap Reference Guide

## 1. Overview of HashMap
HashMap is a hash table based implementation of the Java Map interface. It stores key-value pairs where each key must be unique. HashMap permits one null key and multiple null values.

## 2. Key Concepts & Architecture
- **Key-Value Mapping**: Maps keys of type K to values of type V.
- **Hashing**: Uses `hashCode()` to derive an integer hash value for each key, which determines the target bucket index.
- **Buckets & Array Structure**: Internally backed by an array of Node objects (buckets).
- **Collision Handling**: When two keys resolve to the same bucket index (collision), HashMap uses linked lists (or balanced trees when bucket size exceeds TREEIFY_THRESHOLD = 8).

## 3. Mandatory Contract: equals() and hashCode()
- If two objects are equal according to `equals(Object)`, calling `hashCode()` on each must produce the same integer result.
- Overriding `equals()` without overriding `hashCode()` breaks HashMap lookup because equal keys will be routed to different bucket indices.
- Never mutate key objects after placing them in a HashMap; changing key properties that affect `hashCode()` makes the entry unreachable.

## 4. Key Operations
- `put(K key, V value)`: Inserts or updates the entry for key.
- `get(Object key)`: Retrieves value associated with key, or null if key does not exist.
- `containsKey(Object key)`: Checks if key exists.
- `remove(Object key)`: Removes mapping for key.
- `getOrDefault(Object key, V defaultValue)`: Safe retrieval with fallback.

## 5. Common Patterns & Best Practices
- **Frequency Counting**: Use `map.put(key, map.getOrDefault(key, 0) + 1)`.
- **Iteration**: Iterate over `map.entrySet()` using `for (Map.Entry<K, V> entry : map.entrySet())`.

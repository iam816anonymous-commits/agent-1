# Practical HashMap Code Examples

## Example 1: Word Frequency Counter
```java
import java.util.HashMap;
import java.util.Map;

public class WordCounter {
    public static Map<String, Integer> countFrequencies(String[] words) {
        Map<String, Integer> counts = new HashMap<>();
        for (String word : words) {
            counts.put(word, counts.getOrDefault(word, 0) + 1);
        }
        return counts;
    }
}
```

## Example 2: Safely Iterating over HashMap
```java
import java.util.HashMap;
import java.util.Map;

public class MapPrinter {
    public static void printMap(Map<String, Integer> map) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}
```

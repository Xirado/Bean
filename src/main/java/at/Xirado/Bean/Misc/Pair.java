package at.Xirado.Bean.Misc;

public class Pair<K,V>
{
    private final K key;
    private final V value;

    public Pair(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }
}

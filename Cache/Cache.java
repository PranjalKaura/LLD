package Cache;
import java.util.*;

public class Cache<K,V> {
    HashMap<K,V> map;
    ICacheInvalidationStrategy<K> cacheInvalidationStrategy;
}

interface ICacheInvalidationStrategy<K>
{
    public void keyAccessed(K key);
    public K evictKey();
}

class LRUCacheInvalidationStrategy<K> implements ICacheInvalidationStrategy<K>
{

    HashMap<K, Node<K>> map;
    Node<K> head;
    Node<K> tail;

    public LRUCacheInvalidationStrategy()
    {
        this.map = new HashMap<>();
        this.head = new Node<K>(null);
        this.tail = new Node<K>(null);
        this.head.prev = tail;
        this.tail.next = head;
    }

    @Override
    public void keyAccessed(K key) {
        if(map.containsKey(key))
        {
            remove(map.get(key));
        }
        Node<K> node = new Node<K>(key);
        map.put(key, node);
        add(node);
    }

    @Override
    public K evictKey() {
        K lastKey = tail.next.key;
        tail = tail.next;
        return lastKey;
    }

    public void remove(Node<K> node)
    {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    public void add(Node<K> node)
    {
        Node<K> firstNode = head.prev;
        firstNode.next = node;
        node.prev = firstNode;
        node.next = head;
        head.prev = node;
    }
}

class Node<K>
{
    K key;
    Node<K> next;
    Node<K> prev;
    public Node(K key)
    {
        this.key = key;
    }
}

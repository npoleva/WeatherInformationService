package Repositories;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherCacheRepository {
    private static class CacheEntry {
        String data;
        long expireAt;
        CacheEntry(String data, long expireAt) {
            this.data = data;
            this.expireAt = expireAt;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final int ttlSeconds = 15 * 60;

    public void save(String key, String data) {
        long expire = System.currentTimeMillis() + ttlSeconds * 1000L;
        cache.put(key, new CacheEntry(data, expire));
    }

    public String find(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || System.currentTimeMillis() > entry.expireAt) {
            cache.remove(key);
            return null;
        }
        return entry.data;
    }
}

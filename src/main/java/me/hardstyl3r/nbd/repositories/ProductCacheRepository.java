package me.hardstyl3r.nbd.repositories;

import me.hardstyl3r.nbd.objects.Product;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tools.jackson.databind.json.JsonMapper;

public class ProductCacheRepository implements ProductRepository {

    private final ProductRepository productRepository;
    private final JedisPool jedisPool;
    private final JsonMapper objectMapper;

    private static final long TTL_SECONDS = 3600L;

    public ProductCacheRepository(ProductRepository productRepository, JedisPool jedisPool) {
        this.productRepository = productRepository;
        this.jedisPool = jedisPool;
        this.objectMapper = new JsonMapper();
    }

    @Override
    public Product findById(String id) {
        String cacheKey = "product:" + id;

        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(cacheKey);
            if (json != null) {
                return objectMapper.readValue(json, Product.class);
            }
        } catch (Exception e) {
            System.err.println("Error reading from Redis (fallback to DB): " + e.getMessage());
        }

        Product product = productRepository.findById(id);

        if (product != null) {
            try (Jedis jedis = jedisPool.getResource()) {
                String json = objectMapper.writeValueAsString(product);
                jedis.setex(cacheKey, TTL_SECONDS, json);
            } catch (Exception e) {
                System.err.println("Error saving to Redis: " + e.getMessage());
            }
        }

        return product;
    }

    @Override
    public void save(Product product) {
        productRepository.save(product);
        invalidateCache(product.id());
    }

    @Override
    public void delete(String id) {
        productRepository.delete(id);
        invalidateCache(id);
    }

    private void invalidateCache(String id) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("product:" + id);
        } catch (Exception e) {
            System.err.println("Error invalidating cache: " + e.getMessage());
        }
    }
}
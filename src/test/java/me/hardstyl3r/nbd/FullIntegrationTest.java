package me.hardstyl3r.nbd;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import me.hardstyl3r.nbd.objects.Product;
import me.hardstyl3r.nbd.repositories.ProductCacheRepository;
import me.hardstyl3r.nbd.repositories.ProductMongoRepository;
import me.hardstyl3r.nbd.repositories.ProductRepository;
import org.junit.jupiter.api.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FullIntegrationTest {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private JedisPool jedisPool;
    private ProductCacheRepository redisRepository;
    private ProductRepository mongoRepository;

    @BeforeAll
    void setup() {
        try {
            mongoClient = MongoClients.create("mongodb://localhost:27017");
            mongoDatabase = mongoClient.getDatabase("nbd_db");

            mongoDatabase.runCommand(new org.bson.Document("ping", 1));
            jedisPool = new JedisPool("localhost", 6379);
            try (Jedis jedis = jedisPool.getResource()) {
                if (!"PONG".equals(jedis.ping())) throw new RuntimeException("Redis dead");
            }

            mongoRepository = new ProductMongoRepository(mongoDatabase);
            redisRepository = new ProductCacheRepository(mongoRepository, jedisPool);
        } catch (Exception e) {
            assumeTrue(false, "Docker environment is not ready. " + e.getMessage());
        }
    }

    @BeforeEach
    void cleanUp() {
        mongoDatabase.getCollection("test_products").drop();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushAll();
        }
    }

    @AfterAll
    void tearDown() {
        if (mongoClient != null) mongoClient.close();
        if (jedisPool != null) jedisPool.close();
    }

    @Test
    void shouldSaveToMongoAndInvalidateCache() {
        String id = "prod-1";
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set("product:" + id, "stare śmieci");
        }

        Product newProduct = new Product(id, "Nowy Produkt", 100.0, "Opis");
        redisRepository.save(newProduct);

        Product fromMongo = mongoRepository.findById(id);
        assertNotNull(fromMongo);
        assertEquals("Nowy Produkt", fromMongo.name());

        try (Jedis jedis = jedisPool.getResource()) {
            assertFalse(jedis.exists("product:" + id), "Cache powinien być pusty po zapisie");
        }
    }

    @Test
    void shouldFetchFromMongoAndPopulateCacheOnMiss() {
        String id = "prod-2";
        Product p = new Product(id, "Mongo Only", 50.0, "Brak w cache");
        mongoRepository.save(p);

        Product result = redisRepository.findById(id);
        assertEquals("Mongo Only", result.name());

        try (Jedis jedis = jedisPool.getResource()) {
            assertTrue(jedis.exists("product:" + id), "Produkt powinien trafić do cache po odczycie");
            assertEquals("3600", String.valueOf(jedis.ttl("product:" + id)), "TTL powinien być ustawiony (lub bliski 3600)");
        }
    }

    @Test
    void shouldServeFromCacheWithoutTouchingMongo() {
        String id = "prod-3";
        String cachedJson = "{\"id\":\"prod-3\",\"name\":\"Szybki Cache\",\"price\":1.0,\"description\":\"Nie ma mnie w Mongo\"}";

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set("product:" + id, cachedJson);
        }

        assertNull(mongoRepository.findById(id));
        Product result = redisRepository.findById(id);
        assertNotNull(result);
        assertEquals("Szybki Cache", result.name());
    }
}
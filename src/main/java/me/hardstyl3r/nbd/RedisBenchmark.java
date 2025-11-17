package me.hardstyl3r.nbd;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import me.hardstyl3r.nbd.objects.Product;
import me.hardstyl3r.nbd.repositories.ProductCacheRepository;
import me.hardstyl3r.nbd.repositories.ProductMongoRepository;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class RedisBenchmark {

    private ProductMongoRepository mongoRepository;
    private ProductCacheRepository redisRepository;
    private JedisPool jedisPool;
    private MongoClient mongoClient;

    private final String HIT_ID = "bench_hit_id";
    private final String MISS_ID = "bench_miss_id";
    private final String DB_ID = "bench_db_id";

    @Setup(Level.Trial)
    public void setup() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = mongoClient.getDatabase(ConfigLoader.getProperty("mongo.collection.benchmark"));

        mongoRepository = new ProductMongoRepository(db);
        jedisPool = new JedisPool("localhost", 6379);
        redisRepository = new ProductCacheRepository(mongoRepository, jedisPool);

        Product pHit = new Product(HIT_ID, "Hit Product", 100.0, "Opis dla Hitu");
        Product pMiss = new Product(MISS_ID, "Miss Product", 200.0, "Opis dla Missa");
        Product pDb = new Product(DB_ID, "DB Product", 300.0, "Opis dla DB");

        mongoRepository.save(pHit);
        mongoRepository.save(pMiss);
        mongoRepository.save(pDb);

        redisRepository.findById(HIT_ID);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (jedisPool != null) jedisPool.close();
        if (mongoClient != null) mongoClient.close();
    }

    @Benchmark
    public Product testDirectDatabase() {
        return mongoRepository.findById(DB_ID);
    }

    @Benchmark
    public Product testCacheHit() {
        return redisRepository.findById(HIT_ID);
    }

    @Benchmark
    public Product testCacheMiss() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("product:" + MISS_ID);
        }
        return redisRepository.findById(MISS_ID);
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(RedisBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
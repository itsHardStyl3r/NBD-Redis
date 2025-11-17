package me.hardstyl3r.nbd;

import me.hardstyl3r.nbd.objects.Product;
import me.hardstyl3r.nbd.repositories.ProductCacheRepository;
import me.hardstyl3r.nbd.repositories.ProductMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockRedisProductCacheTest {

    @Mock
    private ProductMongoRepository mongoRepository;

    @Mock
    private JedisPool jedisPool;

    @Mock
    private Jedis jedis;

    @InjectMocks
    private ProductCacheRepository repository;

    @Test
    void shouldReturnProductFromCache_WhenItExistsInRedis() throws Exception {
        String id = "100";
        String json = "{\"id\":\"100\",\"name\":\"Test\",\"price\":10.0,\"description\":\"Opis\"}";

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.get("product:100")).thenReturn(json);

        Product result = repository.findById(id);
        assertNotNull(result);
        assertEquals("Test", result.name());

        verify(mongoRepository, never()).findById(any());
    }

    @Test
    void shouldFetchFromDbAndSaveToCache_WhenCacheMiss() {
        String id = "200";
        Product dbProduct = new Product(id, "DbProduct", 20.0, "Opis DB");

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.get("product:200")).thenReturn(null);
        when(mongoRepository.findById(id)).thenReturn(dbProduct);

        Product result = repository.findById(id);
        assertEquals("DbProduct", result.name());

        verify(mongoRepository).findById(id);
        verify(jedis).setex(eq("product:200"), eq(3600L), contains("DbProduct"));
    }

    @Test
    void shouldFetchFromDb_WhenRedisThrowsException() {
        String id = "300";
        Product dbProduct = new Product(id, "Survivor", 30.0, "Opis");

        when(jedisPool.getResource()).thenThrow(new RuntimeException("Connection refused"));
        when(mongoRepository.findById(id)).thenReturn(dbProduct);

        Product result = repository.findById(id);
        assertNotNull(result);
        assertEquals("Survivor", result.name());

        verify(mongoRepository).findById(id);
    }

    @Test
    void shouldInvalidateCache_OnSave() {
        Product newProduct = new Product("400", "New", 40.0, "Desc");
        when(jedisPool.getResource()).thenReturn(jedis);

        repository.save(newProduct);

        verify(mongoRepository).save(newProduct);
        verify(jedis).del("product:400");
    }
}
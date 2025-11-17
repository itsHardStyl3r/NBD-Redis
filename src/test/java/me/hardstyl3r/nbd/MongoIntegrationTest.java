package me.hardstyl3r.nbd;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import me.hardstyl3r.nbd.objects.Product;
import me.hardstyl3r.nbd.repositories.ProductMongoRepository;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoIntegrationTest {

    private MongoClient mongoClient;
    private ProductMongoRepository repository;
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DB_NAME = "nbd_db";

    @BeforeAll
    void setup() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            MongoDatabase database = mongoClient.getDatabase(DB_NAME);
            database.runCommand(new Document("ping", 1));
            repository = new ProductMongoRepository(database);
            database.getCollection(ConfigLoader.getProperty("mongo.collection.test")).drop();
        } catch (Exception e) {
            assumeTrue(false, "Could not connect to MongoDB (is Docker even running?).");
        }
    }

    @AfterAll
    void tearDown() {
        if (mongoClient != null) mongoClient.close();
    }

    @Test
    void shouldSaveAndRetrieveProductFromRealMongo() {
        String productId = "mongo_id";
        Product product = new Product(productId, "MongoDB Book!", 999.99, "Live testing");

        repository.save(product);
        Product fetchedProduct = repository.findById(productId);

        assertNotNull(fetchedProduct, "Could not fetch product from MongoDB");
        assertEquals(product.name(), fetchedProduct.name());
        assertEquals(product.price(), fetchedProduct.price());
        assertEquals(product.description(), fetchedProduct.description());
    }

    @Test
    void shouldReturnNullForUnknownId() {
        Product result = repository.findById("unknown");
        assertNull(result, "Repository should return null on unknown ID");
    }

    @Test
    void shouldUpdateExistingProduct() {
        String id = "rolling_product";
        Product original = new Product(id, "Oldie", 10.0, "Description");
        repository.save(original);

        Product updated = new Product(id, "Newie", 20.0, "New description");
        repository.save(updated);

        Product fetched = repository.findById(id);
        assertEquals("Newie", fetched.name());
        assertEquals(20.0, fetched.price());
        assertEquals("New description", fetched.description());
    }
}
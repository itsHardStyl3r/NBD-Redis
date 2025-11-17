package me.hardstyl3r.nbd.repositories;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import me.hardstyl3r.nbd.objects.Product;
import org.bson.Document;

import static me.hardstyl3r.nbd.objects.ProductMapper.toDocument;
import static me.hardstyl3r.nbd.objects.ProductMapper.toProduct;

public class ProductMongoRepository implements ProductRepository {

    private final MongoCollection<Document> collection;

    public ProductMongoRepository(MongoDatabase database) {
        this.collection = database.getCollection("products");
    }

    @Override
    public Product findById(String id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        return doc == null ? null : toProduct(doc);
    }

    @Override
    public void save(Product product) {
        Document doc = toDocument(product);
        collection.replaceOne(
                Filters.eq("_id", product.id()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public void delete(String id) {
        collection.deleteOne(Filters.eq("_id", id));
    }
}

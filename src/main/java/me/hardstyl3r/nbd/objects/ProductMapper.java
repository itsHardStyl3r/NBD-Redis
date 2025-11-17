package me.hardstyl3r.nbd.objects;

import org.bson.Document;

public class ProductMapper {

    public static Product toProduct(Document doc) {
        return doc == null ? null : new Product(
                doc.getString("_id"),
                doc.getString("name"),
                doc.getDouble("price"),
                doc.getString("description")
        );
    }

    public static Document toDocument(Product product) {
        return product == null ? null : new Document()
                .append("_id", product.id())
                .append("name", product.name())
                .append("price", product.price())
                .append("description", product.description());
    }
}

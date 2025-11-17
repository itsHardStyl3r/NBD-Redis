package me.hardstyl3r.nbd;

import me.hardstyl3r.nbd.objects.Product;
import me.hardstyl3r.nbd.objects.ProductMapper;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductUnitTest {

    @Test
    void shouldReadOnConstruction() {
        Product product = new Product("1", "Laptop", 2500.00, "Super szybki laptop");

        assertNotNull(product);
        assertEquals("Laptop", product.name());
        assertEquals(2500.00, product.price());
    }

    @Test
    void shouldThrowExceptionWhenCreatingInvalidProduct() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Product("", "Zły produkt", 10.0, "Opis");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Product("3", "Zły produkt", -5.0, "Opis");
        });
    }

    @Test
    void shouldMapProductToDocument() {
        Product product = new Product("123", "Gaming Mouse", 199.00, "Steelseries");
        Document document = ProductMapper.toDocument(product);

        assertNotNull(document);
        assertEquals("123", document.getString("_id"));
        assertEquals("Gaming Mouse", document.getString("name"));
        assertEquals(199.00, document.getDouble("price"));
        assertEquals("Steelseries", document.getString("description"));
    }

    @Test
    void shouldMapDocumentToProduct() {
        Document document = new Document()
                .append("_id", "456")
                .append("name", "Keyboard")
                .append("price", 300.00)
                .append("description", "Mechanical");
        Product product = ProductMapper.toProduct(document);

        assertNotNull(product);
        assertEquals("456", product.id());
        assertEquals("Keyboard", product.name());
        assertEquals(300.00, product.price());
        assertEquals("Mechanical", product.description());
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        assertNull(ProductMapper.toDocument(null));
        assertNull(ProductMapper.toProduct(null));
    }

    @Test
    void shouldThrowExceptionWhenDocumentHasMissingId() {
        Document brokenDoc = new Document()
                .append("name", "Ghost")
                .append("price", 10.0);
        assertThrows(IllegalArgumentException.class, () -> {
            ProductMapper.toProduct(brokenDoc);
        });
    }

    @Test
    void shouldThrowNPEWhenDocumentHasMissingPriceField() {
        Document brokenDoc = new Document()
                .append("_id", "broken-1")
                .append("name", "Ghost");
        assertThrows(NullPointerException.class, () -> {
            ProductMapper.toProduct(brokenDoc);
        });
    }
}

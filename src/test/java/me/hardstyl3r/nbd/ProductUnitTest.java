package me.hardstyl3r.nbd;

import me.hardstyl3r.nbd.objects.Product;
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
}
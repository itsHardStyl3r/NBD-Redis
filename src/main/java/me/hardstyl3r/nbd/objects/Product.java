package me.hardstyl3r.nbd.objects;

import java.io.Serializable;

public record Product(String id, String name, double price, String description) implements Serializable {

    public Product {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }
}
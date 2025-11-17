package me.hardstyl3r.nbd.repositories;

import me.hardstyl3r.nbd.objects.Product;

public interface ProductRepository {
    Product findById(String id);
    void save(Product product);
    void delete(String id);
}
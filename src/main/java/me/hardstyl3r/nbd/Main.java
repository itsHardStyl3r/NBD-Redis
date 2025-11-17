package me.hardstyl3r.nbd;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import me.hardstyl3r.nbd.objects.Product;
import me.hardstyl3r.nbd.repositories.ProductMongoRepository;
import me.hardstyl3r.nbd.repositories.ProductRepository;

public class Main {
    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("nbd_db");
            ProductRepository mongoRepo = new ProductMongoRepository(database);

            System.out.println("Saving product...");
            Product p = new Product("101", "Klawiatura Mechaniczna", 350.00, "Clicky switches");
            mongoRepo.save(p);

            System.out.println("Reading product...");
            Product loaded = mongoRepo.findById("101");
            System.out.println("Read product: " + loaded);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.example.catshopbackend.security;

import com.example.catshopbackend.models.Product;
import com.example.catshopbackend.models.Tag;
import com.example.catshopbackend.repositories.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

        @Service
        @RequiredArgsConstructor
        public class ProductInitService {

            private final ProductRepository productRepository;

            @PostConstruct
            public void initializeProducts() {
                if (productRepository.count() == 0) {
                    // Create all sample products
                    createSampleProducts();
                    System.out.println("Sample products have been created.");
                }
            }

            private void createSampleProducts() {
                List<Product> products = Arrays.asList(
                    createProduct(101, "Bio-Hundetrockenfutter",
                        "Getreidefreies Trockenfutter für ausgewachsene Hunde mit Rindfleisch.",
                        24.99, "Hunde", "assets/images/hundetrockenfutter.jpg",
                        true, 4.7, new String[]{"bio", "getreidefrei", "hund"}),

                    createProduct(102, "Premium-Katzenfutter mit Lachs",
                        "Saftiges Nassfutter mit echtem Lachs, reich an Omega-3.",
                        1.89, "Katzen", "assets/images/katzenfutter-lachs.jpg",
                        true, 4.8, new String[]{"nassfutter", "lachs", "katze"}),

                    createProduct(103, "Kleintier-Müsli",
                        "Gesundes Mixfutter für Hamster, Meerschweinchen und Co.",
                        3.49, "Kleintier", "assets/images/kleintier-muesli.jpg",
                        true, 4.3, new String[]{"kleintiere", "müsli", "vitaminreich"}),

                    createProduct(104, "Barf-Rindfleisch-Mix (tiefgekühlt)",
                        "100% Rindfleisch für BARF-Fütterung bei Hunden.",
                        5.99, "Hunde", "assets/images/barf-rind.jpg",
                        false, 4.6, new String[]{"barf", "rind", "hund"}),

                    createProduct(105, "Getreidefreies Trockenfutter für Katzen",
                        "Mit Huhn und Süßkartoffeln – für sensible Mägen.",
                        12.99, "Katzen", "assets/images/katzenfutter-huhn.jpg",
                        true, 4.4, new String[]{"getreidefrei", "katze", "spezialfutter"}),

                    createProduct(106, "Fischflocken für Zierfische",
                        "Nährstoffreiche Flocken für tropische Aquarienfische.",
                        4.25, "Fisch", "assets/images/fischflocken.jpg",
                        true, 4.1, new String[]{"fisch", "aquarium", "flakes"}),

                    createProduct(107, "Kräuter-Heu für Nager",
                        "Duftendes Heu mit Kräutermix für Kaninchen und Meerschweinchen.",
                        2.99, "Kleintier", "assets/images/kraeuter-heu.jpg",
                        true, 4.5, new String[]{"heu", "nager", "bio"}),

                    createProduct(108, "Snacksticks für Hunde – Lamm",
                        "Leckere Zwischenmahlzeit für alle Hunderassen.",
                        3.79, "Hunde", "assets/images/hundesnacks-lamm.jpg",
                        true, 4.9, new String[]{"hund", "snack", "lamm"}),

                    createProduct(109, "Wellensittich-Körnerfutter",
                        "Ausgewogene Körnermischung für gesunde Vögel.",
                        2.49, "Vogel", "assets/images/wellensittich-futter.jpg",
                        true, 4.2, new String[]{"vogel", "körner", "vitamine"})
                );

                for (Product product : products) {
                    createProductIfNotExists(product);
                }
            }

            private Product createProduct(int id, String name, String description, double price,
                                          String category, String imageUrl, boolean inStock,
                                          double rating, String[] tagNames) {
                Product product = new Product();
                product.setId(id);
                product.setName(name);
                product.setDescription(description);
                product.setPrice(price);
                product.setCategory(category);
                product.setImageUrl(imageUrl);
                product.setInStock(inStock);
                product.setRating(rating);

                // Konvertiere String-Tags in Tag-Objekte
                List<Tag> tags = Arrays.stream(tagNames)
                        .map(tagName -> {
                            Tag tag = new Tag();
                            tag.setName(tagName);
                            return tag;
                        })
                        .toList();

                product.setTags(tags);
                return product;
            }

            private void createProductIfNotExists(Product product) {
                if (productRepository.findById(product.getId()).isEmpty()) {
                    productRepository.save(product);
                    System.out.println("Product created: " + product.getName() + " with price: " + product.getPrice());
                }
            }
        }
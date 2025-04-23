package com.example.catshopbackend.mapper;

import com.example.catshopbackend.dto.ProductDTO;
import com.example.catshopbackend.dto.TagDTO;
import com.example.catshopbackend.models.Product;
import com.example.catshopbackend.models.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    // Converts a Tag entity to a TagDTO + checks if there are tags
    public TagDTO toDTO(Tag tag) {
        if (tag == null) return null;
        return new TagDTO(tag.getId(), tag.getName());
    }

    public List<TagDTO> toTagDTOList(List<Tag> tags) {
        if (tags == null) return null;
        return tags.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO toDTO(Product product) {
        if (product == null) return null;
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageUrl(),
                product.isInStock(),
                product.getRating(),
                toTagDTOList(product.getTags())
        );
    }

    public List<ProductDTO> toDTOList(List<Product> products) {
        if (products == null) return null;
        return products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
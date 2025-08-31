package com.probihar.spec;

import com.probihar.model.Post;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecifications {
    public static Specification<Post> hasCategory(String category) {
        return (root, query, cb) ->
                category == null || category.isBlank() ? null :
                        cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    public static Specification<Post> hasSubcategory(String subcategory) {
        return (root, query, cb) ->
                subcategory == null || subcategory.isBlank() ? null :
                        cb.equal(cb.lower(root.get("subcategory")), subcategory.toLowerCase());
    }

    public static Specification<Post> searchQ(String q) {
        return (root, query, cb) ->
                q == null || q.isBlank() ? null :
                        cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%");
    }
}

package com.probihar.controller;

import com.probihar.model.Post;
import com.probihar.repository.PostRepository;
import com.probihar.spec.PostSpecifications;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin // adjust origins for your frontend
@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostRepository repo;

    public PostController(PostRepository repo) {
        this.repo = repo;
    }

    /**
     * Compatible with JSON Server-style params:
     *   q, category, subcategory, _page, _limit, _sort, _order
     */
    // READ (any authenticated user)
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','AUTHOR','ADMIN')")
    public ResponseEntity<List<Post>> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "subcategory", required = false) String subcategory,
            @RequestParam(value = "_page", required = false, defaultValue = "1") int pageNum,
            @RequestParam(value = "_limit", required = false, defaultValue = "5") int limit,
            @RequestParam(value = "_sort", required = false, defaultValue = "id") String sortField,
            @RequestParam(value = "_order", required = false, defaultValue = "asc") String order
    ) {
        Sort.Direction dir = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        // Spring pages are 0-based; JSON Server is 1-based:
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), Math.max(limit, 1), Sort.by(dir, sortField));

        Specification<Post> spec =
                Specification.where(PostSpecifications.searchQ(q))
                        .and(PostSpecifications.hasCategory(category))
                        .and(PostSpecifications.hasSubcategory(subcategory));

        Page<Post> page = repo.findAll(spec, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','AUTHOR','ADMIN')")
    public ResponseEntity<Post> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // CREATE (AUTHOR or ADMIN). AuthorUsername is taken from JWT principal.
    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<Post> create(@Valid @RequestBody Post post) {
        Post saved = repo.save(post);
        return ResponseEntity.created(URI.create("/posts/" + saved.getId())).body(saved);
    }

    // UPDATE: owner OR ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("@postSecurity.isOwner(#id, authentication) or hasRole('ADMIN')")
    public ResponseEntity<Post> update(@PathVariable Long id, @Valid @RequestBody Post updated) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setTitle(updated.getTitle());
                    existing.setCategory(updated.getCategory());
                    existing.setSubcategory(updated.getSubcategory());
                    existing.setCreatedAt(updated.getCreatedAt() != null ? updated.getCreatedAt() : existing.getCreatedAt());
                    existing.setUpdatedAt(updated.getUpdatedAt() != null ? updated.getUpdatedAt() : existing.getUpdatedAt());
                    return ResponseEntity.ok(repo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE: owner OR ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("@postSecurity.isOwner(#id, authentication) or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

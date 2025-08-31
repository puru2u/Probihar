package com.probihar.security;

import com.probihar.repository.PostRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PostSecurity {
    private final PostRepository repo;

    public PostSecurity(PostRepository repo) {
        this.repo = repo;
    }

    public boolean isOwner(Long postId, Authentication auth) {
        if (auth == null || auth.getName() == null) return false;
        return repo.findById(postId)
                .map(p -> p.getAuthorUsername() != null && p.getAuthorUsername().equals(auth.getName()))
                .orElse(false);
    }
}

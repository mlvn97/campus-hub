package fr.esgi.news_api.services;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class NewsAccess {
    private final JdbcClient jdbc;

    boolean canManage(Authentication actor, Long associationId) {
        var authorities = actor.getAuthorities();
        if (authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN_CAMPUS"))) {
            return true;
        }
        if (associationId == null || authorities.stream()
                .noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN_ASSOCIATION"))) {
            return false;
        }

        long userId;
        try {
            userId = Long.parseLong(actor.getName());
        } catch (NumberFormatException exception) {
            return false;
        }

        return jdbc.sql("""
                select exists (
                    select 1 from memberships
                    where association_id = :associationId and user_id = :userId and role = 'PRESIDENT'
                )
                """)
                .param("associationId", associationId)
                .param("userId", userId)
                .query(Boolean.class)
                .single();
    }

    boolean associationExists(long associationId) {
        return jdbc.sql("select exists (select 1 from associations where id = :id)")
                .param("id", associationId)
                .query(Boolean.class)
                .single();
    }
}

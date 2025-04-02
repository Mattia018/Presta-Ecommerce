package com.prestashop.web.specification;



import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.prestashop.web.domain.Order;

public class OrderSpecification {
	public static Specification<Order> hasStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Order> createdAtAfter(LocalDateTime startDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    public static Specification<Order> createdAtBefore(LocalDateTime endDate) {
        return (root, query, cb) -> cb.lessThan(root.get("createdAt"), endDate);
    }

    public static Specification<Order> hasUserEmailLike(String email) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("userEmail")), "%" + email.toLowerCase() + "%");
    }

}

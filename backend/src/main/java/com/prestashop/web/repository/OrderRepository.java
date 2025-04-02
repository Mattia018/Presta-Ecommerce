package com.prestashop.web.repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prestashop.web.domain.Order;
import com.prestashop.web.domain.User;
import com.prestashop.web.domain.enumeration.Status;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order>{
	Optional<Order> findById(Long id);
	
    List<Order> findByUser(User user);
    
    Page<Order>findAll(Pageable pageable);
    
    Page<Order> findByUser(User user, Pageable pageable);
    
    Page<Order> findByUserAndStatus(User user, Status status, Pageable pageable);
    
    
    Page<Order> findBysequId(Long id, Pageable pageable);
    
    long countByUserAndStatusIn(User user, List<Status> statuses);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses")
    long countByStatusIn(@Param("statuses") List<Status> statuses);
    
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status IN :statuses")
    double sumTotalPriceByStatusIn(@Param("statuses") List<Status> statuses);
    
    @Query("SELECT DATE(o.createdAt) as date, COUNT(o) as count, SUM(o.totalPrice) as total " +
            "FROM Order o " +
            "WHERE o.status IN ('CONFIRMED', 'SHIPPED', 'DELIVERED') " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "GROUP BY DATE(o.createdAt)")
     List<Object[]> countDailySales(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);;
    
}

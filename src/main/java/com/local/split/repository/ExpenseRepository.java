package com.local.split.repository;

import com.local.split.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByGroupId(Long groupId);

    // Corrected Query: Only fetches shares eagerly, and relies on @Transactional for payments
    @Query("SELECT e FROM Expense e JOIN FETCH e.group g LEFT JOIN FETCH e.shares s LEFT JOIN FETCH s.friend sf WHERE e.group.id = :groupId")
    List<Expense> findExpensesWithDetailsByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Query("DELETE FROM Expense e WHERE e.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
}
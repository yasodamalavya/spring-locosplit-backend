package com.local.split.repository;

import com.local.split.model.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // <-- Import this
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {

    // A custom query to delete all shares for a given expense ID
    @Modifying
    @Query("DELETE FROM ExpenseShare es WHERE es.expense.id = :expenseId")
    void deleteByExpenseId(@Param("expenseId") Long expenseId);
    @Modifying
    @Query("DELETE FROM ExpenseShare es WHERE es.expense.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
}
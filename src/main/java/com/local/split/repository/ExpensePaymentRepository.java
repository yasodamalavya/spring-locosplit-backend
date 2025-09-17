package com.local.split.repository;

import com.local.split.model.ExpensePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpensePaymentRepository extends JpaRepository<ExpensePayment, Long> {

    @Modifying
    @Query("DELETE FROM ExpensePayment ep WHERE ep.expense.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
}
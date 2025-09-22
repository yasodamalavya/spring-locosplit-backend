package com.local.split.dto;

import com.local.split.model.ExpensePayment;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AddExpenseRequest {
    private String description;
    private Long groupId;
    private List<ExpensePayment> payments;
    private List<Long> allMemberIds; // <-- A new field to hold all member IDs
}
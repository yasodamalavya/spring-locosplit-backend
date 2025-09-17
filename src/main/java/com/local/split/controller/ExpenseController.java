package com.local.split.controller;

import com.local.split.model.Expense;
import com.local.split.model.Friend;
import com.local.split.model.Group;
import com.local.split.dto.AddExpenseRequest; // <-- Import the DTO
import com.local.split.repository.ExpenseRepository;
import com.local.split.repository.ExpenseShareRepository;
import com.local.split.repository.FriendRepository;
import com.local.split.repository.GroupRepository;
import com.local.split.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional; // <-- You can now remove this if you want, but it's fine to keep

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "http://localhost:5173")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private FriendRepository friendRepository;
    
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ExpenseShareRepository expenseShareRepository;

    @GetMapping("/group/{groupId}")
    public List<Expense> getExpensesByGroupId(@PathVariable Long groupId) {
        return expenseService.getExpensesForGroup(groupId);
    }

    // UPDATED: This method now accepts the DTO and calls the new service logic
    @PostMapping
    public Expense addExpense(@RequestBody AddExpenseRequest request) {
        return expenseService.addExpense(request);
    }

    @GetMapping("/balances/{groupId}")
    public Map<String, BigDecimal> getBalances(@PathVariable Long groupId) {
        return expenseService.calculateBalances(groupId);
    }
}
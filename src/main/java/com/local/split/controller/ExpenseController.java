package com.local.split.controller;

import com.local.split.model.Expense;
import com.local.split.model.Friend;
import com.local.split.model.Group;
import com.local.split.repository.ExpenseRepository;
import com.local.split.repository.ExpenseShareRepository;
import com.local.split.repository.FriendRepository;
import com.local.split.repository.GroupRepository;
import com.local.split.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

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

    @PostMapping
    public Expense addExpense(@RequestBody Expense expense) {
        Friend paidBy = friendRepository.findById(expense.getPaidBy().getId()).orElseThrow();
        Group group = groupRepository.findById(expense.getGroup().getId()).orElseThrow();

        expense.setPaidBy(paidBy);
        expense.setGroup(group);
        expense.setDate(LocalDateTime.now());
        
        return expenseService.addExpense(expense);
    }

    @GetMapping("/balances/{groupId}")
    public Map<String, BigDecimal> getBalances(@PathVariable Long groupId) {
        return expenseService.calculateBalances(groupId);
    }
}
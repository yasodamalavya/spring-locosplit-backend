package com.local.split.service;

import com.local.split.model.Expense;
import com.local.split.model.ExpenseShare;
import com.local.split.model.Friend;
import com.local.split.model.Group;
import com.local.split.repository.ExpenseRepository;
import com.local.split.repository.ExpenseShareRepository;
import com.local.split.repository.FriendRepository;
import com.local.split.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList; // <-- Import ArrayList
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private FriendRepository friendRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private ExpenseShareRepository expenseShareRepository;

    // Corrected method: Build and save the complete expense object with shares
    public Expense addExpense(Expense expense) {
        Set<Friend> members = expense.getGroup().getMembers();
        
        if (members.isEmpty()) {
            throw new IllegalStateException("Group has no members to split the expense with.");
        }
        
        List<ExpenseShare> shares = new ArrayList<>();
        BigDecimal totalAmount = expense.getAmount();
        BigDecimal newShareAmount = totalAmount.divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);
        
        for (Friend member : members) {
            ExpenseShare newShare = new ExpenseShare();
            newShare.setExpense(expense);
            newShare.setFriend(member);
            newShare.setShareAmount(newShareAmount);
            shares.add(newShare);
        }
        
        expense.setShares(shares);
        
        return expenseRepository.save(expense);
    }
    
    // NEW METHOD: For getting expenses for a group using a JOIN FETCH query
    public List<Expense> getExpensesForGroup(Long groupId) {
        // Use a custom query to eagerly fetch related data and avoid N+1 queries
        return expenseRepository.findExpensesWithDetailsByGroupId(groupId);
    }

    public Map<String, BigDecimal> calculateBalances(Long groupId) {
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        Set<Friend> friends = groupRepository.findById(groupId).orElseThrow().getMembers();

        Map<Long, BigDecimal> balances = new HashMap<>();
        friends.forEach(friend -> balances.put(friend.getId(), BigDecimal.ZERO));

        for (Expense expense : expenses) {
            balances.computeIfPresent(expense.getPaidBy().getId(), (id, balance) -> balance.add(expense.getAmount()));
            
            if (expense.getShares() != null) {
                for (ExpenseShare share : expense.getShares()) {
                    balances.computeIfPresent(share.getFriend().getId(), (id, balance) -> balance.subtract(share.getShareAmount()));
                }
            }
        }

        Map<String, BigDecimal> finalBalances = new HashMap<>();
        balances.forEach((friendId, balance) ->
            friendRepository.findById(friendId).ifPresent(friend -> finalBalances.put(friend.getName(), balance))
        );

        return finalBalances;
    }
    
    public void recalculateSharesForGroup(Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        Set<Friend> members = group.getMembers();
        
        if (members.isEmpty()) {
            return;
        }
        
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        int memberCount = members.size();
        
        for (Expense expense : expenses) {
            expenseShareRepository.deleteByExpenseId(expense.getId());
            
            BigDecimal totalAmount = expense.getAmount();
            BigDecimal newShareAmount = totalAmount.divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP);
            
            for (Friend member : members) {
                ExpenseShare newShare = new ExpenseShare();
                newShare.setExpense(expense);
                newShare.setFriend(member);
                newShare.setShareAmount(newShareAmount);
                expenseShareRepository.save(newShare);
            }
        }
    }
}
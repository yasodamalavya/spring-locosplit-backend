package com.local.split.service;

import com.local.split.model.Expense;
import com.local.split.model.ExpenseShare;
import com.local.split.model.ExpensePayment;
import com.local.split.model.Friend;
import com.local.split.model.Group;
import com.local.split.dto.AddExpenseRequest;
import com.local.split.repository.ExpenseRepository;
import com.local.split.repository.ExpenseShareRepository;
import com.local.split.repository.ExpensePaymentRepository;
import com.local.split.repository.FriendRepository;
import com.local.split.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private ExpensePaymentRepository expensePaymentRepository;

    // CORRECTED: This method now only calculates shares for the new expense
    public Expense addExpense(AddExpenseRequest request) {
        Expense newExpense = new Expense();
        newExpense.setDescription(request.getDescription());
        
        BigDecimal totalAmount = request.getPayments().stream()
                .map(ExpensePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        newExpense.setAmount(totalAmount);
        
        Group group = groupRepository.findById(request.getGroupId()).orElseThrow();
        newExpense.setGroup(group);
        newExpense.setDate(LocalDateTime.now());
        
        List<Long> allMemberIds = new ArrayList<>();
        allMemberIds.addAll(group.getMembers().stream().map(Friend::getId).collect(Collectors.toList()));
        allMemberIds.addAll(request.getPayments().stream().map(p -> p.getFriend().getId()).collect(Collectors.toList()));
        
        List<Long> memberIdsToAdd = allMemberIds.stream()
                                    .distinct()
                                    .filter(id -> !group.getMembers().stream().anyMatch(f -> f.getId().equals(id)))
                                    .collect(Collectors.toList());

        if (!memberIdsToAdd.isEmpty()) {
            List<Friend> friendsToAdd = friendRepository.findAllById(memberIdsToAdd);
            group.getMembers().addAll(friendsToAdd);
            groupRepository.save(group);
        }

        Expense savedExpense = expenseRepository.save(newExpense);
        
        for (ExpensePayment payment : request.getPayments()) {
            payment.setExpense(savedExpense);
            payment.setFriend(friendRepository.findById(payment.getFriend().getId()).orElseThrow());
            expensePaymentRepository.save(payment);
        }
        
        // This is the correct logic for shares
        Set<Friend> members = group.getMembers();
        if (members.isEmpty()) {
            throw new IllegalStateException("Group has no members to split the expense with.");
        }
        int memberCount = members.size();
        BigDecimal newShareAmount = totalAmount.divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP);
        
        for (Friend member : members) {
            ExpenseShare newShare = new ExpenseShare();
            newShare.setExpense(savedExpense);
            newShare.setFriend(member);
            newShare.setShareAmount(newShareAmount);
            expenseShareRepository.save(newShare);
        }

        return expenseRepository.findById(savedExpense.getId()).orElse(null);
    }
    
    // This method is now obsolete and no longer needed
    public void addMembersToGroup(Long groupId, List<Long> memberIds) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        List<Friend> friendsToAdd = friendRepository.findAllById(memberIds);
        
        for (Friend friend : friendsToAdd) {
            group.getMembers().add(friend);
        }
        
        groupRepository.save(group);
        recalculateSharesForGroup(groupId);
    }

    public List<Expense> getExpensesForGroup(Long groupId) {
        return expenseRepository.findExpensesWithDetailsByGroupId(groupId);
    }

    public Map<String, BigDecimal> calculateBalances(Long groupId) {
        List<Expense> expenses = expenseRepository.findExpensesWithDetailsByGroupId(groupId);
        Set<Friend> friends = groupRepository.findById(groupId).orElseThrow().getMembers();

        Map<Long, BigDecimal> balances = new HashMap<>();
        friends.forEach(friend -> balances.put(friend.getId(), BigDecimal.ZERO));

        for (Expense expense : expenses) {
            if (expense.getPayments() != null) {
                for (ExpensePayment payment : expense.getPayments()) {
                    balances.computeIfPresent(payment.getFriend().getId(), (id, balance) -> balance.add(payment.getAmount()));
                }
            }
            
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
    
    // The recalculateSharesForGroup method is now obsolete and no longer used
    public void recalculateSharesForGroup(Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        Set<Friend> members = group.getMembers();
        
        if (members.isEmpty()) {
            return;
        }
        
        List<Expense> expenses = expenseRepository.findExpensesWithDetailsByGroupId(groupId);
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
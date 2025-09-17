package com.local.split.service;

import com.local.split.model.Group;
import com.local.split.repository.ExpenseRepository;
import com.local.split.repository.GroupRepository;
import com.local.split.repository.ExpenseShareRepository;
import com.local.split.repository.ExpensePaymentRepository; // <-- Import this
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseShareRepository expenseShareRepository;
    
    @Autowired
    private ExpensePaymentRepository expensePaymentRepository; // <-- Autowire this

    @Transactional
    public void deleteGroup(Long groupId) {
        // Step 1: Delete all ExpensePayment records for the group's expenses
        expensePaymentRepository.deleteByGroupId(groupId);

        // Step 2: Delete all ExpenseShare records for the group's expenses
        expenseShareRepository.deleteByGroupId(groupId);

        // Step 3: Now delete all Expense records for the group
        expenseRepository.deleteByGroupId(groupId);

        // Step 4: Finally, delete the group itself
        groupRepository.deleteById(groupId);
    }
}
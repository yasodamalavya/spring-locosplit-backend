package com.local.split.controller;

import com.local.split.model.Friend;
import com.local.split.model.Group;
import com.local.split.repository.FriendRepository;
import com.local.split.repository.GroupRepository;
import com.local.split.service.ExpenseService; // <-- Import the ExpenseService
import com.local.split.service.GroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173")
@Transactional
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private FriendRepository friendRepository;
    
    @Autowired
    private ExpenseService expenseService; // <-- Autowire the ExpenseService

    @GetMapping
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        return groupRepository.findById(id)
                .map(group -> new ResponseEntity<>(group, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public Group createGroup(@RequestBody Group newGroup) {
        // Find the friends in the DB
        Set<Friend> members = newGroup.getMembers().stream()
                                    .map(friend -> friendRepository.findById(friend.getId()).orElse(null))
                                    .collect(Collectors.toSet());
        newGroup.setMembers(members);
        return groupRepository.save(newGroup);
    }
    
    // NEW METHOD: Add a new friend to an existing group and recalculate shares
    @PostMapping("/{groupId}/add-friend/{friendId}")
    public ResponseEntity<Group> addMemberToGroup(@PathVariable Long groupId, @PathVariable Long friendId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        Friend friend = friendRepository.findById(friendId).orElse(null);

        if (group == null || friend == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        group.getMembers().add(friend);
        Group updatedGroup = groupRepository.save(group);
        
        // After adding the new member, trigger the dynamic share recalculation
        expenseService.recalculateSharesForGroup(groupId);

        return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
    }
    
    @Autowired
    private GroupService groupService; // <-- Autowire the new service

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        if (groupRepository.existsById(id)) {
            groupService.deleteGroup(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    

}
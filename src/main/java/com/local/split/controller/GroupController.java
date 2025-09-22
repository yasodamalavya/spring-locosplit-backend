package com.local.split.controller;

import com.local.split.model.Friend;
import com.local.split.model.Group;
import com.local.split.repository.FriendRepository;
import com.local.split.repository.GroupRepository;
import com.local.split.service.ExpenseService;
import com.local.split.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

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
    private ExpenseService expenseService;
    
    @Autowired
    private GroupService groupService;

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
        Set<Friend> members = newGroup.getMembers().stream()
                                    .map(friend -> friendRepository.findById(friend.getId()).orElseThrow())
                                    .collect(Collectors.toSet());
        newGroup.setMembers(members);
        return groupRepository.save(newGroup);
    }
    
    // CORRECTED: This method now only adds a new member and does not trigger recalculation
    @PostMapping("/{groupId}/add-friend/{friendId}")
    public ResponseEntity<Group> addMemberToGroup(@PathVariable Long groupId, @PathVariable Long friendId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        Friend friend = friendRepository.findById(friendId).orElse(null);

        if (group == null || friend == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<Long> memberIds = new ArrayList<>();
        memberIds.add(friendId);
        expenseService.addMembersToGroup(groupId, memberIds);
        
        Group updatedGroup = groupRepository.findById(groupId).orElse(null);

        return new ResponseEntity<>(updatedGroup, HttpStatus.OK);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        if (groupRepository.existsById(id)) {
            groupService.deleteGroup(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
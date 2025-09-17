package com.local.split.controller;

import com.local.split.model.Friend;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.local.split.repository.FriendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "http://localhost:5173")
public class FriendController {

    @Autowired
    private FriendRepository friendRepository;

    @GetMapping
    public List<Friend> getAllFriends() {
        return friendRepository.findAll();
    }

    @PostMapping
    public Friend createFriend(@RequestBody Friend friend) {
        return friendRepository.save(friend);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFriend(@PathVariable Long id) {
        if (friendRepository.existsById(id)) {
            friendRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
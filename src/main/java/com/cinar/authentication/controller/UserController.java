package com.cinar.authentication.controller;

import com.cinar.authentication.dto.UserDto;
import com.cinar.authentication.dto.request.CreateUserRequest;
import com.cinar.authentication.model.User;
import com.cinar.authentication.repository.UserRepository;
import com.cinar.authentication.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("v1/api/user")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper ;

    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username){
        var user = modelMapper.map(userService.getUserByUsername(username),UserDto.class);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email){
        var user = modelMapper.map(userService.getUserByEmail(email),UserDto.class);
        return ResponseEntity.ok(user);
    }
    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest createUserRequest){
        var user = modelMapper.map(userService.createUser(createUserRequest),UserDto.class);
        return ResponseEntity.ok(user);
    }
}

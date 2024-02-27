package com.cinar.authentication.controller;

import com.cinar.authentication.dto.UserDto;
import com.cinar.authentication.dto.request.CreateUserRequest;
import com.cinar.authentication.dto.request.UpdateUserRequest;
import com.cinar.authentication.model.User;
import com.cinar.authentication.repository.UserRepository;
import com.cinar.authentication.service.JwtService;
import com.cinar.authentication.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("v1/api/user")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper ;

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, ModelMapper modelMapper, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }
    /*@GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username){
        var user = modelMapper.map(userService.getUserByUsername(username),UserDto.class);
        return ResponseEntity.ok(user);
    }*/
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email){
        var user = modelMapper.map(userService.getUserByEmail(email),UserDto.class);
        return ResponseEntity.ok(user);
    }
    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest createUserRequest){
        var user = modelMapper.map(userService.createNewUser(createUserRequest),UserDto.class);
        return ResponseEntity.ok(user);
    }
    @PutMapping("/update/{email}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String email,@RequestBody UpdateUserRequest updateUserRequest){
        var user = modelMapper.map(userService.updateUser(email,updateUserRequest),UserDto.class);
        return ResponseEntity.ok(user);
    }
    @DeleteMapping("/delete/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username){
        userService.deleteUser(username);
        return ResponseEntity.ok().build();
    }
}

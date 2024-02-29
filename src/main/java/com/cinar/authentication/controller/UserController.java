package com.cinar.authentication.controller;

import com.cinar.authentication.dto.UserDto;
import com.cinar.authentication.dto.request.CreateUserRequest;
import com.cinar.authentication.dto.request.UpdateUserRequest;
import com.cinar.authentication.dto.response.UserResponse;

import com.cinar.authentication.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("v1/api/user")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper ;

    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;

    }

    //@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<UserResponse> getAllUsers(@RequestParam(value = "pageNo" ,defaultValue = "0",required = false) int pageNo,
                                                    @RequestParam(value = "pageSize" ,defaultValue = "5",required = false) int pageSize) {
        return ResponseEntity.ok(userService.getAllUsers(pageNo,pageSize));
    }
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email){
        var user = modelMapper.map(userService.getUserByEmail(email),UserDto.class);
        return ResponseEntity.ok(user);
    }
    //@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest createUserRequest){
        var user = modelMapper.map(userService.createNewUser(createUserRequest),UserDto.class);
        return ResponseEntity.ok(user);
    }
    //@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PutMapping("/update/{email}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String email,@RequestBody UpdateUserRequest updateUserRequest){
        var user = modelMapper.map(userService.updateUser(email,updateUserRequest),UserDto.class);
        return ResponseEntity.ok(user);
    }
    //@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email){
        userService.deleteUser(email);
        return ResponseEntity.ok().build();
    }
}

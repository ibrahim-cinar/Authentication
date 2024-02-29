package com.cinar.authentication.service;

import com.cinar.authentication.config.UserContextHolder;
import com.cinar.authentication.dto.UserDto;
import com.cinar.authentication.dto.request.CreateUserRequest;
import com.cinar.authentication.dto.request.UpdateUserRequest;
import com.cinar.authentication.dto.response.UserResponse;
import com.cinar.authentication.exceptions.*;
import com.cinar.authentication.model.BaseEntity;
import com.cinar.authentication.model.User;
import com.cinar.authentication.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.modelMapper = modelMapper;

    }

    public UserResponse getAllUsers(int pageNo,int pageSize) {
        Pageable pageable = PageRequest.of(pageNo,pageSize);
        Page<User> users = userRepository.findAll(pageable);
        List<User> userList = users.getContent();
        List<UserDto> content = userList.stream().map(user -> modelMapper.map(user, UserDto.class)).collect(Collectors.toList());

        UserResponse userResponse = new UserResponse();

        userResponse.setContent(content);
        userResponse.setPageNo(users.getNumber());
        userResponse.setPageSize(users.getSize());
        userResponse.setTotalElements(users.getTotalElements());
        userResponse.setTotalPages(users.getTotalPages());
        userResponse.setLast(users.isLast());

        return userResponse;

    }

    protected Optional<User> findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public User getUserByEmail(String email) {
        return findUserByEmail(email).orElseThrow(() -> new EmailNotFoundException("User could not find by email " + email));

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserByEmail(username);
        return user.orElseThrow(EntityNotFoundException::new);
    }

    public static boolean patternMatches(String emailAddress, String regexPattern) {
        return !Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }

    protected boolean isEmailUnique(String email) {
        Optional<User> existingUserEmail = userRepository.findUserByEmail(email);
        return existingUserEmail.isPresent();
    }



    private static boolean isInputValid(CreateUserRequest request) {
        return
                request.getFirstName() != null &&
                request.getLastName() != null &&
                request.getEmail() != null &&
                request.getPassword() != null &&
                request.getAuthorities() != null;
    }

    public User createUser(CreateUserRequest request) {
        var userDetails = getUserLoginEmail();
        logger.info("Logged in user email: " + userDetails);

        return new User(
               userDetails,
                request.getFirstName(), request.getLastName(),
                request.getEmail(),
                bCryptPasswordEncoder.encode(request.getPassword()),
                request.getPhoneNumber(),
                request.getAuthorities());
    }
    public String getUserLoginEmail(){
        return UserContextHolder.getUser().getUsername();
    }

    public User createUserFromRequest(CreateUserRequest request) {
        if (!isInputValid(request)) {
            throw new InvalidInputException("Invalid input");
        }
        if (isEmailUnique(request.getEmail())) {
            throw new EmailAlreadyExistException("Email already exists");
        }

        if (patternMatches(request.getEmail(), "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            throw new InvalidInputException("Email is not valid");
        }
        if (patternMatches(request.getPhoneNumber(), "\\d{10}|(?:\\d{3}-){2}\\d{4}|\\(\\d{3}\\)\\d{3}-?\\d{4}")) {
            throw new InvalidInputException("Phone number is not valid");
        }
        return createUser(request);
    }
    public User createNewUser(CreateUserRequest createUserRequest) {
        User user = createUserFromRequest(createUserRequest);
        return userRepository.save(user);
    }

    public User updateUser(String email, UpdateUserRequest updateUserRequest) {
        var userDetails = getUserLoginEmail();
        User user = findUserByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("User not found with this email: " + email));

        validateUniqueFields(user, updateUserRequest);

        user.setUpdatedBy(userDetails);
        user.setFirstName(updateUserRequest.getFirstName());
        user.setLastName(updateUserRequest.getLastName());
        user.setEmail(updateUserRequest.getEmail());
        user.setPhoneNumber(updateUserRequest.getPhoneNumber());

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, User.class);
    }

    protected void validateUniqueFields(User user, UpdateUserRequest updateUserRequest) {
        if (!user.getEmail().equals(updateUserRequest.getEmail()) && isEmailUnique(updateUserRequest.getEmail())) {
            throw new EmailAlreadyExistException("Email already exists");
        }

    }
    public void deleteUser(String email) {
        if (doesUserExist(email)) {
            userRepository.deleteUserByEmail(email);
            logger.info("Kullanıcı başarıyla silindi: {}", email);
            throw new ResponseStatusException(HttpStatus.OK, "User deleted");
        } else {
            logger.warn("Kullanıcı bulunamadı: {}", email);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }


    private boolean doesUserExist(String email) {
        return userRepository.findUserByEmail(email).isPresent();

    }
}

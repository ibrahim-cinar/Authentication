package com.cinar.authentication.service;

import com.cinar.authentication.config.PasswordEncoderConfig;
import com.cinar.authentication.dto.UserDto;
import com.cinar.authentication.dto.request.CreateUserRequest;
import com.cinar.authentication.exceptions.EmailAlreadyExistException;
import com.cinar.authentication.exceptions.EmailNotFoundException;
import com.cinar.authentication.exceptions.InvalidInputException;
import com.cinar.authentication.exceptions.UserNotFoundException;
import com.cinar.authentication.model.User;
import com.cinar.authentication.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(user -> modelMapper.map(user, UserDto.class)).collect(Collectors.toList());

    }

    protected Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUserByUsername(String username) {
        return findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User could not find by username " + username));


    }

    protected Optional<User> findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public User getUserByEmail(String email) {
        return findUserByEmail(email).orElseThrow(() -> new EmailNotFoundException("User could not find by email " + email));

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElseThrow(EntityNotFoundException::new);
    }

    public static boolean patternMatches(String emailAddress, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }

    protected boolean isEmailUnique(String email) {
        Optional<User> existingUserEmail = userRepository.findUserByEmail(email);
        return existingUserEmail.isEmpty();
    }

    protected boolean isUsernameUnique(String username) {
        Optional<User> existingUserUsername = userRepository.findByUsername(username);
        return existingUserUsername.isEmpty();
    }


    private static boolean isInputValid(CreateUserRequest request) {
        return request.getUsername() != null &&
                request.getFirstName() != null &&
                request.getLastName() != null &&
                request.getEmail() != null &&
                request.getPassword() != null &&
                request.getRole() != null;
    }

    public User createUser(CreateUserRequest request) {
        return new User(request.getUsername(),
                bCryptPasswordEncoder.encode(request.getPassword()),
                request.getFirstName(), request.getLastName(),
                request.getEmail(),
                request.getPhoneNumber()
                , true, true, true,
                true, request.getRole());
    }

    public User createUserFromRequest(CreateUserRequest request) {
        if (!isInputValid(request)) {
            throw new InvalidInputException("Invalid input");
        }
        if (!isEmailUnique(request.getEmail())) {
            throw new EmailAlreadyExistException("Email already exists");
        }

        if (!patternMatches(request.getEmail(), "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            throw new InvalidInputException("Email is not valid");
        }
        if (!patternMatches(request.getPhoneNumber(), "\\d{10}|(?:\\d{3}-){2}\\d{4}|\\(\\d{3}\\)\\d{3}-?\\d{4}")) {
            throw new InvalidInputException("Phone number is not valid");
        }
        return createUser(request);
    }
    public User createNewUser(CreateUserRequest createUserRequest) {
        User user = createUserFromRequest(createUserRequest);
        return userRepository.save(user);
    }
}

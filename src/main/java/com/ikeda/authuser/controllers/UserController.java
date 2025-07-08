package com.ikeda.authuser.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.ikeda.authuser.configs.security.AuthenticationCurrentUserService;
import com.ikeda.authuser.configs.security.UserDetailsImpl;
import com.ikeda.authuser.dtos.UserRecordDto;
import com.ikeda.authuser.models.UserModel;
import com.ikeda.authuser.services.UserService;
import com.ikeda.authuser.specifications.SpecificationTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RequestMapping("/users")
@RestController
public class UserController {

    Logger logger = LogManager.getLogger(UserController.class);

    final UserService userService;
    final AuthenticationCurrentUserService authenticationCurrentUserService;
    final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, AuthenticationCurrentUserService authenticationCurrentUserService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationCurrentUserService = authenticationCurrentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(SpecificationTemplate.UserSpec spec,
                                                       Pageable pageable,
                                                       Authentication authentication){
        UUID currentUserId = authenticationCurrentUserService.getCurrrentUser().getUserId();
        logger.info(String.format("Authentication userId {%s} - getAllUsers ", currentUserId));

        Page<UserModel> userModelPage = userService.findAll(spec, pageable);
        if (!userModelPage.isEmpty()){
            for (UserModel user: userModelPage.toList()){
                user.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getOneUser(user.getUserId())).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(userModelPage);
    }

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<Object> getOneUser(@PathVariable(value = "userId") UUID userId){
        UserDetailsImpl userDetails = authenticationCurrentUserService.getCurrrentUser();
        logger.info(String.format("Authentication userId {%s} - getOneUser do userId {&s} received", userDetails.getUserId(), userId));

        if (userDetails.getUserId().equals(userId) || userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_COORDINATOR"))){
            return ResponseEntity.status(HttpStatus.OK).body(userService.findById(userId).get());
        } else {
            throw new AccessDeniedException("Forbidden");
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "userId") UUID userId){
        UserDetailsImpl userDetails = authenticationCurrentUserService.getCurrrentUser();
        logger.debug(String.format("Authentication userId {&s} - DELETE deleteUser do userId received {&s} ", userDetails.getUsername(), userId));

        userService.delete(userService.findById(userId).get());
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully.");
    }

    @PreAuthorize("hasAnyRole('USER')")
    @PutMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable(value = "userId") UUID userId,
                                             @RequestBody
                                             @Validated(UserRecordDto.UserView.UserPut.class)
                                             @JsonView(UserRecordDto.UserView.UserPut.class)
                                             UserRecordDto userRecordDto){
        UserDetailsImpl userDetails = authenticationCurrentUserService.getCurrrentUser();
        logger.info(String.format("Authentication userId {%s} - updateUser do userId {&s} received", userDetails.getUserId(), userId));

        if (userDetails.getUserId().equals(userId) || userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))){
            logger.debug("PUT updateUser userRecordDto received {} ", userRecordDto);
            return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(userRecordDto, userService.findById(userId).get()));
        } else {
            throw new AccessDeniedException("Forbidden");
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{userId}/active")
    public ResponseEntity<Object> updateActive(@PathVariable(value = "userId") UUID userId) {
        UserDetailsImpl userDetails = authenticationCurrentUserService.getCurrrentUser();
        logger.info(String.format("Authentication userId {%s} - updateActive do userId {%s}", userDetails.getUserId(), userId));

        userService.updateActive(userService.findById(userId).get());
        return ResponseEntity.status(HttpStatus.OK).body("User has active");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{userId}/blocked")
    public ResponseEntity<Object> updateBlocked(@PathVariable(value = "userId") UUID userId) {
        UserDetailsImpl userDetails = authenticationCurrentUserService.getCurrrentUser();
        logger.info(String.format("Authentication userId {%s} - updateBlocked do userId {%s}", userDetails.getUserId(), userId));

        userService.updateBlocked(userService.findById(userId).get());
        return ResponseEntity.status(HttpStatus.OK).body("User has blocked");
    }

    @PreAuthorize("hasAnyRole('USER')")
    @PutMapping("/{userId}/password")
    public ResponseEntity<Object> updatePassword(@PathVariable(value = "userId") UUID userId,
                                                 @RequestBody
                                                 @Validated(UserRecordDto.UserView.PasswordPut.class)
                                                 @JsonView(UserRecordDto.UserView.PasswordPut.class)
                                                 UserRecordDto userRecordDto){
        UserDetailsImpl userDetails = authenticationCurrentUserService.getCurrrentUser();
        logger.info(String.format("Authentication userId {%s} - updatePassword do userId {%s}", userDetails.getUserId(), userId));

        if (userDetails.getUserId().equals(userId) || userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))){
            Optional<UserModel> userModelOptional = userService.findById(userId);
            if (!passwordEncoder.matches(userRecordDto.oldPassword(), userModelOptional.get().getPassword())){
                logger.warn("Mismatched old password! userId {} ", userId);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Mismatched old password!");
            }
            userService.updatePassword(userRecordDto, userModelOptional.get());
            return ResponseEntity.status(HttpStatus.OK).body("Password update successfully.");
        } else {
            throw new AccessDeniedException("Forbidden");
        }
    }

    @PreAuthorize("hasAnyRole('USER')")
    @PutMapping("/{userId}/image")
    public ResponseEntity<Object> updateImage(@PathVariable(value = "userId") UUID userId,
                                              @RequestBody
                                              @Validated(UserRecordDto.UserView.ImagePut.class)
                                              @JsonView(UserRecordDto.UserView.ImagePut.class)
                                              UserRecordDto userRecordDto){
        UserDetailsImpl userDetails = authenticationCurrentUserService.getCurrrentUser();
        logger.info(String.format("Authentication userId {%s} - updateImage do userId {%s}", userDetails.getUserId(), userId));

        if (userDetails.getUserId().equals(userId) || userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))){
            logger.debug("PUT updateImage userId received {} ", userId);
            return ResponseEntity.status(HttpStatus.OK).body(userService.updateImage(userRecordDto, userService.findById(userId).get()));
        } else {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
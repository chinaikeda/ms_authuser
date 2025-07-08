package com.ikeda.authuser.controllers;

import com.ikeda.authuser.clients.OperationalClient;
import com.ikeda.authuser.configs.security.AuthenticationCurrentUserService;
import com.ikeda.authuser.configs.security.UserDetailsImpl;
import com.ikeda.authuser.dtos.UserRecordDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UserOperationalController {

    Logger logger = LogManager.getLogger(UserOperationalController.class);

    final OperationalClient operationalClient;
    final AuthenticationCurrentUserService authenticationCurrentUserService;

    public UserOperationalController(OperationalClient operationalClient, AuthenticationCurrentUserService authenticationCurrentUserService) {
        this.operationalClient = operationalClient;
        this.authenticationCurrentUserService = authenticationCurrentUserService;
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @GetMapping("/users/operational")
    public ResponseEntity<Page<UserRecordDto>> getOperationalAllUsers(
            @PageableDefault(sort = "userId", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestHeader("Authorization") String token){
        UUID currentUserId = authenticationCurrentUserService.getCurrrentUser().getUserId();
        logger.info(String.format("Authentication userId {%s} - getOperationalAllUsers received", currentUserId));
        return ResponseEntity.status(HttpStatus.OK).body(operationalClient.getOperationalAllUsers(pageable, token));
    }

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/users/{userId}/operational")
    public ResponseEntity<Page<UserRecordDto>> getOperationalOneUser(
            @PageableDefault(sort = "userId", direction = Sort.Direction.ASC) Pageable pageable,
            @PathVariable(value = "userId") UUID userId,
            @RequestHeader("Authorization") String token){
        UUID currentUserId = authenticationCurrentUserService.getCurrrentUser().getUserId();
        logger.info(String.format("Authentication userId {%s} - getOperationalOneUser do userId {%s} received", currentUserId, userId));

        return ResponseEntity.status(HttpStatus.OK).body(operationalClient.getOperationalOneUser(userId, pageable, token));
    }
}

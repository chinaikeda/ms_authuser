package com.ikeda.authuser.controllers;

import com.ikeda.authuser.clients.OperationalClient;
import com.ikeda.authuser.dtos.UserRecordDto;
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

    final OperationalClient operationalClient;

    public UserOperationalController(OperationalClient operationalClient) {
        this.operationalClient = operationalClient;
    }

    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/users/{userId}/operational")
    public ResponseEntity<Page<UserRecordDto>> getOperationalUser(
            @PageableDefault(sort = "userId", direction = Sort.Direction.ASC) Pageable pageable,
            @PathVariable(value = "userId") UUID userId,
            @RequestHeader("Authorization") String token){
        return ResponseEntity.status(HttpStatus.OK).body(operationalClient.getOperationalUser(userId, pageable, token));
    }
}

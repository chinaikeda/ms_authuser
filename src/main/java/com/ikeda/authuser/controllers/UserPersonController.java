package com.ikeda.authuser.controllers;

import com.ikeda.authuser.clients.OperationalClient;
import com.ikeda.authuser.dtos.UserRecordDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UserPersonController {

    final OperationalClient operationalClient;

    public UserPersonController(OperationalClient operationalClient) {
        this.operationalClient = operationalClient;
    }

    @GetMapping("/users/{userId}/operational")
    public ResponseEntity<Page<UserRecordDto>> getPersonUser(
            @PageableDefault(sort = "userId", direction = Sort.Direction.ASC) Pageable pageable,
            @PathVariable(value = "userId") UUID userId){
        return ResponseEntity.status(HttpStatus.OK).body(operationalClient.getOperationalUser(userId, pageable));
    }
}

package com.ikeda.authuser.controllers;

import com.ikeda.authuser.clients.PersonClient;
import com.ikeda.authuser.dtos.PersonRecordDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UserPersonController {

    final PersonClient personClient;

    public UserPersonController(PersonClient personClient) {
        this.personClient = personClient;
    }

    @GetMapping("/users/{userId}/person")
    public ResponseEntity<PersonRecordDto> getPersonUser(@PathVariable(value = "userId") UUID userId){
        return ResponseEntity.status(HttpStatus.OK).body(personClient.getPersonUser(userId));
    }
}

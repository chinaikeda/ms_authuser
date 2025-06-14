package com.ikeda.authuser.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.ikeda.authuser.dtos.UserRecordDto;
import com.ikeda.authuser.services.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    Logger logger = LogManager.getLogger(AuthenticationController.class);
    final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> registerUser(@RequestBody
                                               @Validated(UserRecordDto.UserView.RegistrationPost.class)
                                               @JsonView(UserRecordDto.UserView.RegistrationPost.class)
                                               UserRecordDto userRecordDto){
        logger.debug("POST registerUser userRecordDto received {} ", userRecordDto);
        if (userService.existsByLogin(userRecordDto.login())) {
            logger.warn("Login {} is Already Taken ", userRecordDto.login());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Login is Already Taken!");
        }

        if (userService.existsByEmail(userRecordDto.email())) {
            logger.warn("Email {} is Already Taken ", userRecordDto.email());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Email is Already Taken!");
        }

        // TODO: Exitem dois tipos de cadastros,
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userRecordDto));
    }

    @PostMapping("/signup/admin/user")
    public ResponseEntity<Object> registerUserAdmin(UserRecordDto userRecordDto,
                                                    Errors errors){
        if (errors.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getAllErrors());
        }

        // TODO: Exitem dois tipos de cadastros,
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUserAdmin(userRecordDto));
    }

    @GetMapping("/logs")
    public String index(){
        logger.trace("TRACE");
        logger.debug("DEBUG");
        logger.info("INFO");
        logger.warn("WARN");
        logger.error("ERROR");
        return "Logging Spring Boot...";
    }
}
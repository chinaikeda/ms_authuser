package com.ikeda.authuser.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.ikeda.authuser.configs.security.JwtProvider;
import com.ikeda.authuser.dtos.JwtRecordDto;
import com.ikeda.authuser.dtos.LoginRecordDto;
import com.ikeda.authuser.dtos.UserRecordDto;
import com.ikeda.authuser.services.UserService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    Logger logger = LogManager.getLogger(AuthenticationController.class);
    final UserService userService;
    final JwtProvider jwtProvider;
    final AuthenticationManager authenticationManager;

    public AuthenticationController(UserService userService, JwtProvider jwtProvider, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
    }

//    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping("/signup")
    public ResponseEntity<Object> registerUser(@RequestBody
                                               @Validated(UserRecordDto.UserView.RegistrationPost.class)
                                               @JsonView(UserRecordDto.UserView.RegistrationPost.class)
                                               UserRecordDto userRecordDto){
        logger.info(String.format("Authentication userId {%s} - registerUser username received {%s} ", "Sem authenticação", userRecordDto.username()));
        if (userService.existsByUsername(userRecordDto.username())) {
            logger.warn("Username {} is Already Taken ", userRecordDto.username());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Username is Already Taken!");
        }

        if (userService.existsByEmail(userRecordDto.email())) {
            logger.warn("Email {} is Already Taken ", userRecordDto.email());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Email is Already Taken!");
        }

        // TODO - AI : Exitem dois tipos de cadastros,
        //  o primeiro Users que segue para Person, enviando a notificação com userId para finalizar o cadastro em Person e trocar o status para active
        //  o segundo Person que decide ter um Users (será implementado)
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userRecordDto));
    }

//    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/signup/admin/user")
    public ResponseEntity<Object> registerUserAdmin(UserRecordDto userRecordDto,
                                                    Errors errors){
        logger.info(String.format("Authentication userId {%s} - registerUserAdmin username received {%s} ", "Sem authenticação", userRecordDto.username()));
        if (errors.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getAllErrors());
        }

        // TODO - AI : Exitem dois tipos de cadastros,
        //  o primeiro Users que segue para Person, enviando a notificação com userId para finalizar o cadastro em Person e trocar o status para active
        //  o segundo Person que decide ter um Users (será implementado)
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUserAdmin(userRecordDto));
    }

//    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping("/login")
    public ResponseEntity<JwtRecordDto> authenticateUser(@RequestBody @Valid LoginRecordDto loginRecordDto){
        logger.info(String.format("Authentication username received {%s} - authenticateUser ", loginRecordDto.username()));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRecordDto.username(), loginRecordDto.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(new JwtRecordDto(jwtProvider.generateJwt(authentication)));
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
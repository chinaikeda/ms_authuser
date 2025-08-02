package com.ikeda.authuser.services.impl;

import com.ikeda.authuser.dtos.NotificationRecordCommandDto;
import com.ikeda.authuser.dtos.UserRecordDto;
import com.ikeda.authuser.enums.ActionType;
import com.ikeda.authuser.enums.RoleType;
import com.ikeda.authuser.enums.UserStatus;
import com.ikeda.authuser.enums.UserType;
import com.ikeda.authuser.exceptions.NotFoundException;
import com.ikeda.authuser.models.UserModel;
import com.ikeda.authuser.publishers.NotificationCommandPublisher;
import com.ikeda.authuser.publishers.UserEventPublisher;
import com.ikeda.authuser.repositories.UserRepository;
import com.ikeda.authuser.services.RoleService;
import com.ikeda.authuser.services.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    Logger logger = LogManager.getLogger(UserServiceImpl.class);

    final UserRepository userRepository;
    final UserEventPublisher userEventPublisher;
    final RoleService roleService;
    final PasswordEncoder passwordEncoder;
    final NotificationCommandPublisher notificationCommandPublisher;

    public UserServiceImpl(UserRepository userRepository, UserEventPublisher userEventPublisher, RoleService roleService, PasswordEncoder passwordEncoder, NotificationCommandPublisher notificationCommandPublisher) {
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.notificationCommandPublisher = notificationCommandPublisher;
    }

    @Override
    public List<UserModel> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<UserModel> findById(UUID userId) {
        Optional<UserModel> userModelOptional = userRepository.findById(userId);
        if (userModelOptional.isEmpty()){
            throw new NotFoundException("Error: User not found.");
        }
        return userModelOptional;
    }

    @Transactional
    @Override
    public void delete(UserModel userModel) {
        userRepository.delete(userModel);

        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.DELETE));

        try {
            var notificationRecordCommandDto = new NotificationRecordCommandDto("Delete - Exclusão", userModel.getName() + " seu registro foi excluído com sucesso!", userModel.getUserId());
            notificationCommandPublisher.publishNotificationCommand(notificationRecordCommandDto);
        } catch (Exception e){
            logger.error("Error sending notification message with cause: {} ", e.getMessage());
        }
    }

    @Transactional
    @Override
    public UserModel registerUser(UserRecordDto userRecordDto) {
        var userModel = new UserModel();
        BeanUtils.copyProperties(userRecordDto, userModel);
        userModel.setUserStatus(UserStatus.PENDING);
        userModel.setUserType(UserType.USER);
        userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userModel.getRoles().add(roleService.findByRoleName(RoleType.ROLE_USER));
        userRepository.save(userModel);

//      TODO - AI - enviar uma notificação com o link para inserção de user com userId para conclusão do cadastro e consequentemente a alteração do status acima para active
        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.CREATE));

        try {
            var notificationRecordCommandDto = new NotificationRecordCommandDto("Register User - Inclusão", userModel.getName() + " seu registro foi criado com sucesso!", userModel.getUserId());
            notificationCommandPublisher.publishNotificationCommand(notificationRecordCommandDto);
        } catch (Exception e){
            logger.error("Error sending notification message with cause: {} ", e.getMessage());
        }

        return userModel;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    @Override
    public UserModel updateUser(UserRecordDto userRecordDto, UserModel userModel) {
        userModel.setEmail(userRecordDto.email());
        userModel.setName(userRecordDto.name());
        userModel.setPhoneNumber(userRecordDto.phoneNumber());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userRepository.save(userModel);

        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.UPDATE));

        try {
            var notificationRecordCommandDto = new NotificationRecordCommandDto("Update - Atualização", userModel.getName() + " seu registro foi atualizado com sucesso!", userModel.getUserId());
            notificationCommandPublisher.publishNotificationCommand(notificationRecordCommandDto);
        } catch (Exception e){
            logger.error("Error sending notification message with cause: {} ", e.getMessage());
        }

        return userModel;
    }

    @Transactional
    @Override
    public UserModel updateActive(UserModel userModel) {
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(userModel);

        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.UPDATE));

        try {
            var notificationRecordCommandDto = new NotificationRecordCommandDto("Update Active - Atualização", userModel.getName() + " seu registro está ativo!", userModel.getUserId());
            notificationCommandPublisher.publishNotificationCommand(notificationRecordCommandDto);
        } catch (Exception e){
            logger.error("Error sending notification message with cause: {} ", e.getMessage());
        }

        return userModel;
    }

    @Transactional
    @Override
    public UserModel updateBlocked(UserModel userModel) {
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(userModel);

        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.UPDATE));

        try {
            var notificationRecordCommandDto = new NotificationRecordCommandDto("Update Blocked - Atualização", userModel.getName() + " seu registro está bloqueado!", userModel.getUserId());
            notificationCommandPublisher.publishNotificationCommand(notificationRecordCommandDto);
        } catch (Exception e){
            logger.error("Error sending notification message with cause: {} ", e.getMessage());
        }

        return userModel;
    }

    @Override
    public UserModel updatePassword(UserRecordDto userRecordDto, UserModel userModel) {
        userModel.setPassword(passwordEncoder.encode(userRecordDto.password()));
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userRepository.save(userModel);

        try {
            var notificationRecordCommandDto = new NotificationRecordCommandDto("Update Password - Atualização", userModel.getName() + " seu password foi atualizado com sucesso!", userModel.getUserId());
            notificationCommandPublisher.publishNotificationCommand(notificationRecordCommandDto);
        } catch (Exception e){
            logger.error("Error sending notification message with cause: {} ", e.getMessage());
        }

        return userModel;
    }

    @Transactional
    @Override
    public UserModel updateImage(UserRecordDto userRecordDto, UserModel userModel) {
        userModel.setImageUrl(userRecordDto.imageUrl());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userRepository.save(userModel);

        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.UPDATE));

        try {
            var notificationRecordCommandDto = new NotificationRecordCommandDto("Update image - Atualização", userModel.getName() + " sua imagem foi atualizada com sucesso!", userModel.getUserId());
            notificationCommandPublisher.publishNotificationCommand(notificationRecordCommandDto);
        } catch (Exception e){
            logger.error("Error sending notification message with cause: {} ", e.getMessage());
        }

        return userModel;
    }

    @Transactional
    @Override
    public UserModel registerUserAdmin(UserRecordDto userRecordDto) {
        var userModel = new UserModel();
        BeanUtils.copyProperties(userRecordDto, userModel);
        userModel.setUserStatus(UserStatus.PENDING);
        userModel.setUserType(UserType.ADMIN);
        userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userModel.getRoles().add(roleService.findByRoleName(RoleType.ROLE_ADMIN));
        userRepository.save(userModel);

//      TODO - AI - enviar uma notificação com o link para inserção de user com userId para conclusão do cadastro e consequentemente a alteração do status acima para active
        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.CREATE));

        try {
            var notificationRecordCommandDto = new NotificationRecordCommandDto("Register Adm - Inclusão", userModel.getName() + " seu registro de adm foi criado com sucesso!", userModel.getUserId());
            notificationCommandPublisher.publishNotificationCommand(notificationRecordCommandDto);
        } catch (Exception e){
            logger.error("Error sending notification message with cause: {} ", e.getMessage());
        }

        return userModel;
    }

    @Override
    public Page<UserModel> findAll(Specification<UserModel> spec, Pageable pageable) {
        return userRepository.findAll(spec, pageable);
    }

    @Transactional
    @Override
    public UserModel updateUserByPaymentEvents(UserModel userModel) {
        userRepository.save(userModel);

        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.UPDATE));

        return userModel;
    }
}
package com.ikeda.authuser.services.impl;

import com.ikeda.authuser.dtos.UserRecordDto;
import com.ikeda.authuser.enums.ActionType;
import com.ikeda.authuser.enums.UserStatus;
import com.ikeda.authuser.enums.UserType;
import com.ikeda.authuser.exceptions.NotFoundException;
import com.ikeda.authuser.models.UserModel;
import com.ikeda.authuser.publishers.UserEventPublisher;
import com.ikeda.authuser.repositories.UserRepository;
import com.ikeda.authuser.services.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;
    final UserEventPublisher userEventPublisher;

    public UserServiceImpl(UserRepository userRepository, UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
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
        userRepository.save(userModel);

//        TODO - AI - enviar uma notificação com o link para inserção de person com userId para conclusão do cadastro e consequentemente a alteração do status acima para active
        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.CREATE));

        return userModel;
    }

    @Override
    public boolean existsByLogin(String login) {
        return userRepository.existsByLogin(login);
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

        return userModel;
    }

    @Transactional
    @Override
    public UserModel updateActive(UserModel userModel) {
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(userModel);

        return userModel;
    }

    @Transactional
    @Override
    public UserModel updateBlocked(UserModel userModel) {
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(userModel);

        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.UPDATE));

        return userModel;
    }

    @Override
    public UserModel updatePassword(UserRecordDto userRecordDto, UserModel userModel) {
        userModel.setPassword(userRecordDto.password());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userRepository.save(userModel);

        return userModel;
    }

    @Transactional
    @Override
    public UserModel updateImage(UserRecordDto userRecordDto, UserModel userModel) {
        userModel.setImageUrl(userRecordDto.imageUrl());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userRepository.save(userModel);

        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.UPDATE));

        return userModel;
    }

    @Transactional
    @Override
    public UserModel registerUserAdmin(UserRecordDto userRecordDto) {
        var userModel = new UserModel();

        BeanUtils.copyProperties(userRecordDto, userModel);

        userModel.setUserType(UserType.ADMIN);
        userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setPassword(userModel.getPassword());
        userRepository.save(userModel);

//        TODO - AI - enviar uma notificação com o link para inserção de person com userId para conclusão do cadastro e consequentemente a alteração do status acima para active
        userEventPublisher.publishUserEvent(userModel.convertToUserEventDto(ActionType.CREATE));

        return userModel;
    }

    @Override
    public Page<UserModel> findAll(Specification<UserModel> spec, Pageable pageable) {
        return userRepository.findAll(spec, pageable);
    }
}

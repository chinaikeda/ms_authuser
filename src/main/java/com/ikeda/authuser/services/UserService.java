package com.ikeda.authuser.services;

import com.ikeda.authuser.dtos.UserRecordDto;
import com.ikeda.authuser.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    List<UserModel> findAll();

    Optional<UserModel> findById(UUID userId);

    void delete(UserModel userModel);

    UserModel registerUser(UserRecordDto userRecordDto);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    UserModel updateUser(UserRecordDto userRecordDto, UserModel userModel);

    UserModel updateActive(UserModel userModel);

    UserModel updateBlocked(UserModel userModel);

    UserModel updatePassword(UserRecordDto userRecordDto, UserModel userModel);

    UserModel updateImage(UserRecordDto userRecordDto, UserModel userModel);

    UserModel registerUserAdmin(UserRecordDto userRecordDto);

    Page<UserModel> findAll(Specification<UserModel> spec, Pageable pageable);

    UserModel updateUserByPaymentEvents(UserModel userModel);
}

package com.ikeda.authuser.services.impl;

import com.ikeda.authuser.exceptions.NotFoundException;
import com.ikeda.authuser.models.UserModel;
import com.ikeda.authuser.repositories.UserRepository;
import com.ikeda.authuser.services.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    @Override
    public void delete(UserModel userModel) {
        userRepository.delete(userModel);
    }
}

package com.ikeda.authuser.dtos;

import com.fasterxml.jackson.annotation.JsonView;
import com.ikeda.authuser.validations.PasswordConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRecordDto(@NotBlank(groups = UserView.RegistrationPost.class, message = "Login is mandatory")
                            @Size(min = 10, max = 30, groups = UserView.RegistrationPost.class, message = "Size between 10 and 30")
                            @JsonView(UserView.RegistrationPost.class)
                            String login,

                            @NotBlank(groups = UserView.RegistrationPost.class, message = "Email is mandatory")
                            @Email(groups = UserView.RegistrationPost.class, message = "Email must be in the expected format")
                            @NotBlank
                            @JsonView(UserView.RegistrationPost.class)
                            String email,

                            @NotBlank(groups = {UserView.RegistrationPost.class, UserView.PasswordPut.class}, message = "Password is mandatory")
                            @Size(min = 6, max = 20, groups = {UserView.RegistrationPost.class, UserView.PasswordPut.class}, message = "Size must be between 6 and 20")
                            @JsonView({UserView.RegistrationPost.class, UserView.PasswordPut.class})
                            @PasswordConstraint(groups = {UserView.RegistrationPost.class, UserView.PasswordPut.class})
                            String password,

                            @NotBlank(groups = UserView.PasswordPut.class, message = "Old Password is mandatory")
                            @Size(min = 6, max = 20, groups = UserView.PasswordPut.class, message = "Size must be between 6 and 20")
                            @JsonView({UserView.PasswordPut.class})
                            @PasswordConstraint(groups = UserView.PasswordPut.class)
                            String oldPassword,

                            @NotBlank(groups = {UserView.RegistrationPost.class, UserView.UserPut.class}, message = "Name is mandatory")
                            @JsonView({UserView.RegistrationPost.class, UserView.UserPut.class})
                            String name,

                            @JsonView({UserView.RegistrationPost.class, UserView.UserPut.class})
                            String phoneNumber,

                            @NotBlank(groups = UserView.ImagePut.class, message = "Image URL is mandatory")
                            @JsonView({UserView.ImagePut.class})
                            String imageUrl) {

    public interface UserView{
        interface RegistrationPost{}
        interface UserPut{}
        interface PasswordPut{}
        interface ImagePut{}
    }
}

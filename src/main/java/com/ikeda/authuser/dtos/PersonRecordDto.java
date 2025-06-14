package com.ikeda.authuser.dtos;

import com.ikeda.authuser.enums.PersonSex;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record PersonRecordDto(@NotBlank(message = "Full name is mandatory")
                              String fullName,

                              @NotBlank(message = "Sex is mandatory")
                              PersonSex personSex,

                              LocalDateTime dateOfBirth) {
}

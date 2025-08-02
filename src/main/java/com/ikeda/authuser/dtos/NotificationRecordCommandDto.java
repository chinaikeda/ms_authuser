package com.ikeda.authuser.dtos;

import java.util.UUID;

public record NotificationRecordCommandDto(String title,
                                           String message,
                                           UUID userId) {
}

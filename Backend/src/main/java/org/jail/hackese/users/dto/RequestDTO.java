package org.jail.hackese.users.dto;

import java.util.Date;

public record RequestDTO(
        String name,
        String email,
        String password,
        Date dob
) {
}

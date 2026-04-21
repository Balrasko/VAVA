package dev.vavateam1.dto;

import dev.vavateam1.model.User;
import dev.vavateam1.model.UserSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWithSessionDto {
    private User user;
    private UserSession session;
}

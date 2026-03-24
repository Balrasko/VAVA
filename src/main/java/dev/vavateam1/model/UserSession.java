package dev.vavateam1.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    private int id;
    private int userId;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package dev.vavateam1.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private int id;
    private int roleId;
    private String name;
    private String email;
    private String password;
    private boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
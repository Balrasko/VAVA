package dev.vavateam1.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private int id;
    private String name;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
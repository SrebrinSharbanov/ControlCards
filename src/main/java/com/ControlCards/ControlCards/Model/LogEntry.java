package com.ControlCards.ControlCards.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_entries")
@Getter
@Setter
@ToString
public class LogEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    public LogEntry() {
        this.createdAt = LocalDateTime.now();
    }

    public LogEntry(User user, String description) {
        this();
        this.user = user;
        this.description = description;
    }
}

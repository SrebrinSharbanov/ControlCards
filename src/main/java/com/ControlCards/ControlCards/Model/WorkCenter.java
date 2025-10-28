package com.ControlCards.ControlCards.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "work_centers")
@Getter
@Setter
@ToString
public class WorkCenter extends BaseEntity {

    @Column(name = "number", nullable = false, length = 5)
    private String number;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "machine_type", length = 100)
    private String machineType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id", nullable = false)
    private Workshop workshop;

    // Constructors
    public WorkCenter() {
    }
}

package com.ControlCards.ControlCards.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workshops")
@Getter
@Setter
@ToString
public class Workshop extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "workshop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkCenter> workCenters;

    public Workshop() {
        this.workCenters = new ArrayList<>();
        this.active = true;
    }
}

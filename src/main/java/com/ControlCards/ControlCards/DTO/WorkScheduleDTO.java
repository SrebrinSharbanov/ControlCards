package com.ControlCards.ControlCards.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
public class WorkScheduleDTO {
    
    private UUID id;
    private LocalDate date;
    private Integer shift;
    private String workCenter; // Work center number (for API)
    private UUID workCenterId; // Work center ID (for form selection)
    private String salesOrder;
    private Integer item;
    private String productionOrder;
    private String product;
    private Integer quantity;
    private Integer timeInMinutes;
}


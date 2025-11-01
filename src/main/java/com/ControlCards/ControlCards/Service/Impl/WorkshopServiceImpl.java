package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Repository.WorkshopRepository;
import com.ControlCards.ControlCards.Service.WorkshopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class WorkshopServiceImpl implements WorkshopService {

    private final WorkshopRepository workshopRepository;

    @Autowired
    public WorkshopServiceImpl(WorkshopRepository workshopRepository) {
        this.workshopRepository = workshopRepository;
    }

    @Override
    public List<Workshop> findAll() {
        log.debug("Finding all workshops");
        return workshopRepository.findAll();
    }

    @Override
    public Optional<Workshop> findById(UUID id) {
        return workshopRepository.findById(id);
    }

    @Override
    public Workshop save(Workshop workshop) {
        log.info("Saving workshop: {}", workshop.getName());
        return workshopRepository.save(workshop);
    }

    @Override
    public void deleteById(UUID id) {
        log.info("Deleting workshop with ID: {}", id);
        workshopRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return workshopRepository.existsById(id);
    }
}


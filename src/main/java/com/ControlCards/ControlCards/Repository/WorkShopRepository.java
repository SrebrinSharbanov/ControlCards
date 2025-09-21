package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.WorkCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkShopRepository extends JpaRepository<WorkCenter, Integer> {
}

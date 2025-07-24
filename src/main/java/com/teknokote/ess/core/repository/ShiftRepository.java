package com.teknokote.ess.core.repository;

import com.teknokote.ess.core.model.shifts.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findAllByOffDay(boolean offDay);

    List<Shift> deleteAllByOffDayFalse();
}

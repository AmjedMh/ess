package com.teknokote.ess.core.service.shifts;

import com.teknokote.core.service.BaseService;
import com.teknokote.ess.dto.PeriodDto;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRotationService extends BaseService<Long, ShiftRotationDto> {
    List<ShiftRotationDto> findAllByStation(Long stationId);

    ShiftRotationDto findValidRotation(Long stationId, LocalDate month);
    ShiftRotationDto addShiftRotation(Long stationId, ShiftRotationDto shiftRotationDto, HttpServletRequest request);
    List<PeriodDto> listPeriodTypesForStation(Long stationId);

    /**
     * Avant de procéder à la suppression de la rotation, le système doit vérifier s'il y a eu des exécutions pendant
     * la période définie de cette rotation.
     * Si aucune exécution n'a eu lieu pendant la période de la rotation, la suppression peut être effectuée avec succès.
     * Sinon, la suppression ne doit pas être autorisée et l'utilisateur doit être informé qu'il y a eu des exécutions pendant cette période.
     * @param stationId
     * @param shiftRotationId
     */
    void deleteShiftRotation(Long stationId,Long shiftRotationId,HttpServletRequest request);
}

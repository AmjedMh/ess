package com.teknokote.ess.core.service.impl.shifts;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.core.dao.PumpAttendantDao;
import com.teknokote.ess.core.dao.impl.organization.PumpAttendantTeamDaoImpl;
import com.teknokote.ess.core.dao.organization.PumpAttendantTeamDao;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.core.service.GenericCheckedService;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.model.shifts.AffectedPumpAttendant;
import com.teknokote.ess.core.service.impl.StationService;
import com.teknokote.ess.core.service.organization.PumpAttendantTeamService;
import com.teknokote.ess.core.service.shifts.ShiftRotationService;
import com.teknokote.ess.dto.PumpAttendantDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.organization.DynamicPumpAttendantTeamDto;
import com.teknokote.ess.dto.organization.PumpAttendantTeamDto;
import com.teknokote.ess.dto.shifts.AffectedPumpAttendantDto;
import com.teknokote.ess.dto.shifts.ShiftRotationDto;
import com.teknokote.ess.http.logger.EntityActionEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
@Getter
public class PumpAttendantTeamServiceImpl extends GenericCheckedService<Long, PumpAttendantTeamDto> implements PumpAttendantTeamService {
    @Autowired
    private ESSValidator<PumpAttendantTeamDto> validator;
    @Autowired
    private PumpAttendantTeamDao dao;
    @Autowired
    private StationService stationService;
    @Autowired
    private ShiftRotationService shiftRotationService;
    @Autowired
    private PumpAttendantDao pumpAttendantDao;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    public List<PumpAttendantTeamDto> findByStation(Long stationId) {
        return getDao().findAllByStation(stationId);
    }

    public List<PumpAttendantTeamDto> findByStationAndRotation(Long stationId, Long shiftRotationId) {
        return getDao().findByStationAndRotation(stationId, shiftRotationId);
    }

    @Transactional
    public PumpAttendantTeamDto addTeam(Long stationId, PumpAttendantTeamDto pumpAttendantTeamDto, HttpServletRequest request) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        StationDto stationDto = stationService.checkedFindById(stationId);
        pumpAttendantTeamDto.setStationId(stationId);
        for (AffectedPumpAttendantDto affectedPumpAttendant : pumpAttendantTeamDto.getAffectedPumpAttendant()) {
            final List<PumpAttendantTeamDto> teamsByRotationAndPumpAttendant = this.findTeamsByRotationAndPumpAttendant(pumpAttendantTeamDto.getShiftRotationId(), affectedPumpAttendant.getPumpAttendantId());
            if (isNotEmpty(teamsByRotationAndPumpAttendant)) {
                throw new ServiceValidationException("PumpAttendant: "+affectedPumpAttendant.getPumpAttendant().getFirstName()+" is already affected to another team(s) for this rotation: Team:[" + teamsByRotationAndPumpAttendant.get(0).getName() + "]");
            }
        }
        PumpAttendantTeamDto pumpAttendantTeam= create(pumpAttendantTeamDto);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Ajout équipe de pomistes "+"'"+pumpAttendantTeam.getName()+"'"+ " pour la station " +"'"+ stationDto.getName()+"'" +" pour le  compte client "+"'"+stationDto.getCustomerAccountName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return  pumpAttendantTeam;
    }

    @Transactional
    public PumpAttendantTeamDto updateTeam(Long stationId, PumpAttendantTeamDto pumpAttendantTeamDto,HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StationDto stationDto = stationService.checkedFindById(stationId);
        Optional<PumpAttendantTeamDto> pumpAttendantTeam = findById(pumpAttendantTeamDto.getId());
        if (!pumpAttendantTeam.isPresent()) {
            throw new ServiceValidationException("PumpAttendantTeam not found");
        }
        pumpAttendantTeamDto.setStationId(stationId);
        for (AffectedPumpAttendantDto affectedPumpAttendant : pumpAttendantTeamDto.getAffectedPumpAttendant()) {
            final List<PumpAttendantTeamDto> teamsByRotationAndPumpAttendant = this.findTeamsByRotationAndPumpAttendant(
                            pumpAttendantTeamDto.getShiftRotationId(), affectedPumpAttendant.getPumpAttendantId())
                    .stream()
                    .filter(teamDto -> !teamDto.getId().equals(pumpAttendantTeamDto.getId()))
                    .toList();

            if (isNotEmpty(teamsByRotationAndPumpAttendant)) {
                PumpAttendantDto pumpAttendantAffected = pumpAttendantDao.findById(affectedPumpAttendant.getPumpAttendantId())
                        .orElseThrow(() -> new ServiceValidationException("PumpAttendant with name " + affectedPumpAttendant.getPumpAttendant().getFirstName() + " not found"));

                throw new ServiceValidationException("PumpAttendant: " + pumpAttendantAffected.getFirstName() +
                        " is already affected to another team(s) for this rotation: Team:[" + teamsByRotationAndPumpAttendant.get(0).getName() + "]");
            }
        }

        PumpAttendantTeamDto pumpAttendantsTeam= update(pumpAttendantTeamDto);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Modification équipe de pompistes "+"'"+pumpAttendantsTeam.getName()+"'"+ " pour la station " +"'"+ stationDto.getName()+"'" +" pour le  compte client "+"'"+stationDto.getCustomerAccountName()+"'",
                (User)authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return pumpAttendantsTeam;
    }

    public List<DynamicPumpAttendantTeamDto> getDynamicPumpAttendantTeam(Long stationId) {
        List<PumpAttendantTeamDto> pumpAttendantTeamDtos = this.findByStation(stationId);
        return pumpAttendantTeamDtos.stream()
                .map(this::mapToDynamicTableDto)
                .toList();
    }

    public List<DynamicPumpAttendantTeamDto> getDynamicPumpAttendantTeams(Long stationId, Long shiftRotationId) {
        List<PumpAttendantTeamDto> pumpAttendantTeamDtos = this.findByStationAndRotation(stationId, shiftRotationId);
        return pumpAttendantTeamDtos.stream()
                .map(this::mapToDynamicTableDto)
                .toList();
    }

    public DynamicPumpAttendantTeamDto mapToDynamicTableDto(PumpAttendantTeamDto teamDto) {
        DynamicPumpAttendantTeamDto dto = new DynamicPumpAttendantTeamDto();
        dto.setId(teamDto.getId());
        dto.setTeamName(teamDto.getName());
        dto.setShiftRotationId(teamDto.getShiftRotationId());
        Map<Long, PumpAttendantDto> pumpAttendantToPump = teamDto.getAffectedPumpAttendant().stream()
                .collect(Collectors.toMap(
                        AffectedPumpAttendantDto::getPumpId,
                        AffectedPumpAttendantDto::getPumpAttendant
                ));

        dto.setPumpAttendantToPump(pumpAttendantToPump);
        return dto;
    }

    @Override
    public List<PumpAttendantTeamDto> findTeamsByRotationAndPumpAttendant(Long shiftRotationId, Long pumpAttendantId) {
        return getDao().findTeamsByRotationAndPumpAttendant(shiftRotationId, pumpAttendantId);
    }

    @Override
    public List<PumpAttendantTeamDto> findByPumpAttendant(Long pumpAttendantId) {
        return getDao().findByPumpAttendant(pumpAttendantId);
    }

    @Override
    public List<PumpAttendantTeamDto> findByRotation(Long stationId, Long shiftRotationId) {
        ShiftRotationDto currentRotation = shiftRotationService.findValidRotation(stationId, LocalDate.now());
        Long actualShiftRotationId = (shiftRotationId != null) ? shiftRotationId : currentRotation.getId();
        return getDao().findByStationAndRotation(stationId, actualShiftRotationId);
    }
}

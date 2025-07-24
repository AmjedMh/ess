package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.EntityNotFoundException;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.core.service.ActivatableGenericCheckedService;
import com.teknokote.core.service.ESSValidationResult;
import com.teknokote.core.service.ESSValidator;
import com.teknokote.ess.core.dao.PumpAttendantDao;
import com.teknokote.ess.core.model.organization.User;
import com.teknokote.ess.core.service.PumpAttendantService;
import com.teknokote.ess.core.service.impl.validators.StationValidator;
import com.teknokote.ess.core.service.organization.PumpAttendantTeamService;
import com.teknokote.ess.dto.PumpAttendantDto;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.http.logger.EntityActionEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Getter
public class PumpAttendantServiceImpl extends ActivatableGenericCheckedService<Long, PumpAttendantDto> implements PumpAttendantService {
    @Autowired
    private ESSValidator<PumpAttendantDto> validator;
    @Autowired
    private PumpAttendantDao dao;
    @Autowired
    private PumpAttendantTeamService pumpAttendantTeamService;
    @Autowired
    private StationValidator stationValidator;
    @Autowired
    private StationService stationService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Value("${photo.storage.path}")
    private String photoStoragePath;
    private static final String FOR_STATION = " pour la  station ";
    private static final String FOR_CUSTOMER_ACCOUNT = " pour le  compte client ";

    public List<PumpAttendantDto> findByStation(Long stationId, Boolean actif, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        return getDao().findAllByStation(stationId, pageRequest).stream()
                .filter(pumpAttendantDto -> actif.equals(pumpAttendantDto.getActif()))
                .collect(Collectors.toList());
    }

    public Page<PumpAttendantDto> findAllByStation(Long stationId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        return getDao().findAllByStation(stationId, pageRequest);
    }

    @Transactional
    public PumpAttendantDto addPumpAttendant(Long stationId, PumpAttendantDto pumpAttendantDto, MultipartFile photo, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StationDto stationDto = stationService.checkedFindById(stationId);
        final ESSValidationResult validationResult = stationValidator.validateOnAddPumpAttendant(pumpAttendantDto);

        if (validationResult.hasErrors()) {
            throw new ServiceValidationException(validationResult.getMessage());
        }
        pumpAttendantDto.setStationId(stationId);
        if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(pumpAttendantDto, photo);
            pumpAttendantDto.setPhoto(photoPath);
        }
        PumpAttendantDto pumpAttendant = create(pumpAttendantDto);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Ajout pompiste " + "'" + pumpAttendant.getFirstName() + "'" + FOR_STATION + "'" + stationDto.getName() + "'" + FOR_CUSTOMER_ACCOUNT + "'" + stationDto.getCustomerAccountName() + "'",
                (User) authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return pumpAttendant;
    }

    @Transactional
    public PumpAttendantDto updatePumpAttendant(Long stationId, PumpAttendantDto pumpAttendantDto, MultipartFile photo, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StationDto stationDto = stationService.checkedFindById(stationId);
        PumpAttendantDto existingPumpAttendant = checkedFindById(pumpAttendantDto.getId());
        final ESSValidationResult validationResult = stationValidator.validateOnAddPumpAttendant(pumpAttendantDto);
        if (validationResult.hasErrors()) {
            throw new ServiceValidationException(validationResult.getMessage());
        }
        // Update photo if a new photo is provided
        if (photo != null && !photo.isEmpty()) {
            String photoPath = savePhoto(existingPumpAttendant, photo);
            pumpAttendantDto.setPhoto(photoPath);
        }
        PumpAttendantDto pumpAttendant = update(pumpAttendantDto);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Modification pompiste " + "'" + pumpAttendant.getFirstName() + "'" + FOR_STATION + "'" + stationDto.getName() + "'" + " for compte client " + "'" + stationDto.getCustomerAccountName() + "'",
                (User) authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return pumpAttendant;
    }


    private String savePhoto(PumpAttendantDto dto, MultipartFile photo) {
        String fileName = dto.getFirstName() + dto.getMatricule() + ".jpg";

        try {
            Files.copy(photo.getInputStream(), Paths.get(photoStoragePath + File.separator + fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ServiceValidationException("Could not save photo");
        }

        return photoStoragePath + File.separator + fileName;
    }

    public String serveImageBase64(String imageName) throws IOException {
        Path imagePath = Paths.get(photoStoragePath, imageName);

        byte[] imageBytes = Files.readAllBytes(imagePath);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    @Override
    public PumpAttendantDto findByStationAndTag(Long stationId, String tag) {
        return getDao().findByStationAndTag(stationId, tag).orElseThrow(() -> new EntityNotFoundException("pump attendant with tag: " + tag + " not found"));
    }

    @Override
    public Optional<PumpAttendantDto> findPumpAttandantByStationAndTag(Long stationId, String tag) {
        return getDao().findPumpAttandantByStationAndTag(stationId, tag);
    }

    @Override
    public PumpAttendantDto findByStationAndMatricule(Long stationId, String matricule) {
        return getDao().findByStationAndMatricule(stationId, matricule).orElseThrow(() -> new EntityNotFoundException("pump attendant with matricule: " + matricule + " not found"));
    }

    @Override
    public PumpAttendantDto findByIdAndStationId(Long id, Long stationId) {
        final PumpAttendantDto pumpAttendant = checkedFindById(id);
        if (!stationId.equals(pumpAttendant.getStationId())) {
            throw new ServiceValidationException("No valid pump attendant on the specified station.");
        }
        return pumpAttendant;
    }

    @Override
    public PumpAttendantDto deactivatePumpAttendant(Long stationId, Long id, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StationDto stationDto = stationService.checkedFindById(stationId);
        if (CollectionUtils.isNotEmpty(pumpAttendantTeamService.findByPumpAttendant(id))) {
            throw new ServiceValidationException("Cannot deactivate pump attendant because he is affected to a team.");
        }
        PumpAttendantDto pumpAttendant = getDao().deactivate(id);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Désactivation  pompiste " + "'" + pumpAttendant.getFirstName() + "'" + FOR_STATION + "'" + stationDto.getName() + "'" + FOR_CUSTOMER_ACCOUNT + "'" + stationDto.getCustomerAccountName() + "'",
                (User) authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return pumpAttendant;
    }

    @Override
    public PumpAttendantDto activatePumpAttendant(Long stationId, Long id, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StationDto stationDto = stationService.checkedFindById(stationId);
        if (CollectionUtils.isNotEmpty(pumpAttendantTeamService.findByPumpAttendant(id))) {
            throw new ServiceValidationException("Cannot deactivate pump attendant because he is affected to a team.");
        }
        PumpAttendantDto pumpAttendant = getDao().activate(id);
        EntityActionEvent event = new EntityActionEvent(
                this,
                "Activation pompiste " + "'" + pumpAttendant.getFirstName() + "'" + " pour la station " + "'" + stationDto.getName() + "'" + FOR_CUSTOMER_ACCOUNT + "'" + stationDto.getCustomerAccountName() + "'",
                (User) authentication.getPrincipal(),
                request
        );
        eventPublisher.publishEvent(event);
        return pumpAttendant;
    }
}

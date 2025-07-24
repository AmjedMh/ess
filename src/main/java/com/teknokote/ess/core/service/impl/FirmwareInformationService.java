package com.teknokote.ess.core.service.impl;

import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.core.model.FirmwareInformation;
import com.teknokote.ess.core.repository.FirmwareInformationRepository;
import com.teknokote.ess.dto.FirmwareInformationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FirmwareInformationService {
    @Autowired
    private FirmwareInformationRepository firmwareInformationRepository;
    @Autowired
    private ControllerService controllerService;
    /**
     * Met à jour les informations de version d'un contrôleur (ptsId)
     * Dans le cas où FirmwareInformation existe avec la version {@param firmwareVersion}:
     *  - si FirmwareInformation est la dernière en date, rien faire
     *  - sinon, mettre à jour la date (modificationDate) avec la date actuelle
     * Dans le cas où FirmwareInformation n'existe pas avec la version {@param firmwareVersion}, l'ajouter.
     */
    public void updateFirmwareInformation(String ptsId, String firmwareVersion, boolean isFirmwareVersionSupported) {

        final List<FirmwareInformation> versionsForPtsId = firmwareInformationRepository.findAllByPtsId(ptsId);
        final List<FirmwareInformation> communicatedVersion = versionsForPtsId.stream().filter(el -> el.getDateTime().equals(firmwareVersion)).toList();
        if(communicatedVersion.size()>1) throw new ServiceValidationException("Cas inattendu: même version existe plusieures fois pour le même contrôleur.");
        if(communicatedVersion.isEmpty()){
            FirmwareInformation firmwareInformation = FirmwareInformation.builder()
               .controllerPts(controllerService.findController(ptsId))
               .ptsId(ptsId)
               .versionState(isFirmwareVersionSupported)
               .dateTime(firmwareVersion)
               .modificationDate(LocalDateTime.now()).build();
            firmwareInformationRepository.save(firmwareInformation);
        }
        else
        {
            final Optional<FirmwareInformation> lastModifiedFirmwareVersion = versionsForPtsId.stream().max(Comparator.comparing(FirmwareInformation::getModificationDate));
            if(!lastModifiedFirmwareVersion.isEmpty() && lastModifiedFirmwareVersion.get().getDateTime().equals(firmwareVersion)){
                final FirmwareInformation firmwareInformation = communicatedVersion.get(0);
                firmwareInformation.setModificationDate(LocalDateTime.now());
                firmwareInformationRepository.save(firmwareInformation);
            }
        }
    }

    /**
     * Renvoie la dernière version enregistrée du firmware du contrôleur identifié par {@param ptsId}
     * Si version inconnue, renvoie "-"
     */
    public String firmwareVersion(String ptsId){
        return firmwareInformationRepository.getLastModifiedFirmwareInformationVersionByPtsId(ptsId).orElse("-");
    }

    public FirmwareInformationDto currentFirmwareVersion(String ptsId){
        Optional<FirmwareInformation> firmwareInformation =  firmwareInformationRepository.getLastModifiedFirmwareInformationVersion(ptsId);
        if (firmwareInformation.isPresent()){
            FirmwareInformation firmwareInformationDto = firmwareInformation.get();
            return FirmwareInformationDto.builder().dateTime(firmwareInformationDto.getDateTime()).ptsId(firmwareInformationDto.getPtsId()).versionState(firmwareInformationDto.isVersionState()).build();
        }else {
            return null;
        }
    }
}


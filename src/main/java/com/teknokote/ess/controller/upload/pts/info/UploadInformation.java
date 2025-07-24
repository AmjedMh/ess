package com.teknokote.ess.controller.upload.pts.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teknokote.ess.controller.upload.pts.validation.EnumValidationResult;
import com.teknokote.ess.core.model.configuration.ControllerPts;
import com.teknokote.ess.core.model.configuration.ControllerPtsConfiguration;
import com.teknokote.ess.core.model.exchanges.EnumUploadMessageType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Bean avec les informations nécessaires à valider:
 * - le contrôleur origine de l'upload,
 * - la dernière version du firmware connue pour ce contrôleur
 * - le type de l'upload
 * - la référence de la configuration "configurationId" du contrôleur concernée par l'upload
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadInformation
{
   @JsonProperty("PtsId")
   private String ptsId;
   @JsonProperty("Packets")
   private List<UploadPacketInformation> uploadPacketInformations;
   private ControllerPts identifiedControllerPts;
   private ControllerPtsConfiguration identifiedControllerPtsConfiguration;
   private String identifiedConfigurationId;
   private String identifiedVersion;
   private boolean expectedFormat = true;

   /**
    * Permet de compléter le contrôleur identifié et la version du firmware
    */
   public UploadInformation completeControllerInfo(Function<String, ControllerPts> fnIdentifyController,
                                                   BiFunction<String, String, ControllerPtsConfiguration> fnIdentifyControllerConfig,
                                                   UnaryOperator<String> fnIdentifyFrmwInformation) {
      this.identifiedControllerPts = fnIdentifyController.apply(ptsId);
      final Set<String> distinctConfigurationIds = getDistinctConfigurationIds();

      if (distinctConfigurationIds.size() == 1) {
         this.identifiedConfigurationId = distinctConfigurationIds.iterator().next();
         this.identifiedControllerPtsConfiguration = fnIdentifyControllerConfig.apply(ptsId, identifiedConfigurationId);
      }

      this.identifiedVersion = Objects.nonNull(identifiedControllerPts) ? fnIdentifyFrmwInformation.apply(ptsId) : "-";
      return this;
   }
   @JsonIgnoreProperties(ignoreUnknown = true)
   @Data
   public static class UploadPacketInformation
   {
      @JsonProperty("Id")
      @Getter
      private String id;
      @JsonProperty("Type")
      @Getter
      private String type;
      // Présente dans le cas d'UploadConfiguration
      @JsonProperty("ConfigurationId")
      private String configurationId;
      @Getter
      @JsonProperty("Data")
      private Data data;

      /**
       * Renvoie "configurationId" remontée par le contrôleur
       * Pour UploadPumpTransaction, "ConfigurationId" est positionnée niveau "Data"
       * Pour UploadConfiguration, "ConfigurationId" est positionnée niveau "Packets"
       */
      public String getConfigurationId(){
         if(Objects.nonNull(configurationId)) return configurationId;
         return data.getConfigurationId();
      }
      @JsonIgnoreProperties(ignoreUnknown = true)
      @NoArgsConstructor
      @Getter
      public static class Data{
         @JsonProperty("ConfigurationId")
         private String configurationId;
      }
   }

   /**
    * Renvoie une liste des valeurs uniques des configuraionId renvoyées par le contrôleur
    */
   public Set<String> getDistinctConfigurationIds(){
      return uploadPacketInformations.stream().map(UploadPacketInformation::getConfigurationId).filter(Objects::nonNull).collect(Collectors.toSet());
   }

   /**
    * Permet de valider les informations sur l'upload reçu depuis le contrôleur
    */
   public List<EnumValidationResult> validate()
   {
      List<EnumValidationResult> validations=new ArrayList<>();
      if(Objects.isNull(ptsId)) validations.add(EnumValidationResult.PTS_ID_NOT_PROVIDED);
      if(Objects.isNull(identifiedControllerPts)) validations.add(EnumValidationResult.UNKNOW_PTS_ID);
      if(Objects.isNull(uploadPacketInformations) || uploadPacketInformations.isEmpty())
      {
         validations.add(EnumValidationResult.UNKNOW_UPLOAD_TYPE);
         validations.add(EnumValidationResult.CONFIGURATION_ID_NOT_PROVIDED);
         return validations;
      }
      if(uploadPacketInformations.stream().anyMatch(el->Objects.isNull(el.getType()))){
         validations.add(EnumValidationResult.UNKNOW_UPLOAD_TYPE);
      }
      if(uploadPacketInformations.stream().anyMatch(el->Objects.isNull(el.getConfigurationId()))){
         validations.add(EnumValidationResult.CONFIGURATION_ID_NOT_PROVIDED);
      }
      final Set<String> distinctConfigurationIds = getDistinctConfigurationIds();
      if(distinctConfigurationIds.size()>1) {
         validations.add(EnumValidationResult.MULTIPLE_CONFIGURATION_ID);
      }
      return validations;
   }

   public EnumUploadMessageType getDetectedKnownMessageType(){
      if(uploadPacketInformations.isEmpty()) return null;
      return EnumUploadMessageType.valueOf(uploadPacketInformations.get(0).getType());
   }
}

package com.teknokote.ess.controller.upload.pts.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadPacketInformation
{
   @JsonProperty("Id")
   private String id;
   @JsonProperty("Type")
   private String type;
   // Présente dans le cas d'UploadConfiguration
   @JsonProperty("ConfigurationId")
   private String configurationId;
   @JsonProperty("Data")
   private Data data;

   @JsonIgnoreProperties(ignoreUnknown = true)
   @NoArgsConstructor
   @Getter
   public static class Data{
      @JsonProperty("ConfigurationId")
      private String configurationId;
   }
}

package com.teknokote.ess.controller.upload.pts.validation;

/**
 * Résultat de validation du message reçu du contrôleur
 */
public enum EnumValidationResult
{
   UNKNOW_PTS_ID,
   PTS_ID_NOT_PROVIDED,
   UNKNOW_UPLOAD_TYPE,
   CONFIGURATION_ID_NOT_PROVIDED,
   // Plusieurs "configurationId" figurent dans les différents packets reçus
   MULTIPLE_CONFIGURATION_ID;
}

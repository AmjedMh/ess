package com.teknokote.ess.core.model.requests;

public enum EnumRequestStatus
{
   PLANNED,
   EXECUTED,
   WAITING_RESPONSE, // Attente de réponse du contrôleur
   FAILED,
   CANCELED
}

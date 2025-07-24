package com.teknokote.ess.core.model.organization;

public enum EnumCustomerAccountStatus
{
   ENABLED, // Compte client actif
   DISABLED, // Compte client inactif. Tous ses utilisateurs et stations sont désactivées.
   ACCESS_DISABLED // compte client avec accès désactivé. Les données des stations continuent à être injectées en base
}

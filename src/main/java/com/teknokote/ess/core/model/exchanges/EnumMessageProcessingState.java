package com.teknokote.ess.core.model.exchanges;

public enum EnumMessageProcessingState
{
   /**
    * Message reçu et ne représente pas de problème de structure, ni de présence d'information obligatoires (par exemple: PtsId, Type, configurationId).
    * Non encore traité par l'application.
    * Etat intermédiaire
    */
   RECEIVED,
   /**
    * Message en cours de traitement
    * Etat intermédiaire
    */
   PROCESSING,
   /**
    * Le message ne peut pas être traité tant que la configuration "configurationId" n'est pas obtenue du contrôleur.
    * Dans ce cas, l'application devrait envoyer un GetConfiguration à "True" pour récupérer la configuration en question.
    * Etat intermédiaire - ce cas ne devrait pas arriver vu que toute modification sur la configuration du contrôleur
    * provoque un UploadConfiguration en priorité.
    */
   UNKNOWN_CONFIGURATION,
   /**
    * Traitement réussi.
    * Etat Final
    */
   SUCCESS,
   /**
    * Traitement message en échec. Le traitement des messages avec ce type d'erreur doit être repris.
    * Etat Intermédiaire
    */
   FAIL,
   /**
    * Traitement message en échec d'extraction d'information (problème de structure du message reçu)
    * Etat Final
    */
   FAIL_EXTRACT_INFO,
   /**
    * Message déjà reçu. Donc, il est ignoré.
    * Etat Final
    */
   DUPLICATE
}

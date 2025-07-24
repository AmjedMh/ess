package com.teknokote.ess.core.model.exchanges;

import com.teknokote.core.model.ESSEntity;
import com.teknokote.ess.core.model.organization.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entité destinée à tracer les échanges "brutes" avec les contrôleurs.
 *
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MessagePtsLog extends ESSEntity<Long, User>
{
    private static final long serialVersionUID = -1388539849834649089L;


    /**
     * Identifiant d'un contrôleur
     */
    private String ptsId;
    /**
     * Origine du message
     */
    @Enumerated(EnumType.STRING)
    private EnumMessageOrigin messageOrigin;
    /**
     * Version du firmware installé sur le contrôleur.
     * Peut être tirée de l'entité {@link com.teknokote.ess.core.model.FirmwareInformation}
     */
    private String firmwareVersion;
    /*
      Date d'envoi/réception du message
     */
    private LocalDateTime messageDate;
    /**
     * Date de mise de l'état de la trace
     */
    private LocalDateTime updateDate;
    /**
     * Type du message de trace
     */
    @Enumerated(EnumType.STRING)
    private EnumUploadMessageType messageType;
    /**
     * Canal d'échange du message
     */
    @Enumerated(EnumType.STRING)
    private EnumChannel channel;
    /**
     * Etat du traitement du message.
     */
    @Enumerated(EnumType.STRING)
    private EnumMessageProcessingState processingState;
    /**
     * Configuration id remonté par le contrôleur
     */
    private String configurationId;
    /**
     * Corps du message échangé
     */
    @Column(columnDefinition = "TEXT")
    private String message;
    /**
     * Raison de l'échec de traitement
     */
    @Column(columnDefinition = "TEXT")
    private String failReason;

    /**
     * Crée une nouvelle trace d'origine WSS
     */
    public static MessagePtsLog logWSSMsg(String ptsId, EnumMessageOrigin messageDirection, String firmwareVersion, String message)
    {
        MessagePtsLog messagePTSLog= new MessagePtsLog();
        messagePTSLog.setPtsId(ptsId);
        messagePTSLog.setMessage(message);
        messagePTSLog.setMessageOrigin(messageDirection);
        messagePTSLog.setFirmwareVersion(firmwareVersion);
        messagePTSLog.setMessageDate(LocalDateTime.now());
        messagePTSLog.setProcessingState(EnumMessageProcessingState.SUCCESS);
        messagePTSLog.setChannel(EnumChannel.WSS);
        return messagePTSLog;
    }
    public static MessagePtsLog logWSSMsgAsReceived(String ptsId, EnumMessageOrigin messageDirection, String firmwareVersion,EnumUploadMessageType messageType, String message){
        MessagePtsLog messagePTSLog = logWSSMsg(ptsId, messageDirection, firmwareVersion, message);
        messagePTSLog.setProcessingState(EnumMessageProcessingState.RECEIVED);
        messagePTSLog.setMessageType(messageType);
        return messagePTSLog;
    }

    public static MessagePtsLog logWSSMsgSuccess(String ptsId, EnumMessageOrigin messageDirection, String firmwareVersion, String message){
        MessagePtsLog messagePTSLog = logWSSMsg(ptsId, messageDirection, firmwareVersion, message);
        messagePTSLog.setProcessingState(EnumMessageProcessingState.SUCCESS);
        return messagePTSLog;
    }

    public static MessagePtsLog logWSSMsgFail(String ptsId, EnumMessageOrigin messageDirection, String firmwareVersion, String message){
        MessagePtsLog messagePTSLog = logWSSMsg(ptsId, messageDirection, firmwareVersion, message);
        messagePTSLog.setProcessingState(EnumMessageProcessingState.FAIL);
        return messagePTSLog;
    }

    /**
     * Créée une nouvelle instance pour les messages envoyés en UPLOAD par le contrôleur
     */
    public static MessagePtsLog logUploadedMsgAsReceived(String ptsId, EnumMessageOrigin messageOrigin, String firmwareVersion, String configurationId,EnumUploadMessageType messageType, String message){
        MessagePtsLog messagePTSLog= new MessagePtsLog();
        messagePTSLog.setPtsId(ptsId);
        messagePTSLog.setMessage(message);
        messagePTSLog.setMessageOrigin(messageOrigin);
        messagePTSLog.setFirmwareVersion(firmwareVersion);
        messagePTSLog.setMessageDate(LocalDateTime.now());
        messagePTSLog.setProcessingState(EnumMessageProcessingState.RECEIVED);
        messagePTSLog.setChannel(EnumChannel.UPLOAD);
        messagePTSLog.setConfigurationId(configurationId);
        messagePTSLog.setMessageType(messageType);
        return messagePTSLog;
    }

    /**
     * Créée une nouvelle instance avec échec d'extraction d'information
     */
    public static MessagePtsLog logUploadedMsgAsFailExtractInfo(String ptsId, EnumMessageOrigin messageOrigin, String firmwareVersion, String message){
        MessagePtsLog messagePTSLog= new MessagePtsLog();
        messagePTSLog.setPtsId(ptsId);
        messagePTSLog.setMessage(message);
        messagePTSLog.setMessageOrigin(messageOrigin);
        messagePTSLog.setFirmwareVersion(firmwareVersion);
        messagePTSLog.setMessageDate(LocalDateTime.now());
        messagePTSLog.setProcessingState(EnumMessageProcessingState.FAIL_EXTRACT_INFO);
        messagePTSLog.setChannel(EnumChannel.UPLOAD);
        return messagePTSLog;
    }
}

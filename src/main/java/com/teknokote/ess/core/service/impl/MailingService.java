package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.CustomerAccountDao;
import com.teknokote.ess.core.dao.mappers.UserControllerMapper;
import com.teknokote.ess.core.model.Alert;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.keycloak.email.EmailException;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class MailingService
{
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private AlertService alertService;
    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private CustomerAccountDao customerAccountDao;
    @Autowired
    private UserControllerMapper userMapper;
    public static final String ALERT_SUBJECT = "Alert detected from Controller";
    public static final String INACTIVITY_ALERT = "Station Inactivity Alert";
    public static final String RESET_PASSWORD_SUBJECT = "Password Reset";
    @Autowired
    private TemplateEngine templateEngine;
    public void sendAlertEmail(String recipientEmail,Alert alert,Station station) throws EmailException {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
            helper.setTo(recipientEmail);
            helper.setSubject(ALERT_SUBJECT);
            // Create a Thymeleaf context and add variables
            Context context = new Context();
            context.setVariable("alert", alert);
            context.setVariable("station", station);
            String emailContent = templateEngine.process("alert-mail", context);
            helper.setText(emailContent,true);
            javaMailSender.send(mimeMessage);
    } catch (MessagingException e) {
            throw new EmailException(e);
        }
    }
    public void sendInactivityEmail(String recipientEmail, StationDto station, String lastUploadTime) throws EmailException{
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
            helper.setTo(recipientEmail);
            helper.setSubject(INACTIVITY_ALERT);
            Context context = new Context();
            context.setVariable("station", station);
            context.setVariable("lastUploadTime", lastUploadTime);
            String emailContent = templateEngine.process("inactivity-alert-mail", context);
            helper.setText(emailContent,true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailException(e);
        }
    }

    /**
     * Envoie l'alert
     */
    public void sendAlert(Alert alert, Station station)
    {
        List<String> recipientEmail = listMails(station.getCustomerAccountId());
        for(String userEmail : recipientEmail)
        {
            try
            {
                sendAlertEmail(userEmail, alert, station);
            }
            catch(EmailException exception)
            {
                throw new RuntimeException(exception);
            }
            alertService.treatAlert(alert);
        }
    }
    public List<String> listMails(Long customerAccountId) {
        UserDto userDto = customerAccountDao.findMasterUserWithCustomerAccountId(customerAccountId);
        if (userDto!=null) {
            Optional<UserRepresentation> users = keycloakService.getUserIdentity(userDto.getUsername());
            return users.stream().map(UserRepresentation::getEmail).toList();
        }
        return Collections.emptyList();
    }
    public void sendResetPassword(String email,String resetToken) throws EmailException {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
            helper.setTo(email);
            helper.setSubject(RESET_PASSWORD_SUBJECT);
            Context context = new Context();
            context.setVariable("resetToken", resetToken);
            String emailContent = templateEngine.process("reset-password", context);
            helper.setText(emailContent,true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailException(e);
        }
    }
}

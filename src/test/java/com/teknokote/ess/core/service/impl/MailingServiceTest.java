package com.teknokote.ess.core.service.impl;

import com.teknokote.ess.core.dao.CustomerAccountDao;
import com.teknokote.ess.core.model.Alert;
import com.teknokote.ess.core.model.configuration.Station;
import com.teknokote.ess.dto.StationDto;
import com.teknokote.ess.dto.UserDto;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailingServiceTest {

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private TemplateEngine templateEngine;
    @InjectMocks
    private MailingService mailingService;
    @Mock
    private AlertService alertService;
    @Mock
    private KeycloakService keycloakService;
    @Mock
    private CustomerAccountDao customerAccountDao;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp(){
        mimeMessage = mock(MimeMessage.class);
        lenient().when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }
    @Test
    void testSendAlert_Success(){
        // Given
        Alert alert = new Alert();
        Station station = new Station();
        station.setCustomerAccountId(1L);

        UserDto userDto = UserDto.builder().build();
        userDto.setUsername("user1");

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("user1");
        userRepresentation.setEmail("user1@domain.com");

        // Simulate database return for user emails
        when(customerAccountDao.findMasterUserWithCustomerAccountId(1L)).thenReturn(userDto);
        when(keycloakService.getUserIdentity("user1")).thenReturn(Optional.of(userRepresentation));

        // Simulate proper email setup
        String emailContent = "Test email content"; // This should match with your Thymeleaf template
        when(templateEngine.process(eq("alert-mail"), any())).thenReturn(emailContent);

        // When
        mailingService.sendAlert(alert, station);

        // Then
        verify(alertService).treatAlert(alert);
        verify(javaMailSender, atLeastOnce()).send(any(MimeMessage.class));
    }

    @Test
    void testSendAlert_ThrowsRuntimeException() {
        // Given
        Alert alert = new Alert();
        Station station = new Station();
        station.setCustomerAccountId(1L);

        UserDto userDto = UserDto.builder().build();
        userDto.setUsername("user1");
        when(customerAccountDao.findMasterUserWithCustomerAccountId(1L)).thenReturn(userDto);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("user1");
        userRepresentation.setEmail("user1@domain.com");
        when(keycloakService.getUserIdentity("user1")).thenReturn(Optional.of(userRepresentation));

        // Simulate when the process returns a valid email content
        when(templateEngine.process(eq("alert-mail"), any())).thenReturn("Valid email content");

        // Simulate an unchecked exception being thrown when sending the email
        doThrow(new RuntimeException("Email sending failed"))
                .when(javaMailSender).send(any(MimeMessage.class));

        // When & Then
        Executable executable = () -> mailingService.sendAlert(alert, station);
        RuntimeException thrown = assertThrows(RuntimeException.class, executable);
        assertEquals("Email sending failed", thrown.getMessage());
    }

    @Test
    void testSendAlertEmail() throws Exception {
        String recipientEmail = "test@example.com";
        Alert alert = new Alert();
        Station station = new Station();

        // Define Thymeleaf template processing
        when(templateEngine.process(eq("alert-mail"), any())).thenReturn("email content");

        // Invoke the method
        mailingService.sendAlertEmail(recipientEmail, alert, station);

        // Verify interactions
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendInactivityEmail() throws Exception {
        // Set up variables
        String recipientEmail = "test@example.com";
        StationDto station = StationDto.builder().build();
        String lastUploadTime = "2023-01-01T00:00:00Z";

        // Define Thymeleaf template processing
        when(templateEngine.process(eq("inactivity-alert-mail"), any())).thenReturn("inactivity email content");

        // Invoke the method
        mailingService.sendInactivityEmail(recipientEmail, station, lastUploadTime);

        // Verify interactions
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendResetPassword() throws Exception {
        // Set up variables
        String email = "test@example.com";
        String resetToken = "dummyToken";

        // Define Thymeleaf template processing
        when(templateEngine.process(eq("reset-password"), any())).thenReturn("reset email content");

        // Invoke the method
        mailingService.sendResetPassword(email, resetToken);

        // Verify interactions
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testListMails_UserDtoFound_UserRepresentationFound() {
        // Given
        Long customerAccountId = 1L;
        UserDto userDto = UserDto.builder().build();
        userDto.setUsername("user1");

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEmail("user1@example.com");

        // Mock the behavior
        when(customerAccountDao.findMasterUserWithCustomerAccountId(customerAccountId)).thenReturn(userDto);
        when(keycloakService.getUserIdentity("user1")).thenReturn(Optional.of(userRepresentation));

        // When
        List<String> result = mailingService.listMails(customerAccountId);

        // Then
        assertEquals(1, result.size());
        assertEquals("user1@example.com", result.get(0));
    }

    @Test
    void testListMails_UserDtoFound_UserRepresentationNotFound() {
        // Given
        Long customerAccountId = 1L;
        UserDto userDto = UserDto.builder().build();
        userDto.setUsername("user1");

        // Mock the behavior
        when(customerAccountDao.findMasterUserWithCustomerAccountId(customerAccountId)).thenReturn(userDto);
        when(keycloakService.getUserIdentity("user1")).thenReturn(Optional.empty());

        // When
        List<String> result = mailingService.listMails(customerAccountId);

        // Then
        assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());    }

    @Test
    void testListMails_UserDtoNotFound() {
        // Given
        Long customerAccountId = 1L;

        // Mock the behavior
        when(customerAccountDao.findMasterUserWithCustomerAccountId(customerAccountId)).thenReturn(null);

        // When
        List<String> result = mailingService.listMails(customerAccountId);

        // Then
        assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

}
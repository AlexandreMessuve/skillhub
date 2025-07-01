package com.skillhub.service;

import com.skillhub.entity.UserInfo;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTests {
    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    private UserInfo testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "appBaseUrl", "http://test-url.com");

        testUser = UserInfo.builder()
                .firstName("John")
                .email("john.doe@email.com")
                .verificationToken("abc-123-def-456")
                .build();
    }

    @Test
    void sendWelcomeEmail_shouldConstructCorrectMessage() {
        emailService.sendWelcomeEmail(testUser);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertTrue(Arrays.stream(sentMessage.getTo()).map(String::toLowerCase).anyMatch("john.doe@email.com"::equals));
        assertEquals("Welcome to SkillHub!", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("Hey John"));
    }

    @Test
    void sendVerifyEmailHtml_shouldConstructCorrectHtmlMessage(){
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);


        emailService.sendVerifyEmailHtml(testUser);

        ArgumentCaptor<MimeMessage> mimeMessageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(mimeMessageCaptor.capture());

        assertEquals(mimeMessage, mimeMessageCaptor.getValue());
    }
}

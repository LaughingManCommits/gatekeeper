package com.gate.keeper.controller;

import you.shall.not.pass.dto.ViolationResponseDto;
import you.shall.not.pass.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import you.shall.not.pass.controller.AuthenticateController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateControllerTest {

  @Mock
  private AuthenticationService authenticationService;

  @InjectMocks
  private AuthenticateController authenticateController;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;


  @Test
  void testAuthenticate() {
    // Mocking
    when(request.getAttribute(any())).thenReturn(true); // Mocking authenticated attribute

    // Testing
    ResponseEntity<String> responseEntity = authenticateController.authenticate(response, request);

    // Verification
    verify(authenticationService).createAuthenticatedUserSession(request, response);
    assertEquals(200, responseEntity.getStatusCodeValue());
  }

  @Test
  void testLogout() {
    // Testing
    ResponseEntity<String> responseEntity = authenticateController.logout();

    // Verification
    assertEquals(200, responseEntity.getStatusCodeValue());
    assertEquals("logout success", responseEntity.getBody());
  }

  @Test
  void testHandleCsrfError() {
    // Testing
    ViolationResponseDto violationResponseDto = authenticateController.handleCsrfError();

    // Verification
    assertEquals("TODO!!!", violationResponseDto.getMessage());
  }

  @Test
  void testCsrfViolationExceptionHandling() {
    // Mocking
//    when(authenticationService.createAuthenticatedUserSession(any(), any())).thenThrow(CsrfViolationException.class);

    // Testing
    ResponseEntity<String> responseEntity = authenticateController.authenticate(response, request);

    // Verification
    verify(authenticationService).createAuthenticatedUserSession(request, response);
    assertEquals(400, responseEntity.getStatusCodeValue());
  }
}

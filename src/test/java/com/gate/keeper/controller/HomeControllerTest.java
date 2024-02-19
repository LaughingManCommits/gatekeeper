package com.gate.keeper.controller;


import you.shall.not.pass.service.AuthenticationService;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import you.shall.not.pass.controller.HomeController;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {
  @Mock
  private AuthenticationService authenticationService;

  @InjectMocks
  private HomeController homeController;


  private static Stream<Arguments> requestMethodsProvider() {
    return Stream.of(
        Arguments.of("GET"),
        Arguments.of("POST")
    );
  }

  @ParameterizedTest
  @MethodSource("requestMethodsProvider")
  void shouldCreateAnonymousSessionAndReturnHomePage(String requestMethod) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod(requestMethod);
    MockHttpServletResponse response = new MockHttpServletResponse();

    String result = homeController.hello(response, request);

    verify(authenticationService).createAnonymousSession(any(), any());
    assertEquals("home-screen", result);
  }
}


package com.gate.keeper.controller;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gate.keeper.dto.AuthenticationResponseDto;
import com.gate.keeper.dto.ViolationResponseDto;
import com.gate.keeper.exception.CsrfViolationException;
import com.gate.keeper.service.AuthenticationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.gate.keeper.service.AuthenticationService.AUTHENTICATED;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticateController {

    private final AuthenticationService authenticationService;
    private final Gson gson;

    @PostMapping({"/authenticate"})
    public ResponseEntity<String> authenticate(HttpServletResponse response, HttpServletRequest request) {
        authenticationService.createAuthenticatedUserSession(request, response);
        Object authenticated = request.getAttribute(AUTHENTICATED);
        AuthenticationResponseDto.AuthenticationResponseDtoBuilder builder = AuthenticationResponseDto.builder();
        builder.authenticated(authenticated != null);
        String json = gson.toJson(builder.build());
        log.info("authenticate response: " + json);
        return ResponseEntity.ok(json);
    }

    @GetMapping({"/logout"})
    public ResponseEntity<String> logout() {
        // TODO bonus implement logout feature and add integration tests to prove it works
        return ResponseEntity.ok("logout success");
    }

    @ExceptionHandler(CsrfViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody ViolationResponseDto handleCsrfError() {
        //TODO Improve error handling to cover all known unchecked exceptions, validate service layers for exceptions

        return ViolationResponseDto.builder().message("TODO!!!").build();
    }



}

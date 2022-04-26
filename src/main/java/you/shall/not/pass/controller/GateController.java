package you.shall.not.pass.controller;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import you.shall.not.pass.dto.ResponseDto;
import you.shall.not.pass.dto.StaticResourcesDto;
import you.shall.not.pass.dto.ViolationDto;
import you.shall.not.pass.exception.CsrfViolationException;
import you.shall.not.pass.filter.staticresource.StaticResourceService;
import you.shall.not.pass.service.AuthenticationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static you.shall.not.pass.service.AuthenticationService.AUTHENTICATED;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GateController {

    private final StaticResourceService resourceService;
    private final AuthenticationService authenticationService;
    private final Gson gson;

    @PostMapping({"/access"})
    public ResponseEntity<String> access(HttpServletResponse response, HttpServletRequest request) {
        authenticationService.createAuthenticatedUserSession(request, response);
        Object authenticated = request.getAttribute(AUTHENTICATED);
        ResponseDto.ResponseDtoBuilder builder = ResponseDto.builder();
        builder.authenticated(authenticated != null);
        String json = gson.toJson(builder.build());
        log.info("Access response: " + json);
        return ResponseEntity.ok(json);
    }

    @GetMapping({"/resources"})
    public ResponseEntity<String> resources() {
        StaticResourcesDto resources = StaticResourcesDto.builder()
                .resources(resourceService.getAllStaticResources()).build();
        return ResponseEntity.ok(gson.toJson(resources));
    }

    @GetMapping({"/home"})
    public String hello(HttpServletResponse response, HttpServletRequest request, ModelMap model) {
        authenticationService.createAnonymousSession(request, response);
        model.addAttribute("title", "Basic Authentication and Access Application");
        return "home-screen";
    }

    @ExceptionHandler(CsrfViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody ViolationDto handleResourceNotFound() {
        // TODO Improve error handling to cover all known unchecked exceptions, validate service layers for exceptions
        return ViolationDto.builder().message("TODO!!!").build();
    }

}

package you.shall.not.pass.controller;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import you.shall.not.pass.dto.StaticResourcesDto;
import you.shall.not.pass.dto.ResponseDto;
import you.shall.not.pass.dto.ViolationDto;
import you.shall.not.pass.exception.CsrfViolationException;
import you.shall.not.pass.filter.staticresource.StaticResourceService;
import you.shall.not.pass.service.CookieService;
import you.shall.not.pass.service.CsrfProtectionService;
import you.shall.not.pass.service.SessionService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static you.shall.not.pass.filter.SecurityFilter.SESSION_COOKIE;

@Slf4j
@Controller
public class GateController {


    private final SessionService sessionService;
    private final CsrfProtectionService csrfProtectionService;
    private final StaticResourceService resourceService;
    private final CookieService cookieService;
    private final Gson gson;

    public GateController(SessionService sessionService, CsrfProtectionService csrfProtectionService,
                          StaticResourceService resourceService, CookieService cookieService, Gson gson) {
        this.sessionService = sessionService;
        this.csrfProtectionService = csrfProtectionService;
        this.resourceService = resourceService;
        this.cookieService = cookieService;
        this.gson = gson;
    }

    @PostMapping({"/access"})
    public ResponseEntity<String> access(HttpServletResponse response, HttpServletRequest request) {
        ResponseDto.ResponseDtoBuilder builder = ResponseDto.builder();
        final String sessionCookie = cookieService.getCookieValue(request, SESSION_COOKIE);

        csrfProtectionService.validateCsrfCookie(request);

        Optional<String> optionalSession = sessionService.stepUpOrCreateSession(sessionCookie);
        optionalSession.ifPresent(session -> {
            String csrf = csrfProtectionService.getCsrfCookie();
            cookieService.addCookie(csrf, response);
            cookieService.addCookie(session, response);
            builder.authenticated(true);
        });

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
    public String hello(ModelMap model) {
        model.addAttribute("title", "Basic Authentication and Access Application");
        return "home-screen";
    }

    @ExceptionHandler(CsrfViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody ViolationDto handleResourceNotFound() {
        return ViolationDto.builder().message("TODO!!!").build();
    }

}

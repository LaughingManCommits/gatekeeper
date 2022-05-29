package you.shall.not.pass.controller;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import you.shall.not.pass.dto.StaticResourcesResponseDto;
import you.shall.not.pass.security.staticresource.StaticResourcePathsService;

@RestController
@RequiredArgsConstructor
public class ResourcesController {

    private final StaticResourcePathsService resourceService;

    private final Gson gson;

    @GetMapping({"/resources"})
    public ResponseEntity<String> resources() {
        StaticResourcesResponseDto resources = StaticResourcesResponseDto.builder()
                .resources(resourceService.getAllStaticResources()).build();
        return ResponseEntity.ok(gson.toJson(resources));
    }

}

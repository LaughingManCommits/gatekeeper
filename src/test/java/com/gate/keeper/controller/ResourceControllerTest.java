package com.gate.keeper.controller;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import you.shall.not.pass.dto.StaticResourcesResponseDto;
import you.shall.not.pass.security.staticresource.StaticResourcePathsService;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import you.shall.not.pass.controller.ResourcesController;

public class ResourceControllerTest {

  @Mock
  private StaticResourcePathsService resourceService;

  @Mock
  private Gson gson;

  @InjectMocks
  private ResourcesController resourcesController;


  @Test
  void testResources() {

    String[] resourcesArray = {"/resource1", "/resource2"};

    when(resourceService.getAllStaticResources()).thenReturn(asList(resourcesArray));

    StaticResourcesResponseDto expectedResponseDto = StaticResourcesResponseDto.builder()
        .resources(asList(resourcesArray)).build();

    when(gson.toJson(expectedResponseDto)).thenReturn("Json Response");

    ResponseEntity response = resourcesController.resources();

    assertEquals(ResponseEntity.ok("Json Response"), response);
  }
}

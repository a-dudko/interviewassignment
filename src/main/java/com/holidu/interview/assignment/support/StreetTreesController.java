package com.holidu.interview.assignment.support;

import com.holidu.interview.assignment.service.StreetTreeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StreetTreesController {

    private StreetTreeService streetTreeService;

    public StreetTreesController(StreetTreeService streetTreeService) {
        this.streetTreeService = streetTreeService;
    }

    @GetMapping(name = "treesGetEndpoint", value = "/trees")
    public Map<String, Long> treesGet(
            @RequestParam(value = "radius") double radius,
            @RequestParam(value = "x") double xCoord,
            @RequestParam(value = "y") double yCoord
    ) {
        return streetTreeService.getTreesCount(xCoord, yCoord, radius);
    }
}

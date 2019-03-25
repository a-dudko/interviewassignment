package com.holidu.interview.assignment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.holidu.interview.assignment.domain.StreetTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StreetTreeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreetTreeService.class);

    private static final String BASE_URL = "https://data.cityofnewyork.us/resource/nwxe-4ae8.json";
    private static final String URL_PARAMS = "?$where=x_sp > %.1f AND x_sp < %.1f AND y_sp > %.1f AND y_sp < %.1f";
    private static final int LIMIT = 1000;
    private static final String LIMIT_AND_OFFSET_PARAMS = "&$limit=" + LIMIT + "&$offset=%d";
    private static final String ABSENT_NAME = "<without name>";
    private static final double PRECISION = 0.000001;
    private static final double METER_TO_FEET_RATIO = 3.28084;

    private ObjectMapper objectMapper;

    public StreetTreeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Long> getTreesCount(double x, double y, double radiusInMeters) {
        double radiusInFeet = radiusInMeters * METER_TO_FEET_RATIO;
        double minX = x - radiusInFeet - 1;
        double maxX = x + radiusInFeet + 1;
        double minY = y - radiusInFeet - 1;
        double maxY = y + radiusInFeet + 1;
        String urlWithParams = BASE_URL + String.format(URL_PARAMS, minX, maxX, minY, maxY)
                .replace(" ", "%20")
                .replace(",", ".");

        return countTreesInArea(findAllTrees(urlWithParams), x, y, radiusInFeet);
    }

    private List<StreetTree> findAllTrees(String url) {
        List<StreetTree> allTrees = new ArrayList<>();
        int currentPage = 0;
        List<StreetTree> currentPageTrees;
        do {
            String urlWithPagination = url + String.format(LIMIT_AND_OFFSET_PARAMS, currentPage * LIMIT);
            currentPageTrees = getStreetTrees(urlWithPagination);
            allTrees.addAll(currentPageTrees);
            currentPage++;
        } while (currentPageTrees.size() == LIMIT);
        return allTrees;
    }

    private Map<String, Long> countTreesInArea(List<StreetTree> trees, double x, double y, double radius) {
        return trees.stream()
                .filter(tree -> isTreeInCircle(tree, x, y, radius))
                .map(tree -> tree.getName() != null ? tree.getName() : ABSENT_NAME)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private List<StreetTree> getStreetTrees(String uriWithParams) {
        List<StreetTree> trees = new ArrayList<>();
        LOGGER.debug("Getting response from: {}", uriWithParams);
        try {
            trees = objectMapper.readValue(new URL(uriWithParams), new TypeReference<List<StreetTree>>(){});
        } catch (IOException e) {
            LOGGER.error("Couldn't parse response from " + uriWithParams, e);
        }
        return trees;
    }

    private boolean isTreeInCircle(StreetTree tree, double xCoord, double yCoord, double radius) {
        if (tree.getXCoord() == null || tree.getYCoord() == null) {
            return false;
        }
        double xLength = tree.getXCoord() - xCoord;
        double yLength = tree.getYCoord() - yCoord;
        double distance = Math.sqrt(Math.pow(xLength, 2.0) + Math.pow(yLength, 2.0));
        return distance - radius < PRECISION;
    }
}

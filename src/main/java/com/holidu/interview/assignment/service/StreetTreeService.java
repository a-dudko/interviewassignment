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

    private final static Logger LOGGER = LoggerFactory.getLogger(StreetTreeService.class);

    private final static String BASE_URL = "https://data.cityofnewyork.us/resource/nwxe-4ae8.json";
    private final static String URL_PARAMS = "?$where=x_sp > %.1f AND x_sp < %.1f AND y_sp > %.1f AND y_sp < %.1f";
    private final static int LIMIT = 1000;
    private final static String LIMIT_AND_OFFSET_PARAMS = "&$limit=" + LIMIT + "&$offset=%d";
    private static final String ABSENT_NAME = "<without name>";
    private static final double PRECISION = 0.000001;

    private ObjectMapper objectMapper;

    public StreetTreeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Long> getTreesCount(double x, double y, double radius) {
        String urlWithParams = BASE_URL + String.format(URL_PARAMS, x - radius - 1, x + radius + 1, y - radius - 1, y + radius + 1)
                .replace(" ", "%20")
                .replace(",", ".");

        return countTreesInArea(findAllTrees(urlWithParams), x, y, radius);
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

package com.holidu.interview.assignment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.holidu.interview.assignment.domain.StreetTree;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class StreetTreeServiceTest {

    private static final String BASE_EXPECTED_URL = "https://data.cityofnewyork.us/resource/nwxe-4ae8.json";

    @InjectMocks
    private StreetTreeService streetTreeService;

    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void mapperShouldCallCorrectUrl() throws IOException {
        when(objectMapper.readValue(any(URL.class), any(TypeReference.class))).thenReturn(new ArrayList<>());
        String expectedUrl = BASE_EXPECTED_URL +
                "?$where=x_sp%20>%202.0%20AND%20x_sp%20<%208.0%20AND%20y_sp%20>%2010.0%20AND%20y_sp%20<%2016.0&$limit=1000&$offset=0";
        streetTreeService.getTreesCount(5, 13, 2);
        verify(objectMapper).readValue(eq(new URL(expectedUrl)), any(TypeReference.class));
    }

    @Test
    public void mapperShouldCallNextPageIfLimitNumberIsRetrievedOnly() throws IOException {
        ArrayList<StreetTree> results = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            results.add(new StreetTree());
        }
        String firstPageUrl = BASE_EXPECTED_URL +
                "?$where=x_sp%20>%202.0%20AND%20x_sp%20<%208.0%20AND%20y_sp%20>%2010.0%20AND%20y_sp%20<%2016.0&$limit=1000&$offset=0";
        String secondPageUrl = BASE_EXPECTED_URL +
                "?$where=x_sp%20>%202.0%20AND%20x_sp%20<%208.0%20AND%20y_sp%20>%2010.0%20AND%20y_sp%20<%2016.0&$limit=1000&$offset=1000";
        String thirdPageUrl = BASE_EXPECTED_URL +
                "?$where=x_sp%20>%202.0%20AND%20x_sp%20<%208.0%20AND%20y_sp%20>%2010.0%20AND%20y_sp%20<%2016.0&$limit=1000&$offset=2000";
        when(objectMapper.readValue(eq(new URL(firstPageUrl)), any(TypeReference.class))).thenReturn(results);
        when(objectMapper.readValue(eq(new URL(secondPageUrl)), any(TypeReference.class))).thenReturn(new ArrayList<>());

        streetTreeService.getTreesCount(5, 13, 2);

        verify(objectMapper).readValue(eq(new URL(firstPageUrl)), any(TypeReference.class));
        verify(objectMapper).readValue(eq(new URL(secondPageUrl)), any(TypeReference.class));
        verify(objectMapper, never()).readValue(eq(new URL(thirdPageUrl)), any(TypeReference.class));
    }

    @Test
    public void getTreesCountShouldReturnCorrectTreesCount() throws IOException {
        List<StreetTree> allTrees = asList(
            new StreetTree("first", 8.0, 4.0),
            new StreetTree("second", 10.0, 9.0),
            new StreetTree("first", 12.0, 8.0),
            new StreetTree("second", 7.0, 2.0),
            new StreetTree("third", null, 2.0),
            new StreetTree("fourth", 7.0, null),
            new StreetTree(null, 10.0, 5.0)
        );
        when(objectMapper.readValue(any(URL.class), any(TypeReference.class))).thenReturn(allTrees);
        Map<String, Long> expectedResult = new HashMap<>();
        expectedResult.put("first", 2L);
        expectedResult.put("second", 1L);
        expectedResult.put("<without name>", 1L);

        Map<String, Long> actualResult = streetTreeService.getTreesCount(10, 5, 4);
        assertThat(actualResult, is(expectedResult));
    }
}

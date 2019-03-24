package com.holidu.interview.assignment;

import com.holidu.interview.assignment.service.StreetTreeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class AppTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StreetTreeService streetTreeService;

    @Test
    public void healthCheckEndpointShouldReturn200WithDefaultMessage() throws Exception {
        this.mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greetings from the Holidu interview assignment!")));
    }

    @Test
    public void treesEndpointShouldReturn400IfRadiusArgumentIsMissing() throws Exception {
        this.mockMvc.perform(get("/trees?x=3&y=10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Required double parameter 'radius' is not present")));
    }

    @Test
    public void treesEndpointShouldReturn400IfXArgumentIsMissing() throws Exception {
        this.mockMvc.perform(get("/trees?radius=3&y=10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Required double parameter 'x' is not present")));
    }

    @Test
    public void treesEndpointShouldReturn400IfYArgumentIsMissing() throws Exception {
        this.mockMvc.perform(get("/trees?radius=3&x=10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Required double parameter 'y' is not present")));
    }

    @Test
    public void treesEndpointShouldReturn200WithContentBasedOnServiceResult() throws Exception {
        HashMap<String, Long> mockResponse = new HashMap<>();
        mockResponse.put("tree", 5L);
        when(streetTreeService.getTreesCount(3, 5, 7)).thenReturn(mockResponse);
        this.mockMvc.perform(get("/trees?radius=7&x=3&y=5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"tree\":5}"));
    }
}

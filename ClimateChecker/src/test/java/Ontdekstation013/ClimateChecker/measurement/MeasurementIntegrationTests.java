package Ontdekstation013.ClimateChecker.measurement;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MeasurementIntegrationTests {
    
    @Autowired
    private MockMvc mockMvc;

    private Logger LOG = LogManager.getLogger(MeasurementIntegrationTests.class);

    @Test
    @Order(1)
    public void testGetLatestMeasurements() throws Exception{
        
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/measurement/latest" + "?limit=60")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

        LOG.info(result.getResponse().getContentAsString());

        assert(result.getResponse().getContentAsString().contains("id"));
        assert(result.getResponse().getContentAsString().contains("timestamp"));
        assert(result.getResponse().getContentAsString().contains("longitude"));
        assert(result.getResponse().getContentAsString().contains("latitude"));
        assert(result.getResponse().getContentAsString().contains("temperature"));
    }

}

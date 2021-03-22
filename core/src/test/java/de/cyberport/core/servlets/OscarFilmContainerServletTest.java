package de.cyberport.core.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.cyberport.core.helpers.TestDataAdapter;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Sentham
 */
@ExtendWith(AemContextExtension.class)
class OscarFilmContainerServletTest {

    private static final String APPLICATION_JSON = "application/json";

    private OscarFilmContainerServlet underTest = new OscarFilmContainerServlet();

    private MockSlingHttpServletRequest request;

    private MockSlingHttpServletResponse response;

    private static final String SORTED_RESULTS = "sortedResults.json";


    @BeforeEach
    void setUp(AemContext context) {
        request = context.request();
        response = context.response();
        context.load().json("/oscars.json", "/content/oscars");
        context.currentResource("/content/oscars");
        request.setResource(context.currentResource());
    }

    @Test
    @DisplayName("Filtering of movies with single query parameter")
    void singleParam(AemContext context) throws IOException {

        final Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("year", 1964);
        request.setParameterMap(requestParams);
        underTest.doGet(request, response);

        String jsonString = response.getOutputAsString();
        JsonObject jsonResp = new Gson().fromJson(jsonString, JsonObject.class);
        JsonArray resultsArray = (JsonArray) jsonResp.get("results");
        resultsArray.forEach((element) -> {
            if (element.isJsonObject()) {
                JsonObject resource = element.getAsJsonObject();
                assertEquals(resource.get("year").getAsInt(), 1964);
            }
        });
        assertEquals(13, resultsArray.size(),"Received incorrect number of results");
        assertTrue(APPLICATION_JSON.equals(context.response().getContentType()), "Incorrect content type received");
    }

    @Test
    @DisplayName("Filtering movies with multiple parameters")
    void multipleParam(AemContext context) throws IOException {

        final Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("minAwards", 4);
        requestParams.put("minYear", 2000);
        requestParams.put("maxYear", 2003);
        requestParams.put("sortBy", OscarSortBy.title);
        requestParams.put("nominations", 13);

        request.setParameterMap(requestParams);
        underTest.doGet(request, response);

        String jsonString = response.getOutputAsString();
        JsonObject jsonResp = new Gson().fromJson(jsonString, JsonObject.class);
        JsonArray resultsArray = (JsonArray) jsonResp.get("results");
        resultsArray.forEach((element) -> {
            if (element.isJsonObject()) {
                JsonObject resource = element.getAsJsonObject();
                assertThat( resource.get("year").getAsInt(), greaterThanOrEqualTo(2000));
                assertThat( resource.get("year").getAsInt(), lessThanOrEqualTo(2003));
                assertThat(resource.get("awards").getAsInt(), greaterThanOrEqualTo(4));
                assertEquals(13, resource.get("nominations").getAsInt());
            }

        });
        assertTrue("application/json".equals(context.response().getContentType()), "Incorrect content type received");
        assertEquals(2, resultsArray.size(),"Received incorrect number of results");
    }

    @Test
    @DisplayName("Testing the response when the parameter values doesn't match with any movie")
    void noMatchingParam(AemContext context) throws IOException {

        final Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("minAwards", 100);
        request.setParameterMap(requestParams);
        underTest.doGet(request, response);

        String jsonString = response.getOutputAsString();
        JsonObject jsonResp = new Gson().fromJson(jsonString, JsonObject.class);
        JsonArray resultsArray = (JsonArray) jsonResp.get("results");
        assertTrue("application/json".equals(context.response().getContentType()), "Incorrect content type received");
        assertEquals(0, resultsArray.size(),"Received incorrect number of results");
    }

    @Test
    @DisplayName("Testing unsupported parameter")
    void unsupportedParam(AemContext context) throws IOException {

        final Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("language", "English");
        request.setParameterMap(requestParams);
        underTest.doGet(request, response);

        JsonObject jsonResp = new Gson().fromJson(response.getOutputAsString(), JsonObject.class);
        JsonArray resultsArray = jsonResp.get("results").getAsJsonArray();

        assertTrue("application/json".equals(context.response().getContentType()), "Incorrect content type received");
        assertEquals(1316, resultsArray.size(),"Received incorrect number of results");
    }

    @Test
    @DisplayName("Test Sorting order")
    void testSortingOrder(AemContext context) throws IOException, JSONException {

        JsonObject jsonObject = TestDataAdapter.loadTestData(TestDataAdapter.getFilePath(SORTED_RESULTS));
        String testJsonArray = jsonObject.get("results").toString();

        final Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("minAwards", 4);
        requestParams.put("minYear", 2000);
        requestParams.put("maxYear", 2003);
        requestParams.put("sortBy", OscarSortBy.nominations);
        request.setParameterMap(requestParams);
        underTest.doGet(request, response);

        String jsonString = response.getOutputAsString();
        JsonObject jsonResp = new Gson().fromJson(jsonString, JsonObject.class);
        String resultsArray = jsonResp.get("results").toString();
        JSONAssert.assertEquals("Assertion failed as the actual result's sorting order didn't match with the sample one", testJsonArray, resultsArray, JSONCompareMode.STRICT);

        assertTrue("application/json".equals(context.response().getContentType()), "Incorrect content type received");
    }

    @Test
    @DisplayName("When wrong min and max values are given")
    void wrongMinMaxValues(AemContext context) throws IOException, JSONException {

        final Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("minAwards", 4);
        requestParams.put("minYear", 2003);
        requestParams.put("maxYear", 2000);
        requestParams.put("sortBy", OscarSortBy.nominations);
        request.setParameterMap(requestParams);
        underTest.doGet(request, response);

        JsonObject jsonResp = new Gson().fromJson(response.getOutputAsString(), JsonObject.class);
        JsonArray resultsArray = jsonResp.get("results").getAsJsonArray();

        assertEquals(0, resultsArray.size(),"Received incorrect number of results");
        assertTrue("application/json".equals(context.response().getContentType()), "Incorrect content type received");
    }

    @Test
    @DisplayName("When wrong and correct param values are mixed.For example negative values")
    void partiallyCorrectValues(AemContext context) throws IOException, JSONException {

        final Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("minAwards", 4);
        //requestParams.put("minYear", "2003");
        requestParams.put("year", -1);
        requestParams.put("sortBy", OscarSortBy.nominations);
        request.setParameterMap(requestParams);
        underTest.doGet(request, response);

        JsonObject jsonResp = new Gson().fromJson(response.getOutputAsString(), JsonObject.class);
        JsonArray resultsArray = jsonResp.get("results").getAsJsonArray();

        assertEquals(0, resultsArray.size(),"Received incorrect number of results");
        assertTrue("application/json".equals(context.response().getContentType()), "Incorrect content type received");
    }
}

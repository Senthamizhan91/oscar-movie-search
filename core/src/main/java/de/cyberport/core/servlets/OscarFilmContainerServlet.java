package de.cyberport.core.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.cyberport.core.utils.OscarUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Servlet that writes information about the Oscar films in json format into the response.
 * It is mounted for all resources of a specific Sling resource type.
 *
 * Based on the request parameters, a filtering and sorting should be applied. Default sort order is by title.
 *
 * For cases when there is no supported request parameter provided in the request,
 * the servlet should return all the films below the requested container.
 *
 * The Servlet must support following request parameters:
 * 1. title - String. The exact film title
 * 2. year - Integer. The exact year when the film was nominated
 * 3. minYear - Integer. The minimum value of the year for the nominated film
 * 4. maxYear - Integer. The maximum value of the year for the nominated film
 * 5. minAwards - Integer. The minimum value for number of awards
 * 6. maxAwards - Integer. The maximum value for number of awards
 * 7. nominations - Integer. The exact number of nominations
 * 8. isBestPicture - Boolean. True to return only the winners of the best picture nomination.
 * 9. sortBy - Enumeration. Sorting in ascending order, supported values are: 'title', 'year', 'awards', 'nominations'. Default value should be 'title'.
 * 10. limit - Integer. Maximum amount of result entries in the response.
 *
 * Please note:
 * More then 1 filter must be supported.
 * The resulting JSON must not contain "jcr:primaryType" and "sling:resourceType" properties
 * When there will be no results based on the provided filter an empty array should be returned. Please refer to the 3rd example.
 *
 * Examples based on the data stored in oscars.json in resources directory.
 *
 * 1. Request parameters: year=2019&minAwards=4
 *
 * Sample response:
 * {
 *   "result": [
 *     {
 *       "title": "Parasite",
 *       "year": "2019",
 *       "awards": 4,
 *       "nominations": 6,
 *       "isBestPicture": true,
 *       "numberOfReferences": 8855
 *     }
 *   ]
 * }
 *
 * 2. Request parameters: minYear=2018&minAwards=3&sortBy=nominations&limit=4
 *
 * Sample response:
 * {
 *   "result": [
 *     {
 *       "title": "Bohemian Rhapsody",
 *       "year": "2018",
 *       "awards": 4,
 *       "nominations": 5,
 *       "isBestPicture": false,
 *       "numberOfReferences": 387
 *     },
 *     {
 *       "title": "Green Book",
 *       "year": "2018",
 *       "awards": 3,
 *       "nominations": 5,
 *       "isBestPicture": true,
 *       "numberOfReferences": 2945
 *     },
 *     {
 *       "title": "Parasite",
 *       "year": "2019",
 *       "awards": 4,
 *       "nominations": 6,
 *       "isBestPicture": true,
 *       "numberOfReferences": 8855
 *     },
 *     {
 *       "title": "Black Panther",
 *       "year": "2018",
 *       "awards": 3,
 *       "nominations": 7,
 *       "isBestPicture": false,
 *       "numberOfReferences": 770
 *     }
 *   ]
 * }
 *
 * 3. Request parameters: title=nonExisting
 *
 * Sample response:
 * {
 *   "result": []
 * }
 * @author Sentham
 */
@Component(service = { Servlet.class }, immediate = true)
@SlingServletResourceTypes(
        resourceTypes="test/filmEntryContainer",
        methods=HttpConstants.METHOD_GET,
        extensions="json")
@ServiceDescription("Oscar Film Container Servlet")
public class OscarFilmContainerServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    private final Gson gson = new Gson();

    private final Map<String, Object> requestParamsMap = new HashMap<>();

    @Override
    public void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException {
        JsonObject jsonObject = new JsonObject();
        String title = request.getParameter(OscarConstants.TITLE);
        if (title != null) {
            requestParamsMap.put(OscarConstants.TITLE, title);
        }
        getFilterParams(OscarConstants.YEAR, request);
        getFilterParams(OscarConstants.MIN_YEAR, request);
        getFilterParams(OscarConstants.MAX_YEAR, request);
        getFilterParams(OscarConstants.MIN_AWARDS, request);
        getFilterParams(OscarConstants.MAX_AWARDS, request);
        getFilterParams(OscarConstants.NOMINATIONS, request);
        String bestPicture = request.getParameter(OscarConstants.IS_BEST_PICTURE);
        if (bestPicture != null) {
            boolean isBestPicture = Boolean.parseBoolean(request.getParameter(OscarConstants.IS_BEST_PICTURE));
            requestParamsMap.put(OscarConstants.IS_BEST_PICTURE, isBestPicture);
        }
        getFilterParams(OscarConstants.LIMIT, request);
        String sortby = request.getParameter(OscarConstants.SORT_BY);
        if (sortby != null) {
            OscarSortBy sortBy = OscarSortBy.valueOf(sortby);
            requestParamsMap.put(OscarConstants.SORT_BY, sortBy);
        }


        JsonArray obj = gson.fromJson(getfilteredJson(request), JsonArray.class);
        jsonObject.add("results", obj);

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        out.print(jsonObject.toString());
        out.flush();
        System.out.println("Final Json " + jsonObject.toString());
    }

    /**
     * Main logic of filtering and sorting the resources based on the request parameters
     * @param request The Sling request
     * @return Filtered list of valuemap
     */
    private String getfilteredJson(SlingHttpServletRequest request) {
        List<ModifiableValueMap> filteredValueMap = new ArrayList<>();
        Resource resource= request.getResource();

        //The below logic can also be simplified by using JCR SQL2 query with which we can pass parameters for filtering and sorting
        for (Resource child : resource.getChildren()){
            ModifiableValueMap childValuemap = child.adaptTo(ModifiableValueMap.class);
            boolean isReadyToAdd = true;
            for (Map.Entry<String, Object> entry1 : requestParamsMap.entrySet()) {
                if (!StringUtils.contains(entry1.getKey(), OscarConstants.SORT_BY) && !StringUtils.contains(entry1.getKey(), OscarConstants.LIMIT)) {
                    isReadyToAdd = isReadyToAdd && compareValues(entry1, childValuemap);
                }
            }
            if (isReadyToAdd) {
                addToValueMapSet(filteredValueMap, childValuemap, request);
            }
            if (requestParamsMap.get("limit") != null && filteredValueMap.size() >= (int) requestParamsMap.get("limit")) {
                break;
            }
        }

        if (requestParamsMap.get(OscarConstants.SORT_BY) != null) {
            String sortBy = requestParamsMap.get(OscarConstants.SORT_BY).toString();
            if (StringUtils.equals(sortBy, "title")) {
                filteredValueMap.sort(Comparator.comparing((ModifiableValueMap map) -> map.get(sortBy).toString()));
            } else {
                filteredValueMap.sort(Comparator.comparing((ModifiableValueMap map) -> Integer.parseInt(map.get(sortBy).toString())));
            }
        } else {
            filteredValueMap.sort(Comparator.comparing((ModifiableValueMap map) -> map.get(OscarConstants.TITLE, String.class)));
        }

        return gson.toJson(filteredValueMap);
    }

    /**
     * Compares each valuemap properties with the request parameters and return boolean
     * @param entry
     * @param childValuemap
     * @return true
     */
    private boolean compareValues(Map.Entry<String, Object> entry, ModifiableValueMap childValuemap) {
        String compareKey = StringUtils.EMPTY;
        if (childValuemap != null) {
            if (StringUtils.equalsIgnoreCase(entry.getKey(), OscarConstants.TITLE)) {
                return OscarUtils.compareString(childValuemap.get(OscarConstants.TITLE, String.class), entry.getValue().toString());
            }
            if (StringUtils.equalsIgnoreCase(entry.getKey(), OscarConstants.YEAR)) {
                return OscarUtils.compareInteger(childValuemap.get(OscarConstants.YEAR, Integer.class), (int) entry.getValue());
            }
            if (StringUtils.equalsIgnoreCase(entry.getKey(), OscarConstants.MIN_YEAR)) {
                return OscarUtils.compareMinValue(childValuemap.get(OscarConstants.YEAR, Integer.class), (int) entry.getValue());
            }
            if (StringUtils.equalsIgnoreCase(entry.getKey(), OscarConstants.MAX_YEAR)) {
                return OscarUtils.compareMaxValue(childValuemap.get(OscarConstants.YEAR, Integer.class), (int) entry.getValue());
            }
            if (StringUtils.equalsIgnoreCase(entry.getKey(), OscarConstants.MIN_AWARDS)) {
                return OscarUtils.compareMinValue(childValuemap.get("awards", Integer.class), (int) entry.getValue());
            }
            if (StringUtils.equalsIgnoreCase(entry.getKey(), OscarConstants.MAX_AWARDS)) {
                return OscarUtils.compareMaxValue(childValuemap.get("awards", Integer.class), (int) entry.getValue());
            }
            if (StringUtils.equalsIgnoreCase(entry.getKey(), OscarConstants.NOMINATIONS)) {
                return OscarUtils.compareInteger(childValuemap.get(OscarConstants.NOMINATIONS, Integer.class), (int) entry.getValue());
            }
            if (StringUtils.equalsIgnoreCase(entry.getKey(), OscarConstants.IS_BEST_PICTURE)) {
                return childValuemap.get(OscarConstants.IS_BEST_PICTURE, Boolean.class) == (Boolean) entry.getValue();
            }
        }

        return false;
    }

    /**
     * Removed unwanted properties from valueMap and adds the valueMap to the list
     * @param filteredList
     * @param childValuemap
     * @param req
     */
    private void addToValueMapSet(List<ModifiableValueMap> filteredList, ModifiableValueMap childValuemap, SlingHttpServletRequest req) {

        if (childValuemap != null) {
            childValuemap.entrySet().removeIf(entry -> StringUtils.startsWith(entry.getKey(), "jcr:") || StringUtils.startsWith(entry.getKey(), "sling:"));
        }

        String limit = req.getParameter(OscarConstants.LIMIT);
        if (limit != null && filteredList.size() < Integer.parseInt(limit)) {
            filteredList.add(childValuemap);
        } else {
            filteredList.add(childValuemap);
        }
    }

    /**
     * Method to get and add the request parameters to a map
     * @param param Parameter to get from the request
     * @param req Sling request
     */
    private void getFilterParams(String param, SlingHttpServletRequest req) {
        String parameter = req.getParameter(param);
        if (parameter != null) {
            requestParamsMap.put(param, Integer.parseInt(parameter));
        }
    }

}

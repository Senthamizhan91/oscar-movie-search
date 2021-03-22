# Oscars Challenge

## Goal:
Your goal will be to provide a search functionality with filtering and sorting based on the test data that is included into the project. 
**Please write JUnit tests to verify your implementation.**
The name of the file with the test data is oscars.json (located in the resources folder).



## Entry point:
For this test task you don't need to have the AEM instance, but knowledge related to Unit testing and testing with wcm.io Testing is required. https://wcm.io/testing/

Please use `de.cyberport.core.servlets.OscarFilmContainerServlet` and corresponding test class `de.cyberport.core.servlets.OscarFilmContainerServletTest` as your starting point for implementation. The servlet has a Javadoc comment with the requirements for the response. If you run the only test method you should be able to see the content of the repository in the console output.

To run the test you can use the following command:

    mvn clean test

As an output in the Terminal you should see all the entries from the test data 

## Test data:
The test data provided in the JSON file `oscars.json` is required for your Unit tests. Please use this data to fill in your repository with the data to work with. 
We already prepared a sample test where you can check how you can load the test data to the in-memory repository: `de.cyberport.core.servlets.OscarFilmContainerServletTest`


## Side information:
Feel free to implement any additional Services/Components/Helpers/Utils/Test classes/etc to fulfil the requirements mentioned in the Javadoc. 
Feel free to add any additional dependencies to the project, for instance for JSON mapping.
Please cover with the Unit tests the requirements mentioned in the Javadoc, also do not forget about the edge cases.
Each of the test entry contains property named 'numberOfReferences'. Let's assume that this value is being updated each hour.
The project structure has been created using the archetypeVersion 23 proposed by Adobe. Non necessary modules has been removed (all, ui.apps, ui.apps.structure, ui.content, it.tests, it.launcher).

**We appreciate TDD and single responsibility principle.**


## Additional questions:
1. The test data contains a big amount of test data. If you would have an ability to put this to the repository, how would you structure it?
2. How can you improve the performance of the functionality that you're going to implement?


## Copied over requirements from the Servlet:

Based on the request parameters, a filtering and sorting should be applied. Default sort order is by title.

For cases when there is no supported request parameter provided in the request, the servlet should return all the films below the requested container.

The Servlet must support following request parameters:
1. title - String. The exact film title
2. year - Integer. The exact year when the film was nominated
3. minYear - Integer. The minimum value of the year for the nominated film
4. maxYear - Integer. The maximum value of the year for the nominated film
5. minAwards - Integer. The minimum value for number of awards
6. maxAwards - Integer. The maximum value for number of awards
7. nominations - Integer. The exact number of nominations
8. isBestPicture - Boolean. True to return only the winners of the best picture nomination.
9. sortBy - Enumeration. Sorting in ascending order, supported values are: *title*, *year*, *awards*, *nominations*. Default value should be *title*.
10. limit - Integer. Maximum amount of result entries in the response.

Please note:
More then 1 filter must be supported.
The resulting JSON must not contain "jcr:primaryType" and "sling:resourceType" properties.
When there will be no results based on the provided filter an empty array should be returned. Please refer to the 3rd example.


## Below you can find some examples based on the data stored in oscars.json in resources directory:


### 1. Request parameters: year=2019&minAwards=4

Sample response:
```
{
  "result": [
    {
      "title": "Parasite",
      "year": "2019",
      "awards": 4,
      "nominations": 6,
      "isBestPicture": true,
      "numberOfReferences": 8855
    }
  ]
}
```


### 2. Request parameters: minYear=2018&minAwards=3&sortBy=nominations&limit=4

Sample response:
```
{
  "result": [
    {
      "title": "Bohemian Rhapsody",
      "year": "2018",
      "awards": 4,
      "nominations": 5,
      "isBestPicture": false,
      "numberOfReferences": 387
    },
    {
      "title": "Green Book",
      "year": "2018",
      "awards": 3,
      "nominations": 5,
      "isBestPicture": true,
      "numberOfReferences": 2945
    },
    {
      "title": "Parasite",
      "year": "2019",
      "awards": 4,
      "nominations": 6,
      "isBestPicture": true,
      "numberOfReferences": 8855
    },
    {
      "title": "Black Panther",
      "year": "2018",
      "awards": 3,
      "nominations": 7,
      "isBestPicture": false,
      "numberOfReferences": 770
    }
  ]
}
```


### 3. Request parameters: title=nonExisting

Sample response:
```
{
  "result": []
}
```
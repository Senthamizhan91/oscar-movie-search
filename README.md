# Oscars Challenge

## Goal:
Your goal will be to provide a search functionality with filtering and sorting based on the test data that is included into the project. 
**Please write JUnit tests to verify your implementation.**
The name of the file with the test data is oscars.json (located in the resources folder).


## Additional questions:
1. The test data contains a big amount of test data. If you would have an ability to put this to the repository, how would you structure it?
2. How can you improve the performance of the functionality that you're going to implement?
 Ans: I would write a JCR-SQL2 query to improve the performance

## Test cases:
I have covered the below test cases as part if my Junit written using wcm.io api
1. When single parameter is passed
2. When multiple parameter is passed
3. When Unsupported parameter is passed
4. When no parameter is passed
5. Testing sorting order
6. When min and max values are swapped/wrongly passed
7. When partially correct or Negative values are passed

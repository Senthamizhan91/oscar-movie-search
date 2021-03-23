# Oscars Challenge

## Goal:
Your goal will be to provide a search functionality with filtering and sorting based on the test data that is included into the project. 
**Please write JUnit tests to verify your implementation.**
The name of the file with the test data is oscars.json (located in the resources folder).

## Solution: 
I have implemented the filtering and sorting of Oscar movies based on the iterative method. This can also be simplified by running JCR SQL2 query on the resource node/path. If time permits I will implement query on a different branch.
Also as described I have written JUnit test to verify my implementation.

## Additional questions:
1. The test data contains a big amount of test data. If you would have an ability to put this to the repository, how would you structure it?
**Ans**: If we would have a additional property called 'Language' to each movie item, then I would store them on repository based on it so that we can avoid more than 512 nodes under single node and the tree is balanced. I would use batch based methods to populate the JCR structure
2. How can you improve the performance of the functionality that you're going to implement?
 **Ans**: I would write a JCR-SQL2 query targetting the resource type to improve the performance

## Test cases:
I have covered the below test cases as part if my Junit written using wcm.io api
1. When single parameter is passed
2. When multiple parameter is passed
3. When Unsupported parameter is passed
4. When no parameter is passed
5. Testing sorting order
6. When min and max values are swapped/wrongly passed
7. When partially correct or Negative values are passed

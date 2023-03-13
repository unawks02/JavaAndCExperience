// --== CS400 Project Three File Header ==--
// Name: Sage Fritz
// Email: sgfritz2@wisc.edu
// Team: blue
// Group: BL
// TA: Surabhi Gupta
// Lecturer: Gary Dahl
// Notes to Grader: I-Hsien and I have slightly different
//       interfaces and  placeholders.
import java.util.NoSuchElementException;
/**
 * The following interface is used to implement AirplaneBackEnd code. It is used for final code and placeholders.
 */
interface AirplaneBackEndInterface{
        /** The following method lays out the general syntax for classes that implement this interface.
         */
        public int findShortestPath(CS400Graph<String>  graph, String start, String destination);
        /** Returns the duration of the flight based on mileage.
         */
        public double getDuration(double distance);
        /** Returns the cost of the flight based on time (in hours)
         */
        public double getCost(double hours);
        /** Returns a string representation of all cities in the graph
         */     
        public String listCities(CS400Graph<String> graph);
}
/**
 * This class represents a completed implementation of Airplane Back End data. 
 * As described in the proposal, it calculates the shortest path for flights.
 * It also calculates flight duration and cost.
 */
public class AirplaneBackEnd implements AirplaneBackEndInterface{
  /**
   * This method calls the Djikstra's Shortest Path algorithm, as implemented by Sage Fritz in their CS400Graph file.
   * It prints out a string, telling the user what the shortest path is.
   * It returns an integer value, representing the mileage of the shortest path.
   * @param graph the graph created by Data Wrangler to use Djikstra's shortest path from
   * @param start the starting city
   * @param destination the ending city
   *
   * @return the mileage of the shortest path or 1,000,000 if no path found
   */
  @Override
  public int findShortestPath(CS400Graph<String> graph, String start, String destination){
          try{
                  //calls CS400Graph Djikstra's algorithm
          String shortestPath = graph.shortestPath(start, destination).toString();
          System.out.println("The shortest flight from " + start + " to " + destination +  " is: " + shortestPath);
          //prints a string telling the user the shortest path, returns mileage
          return graph.getPathCost(start, destination);
          }
          catch(NoSuchElementException ex){
                  if (ex.getMessage().contains("No path found")){ //no path found
                          System.out.println("No flight found from " + start + " to " + destination);
                  }
                  else{
                          System.out.println(ex.getMessage());
                  }
                  return 1000000;
          }
          catch (Exception ex){
                  //handles exceptions thrown by shortestPath
                  System.out.println("Unexpected exception.");
                  System.out.println(ex.getMessage());
                  return 1000000;
          }
   }
  
/**
 * This method estimates the duration of a flight based on mileage, assuming the plane travels 200 mph.
 * @param distance the mileage of the flight 
 */
 @Override
 public double getDuration(double distance){
         //calculates duration in hours 
         //assumes the plane travels 200 miles per hour
         return (distance/200);
 }
 /**
  * This method estimates the cost for this flight based on its duration ($100 per hour).
  * @param hours the duration of the flight in hours
  */
 @Override
 public double getCost(double hours){
         //calculates the cost of the flight in USD
         // assumes the cost for flights is $100 an hour
         return (hours * 100);
 }
 /**
  * This method was added after the proposal document as suggested by KEDAR.
  * It returns a string with a set representing the city names.
  *
  * @param graph the CS400Graph made by Data Wrangler
  */
 public String listCities(CS400Graph<String> graph){
         return (graph.vertices.keySet().toString());
 }
}
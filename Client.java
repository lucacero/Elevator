import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        introPartOne();

        System.out.println("Enter the number of floors:");
        int numFloors = scanner.nextInt();
        while (numFloors <= 0) {
            System.out.println("The number of floors must be greater than 0. Please enter again:");
            numFloors = scanner.nextInt();
        }

        System.out.println("Enter the starting floor of the elevator:");
        int startingFloor = scanner.nextInt();
        while (startingFloor < 0 || startingFloor >= numFloors) {
            System.out.println("Invalid floor. Please enter a floor between 0 and " + (numFloors - 1) + ":");
            startingFloor = scanner.nextInt();
        }

        Elevator elevator = new Elevator(2200, startingFloor); // Elevator with a 2200-pound capacity

        List<Person> waitingPeople = new ArrayList<>();
        int personId = 1;

        if (numFloors < 10) {
            // Fewer than 10 floors: Ask how many passengers on each floor
            for (int i = 0; i < numFloors; i++) {
                System.out.println("Enter the number of people waiting on floor " + i + ":");
                int numPeople = scanner.nextInt();

                for (int j = 0; j < numPeople; j++) {
                    System.out.println("Enter the destination floor for person " + personId + ":");
                    int destinationFloor = scanner.nextInt();

                    while (destinationFloor < 0 || destinationFloor >= numFloors || destinationFloor == i) {
                        System.out.println("Invalid destination. Enter a floor between 0 and " + (numFloors - 1) + ", different from their current floor:");
                        destinationFloor = scanner.nextInt();
                    }

                    waitingPeople.add(new Person(personId++, i, destinationFloor));
                }
            }
        } else {
            // 10+ floors: Ask which floors have passengers
            System.out.println("Enter the floors that have passengers (separate by space, e.g., '1 3 5'):");
            scanner.nextLine(); // Consume the newline character
            String[] floorsWithPassengers = scanner.nextLine().split(" ");

            for (String floor : floorsWithPassengers) {
                int currentFloor = Integer.parseInt(floor);

                if (currentFloor < 0 || currentFloor >= numFloors) {
                    System.out.println("Floor " + currentFloor + " is invalid. Skipping.");
                    continue;
                }

                System.out.println("Enter the number of people waiting on floor " + currentFloor + ":");
                int numPeople = scanner.nextInt();

                for (int j = 0; j < numPeople; j++) {
                    System.out.println("Enter the destination floor for person " + personId + ":");
                    int destinationFloor = scanner.nextInt();

                    while (destinationFloor < 0 || destinationFloor >= numFloors || destinationFloor == currentFloor) {
                        System.out.println("Invalid destination. Enter a floor between 0 and " + (numFloors - 1) + ", different from their current floor:");
                        destinationFloor = scanner.nextInt();
                    }

                    waitingPeople.add(new Person(personId++, currentFloor, destinationFloor));
                }
            }
        }

        introPartTwo();

        String strategy = scanner.next();
        while (!strategy.equalsIgnoreCase("fcfs") &&
                !strategy.equalsIgnoreCase("sstf") &&
                !strategy.equalsIgnoreCase("scan") &&
                !strategy.equalsIgnoreCase("look") &&
                !strategy.equalsIgnoreCase("destination")) {
            System.out.println("Invalid strategy. Please choose from fcfs, sstf, scan, look, or destination:");
            strategy = scanner.next();
        }

        String mostEfficientStrategy = findMostEfficientStrategy(elevator, waitingPeople);

        System.out.println("Executing strategy: " + strategy.toUpperCase());
        elevator.executeStrategy(waitingPeople, strategy);

        provideFeedback(strategy.toLowerCase(), mostEfficientStrategy);

        System.out.println("Simulation completed!");
        scanner.close();
    }

    public static String findMostEfficientStrategy(Elevator elevator, List<Person> waitingPeople) {
        Map<String, Integer> flightsCount = new HashMap<>();
    
        flightsCount.put("fcfs", elevator.simulateStrategy(waitingPeople, "fcfs"));
        flightsCount.put("sstf", elevator.simulateStrategy(waitingPeople, "sstf"));
        flightsCount.put("scan", elevator.simulateStrategy(waitingPeople, "scan"));
        flightsCount.put("look", elevator.simulateStrategy(waitingPeople, "look"));
        flightsCount.put("destination", elevator.simulateStrategy(waitingPeople, "destination"));
    
        String mostEfficient = flightsCount.entrySet()
            .stream()
            .min(Comparator.comparingInt(Map.Entry::getValue))
            .get()
            .getKey();
    
        System.out.println("Flights count for each algorithm:");
        flightsCount.forEach((key, value) -> System.out.println(key.toUpperCase() + ": " + value + " flights"));
    
        return mostEfficient;
    }
    

    public static void provideFeedback(String selectedStrategy, String mostEfficient) {
        if (selectedStrategy.equalsIgnoreCase(mostEfficient)) {
            System.out.println("Good choice! That is the most efficient algorithm for this situation!");
        } else {
            System.out.println("There are more efficient algorithms for this situation.");
            System.out.println("Consider trying the " + mostEfficient.toUpperCase() + " strategy to optimize elevator usage.");
        }
    }


    private static void introPartOne() {
        System.out.println("Welcome to the Elevator Simulation!");
        System.out.println("In this simulation, you will be able to control an elevator and its passengers.");
        System.out.println("You will be asked to enter the number of floors, the starting floor of the elevator,");
        System.out.println("and the number of people waiting on each floor with their destination floors.");
        System.out.println("Let's get started!");
    }

    private static void introPartTwo() {
        System.out.println("The elevator is now ready to operate.");
        System.out.println("You can choose from the following strategies to serve the passengers:");
        System.out.println("1. FCFS (First-Come, First-Served: Serve passengers in the order they arrive.");
        System.out.println("2. SSTF (Shortest Seek Time First): Prioritize passengers closest to the current position.");
        System.out.println("3. SCAN: Move in one direction, picking up and dropping off passengers, then reverse direction.");
        System.out.println("4. LOOK: Similar to SCAN but only travels as far as the last request in each direction before reversing."); 
        System.out.println("5. Destination Dispatch: Group passengers with similar destinations to minimize trips.");
    }
}
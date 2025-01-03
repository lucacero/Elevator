import java.util.*;

public class Elevator {
    private final int startingFloor;
    private int currentFloor;
    private List<Person> passengers;
    private final int maxCapacityWeight; // Maximum weight capacity in pounds
    private final int personWeight = 180; // Assumed weight of each person in pounds
    private int totalStops;
    private int totalFloorsTraveled;

    // Default constructor
    public Elevator(int maxCapacityWeight) {
        this(maxCapacityWeight, 0); // Default starting floor is 0
    }

    // Overloaded constructor
    public Elevator(int maxCapacityWeight, int startingFloor) {
        this.maxCapacityWeight = maxCapacityWeight;
        this.startingFloor = startingFloor;
        this.currentFloor = startingFloor;
        this.passengers = new ArrayList<>();
        this.totalStops = 0;
        this.totalFloorsTraveled = 0;
    }

    public void moveToFloor(int floor) {
        if (currentFloor != floor) { // Only count as a stop if the elevator moves to a different floor
            System.out.println("Elevator moving from floor " + currentFloor + " to floor " + floor);
            totalFloorsTraveled += Math.abs(floor - currentFloor);
            currentFloor = floor;
            totalStops++;
        } else {
            System.out.println("Elevator is already on floor " + floor + ", no additional stop counted.");
        }
    }
    

    public boolean canTakeMorePassengers() {
        int currentWeight = passengers.size() * personWeight;
        return currentWeight + personWeight <= maxCapacityWeight;
    }

    public void pickUpPerson(Person person) {
        if (canTakeMorePassengers()) {
            passengers.add(person);
            System.out.println("Picked up Person " + person.getId() + " at floor " + person.getCurrentFloor());
        } else {
            System.out.println("Cannot pick up Person " + person.getId() + " due to weight limit.");
        }
    }

    public int getCurrentFloor() {
        return this.currentFloor;
    }

    public int getStartingFloor() {
        return this.startingFloor;
    }

    public void dropOffPerson(Person person) {
        passengers.remove(person);
        System.out.println("Dropped off Person " + person.getId() + " at floor " + person.getDestinationFloor());
    }

    public void reset() {
        this.currentFloor = this.startingFloor;
        passengers.clear();
        totalStops = 0;
        totalFloorsTraveled = 0;
    }

    public int simulateStrategy(List<Person> waitingPeople, String strategy) {
        int originalTotalStops = totalStops;
        int originalTotalFloorsTraveled = totalFloorsTraveled;
    
        // Execute the strategy
        executeStrategy(waitingPeople, strategy);
    
        // Capture the number of stops and floors traveled after running the strategy
        int flights = totalStops;
    
        // Restore the original state
        totalStops = originalTotalStops;
        totalFloorsTraveled = originalTotalFloorsTraveled;
    
        return flights;
    }    

    public void rankStrategies(List<Person> waitingPeople) {
        Map<String, int[]> results = new HashMap<>();
        String[] strategies = {"fcfs", "sstf", "scan", "look", "destination"};

        for (String strategy : strategies) {
            executeStrategy(new ArrayList<>(waitingPeople), strategy);
            results.put(strategy, new int[]{totalStops, totalFloorsTraveled});
        }

        results.entrySet().stream()
            .sorted(Comparator.comparingInt(e -> e.getValue()[0]))
            .forEach(entry -> {
                System.out.println("Strategy: " + entry.getKey() +
                        ", Stops: " + entry.getValue()[0] +
                        ", Floors Traveled: " + entry.getValue()[1]);
            });
    }
    

    public void executeStrategy(List<Person> waitingPeople, String strategy) {
        totalStops = 0;
        totalFloorsTraveled = 0;

        switch (strategy.toLowerCase()) {
            case "fcfs":
                fcfs(waitingPeople);
                break;
            case "sstf":
                sstf(waitingPeople);
                break;
            case "scan":
                scan(waitingPeople, true); // Assume moving up initially
                break;
            case "look":
                look(waitingPeople, true); // Assume moving up initially
                break;
            case "destination":
                destinationDispatch(waitingPeople);
                break;
            default:
                System.out.println("Unknown strategy. Defaulting to FCFS.");
                fcfs(waitingPeople);
        }

        System.out.println("Strategy: " + strategy);
        System.out.println("Total stops: " + totalStops);
        System.out.println("Total floors traveled: " + totalFloorsTraveled);
    }

    // Revised FCFS Algorithm
    private void fcfs(List<Person> waitingPeople) {
        reset();
        List<Person> queue = new ArrayList<>(waitingPeople);

        while (!queue.isEmpty()) {
            List<Person> toPickUp = new ArrayList<>();

            // Collect passengers that can be picked up on the current trip
            for (Person person : queue) {
                if (canTakeMorePassengers()) {
                    toPickUp.add(person);
                } else {
                    break; // Stop collecting if weight capacity is reached
                }
            }

            // Pick up all collected passengers
            for (Person person : toPickUp) {
                moveToFloor(person.getCurrentFloor());
                pickUpPerson(person);
            }

            // Drop off all passengers
            for (Person person : toPickUp) {
                moveToFloor(person.getDestinationFloor());
                dropOffPerson(person);
                queue.remove(person);
            }
        }
    }

    // Revised SSTF Algorithm
    private void sstf(List<Person> waitingPeople) {
        reset();
        List<Person> queue = new ArrayList<>(waitingPeople);

        while (!queue.isEmpty()) {
            List<Person> toPickUp = new ArrayList<>();
            while (toPickUp.size() < maxCapacityWeight / personWeight) {
                Person closest = findClosestPerson(queue);
                if (closest == null) break;

                toPickUp.add(closest);
                queue.remove(closest);
            }

            for (Person person : toPickUp) {
                moveToFloor(person.getCurrentFloor());
                pickUpPerson(person);
            }

            for (Person person : toPickUp) {
                moveToFloor(person.getDestinationFloor());
                dropOffPerson(person);
            }
        }
    }

    private void scan(List<Person> waitingPeople, boolean initialDirection) {
        reset();
        List<Person> queue = new ArrayList<>(waitingPeople);
        boolean[] movingUp = {initialDirection}; // Use a wrapper to modify the direction
    
        while (!queue.isEmpty()) {
            List<Person> toPickUp = new ArrayList<>();
    
            for (Person person : queue) {
                if ((movingUp[0] && person.getCurrentFloor() >= currentFloor)
                        || (!movingUp[0] && person.getCurrentFloor() <= currentFloor)) {
                    if (canTakeMorePassengers()) {
                        toPickUp.add(person);
                    }
                }
            }
    
            for (Person person : toPickUp) {
                moveToFloor(person.getCurrentFloor());
                pickUpPerson(person);
                queue.remove(person);
            }
    
            for (Person person : toPickUp) {
                moveToFloor(person.getDestinationFloor());
                dropOffPerson(person);
            }
    
            // Change direction if necessary
            boolean hasMatchingFloors = queue.stream().anyMatch(p -> (movingUp[0] && p.getCurrentFloor() >= currentFloor)
                    || (!movingUp[0] && p.getCurrentFloor() <= currentFloor));
            if (!hasMatchingFloors) {
                movingUp[0] = !movingUp[0]; // Reverse direction
            }
        }
    }
    

    private void look(List<Person> waitingPeople, boolean initialDirection) {
        reset();
        List<Person> queue = new ArrayList<>(waitingPeople);
        boolean[] movingUp = {initialDirection}; // Use a wrapper to modify the direction
    
        while (!queue.isEmpty()) {
            List<Person> toPickUp = new ArrayList<>();
    
            for (Person person : queue) {
                if ((movingUp[0] && person.getCurrentFloor() >= currentFloor)
                        || (!movingUp[0] && person.getCurrentFloor() <= currentFloor)) {
                    if (canTakeMorePassengers()) {
                        toPickUp.add(person);
                    }
                }
            }
    
            for (Person person : toPickUp) {
                moveToFloor(person.getCurrentFloor());
                pickUpPerson(person);
                queue.remove(person);
            }
    
            for (Person person : toPickUp) {
                moveToFloor(person.getDestinationFloor());
                dropOffPerson(person);
            }
    
            if (toPickUp.isEmpty()) {
                movingUp[0] = !movingUp[0]; // Reverse direction
            }
        }
    }    

    // Revised Destination Dispatch Algorithm
    private void destinationDispatch(List<Person> waitingPeople) {
        reset();
        Map<Integer, List<Person>> destinations = new HashMap<>();

        for (Person person : waitingPeople) {
            destinations.computeIfAbsent(person.getDestinationFloor(), k -> new ArrayList<>()).add(person);
        }

        List<Integer> floors = new ArrayList<>(destinations.keySet());
        floors.sort(Comparator.naturalOrder());

        for (Integer floor : floors) {
            List<Person> group = destinations.get(floor);

            for (Person person : group) {
                moveToFloor(person.getCurrentFloor());
                pickUpPerson(person);
            }

            for (Person person : group) {
                moveToFloor(person.getDestinationFloor());
                dropOffPerson(person);
            }
        }
    }

    private Person findClosestPerson(List<Person> people) {
        Person closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Person person : people) {
            int distance = Math.abs(person.getCurrentFloor() - currentFloor);
            if (distance < minDistance) {
                minDistance = distance;
                closest = person;
            }
        }

        return closest;
    }
}

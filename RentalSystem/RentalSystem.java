package RentalSystem;

import java.util.*;

public class RentalSystem
{
    public static void main(String[] args)
    {
        RentalController controller = RentalController.getInstance();
        controller.setVehicleStrategy(new DefaultSearchVehicleStrategy());
        controller.setVehiclePricingStrategy(new DefaultVehiclePricingStrategy());

        controller.addVehicle(VehicleFactory.createVehicle("car", "CH01", "red"));
        controller.addVehicle(VehicleFactory.createVehicle("car", "CH02", "green")); 
        controller.addVehicle(VehicleFactory.createVehicle("bike", "CH03", "red"));

        User user1 = new User("User1");
        User user2 = new User("User2");

        Reservation res1 = controller.bookVehicle(user1, "red", 3, 3, 10);
        controller.releaseVehicle(res1, 6);
        Reservation res2 = controller.bookVehicle(user2, "red", 3, 3, 10);
        controller.releaseVehicle(res2, 15);
    }
}

class RentalController
{
    List<Vehicle> vehicles;
    ISearchVehicleStrategy vehicleSearchStrategy;
    IVehiclePricingStrategy vehiclePricingStrategy;
    ReservationManager reservationManager;

    static RentalController controllerInstance;

    private RentalController()
    {
        this.vehicles = new ArrayList<>();
        this.reservationManager = new ReservationManager();
    }

    public static RentalController getInstance()
    {
        if(RentalController.controllerInstance==null) RentalController.controllerInstance = new RentalController();
        return RentalController.controllerInstance;
    }

    public void setVehicleStrategy(ISearchVehicleStrategy vehicleSearchStrategy)
    {
        this.vehicleSearchStrategy = vehicleSearchStrategy;
    }

    public void setVehiclePricingStrategy(IVehiclePricingStrategy vehiclePricingStrategy)
    {
        this.vehiclePricingStrategy = vehiclePricingStrategy;
    }

    public void addVehicle(Vehicle vehicle)
    {
        vehicles.add(vehicle);
    }

    public Vehicle getVehicle(String color, int seats)
    {
        List<Vehicle> availableVehicles = vehicleSearchStrategy.searchVehicle(vehicles, color, seats);
        if(availableVehicles.size() == 0)
        {
            throw new RuntimeException("Sorry, no vehicle is available for now");
        }
        return availableVehicles.get(0);
    }

    public Vehicle getVehicle(String color, int seats, int startDate, int endDate)
    {
        List<Vehicle> availableVehicles = vehicleSearchStrategy.searchVehicle(vehicles, color, seats);
        for(Vehicle vehicle:availableVehicles)
        {
            List<Reservation> reservations = reservationManager.getReservationsForVehicle(vehicle);
            if(reservations.size()==0) return vehicle;
            boolean validVehicle = true;
            for(Reservation res:reservations)
            {
                if(res.state!=ReservationState.ACTIVE) continue;
                if(startDate>res.endDate || endDate<res.startDate) continue;
                validVehicle = false;
                break;
            }
            if(validVehicle) return vehicle;
        }
        throw new RuntimeException("Sorry, no vehicle is available for now");
    }

    public Reservation bookVehicle(User user, String color, int seats, int startDate, int endDate)
    {
        Vehicle vehicle = getVehicle(color, seats, startDate, endDate);
        Reservation res = new Reservation(user, vehicle, startDate, endDate);
        reservationManager.addReservation(res);
        vehicle.state = VehicleState.BOOKED;
        res.state = ReservationState.ACTIVE;
        System.out.println(user.name + " has booked Vehicle: " + vehicle.regNumber);
        return res;
    }

    public void releaseVehicle(Reservation reservation, int endDate)
    {
        if(reservation.state!=ReservationState.ACTIVE)
        {
            throw new RuntimeException("This reservation is not active");
        }
        endDate = Math.max(endDate, reservation.endDate);
        int amountPayable = vehiclePricingStrategy.findAmountPayable(reservation.vehicle, endDate-reservation.startDate);
        System.out.println("User has to pay " + amountPayable); //ensure payment completed
        reservation.vehicle.state = VehicleState.FREE;
        reservation.state = ReservationState.COMPLETED;
    }
}

class ReservationManager
{
    List<Reservation> reservations;

    public ReservationManager()
    {
        reservations = new ArrayList<>();
    }

    public void addReservation(Reservation res)
    {
        this.reservations.add(res);
    }

    public List<Reservation> getReservationsForVehicle(Vehicle vehicle)
    {
        List<Reservation> resArr = new ArrayList<>();
        for(Reservation res:reservations)
        {
            if(res.vehicle.regNumber.equals(vehicle.regNumber))
            {
                resArr.add(res);
            }
        }
        return resArr;
    }
}

enum ReservationState
{
    ACTIVE, 
    COMPLETED, 
    REJECTED
}

class Reservation
{
    User user;
    Vehicle vehicle;
    int startDate;
    int endDate;
    ReservationState state;

    public Reservation(User user, Vehicle vehicle, int startDate, int endDate)
    {
        this.user = user;
        this.vehicle = vehicle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.state = ReservationState.ACTIVE;
    }
}

interface IVehiclePricingStrategy 
{
    public int findAmountPayable(Vehicle vehicle, int days);
}

class DefaultVehiclePricingStrategy implements IVehiclePricingStrategy
{
    public int findAmountPayable(Vehicle vehicle, int days) {
        switch (vehicle.seats) {
            case 4:
                return 10*days;
            case 2:
                return 5*days; 
            default:
                return 5*days;
        }
    }
}

interface ISearchVehicleStrategy
{
    public List<Vehicle> searchVehicle(List<Vehicle> vehicles, String color, int seats);
}

class DefaultSearchVehicleStrategy implements ISearchVehicleStrategy
{
    public List<Vehicle> searchVehicle(List<Vehicle> vehicles, String color, int seats)
    {
        List<Vehicle> availableVehicles = new ArrayList<>();
        for(Vehicle vehicle:vehicles)
        {
            if(vehicle.state!=VehicleState.FREE) continue;
            if(vehicle.color.equals(color) && vehicle.seats>=seats) availableVehicles.add(vehicle);
        }
        return availableVehicles;
    }
}

class User
{
    String id;
    String name;

    public User(String name)
    {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }
}

class VehicleFactory
{
    public static Vehicle createVehicle(String vehicleType, String regNumber, String color)
    {
        switch(vehicleType)
        {
            case "car": return new Car(regNumber, color);
            case "bike": return new Bike(regNumber, color);
            default: return new Bike(regNumber, color);
        }
    }
}


abstract class Vehicle
{
    String regNumber;
    String color;
    int seats;
    VehicleState state;

    public Vehicle(String regNumber, String color, int seats)
    {
        this.regNumber = regNumber;
        this.color = color;
        this.seats = seats;
        this.state = VehicleState.FREE;
    }
}

class Car extends Vehicle
{
    public Car(String regNumber, String color)
    {
        super(regNumber, color, 4);
    }
}

class Bike extends Vehicle
{
    public Bike(String regNumber, String color)
    {
        super(regNumber, color, 2);
    }
}

enum VehicleState
{
    FREE, 
    BOOKED, 
    MAINTENANCE
}
import java.util.*;

public class PKLot
{
    public static void main(String[] args)
    {
        ParkingLot lot = new ParkingLot.ParkingLotBuilder()
                        .numCars(3)
                        .numBikes(1)
                        .build();

        // lot.addParkingSlot(new ParkingSlot(1, "car", new DefaultParkingFeesStrategy()));
        // lot.addParkingSlot(new ParkingSlot(2, "car", new DefaultParkingFeesStrategy()));
        // lot.addParkingSlot(new ParkingSlot(3, "bike", new DefaultParkingFeesStrategy()));

        Vehicle vehicle1 = VehicleFactory.createVehicle("CH01", "car");
        Vehicle vehicle2 = VehicleFactory.createVehicle("CH02", "car");
        Vehicle vehicle3 = VehicleFactory.createVehicle("CH03", "bike");

        lot.parkVehicle(vehicle1);
        lot.parkVehicle(vehicle2);
        lot.parkVehicle(vehicle3);

        lot.unParkVehicle(vehicle3);
        lot.unParkVehicle(vehicle2);

        lot.parkVehicle(VehicleFactory.createVehicle("CH04", "bike"));
        // lot.parkVehicle(vehicle3);
    }
}


class ParkingLot 
{
    List<ParkingSlot> slots;

    private ParkingLot(ParkingLotBuilder builder)
    {
        slots = new ArrayList<>();
        for(int i = 0;i<builder.numCars;i++)
        {
            slots.add(new ParkingSlot(i, "car", new DefaultParkingFeesStrategy()));
        }

        for(int i = 1;i<=builder.numBikes;i++)
        {
            slots.add(new ParkingSlot(i*6+1, "bike", new DefaultParkingFeesStrategy()));
        }
    }

    public static class ParkingLotBuilder
    {
        private int numCars;
        private int numBikes;
        
        public ParkingLotBuilder numCars(int numCars)
        {
            this.numCars = numCars;
            return this;
        }

        public ParkingLotBuilder numBikes(int numBikes)
        {
            this.numBikes = numBikes;
            return this;
        }

        public ParkingLot build()
        {
            return new ParkingLot(this);
        }
    }

    public void addParkingSlot(ParkingSlot slot)
    {
        this.slots.add(slot);
    }

    private ParkingSlot findEmptyParkingSlot(Vehicle vehicle)
    {
        for(ParkingSlot slot:slots)
        {
            if(slot.state!=ParkingSlotState.EMPTY) continue;
            if(slot.vehicleType.equals(vehicle.vehicleType))
            {
                return slot;
            } 
        }
        return null;
    }

    public void parkVehicle(Vehicle vehicle)
    {
        ParkingSlot slot = findEmptyParkingSlot(vehicle);
        if(slot == null)
        {
            throw new RuntimeException("ParkingLot does not have more capacity for this type of vehicle.");
        }
        slot.parkVehicle(vehicle, 20);
        System.out.println("Vehicle " + vehicle.licenseNumber + " was parked in slot " + slot.slotID);
    }

    private ParkingSlot findParkingSlotForVehicle(Vehicle vehicle)
    {
        for(ParkingSlot slot:slots)
        {
            if(slot.vehicle==null) continue;
            if(slot.vehicle.licenseNumber.equals(vehicle.licenseNumber))
            {
                return slot;
            } 
        }
        return null;
    }

    public void unParkVehicle(Vehicle vehicle)
    {
        ParkingSlot slot = findParkingSlotForVehicle(vehicle);
        if(slot == null)
        {
            throw new RuntimeException("This vehicle has not been parked in our parking lot");
        }

        int parkingFees = slot.getSlotPrice(50);
        System.out.println("User has paid Rs " + parkingFees); 
        slot.unparkVehicle(); //only once user pays, free the slot
        System.out.println("Vehicle " + vehicle.licenseNumber + " was unparked from slot " + slot.slotID);
    }
}

class VehicleFactory
{
    public static Vehicle createVehicle(String licenseNumber, String vehicleType)
    {
        switch(vehicleType)
        {
            case "car":
                return new Car(licenseNumber);
            case "bike":
                return new Bike(licenseNumber);
            default:
                return new Car(licenseNumber);
        }
    }
}


abstract class Vehicle
{
    String licenseNumber;
    String vehicleType;

    public Vehicle(String licenseNumber, String vehicleType)
    {
        this.licenseNumber = licenseNumber;
        this.vehicleType = vehicleType;
    }
}

class Car extends Vehicle
{
    public Car(String licenseNumber)
    {
        super(licenseNumber, "car");
    }
}

class Bike extends Vehicle
{
    public Bike(String licenseNumber)
    {
        super(licenseNumber, "bike");
    }
}

enum ParkingSlotState
{
    EMPTY, 
    OCCUPIED
}


class ParkingSlot
{
    int slotID;
    int startTime;
    int endTime;
    String vehicleType;
    Vehicle vehicle;
    IParkingFeesStrategy parkingFeesStrategy;
    ParkingSlotState state;

    public ParkingSlot(int slotID, String vehicleType, IParkingFeesStrategy parkingFeesStrategy)
    {
        this.slotID = slotID;
        this.vehicleType = vehicleType;
        this.parkingFeesStrategy = parkingFeesStrategy;
        this.startTime = 0;
        this.endTime = 0;
        this.state = ParkingSlotState.EMPTY;
    }

    public void parkVehicle(Vehicle vehicle, int startTime)
    {
        this.vehicle = vehicle;
        this.startTime = startTime;
        this.state = ParkingSlotState.OCCUPIED;
    }

    public void unparkVehicle()
    {
        this.vehicle = null;
        this.startTime = 0;
        this.endTime = 0;
        this.state = ParkingSlotState.EMPTY;
    }

    public int getSlotPrice(int endTime)
    {
        this.endTime = endTime;
        return parkingFeesStrategy.getParkingFees(this);
    }
}

interface IParkingFeesStrategy
{
    public int getParkingFees(ParkingSlot slot);
}

class DefaultParkingFeesStrategy implements IParkingFeesStrategy
{
    public int getParkingFees(ParkingSlot slot)
    {
        if(slot.endTime==0)
        {
            throw new RuntimeException("Vehicle has yet to exit!");
        }
        int duration = slot.endTime - slot.startTime;
        switch(slot.vehicleType)
        {
            case "car":
                return duration*30;
            case "bike":
                return duration*10;
            default:
                return duration*10;
        }
    }
}
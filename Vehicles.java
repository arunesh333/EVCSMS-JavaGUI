public class Vehicles {
    // Vehicle & customer attributes
    private String regNo, vehType, model, chargingType, slotNumber, name, mobile, cusType;
    private double batteryCapacity, currentSoc, maxPower;

    // Constructor to initialize all data
    public Vehicles(String regNo, String vehType, String model, String chargingType,
                    double batteryCapacity, double currentSoc, double maxPower,
                    String name, String mobile, String cusType, String slotNumber) {
        this.regNo = regNo;
        this.vehType = vehType;
        this.model = model;
        this.chargingType = chargingType;
        this.batteryCapacity = batteryCapacity;
        this.currentSoc = currentSoc;
        this.maxPower = maxPower;
        this.name = name;
        this.mobile = mobile;
        this.cusType = cusType;
        this.slotNumber = slotNumber;
    }

    // Get vehicle registration number
    String getRegNo() {
        return this.regNo;
    }

    // Get slot number
    String getslotNumber() {
        return this.slotNumber;
    }

    // Convert object data into CSV line
    public String toCSV() {
        return regNo + "," + vehType + "," + model + "," + chargingType + "," + batteryCapacity +
               "," + currentSoc + "," + maxPower + "," + name + "," + mobile + "," + cusType + "," + slotNumber;
    }
}

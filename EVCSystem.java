// Importing essential AWT and Swing components for GUI
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import javax.swing.*;

public class EVCSystem {

    // Frames for main and billing windows
    private JFrame frame1, frame2;

    // Labels for headers and input prompts
    private JLabel h1, h2, h3, h4, h5;
    private JLabel l1, l2, l3, l4, l5, l6, l7, l9, l10, l11, l12;
    private JLabel l13, l14, l15, l16, l17, l18, l19, l20, l21, l22, l23;

    // UI Elements for branding
    private JLabel logoLabel, station1Label, station2Label;

    // Input fields for vehicle and billing data
    private JTextField regNo1Field, regNo2Field, vechModelField, Batt_CptyField, current_socField, max_battpowerField;
    private JTextField nameField, MobileField, unitConsumeField, parkingField, costField, costperunitField, discountField, totalAmountField;

    // Dropdown selectors for vehicle type, charging type, customer type, and payment method
    private JComboBox<String> vechTypeBox, Charging_typeBox, cus_typeBox, PaymentBox;

    // Slot status buttons (representing charging slots)
    private JButton box1, box2, box3, box4, box5, box6, box7, box8;

    // Action buttons for slot booking, billing, reset, and payment
    private JButton slotBookButton, billinButton, reset1Button, fetchButton, billPaidButton, reset2Button;

    // Icons and state trackers
    private ImageIcon logoIcon;
    private JButton selectedSlotButton = null;
    private Vehicles veh1;  // Object to hold current vehicle being serviced

    public static void main(String[] args) {
        // Launch the application
        EVCSystem EVCSMS = new EVCSystem();
        EVCSMS.mainWindow();
    }

    // Resets vehicle detail input fields
    private void reset1() {
        regNo1Field.setText("");
        vechModelField.setText("");
        Batt_CptyField.setText("");
        current_socField.setText("");
        max_battpowerField.setText("");
        nameField.setText("");
        MobileField.setText("");
        vechTypeBox.setSelectedItem("");
        Charging_typeBox.setSelectedItem("");
        cus_typeBox.setSelectedItem("");
    }

    // Resets billing window input fields
    private void reset2() {
        regNo2Field.setText("");
        unitConsumeField.setText("");
        costperunitField.setText("");
        parkingField.setText("0.0");
        costField.setText("");
        discountField.setText("");
        totalAmountField.setText("");
        PaymentBox.setSelectedItem("");
        l22.setText("Rs.");
    }

    // Collects vehicle and customer input, creates vehicle object, and saves to CSV
    private void getVehDetails() {
        // Grab input values
        String regNoString = regNo1Field.getText();
        String vechtypeString = vechTypeBox.getSelectedItem().toString();
        String vechModelString = vechModelField.getText();
        String chargeTypeString = Charging_typeBox.getSelectedItem().toString();

        // Mandatory field validation
        if (regNoString.isEmpty() || vechModelString.isEmpty() || nameField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame1, "Please fill all required fields");
            return;
        }

        // Parse numeric values, show error if invalid
        double battCpty = 0.0, currentSoc = 0.0, maxBattPower = 0.0;
        try {
            battCpty = Double.parseDouble(Batt_CptyField.getText());
            currentSoc = Double.parseDouble(current_socField.getText());
            maxBattPower = Double.parseDouble(max_battpowerField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame1, "Battery/SOC/Power values must be numeric!");
            return;
        }

        // Validate mobile number
        String customerName = nameField.getText();
        String Mobile =  MobileField.getText();
        if (!Mobile.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(frame1, "Please enter a valid 10-digit mobile number!");
            return;
        }

        // Check which slot was selected
        String slotNumber = "None";
        if (selectedSlotButton != null) {
            slotNumber = selectedSlotButton.getText().replaceAll("\\<.*?\\>", "").trim();
        }

        // Collect customer type
        String customerType = cus_typeBox.getSelectedItem().toString();

        // Create vehicle object and persist data
        veh1 = new Vehicles(regNoString, vechtypeString, vechModelString, chargeTypeString,
                            battCpty, currentSoc, maxBattPower, customerName, Mobile, customerType, slotNumber);
        FileManager.saveToCSV("Current_data.csv", veh1.toCSV());

        JOptionPane.showMessageDialog(frame1, "Slot Booked! Data Saved");
        reset1(); // Clear form
    }

    // Generates billing details based on registration number
    private void getbillDetials() {
        String VehRegNoString = regNo2Field.getText();
        String Dataline1 = new FileManager().getFromCSV("Current_data.csv", VehRegNoString);

        if (Dataline1 == null) return; // Error handled in FileManager

        // Extract fields from CSV
        String data1[] = Dataline1.split(",");
        String regNo = data1[0], type = data1[1], chargingType = data1[3], cusType = data1[9];
        double batteryCapacity = Double.parseDouble(data1[4]);
        double currentSoc = Double.parseDouble(data1[5]);
        double maxPower = Double.parseDouble(data1[6]);

        // Calculate energy consumed and charging time
        double unitsConsumed = batteryCapacity * ((100 - currentSoc) / 100.0);
        double chargeTime = (unitsConsumed / maxPower) * 60;
        int chargingTime = (int) Math.ceil(chargeTime);

        // Apply discount based on customer type
        double discount = switch (cusType) {
            case "Commerical Drivers" -> 0.03;
            case "Service Vehicles" -> 0.06;
            default -> 0.0;
        };

        // Cost per unit based on vehicle and charger type
        double costperunit = switch (type) {
            case "Two Wheeler" -> 2.5;
            case "Three Wheeler" -> 3.0;
            case "Four Wheeler" -> 4.5;
            case "Bus" -> 5.5;
            case "Vans" -> 6.0;
            default -> 0.0;
        };
        costperunit += switch (chargingType) {
            case "AC slow" -> 4.0;
            case "AC Fast" -> 5.0;
            case "DC Fast" -> 6.0;
            case "DC Ultra Fast" -> 7.0;
            default -> 0.0;
        };

        // Final cost calculation
        double totalCost = costperunit * unitsConsumed;
        double parkingTime = Double.parseDouble(parkingField.getText());
        double parkingCost = parkingTime * 20;
        double totalAmount = totalCost - (totalCost * discount) + parkingCost;

        // Show all values on screen
        unitConsumeField.setText(String.valueOf(unitsConsumed));
        costperunitField.setText(String.valueOf(costperunit));
        costField.setText(String.valueOf(totalCost));
        discountField.setText(String.valueOf(discount * 100) + '%');
        totalAmountField.setText(String.valueOf(totalAmount));
        l22.setText("Rs. " + totalAmount);
        l23.setText("Charging Time: " + chargingTime + "Mins");
    }

    // Changes slot button status (color, label) on selection/deselection
    private void slotStatus(JButton clickedBox) {
        if (selectedSlotButton != null && selectedSlotButton != clickedBox) {
            selectedSlotButton.setBackground(Color.GREEN);
            selectedSlotButton.setText(selectedSlotButton.getText().replace("Occupied", "Available"));
        }

        if (clickedBox.getBackground().equals(Color.GREEN)) {
            clickedBox.setBackground(Color.RED);
            clickedBox.setText(clickedBox.getText().replace("Available", "Occupied"));
            selectedSlotButton = clickedBox;
        } else if (clickedBox.getBackground().equals(Color.RED)) {
            clickedBox.setEnabled(false);
            clickedBox.setBackground(Color.GREEN);
            clickedBox.setText(clickedBox.getText().replace("Occupied", "Available"));
            selectedSlotButton = null;
        }
    }

    // Records payment, saves billing report, and frees up the slot
    void billPaid() {
        if (veh1 == null) {
            JOptionPane.showMessageDialog(null, "Please fetch vehicle details before making payment.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double unitConsumed = Double.parseDouble(unitConsumeField.getText().trim());
            double costPerUnit = Double.parseDouble(costperunitField.getText().trim());
            double totalCost = Double.parseDouble(costField.getText().trim());
            double discount = Double.parseDouble(discountField.getText().replace("%", "")) / 100.0;
            double parkingCost = Double.parseDouble(parkingField.getText().trim());
            double finalAmount = Double.parseDouble(totalAmountField.getText().trim());
            int chargingTime = Integer.parseInt(l23.getText().replaceAll("\\D+", ""));

            FileManager.saveReportToCSV("Report_data.csv", veh1, unitConsumed, costPerUnit, totalCost, discount, parkingCost, finalAmount, chargingTime);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid numeric input in billing fields!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Free the charging slot after payment
        if (selectedSlotButton != null) {
            selectedSlotButton.setBackground(Color.GREEN);
            selectedSlotButton.setText("<html><center>" + veh1.getslotNumber().replaceAll("\\D+", "") + "<br>Available</center></html>");
            selectedSlotButton.setEnabled(true);
            selectedSlotButton = null;
        }

        FileManager.removeLineFromCSV("Current_data.csv", veh1.getRegNo());
        JOptionPane.showMessageDialog(null, "Payment recorded and vehicle removed from active list.");
        reset2();
    }

    private void mainWindow() 
    {
        frame1 = new JFrame("EV Charging Managing System");
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame1.setSize(700, 720);

        logoIcon = new ImageIcon("logo.png");
        Image logoImg = logoIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        logoLabel = new JLabel(new ImageIcon(logoImg));

        h1 = new JLabel("Vehicle Detials");
        h2 = new JLabel("Customer Details") ;
        h3 = new JLabel("Slot Status");
        h4 = new JLabel("CHARGING TIME");

        l1 = new JLabel("Vehicle Reg. No.");
        l2 = new JLabel("Vehicle Type");
        l3 = new JLabel("Brand /Model");
        l4 = new JLabel("Charging Type");
        l5 = new JLabel("Battery Capacity");
        l6 = new JLabel("Current SOC(%)");
        l7 = new JLabel("Max. Battery Power");

        l9 = new JLabel("Name");
        l10 = new JLabel("Mobile Number");
        l11 = new JLabel("Customer Type");

        l12 = new JLabel("00 : 00 Hours");

        regNo1Field = new  JTextField();
        vechTypeBox = new JComboBox<>(new String[]
            {"Two Wheeler","Three Wheeler","Four Wheeler","Bus","Vans"});
        vechModelField = new  JTextField();
        Charging_typeBox = new JComboBox<>(new String[]
            {"AC slow","AC Fast", "DC Fast", "DC Ultra Fast"});
        Batt_CptyField = new  JTextField();
        current_socField = new  JTextField();
        max_battpowerField = new  JTextField();


        nameField = new JTextField();
        MobileField = new JTextField();
        cus_typeBox = new JComboBox<>(new String[]        
            {"Private Individual","Commerical Drivers", "Service Vehicles"});

        box1 = new JButton("<html> <center> 1 <br> Available </center> </html>");
        box1.setBackground(Color.green);
        box1.setFont(new Font("Arial",Font.BOLD, 12));
        box2 = new JButton("<html> <center> 2 <br> Available </center> </html>"); 
        box2.setBackground(Color.green);
        box2.setFont(new Font("Arial",Font.BOLD, 12));
        box3 = new JButton("<html> <center> 3 <br> Available </center> </html>");
        box3.setBackground(Color.green);
        box3.setFont(new Font("Arial",Font.BOLD, 12));
        box4 = new JButton("<html> <center> 4 <br> Available </center> </html>");
        box4.setBackground(Color.green);
        box4.setFont(new Font("Arial",Font.BOLD, 12));
        box5 = new JButton("<html> <center> 5 <br> Available </center> </html>");
        box5.setBackground(Color.green);
        box5.setFont(new Font("Arial",Font.BOLD, 12));
        box6 = new JButton("<html> <center> 6 <br> Available </center> </html>");
        box6.setBackground(Color.green);
        box6.setFont(new Font("Arial",Font.BOLD, 12));
        box7 = new JButton("<html> <center> 7 <br> Available </center> </html>");
        box7.setBackground(Color.green);
        box7.setFont(new Font("Arial",Font.BOLD, 12));
        box8 = new JButton("<html> <center> 8 <br> Available </center> </html>");
        box8.setBackground(Color.green);
        box8.setFont(new Font("Arial",Font.BOLD, 12));

        slotBookButton = new JButton("Confirm the Slot");
        billinButton = new JButton("Generate Bill");
        reset1Button = new JButton("Reset");

        station1Label = new JLabel("EV Charging Station");
        station2Label = new JLabel("Billing System");
        station1Label.setFont(new Font("Arial", Font.BOLD, 20));
        station2Label.setFont(new Font("Arial", Font.BOLD, 20));

        h1.setBounds(25, 140, 150, 20);
        l1.setBounds(50, 170, 100, 30);
        regNo1Field.setBounds(170, 170, 150, 30);
        l2.setBounds(50, 215, 100, 30);
        vechTypeBox.setBounds(170, 215, 150, 30);
        l3.setBounds(50, 260, 100, 30);
        vechModelField.setBounds(170, 260, 150, 30);
        l4.setBounds(50, 305, 100, 30);
        Charging_typeBox.setBounds(170, 305, 150, 30);
        l5.setBounds(340, 170, 150, 30);
        Batt_CptyField.setBounds(510, 170, 150, 30);
        l6.setBounds(340, 215, 150, 30);
        current_socField.setBounds(510, 215, 150, 30);
        l7.setBounds(340, 260, 150, 30);
        max_battpowerField.setBounds(510, 260, 150, 30);

        h2.setBounds(25, 355, 150, 20);
        l9.setBounds(50, 385, 100, 30);
        nameField.setBounds(170, 385, 150, 30);
        l10.setBounds(50, 430, 100, 30);
        MobileField.setBounds(170, 430, 150, 30);
        l11.setBounds(340, 385, 150, 30);
        cus_typeBox.setBounds(510, 385, 150, 30);

        h3.setBounds(25, 480, 150, 20);
        box1.setBounds(50, 510, 90, 50);
        box2.setBounds(150, 510, 90, 50);
        box3.setBounds(250, 510, 90, 50);
        box4.setBounds(350, 510, 90, 50);
        box5.setBounds(50, 570, 90, 50);
        box6.setBounds(150, 570, 90, 50);
        box7.setBounds(250, 570, 90, 50);
        box8.setBounds(350, 570, 90, 50);

        slotBookButton.setBounds(500, 510, 150,30);
        billinButton.setBounds(500, 550, 150,30);
        reset1Button.setBounds(500, 590, 150,30);
        h4.setBounds(115, 650, 100, 30);
        l12.setBounds(235, 650, 100, 30);

        logoLabel.setBounds(150, 20, 160, 100);
        station1Label.setBounds(350, 40, 300, 30);
        station2Label.setBounds(350, 70, 200, 30);

        frame1.add(h1);
        frame1.add(l1);
        frame1.add(regNo1Field);
        frame1.add(l2);
        frame1.add(vechTypeBox);
        frame1.add(l3);
        frame1.add(vechModelField);
        frame1.add(l4);
        frame1.add(Charging_typeBox);
        frame1.add(l5);
        frame1.add(Batt_CptyField);
        frame1.add(l6);
        frame1.add(current_socField);
        frame1.add(l7);
        frame1.add(max_battpowerField);

        frame1.add(h2);
        frame1.add(l9);
        frame1.add(nameField);
        frame1.add(l10);
        frame1.add(MobileField);
        frame1.add(l11);
        frame1.add(cus_typeBox);

        frame1.add(h3);
        frame1.add(box1);
        frame1.add(box2);
        frame1.add(box3);
        frame1.add(box4);
        frame1.add(box5);
        frame1.add(box6);
        frame1.add(box7);
        frame1.add(box8);

        frame1.add(slotBookButton);
        frame1.add(billinButton);
        frame1.add(reset1Button);

        frame1.add(logoLabel);
        frame1.add(station1Label);
        frame1.add(station2Label);

        billinButton.addActionListener(e -> billingWindow());

        box1.addActionListener(e-> slotStatus(box1));
        box2.addActionListener(e-> slotStatus(box2));
        box3.addActionListener(e-> slotStatus(box3));
        box4.addActionListener(e-> slotStatus(box4));
        box5.addActionListener(e-> slotStatus(box5));
        box6.addActionListener(e-> slotStatus(box6));
        box7.addActionListener(e-> slotStatus(box7));
        box8.addActionListener(e-> slotStatus(box8));
        reset1Button.addActionListener(e-> reset1());
        slotBookButton.addActionListener(e-> getVehDetails());
        frame1.setLayout(null); 
        frame1.setVisible(true);
    }

    private void billingWindow()
    {
        frame2 = new JFrame("EV Charging Managing System");
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.setSize(700, 720);
        frame2.setLayout(null); 
        frame2.setVisible(true);

        logoIcon = new ImageIcon("logo.png");
        Image logoImg = logoIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        logoLabel = new JLabel(new ImageIcon(logoImg));

        station1Label = new JLabel("EV Charging Station");
        station2Label = new JLabel("Billing System");
        station1Label.setFont(new Font("Arial", Font.BOLD, 20));
        station2Label.setFont(new Font("Arial", Font.BOLD, 20));

        h5 = new JLabel("Billing");
        l13 = new JLabel("Vehicle Reg. No.");
        l14 = new JLabel("Unit Consumed");
        l15 = new JLabel("Cost per Unit");
        l16 = new JLabel("Parking Hours");
        l17 = new JLabel("Payment Method");
        l18 = new JLabel("Total Cost");
        l19 = new JLabel("Discount (If Appl.)");
        l20 = new JLabel("Total Amount");
        l21 = new JLabel("TOTAL AMOUNT TO BE PAID");
        l22 = new JLabel("RS. ");
        l23 = new JLabel("Charging Time: ");

        regNo2Field = new  JTextField();
        unitConsumeField = new  JTextField();
        costperunitField = new  JTextField();
        parkingField  = new  JTextField();
        PaymentBox = new JComboBox<>(new String[]        
            {"Cash", "UPI", "Credit/Debit card"});
        costField  = new  JTextField();
        discountField  = new  JTextField();
        totalAmountField  = new  JTextField();

        fetchButton = new JButton("Fetch Details");
        billPaidButton = new JButton("Payment Done");
        reset2Button = new JButton("Reset");

        logoLabel.setBounds(150, 20, 160, 100);
        station1Label.setBounds(350, 40, 300, 30);
        station2Label.setBounds(350, 70, 200, 30);

        h5.setBounds(25,140,150,20); 
        l13.setBounds(50,170,100,30);
        l14.setBounds(50,215,100,30);
        l15.setBounds(50,260,100,30);
        l16.setBounds(50,305,100,30);
        l17.setBounds(340,170,100,30);
        l18.setBounds(340,215,100,30);
        l19.setBounds(340,260,100,30);
        l20.setBounds(340,305,100,30);
        l21.setBounds(50,380,200,30);
        l22.setBounds(280,380,150,30);
        l23.setBounds(400,380,150,30);

        regNo2Field.setBounds(170,170,150,30);
        unitConsumeField.setBounds(170,215,150,30);
        costperunitField.setBounds(170,260,150,30);
        parkingField.setBounds(170,305,150,30);
        PaymentBox.setBounds(500,170,150,30);
        costField.setBounds(500,215,150,30);
        discountField.setBounds(500,260,150,30);
        totalAmountField.setBounds(500,305,150,30);

        fetchButton.setBounds(70,430,150,30);
        billPaidButton.setBounds(265,430,150,30);
        reset2Button.setBounds(460,430,150,30);

        Double parkingtime =0.0;
        parkingField.setText(String.valueOf(parkingtime));

        frame2.add(logoLabel);
        frame2.add(station1Label);
        frame2.add(station2Label);
        frame2.add(h5);
        frame2.add(l13);
        frame2.add(l14);        
        frame2.add(l15);
        frame2.add(l16);
        frame2.add(l17);        
        frame2.add(l18);
        frame2.add(l19);
        frame2.add(l20);        
        frame2.add(l21);
        frame2.add(l22);
        frame2.add(l23);
        frame2.add(unitConsumeField);
        frame2.add(costperunitField);
        frame2.add(parkingField);
        frame2.add(PaymentBox);
        frame2.add(regNo2Field);
        frame2.add(costField);
        frame2.add(discountField);
        frame2.add(totalAmountField);
        frame2.add(fetchButton);
        frame2.add(billPaidButton);
        frame2.add(reset2Button);
        fetchButton.addActionListener(e-> getbillDetials());
        billPaidButton.addActionListener(e -> billPaid());
        reset2Button.addActionListener(e-> reset2());   
    }
}


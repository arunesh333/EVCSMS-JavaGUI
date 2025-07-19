import java.io.*;
import javax.swing.*;

public class FileManager {

    // Appends a new data line to a CSV file
    public static void saveToCSV(String filePath, String dataLine) {
        try (FileWriter fw = new FileWriter(filePath, true)) {
            fw.write(dataLine + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Saves final billing report as CSV
    public static void saveReportToCSV(String filePath, Vehicles veh, double unitConsumed, double costPerUnit,
                                       double totalCost, double discount, double parkingCost, double finalAmount, int chargingTime) {
        String dataLine = veh.toCSV() + "," + unitConsumed + "," + costPerUnit + "," + totalCost + "," +
                          (discount * 100) + "%" + "," + parkingCost + "," + finalAmount + "," + chargingTime;

        try (FileWriter fw = new FileWriter(filePath, true)) {
            fw.write(dataLine + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Fetches a vehicle's CSV line using its registration number
    public static String getFromCSV(String filePath, String vehReg) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length > 0 && fields[0].trim().equalsIgnoreCase(vehReg.trim())) {
                    return line;
                }
            }
            JOptionPane.showMessageDialog(null, "No vehicle found", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    // Removes a vehicle entry from CSV once payment is done
    public static void removeLineFromCSV(String filePath, String vehReg) {
        File inputFile = new File(filePath);
        File tempFile = new File("temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (!fields[0].equalsIgnoreCase(vehReg)) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error removing entry: " + e.getMessage());
        }

        // Replace old file with updated temp file
        inputFile.delete();
        tempFile.renameTo(inputFile);
    }
}

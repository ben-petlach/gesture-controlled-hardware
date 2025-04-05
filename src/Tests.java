import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;
import java.util.Scanner;

/**
 * Interactive test program for the DeviceManager.
 * Allows control of multiple Arduino components through console commands.
 */
public class Tests {
    
    // Update this with your Arduino port
    private static final String ARDUINO_PORT = "/dev/cu.usbserial-0001"; // Adjust for your Mac
    
    public static void main(String[] args) {
        try {
            // Initialize and connect to the Arduino board
            System.out.println("Connecting to Arduino board...");
            FirmataDevice board = new FirmataDevice(ARDUINO_PORT);
            board.start();
            board.ensureInitializationIsDone();
            System.out.println("Connected successfully to Arduino!");
            
            // Create device manager with sample devices
            DeviceManager manager = initializeDeviceManager(board);
            
            // Run interactive test console
            runInteractiveConsole(manager);
            
            // Cleanup
            board.stop();
            System.out.println("Connection closed.");
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initializes a DeviceManager with device controllers.
     */
    private static DeviceManager initializeDeviceManager(FirmataDevice board) throws IOException {
        DeviceManager manager = new DeviceManager(board);
        
        // Add LED controllers (digital PWM)
        manager.addController(new LEDController(board, 3));  // Red LED
        
        // Add servo motors
        manager.addController(new ServoController(board, 9));
        
        // You can add more device controllers here
        manager.addController(new BuzzerController(board, 5));
        
        return manager;
    }
    
    /**
     * Interactive console for controlling devices.
     */
    private static void runInteractiveConsole(DeviceManager manager) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        printHelp();
        
        while (running) {
            System.out.print("\nCommand > ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split("\\s+");
            
            if (parts.length == 0) continue;
            
            try {
                switch (parts[0].toLowerCase()) {
                    case "help":
                        printHelp();
                        break;
                        
                    case "list":
                        listDevices(manager);
                        break;
                        
                    case "set":
                        if (parts.length < 3) {
                            System.out.println("Error: 'set' requires device index and value");
                            break;
                        }
                        int index = Integer.parseInt(parts[1]);
                        int value = Integer.parseInt(parts[2]);
                        manager.controlDevice(index, value);
                        System.out.println("Set device " + index + " to value " + value);
                        break;
                        
                    case "sweep":
                        if (parts.length < 2) {
                            System.out.println("Error: 'sweep' requires a device index");
                            break;
                        }
                        sweepDevice(manager, Integer.parseInt(parts[1]));
                        break;
                        
                    case "all":
                        if (parts.length < 2) {
                            System.out.println("Error: 'all' requires a value");
                            break;
                        }
                        setAllDevices(manager, Integer.parseInt(parts[1]));
                        break;
                        
                    case "demo":
                        runDemoSequence(manager);
                        break;
                        
                    case "exit":
                    case "quit":
                        running = false;
                        System.out.println("Exiting...");
                        break;
                        
                    default:
                        System.out.println("Unknown command. Type 'help' for options.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error communicating with device: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    private static void printHelp() {
        System.out.println("\n=== Arduino Device Manager Test Console ===");
        System.out.println("Available commands:");
        System.out.println("  help              - Show this help message");
        System.out.println("  list              - List all connected devices");
        System.out.println("  set <idx> <val>   - Set device at index to value");
        System.out.println("  sweep <idx>       - Sweep device value from min to max");
        System.out.println("  all <val>         - Set all devices to the same value");
        System.out.println("  demo              - Run demonstration sequence on all devices");
        System.out.println("  exit/quit         - Exit the program");
    }
    
    private static void listDevices(DeviceManager manager) {
        int count = manager.getControllerCount();
        System.out.println("\nConnected devices (" + count + " total):");
        
        for (int i = 0; i < count; i++) {
            DeviceController controller = manager.getController(i);
            System.out.println("  [" + i + "] " + controller.getClass().getSimpleName());
        }
    }
    
    private static void sweepDevice(DeviceManager manager, int index) throws IOException, InterruptedException {
        DeviceController device = manager.getController(index);
        System.out.println("Sweeping device " + index + "...");
        
        // Determine device type to get appropriate min/max values
        int minVal = 0;
        int maxVal = 255;
        
        if (device instanceof ServoController) {
            minVal = 0;   // Servo min position
            maxVal = 180; // Servo max position
        }
        
        // Sweep up
        for (int i = minVal; i <= maxVal; i += 5) {
            manager.controlDevice(index, i);
            Thread.sleep(50);
        }
        
        // Sweep down
        for (int i = maxVal; i >= minVal; i -= 5) {
            manager.controlDevice(index, i);
            Thread.sleep(50);
        }
        
        System.out.println("Sweep complete.");
    }
    
    private static void setAllDevices(DeviceManager manager, int value) throws IOException {
        int count = manager.getControllerCount();
        for (int i = 0; i < count; i++) {
            try {
                manager.controlDevice(i, value);
            } catch (IllegalArgumentException e) {
                // Skip devices where the value is out of range
                System.out.println("Skipped device " + i + ": " + e.getMessage());
            }
        }
        System.out.println("Set all compatible devices to value " + value);
    }
    
    private static void runDemoSequence(DeviceManager manager) throws IOException, InterruptedException {
        System.out.println("Running demo sequence...");
        int count = manager.getControllerCount();
        
        // Turn everything off
        setAllDevices(manager, 0);
        Thread.sleep(500);
        
        // Individual device activation
        for (int i = 0; i < count; i++) {
            System.out.println("Activating device " + i);
            
            // Try to determine if it's a servo or LED
            DeviceController device = manager.getController(i);
            
            if (device instanceof ServoController) {
                // For servos, move to different positions
                manager.controlDevice(i, 0);
                Thread.sleep(500);
                manager.controlDevice(i, 90);
                Thread.sleep(500);
                manager.controlDevice(i, 180);
                Thread.sleep(500);
                manager.controlDevice(i, 90);
                Thread.sleep(500);
                manager.controlDevice(i, 0);
            } else {
                // For LEDs, fade in and out
                for (int j = 0; j <= 255; j += 5) {
                    try {
                        manager.controlDevice(i, j);
                        Thread.sleep(20);
                    } catch (IllegalArgumentException e) {
                        // Skip if value out of range
                    }
                }
                
                Thread.sleep(300);
                
                for (int j = 255; j >= 0; j -= 5) {
                    try {
                        manager.controlDevice(i, j);
                        Thread.sleep(20);
                    } catch (IllegalArgumentException e) {
                        // Skip if value out of range
                    }
                }
            }
            
            Thread.sleep(300);
        }
        
        // Finale - all on and off a few times
        for (int i = 0; i < 3; i++) {
            setAllDevices(manager, 180); // Max for servos, bright for LEDs
            Thread.sleep(300);
            setAllDevices(manager, 0);
            Thread.sleep(300);
        }
        
        System.out.println("Demo sequence complete.");
    }
    
    /**
     * LED controller implementation.
     */
    private static class LEDController extends DeviceController {
        public LEDController(FirmataDevice board, int pinNumber) throws IOException {
            super(board, pinNumber, Pin.Mode.PWM, 0, 255);
        }
    }
    
    /**
     * Servo controller implementation.
     */
    private static class ServoController extends DeviceController {
        public ServoController(FirmataDevice board, int pinNumber) throws IOException {
            super(board, pinNumber, Pin.Mode.SERVO, 0, 180);
        }
    }
}
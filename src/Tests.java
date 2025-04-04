import org.firmata4j.firmata.FirmataDevice;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Tests {
    
    public static void main(String[] args) {
        // Change this to your Arduino port on your Mac
        String arduinoPort = "/dev/cu.usbserial-0001"; // Update this to match your Arduino port
        FirmataDevice arduino = new FirmataDevice(arduinoPort);
        
        try {
            System.out.println("Connecting to Arduino...");
            arduino.start();
            arduino.ensureInitializationIsDone();
            System.out.println("Arduino connected and initialized");
            
            // Create a servo on digital pin 9 (commonly used for servos)
            ServoController servo = new ServoController(arduino, 9);
            
            // Create a Random number generator
            Random random = new Random();
            
            // Move the servo to 5 random positions
            for (int i = 0; i < 5; i++) {
                // Generate a random angle between 0 and 180
                int randomAngle = random.nextInt(181); // 0 to 180 inclusive
                
                System.out.println("Moving servo to position " + (i+1) + ": " + randomAngle + " degrees");
                servo.setAngle(randomAngle);
                
                // Wait for 2 seconds between movements
                TimeUnit.MILLISECONDS.sleep(2000);
            }
            
            System.out.println("Test completed successfully!");
            
            // Properly shutdown the connection
            arduino.stop();
            
        } catch (IOException e) {
            System.err.println("Error communicating with Arduino: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Operation interrupted: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
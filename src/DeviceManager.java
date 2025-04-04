import org.firmata4j.firmata.FirmataDevice;import org.firmata4j.IODevice;
import org.firmata4j.Pin;

public class DeviceManager {
    public static void main(String[] args) {
        try {
            // Change the port name to match your system
            // For Windows, it might be "COM3", for Linux/Mac something like "/dev/ttyUSB0"
            String port = "/dev/cu.usbserial-0001";
            IODevice board = new FirmataDevice(port);

            board.start();
            board.ensureInitializationIsDone();

            // Set pin D9 to SERVO mode
            Pin servo = board.getPin(9);
            servo.setMode(Pin.Mode.SERVO);

            // Sweep servo from 0 to 180 degrees
            for (int angle = 0; angle <= 180; angle += 10) {
                System.out.println("Moving to angle: " + angle);
                servo.setValue(angle);
                Thread.sleep(100);  // wait 100ms for the servo to reach the position
            }

            // Sweep servo back from 180 to 0 degrees
            for (int angle = 180; angle >= 0; angle -= 10) {
                System.out.println("Moving to angle: " + angle);
                servo.setValue(angle);
                Thread.sleep(100);
            }

            board.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
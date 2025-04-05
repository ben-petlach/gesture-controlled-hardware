import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

/**
 * Controller for a servo motor.
 */
public class ServoController extends DeviceController {
    public static final int MIN_ANGLE = 0;
    public static final int MAX_ANGLE = 180;
    
    /**
     * Creates a new servo controller.
     * 
     * @param board The Firmata device
     * @param pinNumber The pin number for the servo
     * @throws IOException If there's an error setting up the pin
     */
    public ServoController(FirmataDevice board, int pinNumber) throws IOException {
        super(board, pinNumber, Pin.Mode.SERVO, MIN_ANGLE, MAX_ANGLE, "Servo");
    }
}
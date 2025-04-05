import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

/**
 * Controller for a buzzer component.
 */
public class BuzzerController extends DeviceController {
    public static final int MIN_VOLUME = 0;
    public static final int MAX_VOLUME = 255;

    /**
     * Creates a new buzzer controller.
     * 
     * @param board The Firmata device
     * @param pinNumber The pin number for the buzzer
     * @throws IOException If there's an error setting up the pin
     */
    public BuzzerController(FirmataDevice board, int pinNumber) throws IOException {
        super(board, pinNumber, Pin.Mode.PWM, MIN_VOLUME, MAX_VOLUME, "Buzzer");
    }
}
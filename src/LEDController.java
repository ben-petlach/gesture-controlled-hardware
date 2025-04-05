import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

/**
 * Controller for an LED component.
 */
public class LEDController extends DeviceController {
    public static final int MIN_BRIGHTNESS = 0;
    public static final int MAX_BRIGHTNESS = 255;
    
    /**
     * Creates a new LED controller.
     * 
     * @param board The Firmata device
     * @param pinNumber The pin number for the LED
     * @throws IOException If there's an error setting up the pin
     */
    public LEDController(FirmataDevice board, int pinNumber) throws IOException {
        super(board, pinNumber, Pin.Mode.PWM, MIN_BRIGHTNESS, MAX_BRIGHTNESS, "LED");
    }

    /**
     * Sets the LED brightness.
     *
     * @param brightness The PWM value (between MIN_BRIGHTNESS and MAX_BRIGHTNESS).
     * @throws IllegalArgumentException if brightness is out of range.
     */
    public void setBrightness(int brightness) throws IOException {
        setValue(brightness);
    }
}
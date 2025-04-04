import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

public class LEDController extends DeviceController {
    public static final int MIN_BRIGHTNESS = 0;
    public static final int MAX_BRIGHTNESS = 255;

    /**
     * Constructor that initializes the LED on the specified PWM pin.
     *
     * @param board The Firmata FirmataDevice that is already started and initialized.
     * @param pinNumber The digital pin number the LED is connected to.
     */
    public LEDController(FirmataDevice board, int pinNumber) throws IOException {
        super(board, pinNumber, Pin.Mode.PWM, MIN_BRIGHTNESS, MAX_BRIGHTNESS);
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
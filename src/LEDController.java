import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

public class LEDController {
    // PWM range for LED brightness
    public static final int MIN_BRIGHTNESS = 0;
    public static final int MAX_BRIGHTNESS = 255;

    private Pin ledPin;

    /**
     * Constructor that initializes the LED on the specified PWM pin.
     *
     * @param board The Firmata FirmataDevice that is already started and initialized.
     * @param pinNumber The digital pin number the LED is connected to.
     */
    public LEDController(FirmataDevice board, int pinNumber) throws IOException {
        ledPin = board.getPin(pinNumber);
        ledPin.setMode(Pin.Mode.PWM);
    }

    /**
     * Sets the LED brightness.
     *
     * @param brightness The PWM value (between MIN_BRIGHTNESS and MAX_BRIGHTNESS).
     * @throws IllegalArgumentException if brightness is out of range.
     */
    public void setBrightness(int brightness) throws IOException {
        if (brightness < MIN_BRIGHTNESS || brightness > MAX_BRIGHTNESS) {
            throw new IllegalArgumentException("Brightness must be between "
                    + MIN_BRIGHTNESS + " and " + MAX_BRIGHTNESS);
        }
        ledPin.setValue(brightness);
    }
}
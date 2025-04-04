import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

public class BuzzerController {
    // PWM range for buzzer sound level (or intensity)
    public static final int MIN_SOUND = 0;
    public static final int MAX_SOUND = 255;

    private Pin buzzerPin;

    /**
     * Constructor that initializes the buzzer on the specified PWM pin.
     *
     * @param board The Firmata FirmataDevice that is already started and initialized.
     * @param pinNumber The digital pin number the buzzer is connected to.
     */
    public BuzzerController(FirmataDevice board, int pinNumber) throws IOException {
        buzzerPin = board.getPin(pinNumber);
        buzzerPin.setMode(Pin.Mode.PWM);
    }

    /**
     * Sets the buzzer's sound level.
     *
     * @param level The PWM value (between MIN_SOUND and MAX_SOUND).
     * @throws IllegalArgumentException if level is out of range.
     */
    public void setSoundLevel(int level) throws IOException {
        if (level < MIN_SOUND || level > MAX_SOUND) {
            throw new IllegalArgumentException("Sound level must be between "
                    + MIN_SOUND + " and " + MAX_SOUND);
        }
        buzzerPin.setValue(level);
    }
}
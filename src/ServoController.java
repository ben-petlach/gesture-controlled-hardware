import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

public class ServoController extends DeviceController {
    public static final int MIN_ANGLE = 0;
    public static final int MAX_ANGLE = 180;

    /**
     * Constructor that initializes the servo on the specified pin.
     *
     * @param board The Firmata FirmataDevice that is already started and initialized.
     * @param pinNumber The digital pin number the servo is connected to.
     */
    public ServoController(FirmataDevice board, int pinNumber) throws IOException {
        super(board, pinNumber, Pin.Mode.SERVO, MIN_ANGLE, MAX_ANGLE);
    }

    /**
     * Sets the servo to the specified angle.
     *
     * @param angle The target angle (0 to 180 degrees).
     * @throws IllegalArgumentException if the angle is out of range.
     */
    public void setAngle(int angle) throws IOException {
        setValue(angle);
    }
}
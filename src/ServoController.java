import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

public class ServoController {
    public static final int MIN_ANGLE = 0;
    public static final int MAX_ANGLE = 180;

    private Pin servoPin;

    /**
     * Constructor that initializes the servo on the specified pin.
     *
     * @param board The Firmata FirmataDevice that is already started and initialized.
     * @param pinNumber The digital pin number the servo is connected to.
     */
    public ServoController(FirmataDevice board, int pinNumber) throws IOException {
        servoPin = board.getPin(pinNumber);
        servoPin.setMode(Pin.Mode.SERVO);
    }

    /**
     * Sets the servo to the specified angle.
     *
     * @param angle The target angle (0 to 180 degrees).
     * @throws IllegalArgumentException if the angle is out of range.
     */
    public void setAngle(int angle) throws IOException {
        if (angle < 0 || angle > 180) {
            throw new IllegalArgumentException("Angle must be between 0 and 180 degrees.");
        }
        servoPin.setValue(angle);
    }
}
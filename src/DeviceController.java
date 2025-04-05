import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

/**
 * Abstract base class for Arduino device controllers.
 * Provides common functionality for various components that use pins.
 */
public abstract class DeviceController {
    protected Pin devicePin;
    protected final int minValue;
    protected final int maxValue;

    /**
     * Constructor that initializes a device on the specified pin.
     *
     * @param board The Firmata FirmataDevice that is already started and initialized.
     * @param pinNumber The pin number the device is connected to.
     * @param mode The pin mode to set (e.g., Pin.Mode.SERVO, Pin.Mode.PWM).
     * @param minValue The minimum valid value for this device.
     * @param maxValue The maximum valid value for this device.
     */
    protected DeviceController(FirmataDevice board, int pinNumber, Pin.Mode mode, 
                              int minValue, int maxValue) throws IOException {
        this.devicePin = board.getPin(pinNumber);
        this.devicePin.setMode(mode);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Sets the device to the specified value.
     *
     * @param value The target value (between minValue and maxValue).
     * @throws IllegalArgumentException if the value is out of range.
     */
    public void setValue(int value) throws IOException {
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException("Value must be between "
                    + minValue + " and " + maxValue);
        }
        devicePin.setValue(value);
    }
}
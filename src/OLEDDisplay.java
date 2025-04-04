import org.firmata4j.I2CDevice;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;

/**
 * Manages an OLED display connected to the Arduino.
 * This class provides an interface for displaying text messages
 * on an SSD1306 OLED display over I2C.
 */
public class OLEDDisplay {
    private SSD1306 oled;

    /**
     * Creates a new OLED display controller.
     *
     * @param arduino The Firmata device that the OLED display is connected to
     * @throws Exception If there are errors initializing the OLED display
     */
    public OLEDDisplay(FirmataDevice arduino) throws Exception {
        I2CDevice i2cObject = arduino.getI2CDevice((byte) 0x3C);
        oled = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64);
        oled.init();
    }

    /**
     * Displays a text message on the OLED screen.
     * This method clears the previous content first.
     *
     * @param message The text message to display
     */
    public void displayMessage(String message) {
        oled.getCanvas().clear();
        oled.getCanvas().drawString(0, 0, message);
        oled.display();
    }
}

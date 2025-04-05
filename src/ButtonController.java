import org.firmata4j.Pin;
import org.firmata4j.IOEvent;
import org.firmata4j.IODeviceEventListener;
import org.firmata4j.firmata.FirmataDevice;
import java.io.IOException;

/**
 * Controller for button input that triggers actions when pressed.
 * Provides an event-based mechanism for handling button presses.
 */
public class ButtonController implements IODeviceEventListener {
    private final Pin buttonPin;
    private ButtonPressListener listener;
    
    /**
     * Interface for classes that want to receive button press events
     */
    public interface ButtonPressListener {
        /**
         * Called when the button is pressed (state changes to 0)
         */
        void onButtonPressed();
        
        /**
         * Called when the button is released (state changes to 1)
         */
        void onButtonReleased();
    }
    
    /**
     * Creates a new button controller for the specified pin.
     * 
     * @param device The Firmata device
     * @param pinNumber The pin number to use for this button
     * @throws IOException If there's an error configuring the pin
     */
    public ButtonController(FirmataDevice device, int pinNumber) throws IOException {
        // Configure the pin as an input
        buttonPin = device.getPin(pinNumber);
        buttonPin.setMode(Pin.Mode.INPUT);
        
        // Register this class as a listener for device events
        device.addEventListener(this);
        
        System.out.println("Button controller initialized on pin " + pinNumber);
    }
    
    /**
     * Sets a listener to handle button press and release events.
     * 
     * @param listener The listener to be notified of button state changes
     */
    public void setButtonPressListener(ButtonPressListener listener) {
        this.listener = listener;
    }
    
    /**
     * Called when a pin's state changes.
     * If the pin is our button pin, we'll notify our listener.
     */
    @Override
    public void onPinChange(IOEvent event) {
        // Only respond to events from our button pin
        if (event.getPin().getIndex() != buttonPin.getIndex()) {
            return;
        }
        
        // Get the button state
        long state = event.getValue();
        
        // Notify listener based on button state
        if (listener != null) {
            if (state == 0) {
                // Button is pressed (pulled LOW)
                listener.onButtonPressed();
            } else {
                // Button is released (pulled HIGH)
                listener.onButtonReleased();
            }
        }
    }
    
    /**
     * Gets the current state of the button.
     * 
     * @return 0 if pressed, 1 if not pressed
     * @throws IOException If there's an error reading the pin
     */
    public int getState() throws IOException {
        return (int) buttonPin.getValue();
    }
    
    /**
     * Gets the pin number this button is using.
     * 
     * @return The pin number
     */
    public int getPinNumber() {
        return buttonPin.getIndex();
    }
    
    // Required methods from IODeviceEventListener interface
    @Override
    public void onMessageReceive(IOEvent event, String message) {
        // Not used for button handling
    }
    
    @Override
    public void onStart(IOEvent event) {
        // Not used for button handling
    }
    
    @Override
    public void onStop(IOEvent event) {
        // Not used for button handling
    }
}
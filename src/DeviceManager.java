import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages multiple device controllers.
 * Provides centralized access to control multiple Arduino devices.
 */
public class DeviceManager {
    private final List<DeviceController> controllers;
    private final FirmataDevice board;

    /**
     * Constructor that initializes the DeviceManager with a board instance.
     * 
     * @param board The Firmata board that all devices are connected to
     */
    public DeviceManager(FirmataDevice board) {
        this.controllers = new ArrayList<>();
        this.board = board;
    }
    
    /**
     * Constructor that initializes the DeviceManager with a board instance and
     * an array of pre-configured device controllers.
     * 
     * @param board The Firmata board
     * @param controllers Array of preconfigured device controllers
     */
    public DeviceManager(FirmataDevice board, DeviceController[] controllers) {
        this.controllers = new ArrayList<>(Arrays.asList(controllers));
        this.board = board;
    }
    
    /**
     * Adds a device controller to be managed.
     * 
     * @param controller The device controller to add
     */
    public void addController(DeviceController controller) {
        controllers.add(controller);
    }
    
    /**
     * Gets a device controller at the specified index.
     * 
     * @param index The index of the controller to retrieve
     * @return The DeviceController at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public DeviceController getController(int index) {
        if (index < 0 || index >= controllers.size()) {
            throw new IndexOutOfBoundsException("Controller index out of range: " + index);
        }
        return controllers.get(index);
    }
    
    /**
     * Gets the name of a device controller at the specified index.
     * 
     * @param index The index of the controller to retrieve the name for
     * @return The name of the device controller at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public String getDeviceName(int index) {
        return getController(index).getDeviceName();
    }
    
    /**
     * Controls a specific device by setting its value.
     * 
     * @param index The index of the controller to set
     * @param value The value to set
     * @throws IOException If an I/O error occurs during communication with the device
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public void controlDevice(int index, int value) throws IOException {
        DeviceController controller = getController(index);
        controller.setValue(value);
    }
    
    /**
     * Returns the number of controllers being managed.
     * 
     * @return The number of controllers
     */
    public int getControllerCount() {
        return controllers.size();
    }
    
    /**
     * Gets the board instance this manager is using.
     * 
     * @return The FirmataDevice board
     */
    public FirmataDevice getBoard() {
        return board;
    }
}
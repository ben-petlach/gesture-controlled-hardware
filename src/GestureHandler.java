import org.opencv.core.*;

import java.io.IOException;

/**
 * Centralizes all gesture analysis and processing functionality.
 * Works with GestureDetector to process hand gestures for device control.
 */
public class GestureHandler {
    // Application modes
    public static final int MODE_FINGER_COUNTING = 1;
    public static final int MODE_DISTANCE_MEASUREMENT = 2;
    
    // Current application state
    private int currentMode;
    private GestureDetector gestureDetector;
    
    /**
     * Creates a new GestureAnalyzer with default mode.
     */
    public GestureHandler() {
        this.currentMode = MODE_FINGER_COUNTING;
        this.gestureDetector = new GestureDetector();
    }

    // Logic from AI as it was extracted from OpenCV finger detection logic
    /**
     * Processes the current frame for gesture analysis.
     * 
     * @param frame The current video frame
     * @param cameraManager The camera manager
     * @param gestureProcessor The gesture processor
     * @param ui The UI component
     * @param deviceManager The device manager
     * @throws IOException If there's an error controlling devices
     */
    public void processFrame(Mat frame, CameraManager cameraManager,
                            GestureProcessor gestureProcessor, HandGestureUI ui,
                            DeviceManager deviceManager) throws IOException {
        // Extract the region of interest
        Rect handRegion = cameraManager.getHandRegion();
        Mat roiMat = new Mat(frame, handRegion);

        // Create skin mask
        Mat skinMask = gestureProcessor.createSkinMask(roiMat);

        if (currentMode == MODE_FINGER_COUNTING) {
            // Count fingers
            int fingerCount = gestureProcessor.countFingers(skinMask, roiMat);
            ui.displayFingerCount(frame, fingerCount);
            
            // Display device options when in finger counting mode
            ui.displayDeviceOptions(frame, deviceManager);
            
            // Process finger detection using the GestureDetector
            boolean analysisNeeded = gestureDetector.processFingerDetection(frame, fingerCount, ui, deviceManager);
            
            // If we collected enough frames, analyze the gesture
            if (analysisNeeded) {
                int deviceIndex = gestureDetector.analyzeDetectedFingers(deviceManager);
                if (deviceIndex >= 0) {
                    // Switch to distance measurement mode with the selected device
                    currentMode = MODE_DISTANCE_MEASUREMENT;
                    System.out.println("Switching to distance measurement for device " + deviceIndex);
                }
            }
            
        } else if (currentMode == MODE_DISTANCE_MEASUREMENT && 
                  gestureDetector.getSelectedDeviceIndex() >= 0) {
            // Measure distance as percentage
            double percentage = gestureProcessor.getIndexFingerHeightPercentage(skinMask, roiMat);
            ui.displayHeightPercentage(frame, percentage);
            
            // Control the selected device based on height percentage
            controlSelectedDevice(deviceManager, percentage, frame, ui);
        }

        // Display instructions
        int selectedDeviceIndex = gestureDetector.getSelectedDeviceIndex();
        if (selectedDeviceIndex >= 0) {
            // Get the actual device name instead of just the index
            String deviceName = deviceManager.getDeviceName(selectedDeviceIndex);
            ui.displayText(frame, "Controlling: " + deviceName, new Point(10, 70), 
                          new Scalar(0, 255, 0), 1.0);
        } else {
            ui.displayHandPlacementInstructions(frame);
        }
    }
    
    /**
     * Controls the selected device based on the detected height percentage.
     */
    private void controlSelectedDevice(DeviceManager deviceManager, double percentage, 
                                     Mat frame, HandGestureUI ui) throws IOException {
        int selectedDeviceIndex = gestureDetector.getSelectedDeviceIndex();
        
        // Ensure device index is valid
        if (selectedDeviceIndex >= 0 && selectedDeviceIndex < deviceManager.getControllerCount()) {
            // Map percentage to appropriate range for the device
            DeviceController controller = deviceManager.getController(selectedDeviceIndex);
            int minValue = 0;
            int maxValue = 255;
            
            // Get device-specific ranges if possible
            if (controller instanceof LEDController) {
                minValue = LEDController.MIN_BRIGHTNESS;
                maxValue = LEDController.MAX_BRIGHTNESS;
            } else if (controller instanceof ServoController) {
                minValue = ServoController.MIN_ANGLE;
                maxValue = ServoController.MAX_ANGLE;
            } else if (controller instanceof BuzzerController) {
                minValue = BuzzerController.MIN_VOLUME;
                maxValue = BuzzerController.MAX_VOLUME;
            }
            
            int mappedValue = mapPercentageToRange(percentage, minValue, maxValue);
            
            // Control the device
            deviceManager.controlDevice(selectedDeviceIndex, mappedValue);
            
            // Get device name
            String deviceName = deviceManager.getDeviceName(selectedDeviceIndex);
            
            // Display the control value
            ui.displayText(frame, deviceName + " Value: " + mappedValue, new Point(10, 100), 
                          new Scalar(255, 255, 0), 1.0);
        }
    }
    
    /**
     * Maps a percentage value (0-100) to a value between minOutput and maxOutput
     */
    private int mapPercentageToRange(double percentage, int minOutput, int maxOutput) {
        // Ensure percentage is within 0-100 range
        percentage = Math.max(0, Math.min(100, percentage));

        // Map percentage to the specified range
        return (int)Math.round(minOutput + (percentage / 100.0) * (maxOutput - minOutput));
    }
    
    /**
     * Handles the reset button press event.
     * This resets the currently selected device to its minimum value
     * and switches back to finger counting mode.
     * 
     * @param deviceManager The device manager to control devices
     * @throws IOException If there's an issue controlling the device
     */
    public void handleResetButtonPress(DeviceManager deviceManager) throws IOException {
        // Only take action if we're in distance measurement mode with a device selected
        if (currentMode == MODE_DISTANCE_MEASUREMENT && gestureDetector.getSelectedDeviceIndex() >= 0) {
            System.out.println("Reset button pressed - resetting device and switching modes");
            
            int selectedDeviceIndex = gestureDetector.getSelectedDeviceIndex();
            
            // Reset the selected device to minimum value
            DeviceController controller = deviceManager.getController(selectedDeviceIndex);
            int minValue = 0;
            
            // Get device-specific minimum value if possible
            if (controller instanceof LEDController) {
                minValue = LEDController.MIN_BRIGHTNESS;
            } else if (controller instanceof ServoController) {
                minValue = ServoController.MIN_ANGLE;
            } else if (controller instanceof BuzzerController) {
                minValue = BuzzerController.MIN_VOLUME;
            }
            
            // Set device to minimum value
            deviceManager.controlDevice(selectedDeviceIndex, minValue);
            
            // Reset state and switch back to finger counting mode
            resetDetectionState();
            currentMode = MODE_FINGER_COUNTING;
            System.out.println("Switched back to Finger Counting mode");
        }
    }
    
    /**
     * Resets the detection state.
     */
    public void resetDetectionState() {
        gestureDetector.resetDetectionState();
    }
    
    /**
     * Gets the current mode.
     * 
     * @return The current mode (MODE_FINGER_COUNTING or MODE_DISTANCE_MEASUREMENT)
     */
    public int getMode() {
        return currentMode;
    }
    
    /**
     * Sets the current mode.
     * 
     * @param mode The new mode
     */
    public void setMode(int mode) {
        this.currentMode = mode;
    }
    
    /**
     * Gets the GestureDetector used by this analyzer.
     * 
     * @return The GestureDetector instance
     */
    public GestureDetector getGestureDetector() {
        return gestureDetector;
    }
}
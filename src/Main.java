//NOTE: AI was used

import org.opencv.core.*;
import org.firmata4j.firmata.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    /** The serial port identifier for the Arduino connection */
    private static final String PORT = "/dev/cu.usbserial-0001";
    private static final int RESET_BUTTON_PIN = 6;

    // Application state
    private static final int MODE_FINGER_COUNTING = 1;
    private static final int MODE_DISTANCE_MEASUREMENT = 2;
    private static int mode = MODE_FINGER_COUNTING; // Start in finger counting mode
    
    // Constants for gesture recognition
    private static final int FRAMES_TO_COLLECT = 50;
    private static final double DETECTION_THRESHOLD = 0.8; // 80%
    
    // Tracking state
    private static boolean isCollectingFrames = false;
    private static int framesCollected = 0;
    private static List<Integer> detectedFingers = new ArrayList<>();
    private static int selectedDeviceIndex = -1;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Initialize software components
        CameraManager cameraManager = new CameraManager("Hand Gesture Recognition");
        GestureReader gestureReader = new GestureReader();
        HandGestureUI ui = new HandGestureUI();

        // Initialize Arduino Board
        FirmataDevice arduino = new FirmataDevice(PORT);
        arduino.start();
        arduino.ensureInitializationIsDone();

        // Initialize hardware
        DeviceManager manager = new DeviceManager(arduino);
        manager.addController(new LEDController(arduino, 3));
        manager.addController(new ServoController(arduino, 9));
        manager.addController(new BuzzerController(arduino, 5));

        // Set up the reset button on D6
        ButtonController resetButton = new ButtonController(arduino, RESET_BUTTON_PIN);
        resetButton.setButtonPressListener(new ButtonController.ButtonPressListener() {
            @Override
            public void onButtonPressed() {
                try {
                    // This is called when button is pressed down
                    handleResetButtonPress(manager);
                } catch (IOException e) {
                    System.err.println("Error handling button press: " + e.getMessage());
                }
            }
            
            @Override
            public void onButtonReleased() {
                // This is called when button is released
            }
        });

        Mat frame = new Mat();
        while (true) {
            try {
                // Read a new frame
                frame = cameraManager.readFrame();

                // Draw the hand detection region
                ui.drawHandRegion(frame, cameraManager.getHandRegion());

                // Process the frame based on the current mode
                processFrame(frame, cameraManager, gestureReader, ui, manager);

                // Display the frame
                cameraManager.showFrame(frame);

                // Check for key press
                int key = cameraManager.waitKey(10);
                if (key == 27) { // ESC key
                    break;
                } else if (key == 49) { // '1' key
                    resetDetectionState();
                    mode = MODE_FINGER_COUNTING;
                    System.out.println("Mode: Finger Counting");
                } else if (key == 50) { // '2' key
                    mode = MODE_DISTANCE_MEASUREMENT;
                    System.out.println("Mode: Distance Measurement");
                }
            } catch (Exception e) {
                System.err.println("Error processing frame: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }

        // Release resources
        cameraManager.release();
        arduino.stop();
    }

    private static void processFrame(Mat frame, CameraManager cameraManager,
                                     GestureReader gestureReader, HandGestureUI ui,
                                     DeviceManager deviceManager) throws IOException {
        // Extract the region of interest
        Rect handRegion = cameraManager.getHandRegion();
        Mat roiMat = new Mat(frame, handRegion);

        // Create skin mask
        Mat skinMask = gestureReader.createSkinMask(roiMat);

        if (mode == MODE_FINGER_COUNTING) {
            // Count fingers
            int fingerCount = gestureReader.countFingers(skinMask, roiMat);
            ui.displayFingerCount(frame, fingerCount);
            
            // Display device options when in finger counting mode
            ui.displayDeviceOptions(frame, deviceManager);
            
            // Handle finger detection logic
            handleFingerDetection(frame, fingerCount, ui, deviceManager);
            
        } else if (mode == MODE_DISTANCE_MEASUREMENT && selectedDeviceIndex >= 0) {
            // Measure distance as percentage
            double percentage = gestureReader.getIndexFingerHeightPercentage(skinMask, roiMat);
            ui.displayHeightPercentage(frame, percentage);
            
            // Control the selected device based on height percentage
            controlSelectedDevice(deviceManager, percentage, frame, ui);
        }

        // Display instructions
        if (selectedDeviceIndex >= 0) {
            // Get the actual device name instead of just the index
            String deviceName = deviceManager.getDeviceName(selectedDeviceIndex);
            ui.displayText(frame, "Controlling: " + deviceName, new Point(10, 70), 
                          new Scalar(0, 255, 0), 1.0);
        } else {
            ui.displayHandPlacementInstructions(frame);
        }
    }
    
    private static void handleFingerDetection(Mat frame, int fingerCount, HandGestureUI ui, 
                                             DeviceManager deviceManager) {
        // Only process meaningful finger counts (1-5)
        if (fingerCount >= 1 && fingerCount <= deviceManager.getControllerCount()) {
            if (!isCollectingFrames) {
                // Start collecting frames for this detection
                isCollectingFrames = true;
                framesCollected = 0;
                detectedFingers.clear();
                System.out.println("Started collecting frames for finger count: " + fingerCount);
            }
            
            // Add this detection to our collection
            detectedFingers.add(fingerCount);
            framesCollected++;
            
            // Calculate position for the collection progress text
            // We want it to appear below the device options and instruction
            int deviceCount = deviceManager.getControllerCount();
            // Base position (90) + header spacing (25) + device list (deviceCount * 25) + instruction spacing (35)
            int yPosition = 90 + 25 + (deviceCount * 25) + 35;
            
            // Display collection progress
            ui.displayText(frame, 
                          "Collecting: " + framesCollected + "/" + FRAMES_TO_COLLECT, 
                          new Point(30, yPosition), 
                          new Scalar(255, 255, 0), 
                          0.7);
            
            // Check if we've collected enough frames
            if (framesCollected >= FRAMES_TO_COLLECT) {
                analyzeDetectedFingers(deviceManager);
                isCollectingFrames = false;
            }
        } else if (isCollectingFrames) {
            // Add zero as placeholder if no valid fingers detected
            detectedFingers.add(0);
            framesCollected++;
            
            // Calculate position for the collection progress text (same as above)
            int deviceCount = deviceManager.getControllerCount();
            int yPosition = 90 + 25 + (deviceCount * 25) + 35;
            
            // Display collection progress
            ui.displayText(frame, 
                          "Collecting: " + framesCollected + "/" + FRAMES_TO_COLLECT, 
                          new Point(30, yPosition), 
                          new Scalar(255, 255, 0), 
                          0.7);
            
            // Check if we've collected enough frames
            if (framesCollected >= FRAMES_TO_COLLECT) {
                analyzeDetectedFingers(deviceManager);
                isCollectingFrames = false;
            }
        }
    }
    
    private static void analyzeDetectedFingers(DeviceManager deviceManager) {
        // Count occurrences of each finger count
        int[] counts = new int[deviceManager.getControllerCount() + 1];
        for (int fingers : detectedFingers) {
            if (fingers >= 1 && fingers <= deviceManager.getControllerCount()) {
                counts[fingers]++;
            }
        }
        
        // Find the most frequent finger count that meets the threshold
        int mostFrequent = 0;
        int maxCount = 0;
        for (int i = 1; i <= deviceManager.getControllerCount(); i++) {
            if (counts[i] > maxCount) {
                maxCount = counts[i];
                mostFrequent = i;
            }
        }
        
        // Check if it meets our threshold
        double detectionRate = (double) maxCount / FRAMES_TO_COLLECT;
        if (detectionRate >= DETECTION_THRESHOLD && mostFrequent > 0) {
            System.out.println("Detected finger count " + mostFrequent + 
                              " with confidence " + (detectionRate * 100) + "%");
            
            // Switch to distance measurement mode to control this device
            selectedDeviceIndex = mostFrequent - 1; // Convert to 0-based index
            mode = MODE_DISTANCE_MEASUREMENT;
            System.out.println("Switching to distance measurement for device " + selectedDeviceIndex);
        } else {
            System.out.println("No consistent finger count detected. Highest was " + 
                              mostFrequent + " with " + (detectionRate * 100) + "% confidence");
        }
    }
    
    private static void controlSelectedDevice(DeviceManager deviceManager, double percentage, 
                                             Mat frame, HandGestureUI ui) throws IOException {
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
    
    private static void resetDetectionState() {
        isCollectingFrames = false;
        framesCollected = 0;
        detectedFingers.clear();
        selectedDeviceIndex = -1;
    }

    /**
     * Maps a percentage value (0-100) to a value between minOutput and maxOutput
     *
     * @param percentage The input percentage (0-100)
     * @param minOutput The minimum value of the output range
     * @param maxOutput The maximum value of the output range
     * @return The mapped value as an integer
     */
    private static int mapPercentageToRange(double percentage, int minOutput, int maxOutput) {
        // Ensure percentage is within 0-100 range
        percentage = Math.max(0, Math.min(100, percentage));

        // Map percentage to the specified range
        return (int)Math.round(minOutput + (percentage / 100.0) * (maxOutput - minOutput));
    }

    /**
     * Handles the reset button press event.
     * This method resets the currently selected device to its minimum value
     * and switches back to finger counting mode.
     * 
     * @param deviceManager The device manager to control devices
     * @throws IOException If there's an issue controlling the device
     */
    private static void handleResetButtonPress(DeviceManager deviceManager) throws IOException {
        // Only take action if we're in distance measurement mode with a device selected
        if (mode == MODE_DISTANCE_MEASUREMENT && selectedDeviceIndex >= 0) {
            System.out.println("Reset button pressed - resetting device and switching modes");
            
            // Reset the selected device to minimum value
            DeviceController controller = deviceManager.getController(selectedDeviceIndex);
            int minValue = 0;
            
            // Get device-specific minimum value if possible
            if (controller instanceof LEDController) {
                minValue = LEDController.MIN_BRIGHTNESS;
            } else if (controller instanceof ServoController) {
                minValue = 0; // Minimum position
            } else if (controller instanceof BuzzerController) {
                minValue = 0; // Silent
            }
            
            // Set device to minimum value
            deviceManager.controlDevice(selectedDeviceIndex, minValue);
            
            // Reset state and switch back to finger counting mode
            resetDetectionState();
            mode = MODE_FINGER_COUNTING;
            System.out.println("Switched back to Finger Counting mode");
        }
    }
}
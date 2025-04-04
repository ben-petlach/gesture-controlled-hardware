import org.opencv.core.*;
import org.firmata4j.firmata.*;

import java.io.IOException;

public class Main {
    /** The serial port identifier for the Arduino connection */
    private static final String PORT = "/dev/cu.usbserial-0001";

    // Application state
    private static int mode = 0; // 0: idle, 1: finger counting, 2: distance measurement
    private static LEDController led; // LED instance

    public static void main(String[] args) throws IOException, InterruptedException {
        // Initialize software components
        CameraManager cameraManager = new CameraManager("Hand Gesture Recognition");
        GestureReader gestureReader = new GestureReader();
        HandGestureUI ui = new HandGestureUI();

        // Initialize Arduino Board
        FirmataDevice arduino = new FirmataDevice(PORT);
        arduino.start();
        arduino.ensureInitializationIsDone();

        // Initialize LED on pin 5 (PWM pin)
        led = new LEDController(arduino, 3);
        System.out.println("Arduino and LED initialized");

        Mat frame = new Mat();
        while (true) {
            try {
                // Read a new frame
                frame = cameraManager.readFrame();

                // Draw the hand detection region
                ui.drawHandRegion(frame, cameraManager.getHandRegion());

                // Process the frame based on the current mode
                processFrame(frame, cameraManager, gestureReader, ui);

                // Display the frame
                cameraManager.showFrame(frame);

                // Check for key press
                int key = cameraManager.waitKey(10);
                if (key == 27) { // ESC key
                    break;
                } else if (key == 49) { // '1' key
                    mode = 1;
                    System.out.println("Mode: Finger Counting");
                } else if (key == 50) { // '2' key
                    mode = 2;
                    System.out.println("Mode: Distance Measurement");
                }
            } catch (Exception e) {
                System.err.println("Error processing frame: " + e.getMessage());
                break;
            }
        }

        // Release resources
        cameraManager.release();

        // Turn off LED before exiting
        try {
            led.setBrightness(LEDController.MIN_BRIGHTNESS);
        } catch (IOException e) {
            System.err.println("Error turning off LED: " + e.getMessage());
        }
    }

    private static void processFrame(Mat frame, CameraManager cameraManager,
                                     GestureReader gestureReader, HandGestureUI ui) {
        // Extract the region of interest
        Rect handRegion = cameraManager.getHandRegion();
        Mat roiMat = new Mat(frame, handRegion);

        // Create skin mask
        Mat skinMask = gestureReader.createSkinMask(roiMat);

        // Process based on current mode
        if (mode == 1) {
            // Count fingers
            int fingerCount = gestureReader.countFingers(skinMask, roiMat);
            ui.displayFingerCount(frame, fingerCount);
        } else if (mode == 2) {
            // Measure distance as percentage
            double percentage = gestureReader.getIndexFingerHeightPercentage(skinMask, roiMat);
            ui.displayHeightPercentage(frame, percentage);

            try {
                // Map the percentage (0-100) to LED brightness range (MIN_BRIGHTNESS-MAX_BRIGHTNESS)
                int brightness = mapPercentageToRange(percentage, LEDController.MIN_BRIGHTNESS, LEDController.MAX_BRIGHTNESS);
                led.setBrightness(brightness);

                // Display the mapped brightness level on the UI
                ui.displayBrightness(frame, brightness);
            } catch (IOException e) {
                System.err.println("Error controlling LED: " + e.getMessage());
            }
        } else {
            // Idle mode - display instructions
            ui.displayModeInstructions(frame);
        }

        // Display instructions to place hand
        ui.displayHandPlacementInstructions(frame);
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
}
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
        GestureProcessor gestureProcessor = new GestureProcessor();
        HandGestureUI ui = new HandGestureUI();
        GestureHandler gestureHandler = new GestureHandler();

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
                    gestureHandler.handleResetButtonPress(manager);
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
                gestureHandler.processFrame(frame, cameraManager, gestureProcessor, ui, manager);

                // Display the frame
                cameraManager.showFrame(frame);

                // Check for key press
                int key = cameraManager.waitKey(10);
                if (key == 27) { // ESC key
                    break;
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
}
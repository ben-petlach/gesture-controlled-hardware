// Some of the text formatting and screen aligning was created with AI
// Why: Unfamiliar with some methods involving OpenCV screen drawing

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

/**
 * Handles all UI-related functionality for the hand gesture recognition app.
 * Responsible for drawing information and instructions on frames.
 */
public class HandGestureUI {
    // Constants for text positioning and formatting
    private static final Point PRIMARY_TEXT_POSITION = new Point(30, 30);
    private static final Point SECONDARY_TEXT_POSITION = new Point(30, 60);
    private static final Scalar PRIMARY_TEXT_COLOR = new Scalar(0, 255, 0);
    private static final Scalar INSTRUCTION_COLOR = new Scalar(0, 255, 255);
    private static final double PRIMARY_TEXT_SCALE = 1.0;
    private static final double SECONDARY_TEXT_SCALE = 0.8;
    private static final int TEXT_THICKNESS = 2;
    
    /**
     * Displays the result of finger counting operation
     * 
     * @param frame The frame to draw on
     * @param fingerCount The number of fingers detected
     */
    public void displayFingerCount(Mat frame, int fingerCount) {
        Imgproc.putText(frame, "Fingers: " + fingerCount, 
                PRIMARY_TEXT_POSITION,
                Imgproc.FONT_HERSHEY_SIMPLEX, 
                PRIMARY_TEXT_SCALE, 
                PRIMARY_TEXT_COLOR, 
                TEXT_THICKNESS);
    }
    
    /**
     * Displays the height percentage of the index finger
     * 
     * @param frame The frame to draw on
     * @param percentage The percentage value to display
     */
    public void displayHeightPercentage(Mat frame, double percentage) {
        Imgproc.putText(frame, "Height: " + String.format("%.2f", percentage) + "%",
                PRIMARY_TEXT_POSITION, 
                Imgproc.FONT_HERSHEY_SIMPLEX, 
                PRIMARY_TEXT_SCALE, 
                PRIMARY_TEXT_COLOR, 
                TEXT_THICKNESS);
    }
    
    /**
     * Displays hand placement instructions at the bottom of the frame
     * 
     * @param frame The frame to draw on
     */
    public void displayHandPlacementInstructions(Mat frame) {
        String instructions = "Place your hand in the yellow box";
        Point position = new Point(30, frame.rows() - 20);
        
        Imgproc.putText(frame, instructions, 
                position,
                Imgproc.FONT_HERSHEY_SIMPLEX, 
                0.7, 
                INSTRUCTION_COLOR, 
                TEXT_THICKNESS);
    }
    
    /**
     * Draws a rectangle around the hand detection region
     * 
     * @param frame The frame to draw on
     * @param region The rectangle defining the hand detection region
     */
    public void drawHandRegion(Mat frame, Rect region) {
        Imgproc.rectangle(frame, region, new Scalar(0, 255, 255), 2);
    }

    /**
     * Displays text on the given frame at the specified position.
     * 
     * @param frame The frame to display text on
     * @param text The text to display
     * @param position The position (x,y) to display the text
     * @param color The color of the text
     * @param scale The scale factor for the text size
     */
    public void displayText(Mat frame, String text, Point position, Scalar color, double scale) {
        // Use OpenCV's putText method to draw text on the frame
        // Parameters: image, text, position, font face, font scale, color, thickness, line type
        Imgproc.putText(
            frame,                          // Matrix to write on
            text,                           // Text to write
            position,                       // Position (x,y)
            Imgproc.FONT_HERSHEY_SIMPLEX,   // Font face
            scale,                          // Font scale
            color,                          // Color (BGR format)
            2,                              // Thickness
            Imgproc.LINE_AA                 // Line type
        );
    }

    /**
     * Displays available device options with their associated numbers
     * 
     * @param frame The frame to draw on
     * @param deviceManager The device manager containing available devices
     */
    public void displayDeviceOptions(Mat frame, DeviceManager deviceManager) {
        int deviceCount = deviceManager.getControllerCount();
        
        // Start position for the first device option
        int yPosition = 90; // Start below other UI elements
        
        // Display header text
        Imgproc.putText(frame, "Available devices:", 
                new Point(30, yPosition),
                Imgproc.FONT_HERSHEY_SIMPLEX, 
                0.7, 
                INSTRUCTION_COLOR, 
                TEXT_THICKNESS);
        
        yPosition += 25; // Move down for device list
        
        // Display each device with its number
        for (int i = 0; i < deviceCount; i++) {
            String deviceName = deviceManager.getDeviceName(i);
            String deviceInfo = (i + 1) + ": " + deviceName; // Display 1-based indexing for users
            
            Imgproc.putText(frame, deviceInfo, 
                    new Point(40, yPosition), // Indented from header
                    Imgproc.FONT_HERSHEY_SIMPLEX, 
                    0.65, 
                    PRIMARY_TEXT_COLOR, 
                    1);
            
            yPosition += 25; // Move down for next device
        }
        
        // Display instruction about how to select devices
        yPosition += 10; // Add some extra space
        Imgproc.putText(frame, "Show fingers to select device", 
                new Point(30, yPosition),
                Imgproc.FONT_HERSHEY_SIMPLEX, 
                0.65, 
                INSTRUCTION_COLOR, 
                1);
    }
}
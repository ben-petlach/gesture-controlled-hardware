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
     * Displays mode selection instructions in idle mode
     * 
     * @param frame The frame to draw on
     */
    public void displayModeInstructions(Mat frame) {
        Imgproc.putText(frame, "Press 1 for finger counting", 
                PRIMARY_TEXT_POSITION,
                Imgproc.FONT_HERSHEY_SIMPLEX, 
                SECONDARY_TEXT_SCALE, 
                PRIMARY_TEXT_COLOR, 
                TEXT_THICKNESS);
                
        Imgproc.putText(frame, "Press 2 for finger distance", 
                SECONDARY_TEXT_POSITION,
                Imgproc.FONT_HERSHEY_SIMPLEX, 
                SECONDARY_TEXT_SCALE, 
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
     * Displays the current servo angle on the frame
     * 
     * @param frame The frame to draw on
     * @param angle The current servo angle
     */
    public void displayServoAngle(Mat frame, int angle) {
        Point servoTextPosition = new Point(30, 90); // Position below other text
        Scalar servoTextColor = new Scalar(255, 165, 0); // Orange color
        
        Imgproc.putText(frame, "Servo Angle: " + angle + "°",
                servoTextPosition, 
                Imgproc.FONT_HERSHEY_SIMPLEX, 
                SECONDARY_TEXT_SCALE, 
                servoTextColor, 
                TEXT_THICKNESS);
    }

    /**
     * Displays the current buzzer sound level on the frame
     *
     * @param frame The frame to draw on
     * @param level The current sound level
     */
    public void displaySoundLevel(Mat frame, int level) {
        Point soundTextPosition = new Point(30, 90); // Position below other text
        Scalar soundTextColor = new Scalar(255, 0, 255); // Purple color

        Imgproc.putText(frame, "Sound Level: " + level,
                soundTextPosition,
                Imgproc.FONT_HERSHEY_SIMPLEX,
                SECONDARY_TEXT_SCALE,
                soundTextColor,
                TEXT_THICKNESS);
    }

    /**
     * Displays the current LED brightness level on the frame
     *
     * @param frame The frame to draw on
     * @param brightness The current brightness level
     */
    public void displayBrightness(Mat frame, int brightness) {
        Point brightnessTextPosition = new Point(30, 90); // Position below other text
        Scalar brightnessTextColor = new Scalar(0, 255, 255); // Cyan color

        Imgproc.putText(frame, "LED Brightness: " + brightness,
                brightnessTextPosition,
                Imgproc.FONT_HERSHEY_SIMPLEX,
                SECONDARY_TEXT_SCALE,
                brightnessTextColor,
                TEXT_THICKNESS);
    }
}
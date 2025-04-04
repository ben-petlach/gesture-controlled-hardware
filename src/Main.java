import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class Main {
    // Application state
    private static int mode = 0; // 0: idle, 1: finger counting, 2: distance measurement

    public static void main(String[] args) {
        // Initialize the camera manager with a window name
        CameraManager cameraManager = new CameraManager("Hand Gesture Recognition");

        // Create the gesture reader
        GestureReader gestureReader = new GestureReader();

        Mat frame = new Mat();
        while (true) {
            try {
                // Read a new frame
                frame = cameraManager.readFrame();

                // Draw the hand detection region
                cameraManager.drawHandRegion(frame);

                // Process the frame based on the current mode
                processFrame(frame, cameraManager, gestureReader);

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
    }

    private static void processFrame(Mat frame, CameraManager cameraManager, GestureReader gestureReader) {
        // Extract the region of interest
        Rect handRegion = cameraManager.getHandRegion();
        Mat roiMat = new Mat(frame, handRegion);

        // Create skin mask
        Mat skinMask = gestureReader.createSkinMask(roiMat);

        // Add text to display instructions
        String instructions = "Place your hand in the yellow box";
        if (mode == 1) {
            // Count fingers
            int fingerCount = gestureReader.countFingers(skinMask, roiMat);
            Imgproc.putText(frame, "Fingers: " + fingerCount, new Point(30, 30),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
        } else if (mode == 2) {
            // Measure distance as percentage
            double percentage = gestureReader.getIndexFingerHeightPercentage(skinMask, roiMat);
            Imgproc.putText(frame, "Height: " + String.format("%.2f", percentage) + "%",
                    new Point(30, 30), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
        } else {
            // Idle mode - display instructions
            Imgproc.putText(frame, "Press 1 for finger counting", new Point(30, 30),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 255, 0), 2);
            Imgproc.putText(frame, "Press 2 for finger distance", new Point(30, 60),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 255, 0), 2);
        }

        // Display instructions to place hand
        Imgproc.putText(frame, instructions, new Point(30, frame.rows() - 20),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 255, 255), 2);
    }
}
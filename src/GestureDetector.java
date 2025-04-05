import org.opencv.core.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles finger detection and analysis, including tracking detection consistency
 * across frames and determining user gestures.
 */
public class GestureDetector {
    // Constants for gesture recognition
    private static final int FRAMES_TO_COLLECT = 50;
    private static final double DETECTION_THRESHOLD = 0.8; // 80%
    
    // Tracking state
    private boolean isCollectingFrames;
    private int framesCollected;
    private List<Integer> detectedFingers;
    private int selectedDeviceIndex;
    
    /**
     * Initializes a new gesture detector with default state.
     */
    public GestureDetector() {
        isCollectingFrames = false;
        framesCollected = 0;
        detectedFingers = new ArrayList<>();
        selectedDeviceIndex = -1;
    }
    
    /**
     * Processes finger detection from a single frame.
     *
     * @param frame The frame to display status information on
     * @param fingerCount The detected finger count from the current frame
     * @param ui The UI helper to display information
     * @param deviceManager The device manager to get device count
     * @return true if collection is complete and analysis is needed
     */
    public boolean processFingerDetection(Mat frame, int fingerCount, HandGestureUI ui, 
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
        } else if (isCollectingFrames) {
            // Add zero as placeholder if no valid fingers detected
            detectedFingers.add(0);
            framesCollected++;
        }
        
        if (isCollectingFrames) {
            // Calculate position for the collection progress text
            int deviceCount = deviceManager.getControllerCount();
            int yPosition = 90 + 25 + (deviceCount * 25) + 35;
            
            // Display collection progress
            ui.displayText(frame, 
                          "Collecting: " + framesCollected + "/" + FRAMES_TO_COLLECT, 
                          new Point(30, yPosition), 
                          new Scalar(255, 255, 0), 
                          0.7);
        }
        
        // Check if we've collected enough frames
        if (isCollectingFrames && framesCollected >= FRAMES_TO_COLLECT) {
            isCollectingFrames = false;
            return true; // Analysis needed
        }
        
        return false; // No analysis needed yet
    }
    
    /**
     * Analyzes the collected finger detections to determine the most consistent gesture.
     *
     * @param deviceManager The device manager (used to get the number of valid controllers)
     * @return The index of the selected device, or -1 if no consistent detection
     */
    public int analyzeDetectedFingers(DeviceManager deviceManager) {
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
            
            // Store the selected device index
            selectedDeviceIndex = mostFrequent - 1; // Convert to 0-based index
            System.out.println("Selected device index: " + selectedDeviceIndex);
            return selectedDeviceIndex;
        } else {
            System.out.println("No consistent finger count detected. Highest was " + 
                              mostFrequent + " with " + (detectionRate * 100) + "% confidence");
            return -1; // No consistent detection
        }
    }
    
    /**
     * Resets all detection state back to initial values.
     */
    public void resetDetectionState() {
        isCollectingFrames = false;
        framesCollected = 0;
        detectedFingers.clear();
        selectedDeviceIndex = -1;
    }
    
    /**
     * Gets the currently selected device index.
     *
     * @return The selected device index, or -1 if none selected
     */
    public int getSelectedDeviceIndex() {
        return selectedDeviceIndex;
    }
    
    /**
     * Sets the selected device index.
     *
     * @param index The new device index
     */
    public void setSelectedDeviceIndex(int index) {
        this.selectedDeviceIndex = index;
    }
}
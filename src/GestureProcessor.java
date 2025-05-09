// The logic for the Gesture Reader was generated by AI.
// Why: Computer vision is beyond the scope of this course
// GestureReader was made into its own class with modular methods by myself

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

public class GestureProcessor {
    
    /**
     * Count the number of extended fingers in the provided thresholded image.
     * 
     * @param thresholdImage The binary image containing hand silhouette
     * @param roiFrame The original ROI image for visualization
     * @return The number of fingers detected (0-5)
     */
    public int countFingers(Mat thresholdImage, Mat roiFrame) {
        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresholdImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        int fingerCount = 0;

        if (!contours.isEmpty()) {
            // Find the largest contour (assumed to be the hand)
            int maxIndex = 0;
            double maxArea = 0;
            for (int i = 0; i < contours.size(); i++) {
                double area = Imgproc.contourArea(contours.get(i));
                if (area > maxArea) {
                    maxArea = area;
                    maxIndex = i;
                }
            }

            // Filter by minimum area to avoid noise
            if (maxArea > 2000) {
                // Get the convex hull
                MatOfInt hullIndices = new MatOfInt();
                Imgproc.convexHull(contours.get(maxIndex), hullIndices);

                // Calculate convex hull area for fist detection
                MatOfPoint hullPoints = new MatOfPoint();
                List<Point> pointList = new ArrayList<>();
                for (int idx : hullIndices.toArray()) {
                    pointList.add(contours.get(maxIndex).toArray()[idx]);
                }
                hullPoints.fromList(pointList);
                double hullArea = Imgproc.contourArea(hullPoints);

                // Calculate solidity (ratio of contour area to convex hull area)
                // High solidity = closed fist, low solidity = open hand with fingers
                double solidity = maxArea / hullArea;

                // Get defects
                MatOfInt4 defects = new MatOfInt4();
                if (hullIndices.toArray().length > 3) {
                    Imgproc.convexityDefects(contours.get(maxIndex), hullIndices, defects);
                }

                // Get palm center
                Moments moments = Imgproc.moments(contours.get(maxIndex));
                Point center = new Point(moments.m10/moments.m00, moments.m01/moments.m00);
                Imgproc.circle(roiFrame, center, 5, new Scalar(0, 255, 255), -1);

                // Draw contour and convex hull
                Imgproc.drawContours(roiFrame, contours, maxIndex, new Scalar(0, 255, 0), 2);

                // Calculate bounding box to determine orientation
                RotatedRect boundingBox = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(maxIndex).toArray()));

                // Use defects to determine palm radius
                double palmRadius = 0;
                List<Point> palmPoints = new ArrayList<>();

                if (!defects.empty()) {
                    int[] defectsArray = defects.toArray();
                    Point[] contourPoints = contours.get(maxIndex).toArray();

                    for (int i = 0; i < defectsArray.length; i += 4) {
                        int farIdx = defectsArray[i + 2];
                        Point farPoint = contourPoints[farIdx];
                        double distance = calculateDistance(center, farPoint);

                        // Collect defect points for palm size estimation
                        palmPoints.add(farPoint);
                        palmRadius = Math.max(palmRadius, distance);
                    }
                }

                // Ensure we have a valid palm radius
                if (palmRadius == 0) {
                    palmRadius = Math.min(boundingBox.size.width, boundingBox.size.height) / 4;
                }

                // Draw palm circle
                Imgproc.circle(roiFrame, center, (int)palmRadius, new Scalar(255, 0, 255), 2);

                // If the shape is very solid (low protrusions), it's likely a fist
                if (solidity > 0.9) {
                    fingerCount = 0;
                    Imgproc.putText(roiFrame, "Fist detected", new Point(10, 60),
                            Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 0, 255), 2);
                } else {
                    // Filter for potential fingertips
                    List<Point> fingerCandidates = new ArrayList<>();
                    for (Point p : pointList) {
                        // Filter based on distance from center and vertical position
                        double distFromCenter = calculateDistance(center, p);

                        // Only count points that are:
                        // 1. Far enough from the center (extended fingers)
                        // 2. Above or not too far below the center (avoid wrist and lower points)
                        // 3. Not at the extreme left/right bottom corners (often false positives)
                        if (distFromCenter > palmRadius * 0.8 &&
                                p.y < center.y + palmRadius * 0.3 &&  // Stricter vertical constraint
                                !(p.y > center.y && (p.x < center.x - palmRadius * 0.8 || p.x > center.x + palmRadius * 0.8))) {
                            fingerCandidates.add(p);
                            Imgproc.circle(roiFrame, p, 5, new Scalar(0, 255, 0), -1);
                        }
                    }

                    // Sort candidates by angle around center (clockwise)
                    // This ordering helps with adjacent finger detection
                    final Point finalCenter = center;
                    fingerCandidates.sort((p1, p2) -> {
                        double angle1 = Math.atan2(p1.y - finalCenter.y, p1.x - finalCenter.x);
                        double angle2 = Math.atan2(p2.y - finalCenter.y, p2.x - finalCenter.x);
                        return Double.compare(angle1, angle2);
                    });

                    // Filter candidates that are too close together
                    List<Point> fingerTips = new ArrayList<>();
                    if (!fingerCandidates.isEmpty()) {
                        fingerTips.add(fingerCandidates.get(0));

                        for (int i = 1; i < fingerCandidates.size(); i++) {
                            boolean tooClose = false;

                            // Variable threshold based on current finger count
                            // Relaxes the distance threshold when we already have 3 fingers
                            double distThreshold = palmRadius * 0.4;
                            if (fingerTips.size() >= 3) {
                                distThreshold = palmRadius * 0.35;  // More relaxed for 4th finger
                            }

                            // Check distance to all accepted fingertips
                            for (Point existingTip : fingerTips) {
                                if (calculateDistance(existingTip, fingerCandidates.get(i)) < distThreshold) {
                                    tooClose = true;
                                    break;
                                }
                            }

                            if (!tooClose) {
                                fingerTips.add(fingerCandidates.get(i));
                            }
                        }
                    }

                    // Handle thumb separately - look for points to the side
                    for (Point p : pointList) {
                        // Thumb detection criteria adjusted
                        double xDiff = Math.abs(p.x - center.x);
                        double yDiff = Math.abs(p.y - center.y);
                        double distFromCenter = calculateDistance(center, p);

                        // Thumb criteria: more horizontal than vertical distance
                        if (xDiff > yDiff * 1.2 && distFromCenter > palmRadius * 0.7 &&
                                !isTooClose(fingerTips, p, palmRadius * 0.4)) {
                            // Likely a thumb
                            fingerTips.add(p);
                            Imgproc.circle(roiFrame, p, 12, new Scalar(255, 255, 0), -1);
                            break;
                        }
                    }

                    // Draw fingertips
                    for (Point p : fingerTips) {
                        Imgproc.circle(roiFrame, p, 12, new Scalar(255, 0, 0), -1);
                        Imgproc.line(roiFrame, center, p, new Scalar(255, 255, 0), 2);
                    }

                    fingerCount = fingerTips.size();
                }

                // Add text showing finger count directly on ROI image
                Imgproc.putText(roiFrame, "Count: " + fingerCount, new Point(10, 30),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 0, 255), 2);
            }
        }

        return Math.min(fingerCount, 5); // Limit to 5 fingers
    }

    /**
     * Measure the index finger height as a percentage of the ROI height.
     * The bottom of the region is considered to be 40px above the actual bottom.
     * 
     * @param thresholdImage The binary image containing hand silhouette
     * @param roiFrame The original ROI image for visualization
     * @return The percentage of the finger height relative to the adjusted ROI height
     */
    public double getIndexFingerHeightPercentage(Mat thresholdImage, Mat roiFrame) {
        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresholdImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double percentageHeight = 0.0;

        if (!contours.isEmpty()) {
            // Find the largest contour (assumed to be the hand)
            int maxIndex = 0;
            double maxArea = 0;
            for (int i = 0; i < contours.size(); i++) {
                double area = Imgproc.contourArea(contours.get(i));
                if (area > maxArea) {
                    maxArea = area;
                    maxIndex = i;
                }
            }

            // Filter by minimum area to avoid noise
            if (maxArea > 2000) {
                // Draw contour
                Imgproc.drawContours(roiFrame, contours, maxIndex, new Scalar(0, 255, 0), 2);

                // Get palm center
                Moments moments = Imgproc.moments(contours.get(maxIndex));
                Point center = new Point(moments.m10/moments.m00, moments.m01/moments.m00);
                Imgproc.circle(roiFrame, center, 5, new Scalar(0, 255, 255), -1);

                // Get convex hull
                MatOfInt hull = new MatOfInt();
                Imgproc.convexHull(contours.get(maxIndex), hull);

                // Get convex hull points
                Point[] contourPoints = contours.get(maxIndex).toArray();
                List<Point> convexPoints = new ArrayList<>();
                for (int idx : hull.toArray()) {
                    convexPoints.add(contourPoints[idx]);
                }

                // Find the index finger tip (highest point above the center)
                Point indexTip = null;
                double minY = Double.MAX_VALUE;

                for (Point p : convexPoints) {
                    // Look for points above the center (lower y value)
                    if (p.y < center.y && p.y < minY) {
                        minY = p.y;
                        indexTip = p;
                    }
                }

                if (indexTip != null) {
                    // Draw the index finger tip
                    Imgproc.circle(roiFrame, indexTip, 8, new Scalar(0, 0, 255), -1);

                    // Calculate height from adjusted bottom point to index tip
                    int adjustedBottom = roiFrame.rows() - 40; // 40px above bottom
                    double fingerHeight = adjustedBottom - indexTip.y;
                    double maxPossibleHeight = adjustedBottom;
                    
                    // Calculate percentage (0-100%)
                    percentageHeight = (fingerHeight / maxPossibleHeight) * 100.0;

                    // Draw a line showing the measurement
                    Point bottomPoint = new Point(indexTip.x, adjustedBottom);
                    Imgproc.line(roiFrame, indexTip, bottomPoint, new Scalar(255, 255, 0), 2);
                    
                    // Draw the adjusted bottom line
                    Imgproc.line(roiFrame, new Point(0, adjustedBottom), 
                                new Point(roiFrame.cols(), adjustedBottom), 
                                new Scalar(255, 0, 0), 2);

                    // Add text showing the measured percentage
                    Imgproc.putText(roiFrame, String.format("Height: %.1f%%", percentageHeight),
                            new Point(10, 60), Imgproc.FONT_HERSHEY_SIMPLEX, 0.7,
                            new Scalar(0, 255, 255), 2);
                }
            }
        }

        return percentageHeight;
    }

    /**
     * Creates a skin mask from the input frame using HSV color space filtering.
     * 
     * @param roiMat The region of interest from the original frame
     * @return A binary mask highlighting skin pixels
     */
    public Mat createSkinMask(Mat roiMat) {
        // Convert to HSV for better skin detection
        Mat hsvFrame = new Mat();
        Imgproc.cvtColor(roiMat, hsvFrame, Imgproc.COLOR_BGR2HSV);

        // Create mask for skin color detection (works for various skin tones)
        Mat skinMask = new Mat();
        Core.inRange(hsvFrame, new Scalar(0, 20, 70), new Scalar(20, 150, 255), skinMask);

        // Second range for skin detection (to handle some lighting conditions better)
        Mat skinMask2 = new Mat();
        Core.inRange(hsvFrame, new Scalar(170, 20, 70), new Scalar(180, 150, 255), skinMask2);

        // Combine the two masks
        Core.bitwise_or(skinMask, skinMask2, skinMask);

        // Apply Gaussian blur
        Imgproc.GaussianBlur(skinMask, skinMask, new Size(9, 9), 2, 2);

        // Apply morphological operations to clean up the mask
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(skinMask, skinMask, Imgproc.MORPH_CLOSE, kernel);

        return skinMask;
    }
    
    private double calculateDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }
    
    private boolean isTooClose(List<Point> points, Point newPoint, double minDistance) {
        for (Point p : points) {
            if (calculateDistance(p, newPoint) < minDistance) {
                return true;
            }
        }
        return false;
    }
}
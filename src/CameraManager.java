import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

public class CameraManager {
    private VideoCapture camera;
    private String windowName;
    private Rect handRegion;
    
    static {
        // Load the OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    public CameraManager(String windowName) {
        this.windowName = windowName;
        init();
    }
    
    private void init() {
        // Initialize camera
        camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            throw new RuntimeException("Error: Camera not accessible");
        }
        
        // Create window
        HighGui.namedWindow(windowName);
        
        // Read one frame to get dimensions
        Mat tempFrame = new Mat();
        camera.read(tempFrame);
        int frameWidth = tempFrame.cols();
        int frameHeight = tempFrame.rows();
        
        // Define hand detection region (center-right portion of the frame)
        int roiWidth = frameWidth / 3;
        int roiHeight = frameHeight / 2;
        int roiX = frameWidth / 2;
        int roiY = frameHeight / 4;
        handRegion = new Rect(roiX, roiY, roiWidth, roiHeight);
    }
    
    public Mat readFrame() {
        Mat frame = new Mat();
        camera.read(frame);
        if (frame.empty()) {
            throw new RuntimeException("Error: No captured frame");
        }
        
        // Flip horizontally for more intuitive interaction
        Core.flip(frame, frame, 1);
        return frame;
    }
    
    public void showFrame(Mat frame) {
        HighGui.imshow(windowName, frame);
    }
    
    public int waitKey(int delay) {
        return HighGui.waitKey(delay) & 0xFF;
    }
    
    public void release() {
        camera.release();
        HighGui.destroyAllWindows();
    }
    
    public Rect getHandRegion() {
        return handRegion;
    }
    
    public void drawHandRegion(Mat frame) {
        Imgproc.rectangle(frame, handRegion, new Scalar(0, 255, 255), 2);
    }
}
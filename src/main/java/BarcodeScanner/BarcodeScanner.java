package BarcodeScanner;

// Import necessary libraries
import com.google.zxing.*; // ZXing library for barcode scanning
import com.google.zxing.common.HybridBinarizer; // Hybrid binarizer to create a binary bitmap from image luminance
import com.google.zxing.client.j2se.BufferedImageLuminanceSource; // Converts BufferedImage to LuminanceSource
import org.opencv.core.*; // OpenCV core functionalities
import org.opencv.videoio.VideoCapture; // Captures video stream
import org.opencv.imgproc.Imgproc; // OpenCV image processing functionalities
import org.opencv.imgcodecs.Imgcodecs; // For image encoding and decoding
import org.opencv.highgui.HighGui; // OpenCV GUI for displaying images

import javax.imageio.ImageIO; // For working with image input and output
import java.awt.image.BufferedImage; // BufferedImage class for representing images in memory
import java.io.ByteArrayInputStream; // Input stream to read byte arrays
import java.util.ArrayList; // For using ArrayList
import java.util.HashMap; // For using HashMap
import java.util.List; // Java List interface
import java.util.Map; // Java Map interface

public class BarcodeScanner {

    // Static block to load the OpenCV native library when the class is first loaded
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load the OpenCV native library (e.g., opencv_java451.dll)
    }

    // Main method, the entry point for the program
    public static void main(String[] args) {
        // Create a VideoCapture object to capture video from the default camera (0 is usually the built-in camera)
        VideoCapture capture = new VideoCapture(0);

        // Check if the camera is opened successfully; otherwise, print an error message and exit
        if (!capture.isOpened()) {
            System.out.println("Error opening video stream or file");
            return;
        }

        // Create an empty Mat object to store video frames
        Mat frame = new Mat();

        // Infinite loop to continuously capture and process video frames
        while (true) {
            try {
                // Read the next frame from the video stream
                capture.read(frame);

                // Check if the captured frame is valid (not empty)
                if (!frame.empty()) {
                    // Convert the OpenCV Mat frame to a BufferedImage for further processing
                    BufferedImage bufferedImage = MatToBufferedImage(frame);

                    // Scan the BufferedImage for any barcode or QR code
                    Result result = scanBarcode(bufferedImage);

                    // If a barcode is found, print its data and format type
                    if (result != null) {
                        System.out.println("Barcode Data: " + result.getText() + " | Type: " + result.getBarcodeFormat());

                        // Overlay the detected barcode text on the OpenCV frame
                        Imgproc.putText(frame, result.getText(), new Point(10, 50), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
                    }

                    // Display the current video frame with any barcode text overlaid
                    HighGui.imshow("Barcode Scanner", frame);
                } else {
                    // If the frame is empty, print a message
                    System.out.println("Captured empty frame.");
                }

                // Wait for 30ms between frames and check if the 'q' key is pressed to exit the loop
                if (HighGui.waitKey(30) == 'q') {
                    break; // Exit the loop if 'q' is pressed
                }
            } catch (Exception e) {
                // Handle any exceptions and print the error message
                System.out.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Release the video capture when the loop ends, freeing the camera resource
        capture.release();
    }

    // Method to scan a barcode or QR code from a BufferedImage using the ZXing library
    private static Result scanBarcode(BufferedImage image) {
        try {
            // Convert the BufferedImage to LuminanceSource (required by ZXing)
            LuminanceSource source = new BufferedImageLuminanceSource(image);

            // Convert the LuminanceSource to a binary bitmap
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Define hints to help the barcode reader using HashMap and ArrayList
            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            List<BarcodeFormat> possibleFormats = new ArrayList<>();
            possibleFormats.add(BarcodeFormat.QR_CODE);
            possibleFormats.add(BarcodeFormat.EAN_13);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, possibleFormats);

            // Attempt to decode the barcode from the bitmap
            return new MultiFormatReader().decode(bitmap, hints);
        } catch (NotFoundException e) {
            // Handle the case where no barcode was found in the image
            System.out.println("No barcode found in the image.");
            return null;
        } catch (Exception e) {
            // Handle any other exceptions that may occur during barcode scanning
            System.out.println("Error scanning barcode: " + e.getMessage());
            return null;
        }
    }

    // Method to convert an OpenCV Mat (image) to a BufferedImage (Java format)
    private static BufferedImage MatToBufferedImage(Mat frame) {
        try {
            // Create a MatOfByte object to store the image data as a byte array
            MatOfByte matOfByte = new MatOfByte();

            // Encode the Mat frame to a byte array in JPEG format
            Imgcodecs.imencode(".jpg", frame, matOfByte);

            // Convert the byte array to a BufferedImage
            byte[] byteArray = matOfByte.toArray();
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (Exception e) {
            // Handle any exceptions during the conversion process
            System.out.println("Error converting Mat to BufferedImage: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
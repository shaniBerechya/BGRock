package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class CameraTest {
    private Camera camera;
    
    @Test
    public void testLoadCameraData() {
        // Test if data is loaded correctly from the JSON file
        camera = new Camera(1, 2, "./example_input_2/camera_data.json");  
        List<StampedDetectedObjects> detectedObjects = camera.getdetectedObjectList();
        assertNotNull(detectedObjects, "Detected object list should not be null.");
        assertEquals(13, detectedObjects.size(), "Detected object list size should match the JSON data.");
    }

    @Test
    public void testGetDetectedObjectWithDelay() {
        camera = new Camera(1, 2, "./example_input_2/camera_data.json");  
        int currentTime = 6; // Current time
        int expectedTime = currentTime - camera.getFrequency();

        // Retrieve detected objects
        StampedDetectedObjects detected = camera.getDetectedObject(currentTime);

        // Check that the retrieved object matches the calculated time
        assertNotNull(detected, "Detected objects should not be null.");
        assertEquals(expectedTime, detected.getTime(), "Detected object time should match the calculated delay.");

        // Verify that the content matches the JSON file
        assertEquals(2, detected.getDetectedObjects().size(), "Number of detected objects should match JSON data.");
        assertEquals("Chair_Base_1", detected.getDetectedObjects().get(0).getId(), "First object ID should match.");
        assertEquals("Circular_Base_1", detected.getDetectedObjects().get(1).getId(), "Second object ID should match.");
    }

    @Test
    public void testErrorDescription() {
        camera = new Camera(1, 2, "./example_input_2/camera_data.json");  
        int errorTime = 8; // Time where an error occurs according to the JSON

        // Retrieve the error description
        String errorDescription = camera.erorDescripion(errorTime);

        // Check if the camera enters the ERROR state
        assertEquals(STATUS.ERROR, camera.getStatus(), "Camera status should be ERROR.");

        // Verify that the error description matches the JSON data
        assertNotNull(errorDescription, "Error description should not be null.");
        assertEquals("GLaDOS has repurposed the robot to conduct endless cake-fetching tests. Success is a lie.", errorDescription, "Error description should match JSON data.");
    }

    @Test
    public void testGetDetectedObject() {
        camera = new Camera(1, 0, "./example_input_2/camera_data.json");  
        int currentTime = 3;

        // Retrieve detected objects
        StampedDetectedObjects detected = camera.getDetectedObject(currentTime);

        // Check that the objects retrieved match the JSON data
        assertNotNull(detected, "Detected objects should not be null.");
        assertEquals(3, detected.getDetectedObjects().size(), "Number of detected objects should match JSON data.");
        assertEquals("Wall_3", detected.getDetectedObjects().get(0).getId(), "First object ID should match.");
        assertEquals("Chair_Base_1", detected.getDetectedObjects().get(2).getId(), "Second object ID should match.");
    }

    @Test
    public void testCameraStatusOnError() {
        camera = new Camera(1, 2, "./example_input_2/camera_data.json");  
        // Check that the camera changes its status to ERROR in case of an error
        camera.erorDescripion(8); // Simulate an error at time 7
        assertEquals(STATUS.ERROR, camera.getStatus(), "Camera status should be ERROR.");
    }

    @Test
    void testCameraInitialization() {
        camera = new Camera(1, 2, "./example_input_2/camera_data.json");  
        assertEquals(1, camera.getId(), "Camera ID should be initialized correctly.");
        assertEquals(2, camera.getFrequency(), "Camera frequency should be initialized correctly.");
        assertEquals(STATUS.UP, camera.getStatus(), "Camera status should be UP upon initialization.");
    }
}

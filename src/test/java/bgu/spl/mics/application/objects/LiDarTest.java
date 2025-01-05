package bgu.spl.mics.application.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;



public class LiDarTest {

   
    @Test
    public void testLoadDataFromJson() {
        // Validate the data loaded from the LiDarDataBase
        LiDarDataBase database = LiDarDataBase.getInstance("./example_input_2/lidar_data.json");
        StampedCloudPoints stampedPoint = database.getStampedCloudPoints(1, "Wall_1");

        assertNotNull(stampedPoint, "StampedCloudPoints should not be null for valid time and ID.");
        assertEquals("Wall_1", stampedPoint.getId(), "ID should match the expected value.");
        assertEquals(1, stampedPoint.getTime(), "Time should match the expected value.");
    }

    @Test
    public void testTrackingObjectAtCorrectTime() {
        LiDarWorkerTracker workerTracker = new LiDarWorkerTracker(1, 4, "./example_input_2/lidar_data.json");
        // Simulate a TickBroadcast for tracking at time = 5
        workerTracker.addTrackedObjects(1, "Wall_1", "Wall", 1);
        TrackedObject trackedObject = workerTracker.getTrackedObjects(5);
        assertNotNull(trackedObject, "TrackedObject should not be null for valid time.");
        assertEquals(1, trackedObject.getTime(), "TimeTracked should match the current time.");
        assertEquals("Wall_1", trackedObject.getId(), "ID should match the expected value.");
    }

    @Test
    public void testErrorHandlingWhenDataNotFound() {
        LiDarWorkerTracker workerTracker = new LiDarWorkerTracker(1, 4, "./example_input_2/lidar_data.json");
        // Simulate a condition where data is not found
        workerTracker.addTrackedObjects(99, "Invalid_ID", "No Description", 10);
        STATUS status = workerTracker.geStatus();

        assertEquals(STATUS.ERROR, status, "Status should be set to ERROR when no matching data is found.");
    }

    @Test
public void testGetTrackedObjectsProcessesLastObject() {
    // Reset the database to ensure a clean state
    LiDarDataBase.resetInstance();

    // Initialize LiDarWorkerTracker and LiDarService
    LiDarWorkerTracker workerTracker = new LiDarWorkerTracker(1, 4, "./example_input_2/lidar_data.json");

    // Simulate adding objects
    workerTracker.addTrackedObjects(1, "Wall_2", "Wall", 1);
    workerTracker.addTrackedObjects(2, "Wall_1", "Wall", 2);

    // Process objects until the last one
    workerTracker.getTrackedObjects(1); // Process first object
    workerTracker.getTrackedObjects(2); // Process second object
    workerTracker.getTrackedObjects(3); 
    workerTracker.getTrackedObjects(4); 
    workerTracker.getTrackedObjects(5); 
    workerTracker.getTrackedObjects(6); 


    // Assert that lastTrackedObject is empty
    assertTrue(workerTracker.getLastTrackedObject().isEmpty(),
            "lastTrackedObject should be empty after processing the last object.");

}

}

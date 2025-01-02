package bgu.spl.mics.application.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.services.LiDarService;

public class LiDarTest {
    
    private LiDarWorkerTracker workerTracker;
    private LiDarService lidarService;

    public LiDarTest(){
        // Initialize LiDarWorkerTracker with test data
        workerTracker = new LiDarWorkerTracker(1, 4, "./example_input_2/lidar_data.json");
        // Initialize LiDarService
        lidarService = new LiDarService(workerTracker);
    }

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
        // Simulate a TickBroadcast for tracking at time = 5
        workerTracker.addTrackedObjects(1, "Wall_1", "Wall", 1);
        TrackedObject trackedObject = workerTracker.getTrackedObjects(5);
        assertNotNull(trackedObject, "TrackedObject should not be null for valid time.");
        assertEquals(1, trackedObject.getTime(), "TimeTracked should match the current time.");
        assertEquals("Wall_1", trackedObject.getId(), "ID should match the expected value.");
    }

    @Test
    public void testErrorHandlingWhenDataNotFound() {
        // Simulate a condition where data is not found
        workerTracker.addTrackedObjects(99, "Invalid_ID", "No Description", 10);
        STATUS status = workerTracker.geStatus();

        assertEquals(STATUS.ERROR, status, "Status should be set to ERROR when no matching data is found.");
    }

}

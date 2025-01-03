

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.services.FusionSlamService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FusionSlamTest {

@Test
public void testAddTrackedObjectWithMatchingPose() {
    FusionSlam fusionSlam = FusionSlam.getInstance(5);

    // Create a matching pose and tracked object
    Pose pose = new Pose(5.0f, 5.0f, 90.0f, 1);
    ArrayList<CloudPoint> a = new ArrayList<>();
    CloudPoint c = new CloudPoint(0.1, 0.1);
    a.add(c);
    TrackedObject trackedObject = new TrackedObject("Object1", 1, 1,"noam",a );

    // Add the pose first
    fusionSlam.addPose(pose);

    // Add the tracked object
    fusionSlam.addTrackedObject(trackedObject);

    // Verify that the object was processed and converted into a landmark
    assertEquals(1, fusionSlam.getLandmarks().size(), "One landmark should be added.");
    assertEquals("Object1", fusionSlam.getLandmarks().get(0).getId(), "Landmark ID should match the tracked object ID.");
}
@Test
public void testAddPoseWithoutMatchingTrackedObject() {
    FusionSlam fusionSlam = FusionSlam.getInstance(2);

    // Create a pose
    Pose pose = new Pose(5.0f, 5.0f, 90.0f, 1);

    // Add the pose
    fusionSlam.addPose(pose);

    // Verify that the pose is in the waiting list
    assertEquals(1, fusionSlam.getWaitingPoses().size(), "Pose should be added to the waiting list.");
}

@Test
public void testSearchLandmark() {
    FusionSlam fusionSlam = FusionSlam.getInstance(2);
    ArrayList<CloudPoint> a = new ArrayList<>();
    CloudPoint c = new CloudPoint(0.1, 0.1);
    a.add(c);

    // Create and add a landmark
    LandMark landmark = new LandMark("Object1", "Description",a);
    fusionSlam.getLandmarks().add(landmark);

    // Search for the landmark
    LandMark result = fusionSlam.searchLandMark("Object1");

    // Verify the search result
    assertNotNull(result, "Landmark should be found.");
    assertEquals("Object1", result.getId(), "Landmark ID should match.");
}


@Test
public void testAddOrChangeLM_UpdateExistingLandmarks() {
    FusionSlam fusionSlam = FusionSlam.getInstance(2);
    ArrayList<CloudPoint> a = new ArrayList<>();
    CloudPoint c = new CloudPoint(0.1, 0.1);
    a.add(c);

    // Create an existing LandMark
    LandMark existingLandMark = new LandMark("Object1", "Wall Description", a);
    fusionSlam.getLandmarks().add(existingLandMark);

    // Create a new TrackedObject for the same LandMark ID
    ArrayList<CloudPoint> b = new ArrayList<>();
    CloudPoint d = new CloudPoint(3.0, 3.0);
    CloudPoint e = new CloudPoint(4.0, 4.0);
    b.add(d);
    b.add(e);
    TrackedObject trackedObject = new TrackedObject("Object1", 1,1, "Updated Wall",b);

    // Process the object
    fusionSlam.addTrackedObject(trackedObject);

    // Verify that the LandMark was updated
    LandMark updatedLandMark = fusionSlam.searchLandMark("Object1");
    assertNotNull(updatedLandMark, "LandMark should exist.");
    assertEquals(4, updatedLandMark.getCoordinates().size(), "LandMark should have combined coordinates.");
}

@Test
public void testAddOrChangeLM_WithTransformedCoordinates() {
    FusionSlam fusionSlam = FusionSlam.getInstance(2);

    // Create a pose
    Pose pose = new Pose(5.0f, 5.0f, 90.0f, 1);
    fusionSlam.addPose(pose);
    

    // Create a TrackedObject
    ArrayList<CloudPoint> a = new ArrayList<>();
    CloudPoint c = new CloudPoint(0.1, 0.1);
    a.add(c);
    TrackedObject trackedObject = new TrackedObject("Object2", 1,1, "Rotated Object",a);

    // Process the object
    fusionSlam.addTrackedObject(trackedObject);

    // Verify that the LandMark coordinates are transformed correctly
    LandMark landMark = fusionSlam.searchLandMark("Object2");
    assertNotNull(landMark, "LandMark should exist.");
    List<CloudPoint> globalCoordinates = landMark.getCoordinates();

    // Check transformed coordinates
    assertEquals(5.0, globalCoordinates.get(0).getX(), 0.001, "Transformed X should match.");
    assertEquals(6.0, globalCoordinates.get(0).getY(), 0.001, "Transformed Y should match.");
    assertEquals(4.0, globalCoordinates.get(1).getX(), 0.001, "Transformed X should match.");
    assertEquals(5.0, globalCoordinates.get(1).getY(), 0.001, "Transformed Y should match.");
}
@Test
public void testAddOrChangeLM_DifferentLengthCoordinates() {
    FusionSlam fusionSlam = FusionSlam.getInstance(2);

    // Create an existing LandMark with fewer coordinates
    ArrayList<CloudPoint> a = new ArrayList<>();
    CloudPoint c = new CloudPoint(0.1, 0.1);
    a.add(c);

    LandMark existingLandMark = new LandMark("Object3", "Short List",a);
    fusionSlam.getLandmarks().add(existingLandMark);

    // Create a TrackedObject with more coordinates
    TrackedObject trackedObject = new TrackedObject("Object3", 1,1, "Long List",a);

    // Process the object
    fusionSlam.addTrackedObject(trackedObject);

    // Verify that the LandMark coordinates include all new points
    LandMark updatedLandMark = fusionSlam.searchLandMark("Object3");
    assertNotNull(updatedLandMark, "LandMark should exist.");
    assertEquals(3, updatedLandMark.getCoordinates().size(), "LandMark should contain all new and existing coordinates.");
}

}

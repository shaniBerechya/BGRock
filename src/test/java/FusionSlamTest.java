

import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.ArrayList;

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


}

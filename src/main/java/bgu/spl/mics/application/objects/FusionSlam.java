package bgu.spl.mics.application.objects;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    // Singleton instance holder
    private static class FusionSlamHolder {
        // TODO: Implement singleton instance logic.
    }

    ArrayList<LandMark> Landmarks;
    List<Pose> poses;
    List<Pose> waitingPoses; // List for the Poses that arrive at the Fusion-SLAM before the corresponding TrackedObject.
    List<TrackedObject> waitingTrackedObjects; // List for the unlikely event that the TrackedObject arrives at the Fusion-SLAM before the Pose.

    /********************************************* Constrector ***************************************************/

    /********************************************* Methods ***************************************************/
    /**
     * Calculates and updates the landmarks corresponding to the given {@code trackedObject} using the specified {@code pose}.
     *
     * @param trackedObject the tracked object to be transformed into a {@code LandMark}.
     * @param pose the pose such that {@code pose.time} equals {@code trackedObject.time}.
     * @post Updates the {@code Landmarks} collection:
     *       - If a corresponding {@code LandMark} already exists, improves its accuracy.
     *       - If no matching {@code LandMark} exists, creates a new {@code LandMark} and adds it to {@code Landmarks}.
     */
    public void objectsToLandmarks(TrackedObject trackedObject, Pose pose) {
        // TO DO
    }

    /**
     * Adds a {@code trackedObject} to the system and processes it if a matching {@code pose} exists.
     *
     * @param trackedObject the tracked object to be processed or added to {@code waitingTrackedObjects}.
     * @post
     * - If there is a {@code pose} in {@code waitingPoses} where {@code pose.time} matches {@code trackedObject.time}, 
     *   calls {@code objectsToLandmarks(trackedObject, pose)} and moves the {@code pose} to {@code poses}.
     * - Otherwise, the {@code trackedObject} is added to {@code waitingTrackedObjects}.
     */
    public void addTrackedObject(TrackedObject trackedObject) {
        // TO DO
    }

    /**
     * Adds a {@code pose} to the system and processes it if a matching {@code trackedObject} exists.
     *
     * @param pose the pose to be processed or added to {@code waitingPoses}.
     * @post
     * - If there is a {@code trackedObject} in {@code waitingTrackedObjects} where {@code trackedObject.time} matches {@code pose.time}, 
     *   calls {@code objectsToLandmarks(trackedObject, pose)} and removes the {@code trackedObject} from {@code waitingTrackedObjects}.
     * - Otherwise, the {@code trackedObject} is added to {@code waitingTrackedObjects}.
     */
    public void addPose(Pose pose){
        //TO DO
    }

    /**
     * Searches for a {@code LandMark} with the specified {@code id}.
     *
     * @param id the identifier to search for.
     * @return the {@code LandMark} where {@code LandMark.id} equals the given {@code id}, 
     *         or {@code null} if no such {@code LandMark} exists.
     */
    public LandMark searchLandMark(String id) {
        return null;
    }
    
}

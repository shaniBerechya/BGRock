package bgu.spl.mics.application.objects;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {

    //Fileds:
    private ArrayList<LandMark> landmarks;
    private List<Pose> poses;
    private List<Pose> waitingPoses; // List for the Poses that arrive at the Fusion-SLAM before the corresponding TrackedObject.
    private List<TrackedObject> waitingTrackedObjects; // List for the unlikely event that the TrackedObject arrives at the Fusion-SLAM before the Pose.
    private int numOfSensore;

    /********************************************* Constrector ***************************************************/
    // Private constructor to enforce singleton
    private FusionSlam(int numOfSensore) {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
        this.waitingPoses = new ArrayList<>();
        this.waitingTrackedObjects = new ArrayList<>();
        this.numOfSensore = numOfSensore;
    }

    // Singleton accessor
    public synchronized static FusionSlam getInstance(int numOfSensore) {
        return new FusionSlam(numOfSensore);
    }
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
        // Transform the tracked object's coordinates to global coordinates using the pose
        List<CloudPoint> globalCoordinates = new ArrayList<>();
        for (CloudPoint localPoint : trackedObject.getCoordinates()) {
            // Transform each local CloudPoint to global coordinates
            double transformedX = pose.getX() + localPoint.getX() * Math.cos(Math.toRadians(pose.getYaw()))
                                 - localPoint.getY() * Math.sin(Math.toRadians(pose.getYaw()));
            double transformedY = pose.getY() + localPoint.getX() * Math.sin(Math.toRadians(pose.getYaw()))
                                 + localPoint.getY() * Math.cos(Math.toRadians(pose.getYaw()));
    
            globalCoordinates.add(new CloudPoint(transformedX, transformedY));
        }
        // Search for an existing LandMark with the same ID
        LandMark existingLandMark = searchLandMark(trackedObject.getId());
        if (existingLandMark != null) {
            // Improve existing LandMark with new coordinates
            existingLandMark.improve(globalCoordinates);
        } else {
            // Create a new LandMark and add it to the list
            LandMark newLandMark = new LandMark(
                trackedObject.getId(),
                trackedObject.getDescription(),
                globalCoordinates
            );
            landmarks.add(newLandMark);
        }

        // Add the pose to the processed poses list
        if (!poses.contains(pose)) {
            poses.add(pose);
        }
    }

    //noam is not happy with this method :()
    private Pose findPoseByTime(int time) {
        //searching in poses:
        for (Pose pose : poses) {
            if (pose.getTime() == time) {
                return pose;
            }
        }
        //searching in waitingPoses(if no match in the poses list):
        for (Pose pose : waitingPoses) {
            if (pose.getTime() == time) {
                waitingPoses.remove(pose); // Remove from waiting list
                return pose;
            }
        }
        return null; // No matching pose found
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
        Pose matchingPose = findPoseByTime(trackedObject.getTimeDetected());
        if (matchingPose != null) {
            objectsToLandmarks(trackedObject, matchingPose);
            poses.add(matchingPose);
        } else {
            waitingTrackedObjects.add(trackedObject);
        }
    }

    private TrackedObject findTrackedObjectByTime(int time) {
        for (TrackedObject trackedObject : waitingTrackedObjects) {
            if (trackedObject.getTimeDetected() == time) {
                waitingTrackedObjects.remove(trackedObject); // Remove from waiting list
                return trackedObject;
            }
        }
        return null; // No matching tracked object found
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
    public void addPose(Pose pose) {
        TrackedObject matchingTrackedObject = findTrackedObjectByTime(pose.getTime());
        if (matchingTrackedObject != null) {
            objectsToLandmarks(matchingTrackedObject, pose);
        } else {
            waitingPoses.add(pose);
        }
    }

    /**
     * Searches for a {@code LandMark} with the specified {@code id}.
     *
     * @param id the identifier to search for.
     * @return the {@code LandMark} where {@code LandMark.id} equals the given {@code id}, 
     *         or {@code null} if no such {@code LandMark} exists.
     */
    public LandMark searchLandMark(String id) {
        for (LandMark landMark : landmarks) {
            if (landMark.getId().equals(id)) {
                return landMark;
            }
        }
        return null; // No matching landmark found
    }

    public int getNumOfSensore(){
        return numOfSensore;
    }

    public ArrayList<LandMark> getLandmarks(){
        return landmarks;
    }

    //getters for test

    public List<Pose> getWaitingPoses(){
        return waitingPoses;
    }
    
}

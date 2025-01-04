package bgu.spl.mics.application.objects;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.services.CameraService;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    /********************************************* Fildes ***************************************************/
    private AtomicInteger systemRuntime;
    private AtomicInteger numDetectedObjects;
    private AtomicInteger numTrackedObjects;
    private int numLandmarks;
    private List<LandMark> landMarks;

    //Eror handling:
    private boolean isEror;
    private String errorDescription;
    private MicroService faultySensor;

    private ConcurrentHashMap<String, StampedDetectedObjects> lastDetectedByCamera;
    private ConcurrentHashMap<String, List<CloudPoint>> lastTrackedByLidar;
    private ArrayList<Pose> poses;


    /********************************************* Constrector ***************************************************/
    //Singleton
	private static class StatisticalFolderHolder {
        // The single instance of MessageBusImpl that will be created
        private static final StatisticalFolder INSTANCE = new StatisticalFolder();
    }

    // Static method to get the instance of MessageBusImpl
	public static StatisticalFolder getInstance() {
		return StatisticalFolderHolder.INSTANCE;
	}

    private StatisticalFolder(){
        
        systemRuntime = new AtomicInteger(1);
        numDetectedObjects = new AtomicInteger(1);
        numTrackedObjects = new AtomicInteger(1);
        numLandmarks = 0;
        landMarks = new ArrayList<>();

        isEror = false;
        lastDetectedByCamera = new ConcurrentHashMap<>();
        lastTrackedByLidar = new ConcurrentHashMap<>();
        poses = new ArrayList<>();
    }

    /********************************************* Methods ***************************************************/
    //Geters:
    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    //add by 1:
    public void addOneSystemRuntime() {
        for(;;){
            int current = systemRuntime.get();
            int plusOne = current + 1;
            if (systemRuntime.compareAndSet(current, plusOne)){
                break;
            }
        }
    }

    public void addOneNumDetectedObjects() {
        for(;;){
            int current = numDetectedObjects.get();
            int plusOne = current + 1;
            if (numDetectedObjects.compareAndSet(current, plusOne)){
                break;
            }
        }
    }

    public void addOneNumTrackedObjects() {
        for(;;){
            int current = numTrackedObjects.get();
            int plusOne = current + 1;
            if (numTrackedObjects.compareAndSet(current, plusOne)){
                break;
            }
        }
    }

    //seters:
    public void setLandMarks(List<LandMark> landMarks){
        this.numLandmarks = landMarks.size();
        this.landMarks = landMarks;
    }

    public void setErrorDescription(String errorDescription){
        this.errorDescription = errorDescription;
    }

    public void setFaultySensor(MicroService faultySensor) {
        if (!(faultySensor instanceof CameraService)) {
            this.errorDescription = "Sensor " + faultySensor.getName() + " disconnected";
        }
        this.faultySensor = faultySensor;
        isEror = true;
    }

    //Update the statisticalFolder:
    public void updateForCamera(String cameraName, StampedDetectedObjects detectedObjects){
        addOneNumDetectedObjects();
        lastDetectedByCamera.put(cameraName, detectedObjects);
    }

    public void updateForLiDar(String liDarName, TrackedObject trackedObject){
        addOneNumTrackedObjects();;
        lastTrackedByLidar.put(liDarName, trackedObject.getCoordinates());
    }

    public void updateForGPS(Pose pose){
        poses.add(pose);
    }

    //Other methods:

    public void exportToJson(String filePath) {
        try {
            Map<String, Object> output = new LinkedHashMap<>();
    
            if (isEror) {
                // Case: Error occurred
                output.put("error", errorDescription);
                output.put("faultySensor", faultySensor);
    
                // Last detected by camera
                Map<String, Object> lastCamerasFrame = new LinkedHashMap<>();
                for (Map.Entry<String, StampedDetectedObjects> entry : lastDetectedByCamera.entrySet()) {
                    Map<String, Object> cameraData = new LinkedHashMap<>();
                    cameraData.put("time", entry.getValue().getTime());
                    cameraData.put("detectedObjects", entry.getValue().getDetectedObjects());
                    lastCamerasFrame.put(entry.getKey(), cameraData);
                }
                output.put("last Detected By Camera", lastCamerasFrame);
    
                // Last tracked by LiDar
                Map<String, List<Map<String, Double>>> lastLiDarFrame = new LinkedHashMap<>();
                for (Map.Entry<String, List<CloudPoint>> entry : lastTrackedByLidar.entrySet()) {
                    List<Map<String, Double>> cloudPoints = new ArrayList<>();
                    for (CloudPoint point : entry.getValue()) {
                        Map<String, Double> coordinates = new LinkedHashMap<>();
                        coordinates.put("x", point.getX());
                        coordinates.put("y", point.getY());
                        cloudPoints.add(coordinates);
                    }
                    lastLiDarFrame.put(entry.getKey(), cloudPoints);
                }
                output.put("last Tracked By Lidar", lastLiDarFrame);
                //Add the poses
                output.put("Poses", poses);
    
            } else {
                // Case: No error, include full statistics
                Map<String, Object> statistics = new LinkedHashMap<>();
                statistics.put("systemRuntime", systemRuntime);
                statistics.put("numDetectedObjects", numDetectedObjects);
                statistics.put("numTrackedObjects", numTrackedObjects);
                statistics.put("numLandmarks", numLandmarks);
    
                Map<String, Object> landmarks = new LinkedHashMap<>();
                for (LandMark lm : landMarks) {
                    Map<String, Object> landmarkData = new LinkedHashMap<>();
                    landmarkData.put("id", lm.getId());
                    landmarkData.put("description", lm.getDescription());
                    List<Map<String, Double>> coordinatesList = new ArrayList<>();
                    for (CloudPoint point : lm.getCoordinates()) {
                        Map<String, Double> coordinates = new LinkedHashMap<>();
                        coordinates.put("x", point.getX());
                        coordinates.put("y", point.getY());
                        coordinatesList.add(coordinates);
                    }
                    landmarkData.put("coordinates", coordinatesList);
                    landmarks.put(lm.getId(), landmarkData);
                }
                statistics.put("landmarks", landmarks);
                output.put("statistics", statistics);
            }
    
            // Add poses in both cases
            output.put("poses", poses);
    
            // Write JSON to file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(filePath)) {
                gson.toJson(output, writer);
            }
    
        } catch (IOException e) {
            System.err.println("Error writing output to file: " + e.getMessage());
        }
}


}

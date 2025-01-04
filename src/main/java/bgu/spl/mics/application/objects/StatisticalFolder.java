package bgu.spl.mics.application.objects;

import java.io.File;
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
    private ConcurrentHashMap<String, TrackedObject> lastTrackedByLidar;
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
            isEror = true;
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
        lastTrackedByLidar.put(liDarName, trackedObject);
    }

    public void updateForGPS(Pose pose){
        poses.add(pose);
    }

    //Other methods:

    //Creating the finel json output
    public void exportToJson(String filePath) {
        // Create a map to hold the data
        Map<String, Object> data = new LinkedHashMap<>();
         // If there was an error, add error details
         if (isEror) {
            Map<String, Object> errorDetails = new LinkedHashMap<>();
            errorDetails.put("Error", errorDescription);
            errorDetails.put("faultySensor", faultySensor.getName()); // this is the faultySensor itself and not the string
            errorDetails.put("lastDetectedByCamera", lastDetectedByCamera);
            errorDetails.put("lastTrackedByLidar", lastTrackedByLidar);
            errorDetails.put("poses", poses);
            data.put("errorDetails", errorDetails);
        }
    
        // Add statistics
        data.put("systemRuntime", systemRuntime.get());
        data.put("numDetectedObjects", numDetectedObjects.get());
        data.put("numTrackedObjects", numTrackedObjects.get());
        data.put("numLandmarks", numLandmarks);
    
        // Add world map (landmarks)
        data.put("landMarks", landMarks);
    
    
        // Serialize the map to JSON
        Gson gson = new Gson();
        String json = gson.toJson(data);
        // Manually add new lines where you want them
        json = json.replace("},", "},\n"); // Adds a new line after each JSON object in the array

    
        // Write to file
        String outputPath = filePath + "/output_file.json";
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(json);
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
        }
        }
    
    
}

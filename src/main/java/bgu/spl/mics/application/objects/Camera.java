package bgu.spl.mics.application.objects;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLEngineResult.Status;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    //Fileds:
    int id;
    int frequency;
    String cameraDatasPath;
    enum Status {
        Up, Down, Error
    }
    private Status status = Status.Up;
    List<StampedDetectedObjects> detectedObjectList; 

    /********************************************* Constrector ***************************************************/
    /**
     * Constructs a Camera object with the given parameters.
     *
     * @param id              The unique identifier of the camera.
     * @param frequency       The frequency at which the camera operates.
     * @param cameraDatasPath The file path where the camera's data is stored.
     */
    public Camera(int id,int frequency,String cameraDatasPath){
        this.id = id;
        this.frequency = frequency;
        this.cameraDatasPath = cameraDatasPath;
        this.status = Status.Up;
        loadCameraData();
        
    }

    /********************************************* Methods ***************************************************/
    // Prepares the list of detected objects by reading the data from the camera's data path.
    public void loadCameraData() {
        // Initialize the list to store detected objects with timestamps
        detectedObjectList = new ArrayList<>();
    
        // Create a Gson object for parsing JSON
        Gson gson = new Gson();
    
        // Define the type of the data structure we expect from the JSON file based on the JSON structure
        TypeToken<Map<String, List<Map<String, Object>>>> typeToken = new TypeToken<Map<String, List<Map<String, Object>>>>() {};
    
        try (FileReader reader = new FileReader(cameraDatasPath)) {
            // Read the JSON file into the appropriate data structure
            Map<String, List<Map<String, Object>>> allCameraData = gson.fromJson(reader, typeToken.getType());
    
            // Access the data for this specific camera using the camera ID
            List<Map<String, Object>> cameraData = allCameraData.get("camera" + id);
    
            // Iterate over each entry in the camera data
            for (Map<String, Object> entry : cameraData) {
                Integer time = (Integer) entry.get("time");
                StampedDetectedObjects stampedObjects = new StampedDetectedObjects(time);
    
                // Get the list of detected objects at this timestamp
                List<Map<String, String>> detectedObjects = (List<Map<String, String>>) entry.get("detectedObjects");
    
                // Process each detected object
                for (Map<String, String> obj : detectedObjects) {
                    String objId = obj.get("id");
                    String description = obj.get("description");
    
                    // Create a new DetectedObject and add it to the current stamped object list
                    DetectedObject detectedObject = new DetectedObject(objId, description);
                    stampedObjects.addDetectedObject(detectedObject);
                }
    
                // Add the stamped detected objects to the main list
                detectedObjectList.add(stampedObjects);
            }
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        }
    }
    
    /**
    * @return the {@code frequency}
    */
    public int getFrequency(){
        return frequency;
    }

    /**
    * Retrieves the detected objects from the {@code detectedObjectList} that matches the calculated time: {@code time} - {@code frequency}.
    *
    * @param time current time.
    * @return the {@code StampedDetectedObjects} in {@code detectedObjectList} corresponding to the given {@code time} - {@code frequency}.
    */
    public StampedDetectedObjects getDetectedObject(int time){
       return detectedObjectList.get(time);
    }

    
}

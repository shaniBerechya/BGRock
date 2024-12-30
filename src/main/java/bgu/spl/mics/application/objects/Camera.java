package bgu.spl.mics.application.objects;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    enum status {
        Up, Down, Eror
    }
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
        
    }

    /********************************************* Methods ***************************************************/
    public void loadCameraData() throws IOException {
        try {
            JsonElement jsonElement = JsonParser.parseReader(new FileReader(cameraDatasPath));
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String cameraKey = "camera" + id;
            JsonArray cameraData = jsonObject.getAsJsonArray(cameraKey);

            //iteate thro the data in the json
            for (int i = 0; i < cameraData.size(); i++) {
                JsonObject entry = cameraData.get(i).getAsJsonObject();
                int time = entry.get("time").getAsInt();
                
                StampedDetectedObjects stampedObjects = new StampedDetectedObjects(time);
                
                JsonArray detectedObjectsArray = entry.getAsJsonArray("detectedObjects");
                for (int j = 0; j < detectedObjectsArray.size(); j++) {
                    JsonObject obj = detectedObjectsArray.get(j).getAsJsonObject();
                    String id = obj.get("id").getAsString();
                    String description = obj.get("description").getAsString();
                    
                    DetectedObject detectedObject = new DetectedObject(id, description);
                    stampedObjects.addDetectedObject(detectedObject);
                }
                
                detectedObjectList.add(stampedObjects);
            }
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        }
    }
    
    
    /**
     * Prepares the list of detected objects for a given time by reading the data from the camera's data path.
     * 
     * @param time The time limit at which to search for the detected objects.
     * @post The last detected object in {@code detectedObjectList} should have a timestamp equal to {@code time}.
     */
    public void preparesDate(int time){
        //TO DO
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
       //TO DO
       return null;
    }


    
}

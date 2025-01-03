package bgu.spl.mics.application.objects;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
import java.util.List;


/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    //Fileds:
    private int id;
    private int frequency;
    private String cameraDatasPath;
    private STATUS status;
    private List<StampedDetectedObjects> detectedObjectList; 
    private int index;

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
        this.status = STATUS.UP;

        System.out.println("Loading detected objects...");
        loadDetectedObjectsFromFile(cameraDatasPath, "camera"+id);
        System.out.println("Camera initialization complete.");
        
    }

    /********************************************* Methods ***************************************************/
    // Prepares the list of detected objects by reading the data from the camera's data path.

    
    public void loadDetectedObjectsFromFile(String filePath, String cameraKey) {
        try (FileReader reader = new FileReader(filePath)) {
            System.out.println("Attempting to load JSON from: " + filePath);
    
            // שימוש ב-Gson לקריאת הקובץ
            Gson gson = new Gson();
            java.lang.reflect.Type type = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();
            Map<String, List<StampedDetectedObjects>> cameraData = gson.fromJson(reader, type);
    
            // שליפת הנתונים עבור המצלמה המבוקשת
            List<StampedDetectedObjects> cameraObjects = cameraData.get(cameraKey);
    
            // בדיקה אם יש נתונים
            if (cameraObjects != null) {
                detectedObjectList = new ArrayList<>(cameraObjects);
            } else {
                detectedObjectList = new ArrayList<>();
            }
    
            System.out.println("Camera " + id + " loaded " + detectedObjectList.size() + " detected objects.");
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            detectedObjectList = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            detectedObjectList = new ArrayList<>();
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
        StampedDetectedObjects stampedDetectedObjects = null;
        if(getLastTime()  + frequency - 1 < time){//not sure if we need -1
            status = STATUS.DOWN;
       }
       else  if(findByTime(time - frequency) != null 
       && findByTime(time - frequency).getDetectedObjects() != null){ // we need the second check becuse the input files are no good
            stampedDetectedObjects = findByTime(time - frequency);
       }
       return stampedDetectedObjects;
    }

    private StampedDetectedObjects findByTime(int time){
        for(StampedDetectedObjects objects: detectedObjectList){
            if(objects.getTime() == time){
                return objects;
            }
        }
        return null;
    }

    public int getLastTime(){
        return detectedObjectList.get(detectedObjectList.size() - 1).getTime();
    }

    public STATUS getStatus(){
        return status;
    }

    public int getId(){
        return id;
    }

    public List<StampedDetectedObjects> getdetectedObjectList(){
        return detectedObjectList;
    }

    public String erorDescripion(int time){
        StampedDetectedObjects s = findByTime(time);
        if (s != null && s.erorDescripion() != null){
            this.status = STATUS.ERROR;
            return s.erorDescripion();
        }
        return null;
    }
}
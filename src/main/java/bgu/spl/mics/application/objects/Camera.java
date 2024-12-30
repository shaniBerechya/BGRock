package bgu.spl.mics.application.objects;
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
        //TO DO
    }

    /********************************************* Methods ***************************************************/
    /**
     * Prepares the list of detected objects for a given time by reading the data from the camera's data path.
     * 
     * @param time The time at which to search for the detected objects.
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

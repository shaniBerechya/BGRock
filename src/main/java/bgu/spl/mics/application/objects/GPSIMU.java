package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;



import javax.net.ssl.SSLEngineResult.Status;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    //Fileds:
    private int currentTick;
    private String poseDatasPath;
    private List<Pose> poseList;

    enum Status {
        Up, Down, Error
    }
    private Status status = Status.Up;
    /********************************************* Constrector ***************************************************/
    /**
     * Constructs a Camera object with the given parameters.
     *
     * @param id              The unique identifier of the camera.
     * @param frequency       The frequency at which the camera operates.
     * @param poseDatasPath The file path where the GPSIMU's data is stored.
     */
    public GPSIMU(int id,int frequency,String poseDatasPath){
        this.poseDatasPath = poseDatasPath;
        this.poseList = new ArrayList<>();
        loadPoseData();
    }

    /********************************************* Methods ***************************************************/

    /**
     * Prepares the list of pose  by reading the data from the GPSIMU's data path.
     * 
     * @post the list suold contins the json data
     */
    /**
     * Loads the pose data from a JSON file.
     */
    private void loadPoseData() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(poseDatasPath)) {
            // Define the type of the data structure expected from the JSON file
            TypeToken<List<Pose>> typeToken = new TypeToken<List<Pose>>() {};
            // Deserialize JSON to the expected type (List<Pose>)
            poseList = gson.fromJson(reader, typeToken.getType());
        } 
        catch (IOException e) {
            System.err.println("Error reading pose data JSON file: " + e.getMessage());
            status = Status.Error;
        }
    }

    /**
     * Retrieves the pose from the poseList that matches the given time.
     * @param time The current time for which the pose is requested.
     * @return the Pose at the given time or null if not found.
     */
    public Pose getPose(int time) {
        for (Pose pose : poseList) {
            if (pose.getTime() == time) {
                return pose;
            }
        }
        return null; // Return null if no matching pose is found
    }

    public Status getStatus() {
        return status;
    }
    
    public List<Pose> getPoseList() {
        return poseList;
    }
    

   
}

package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    private STATUS status;
    /********************************************* Constrector ***************************************************/
    /**
     * Constructs a Camera object with the given parameters.
     *
     * @param id              The unique identifier of the camera.
     * @param frequency       The frequency at which the camera operates.
     * @param poseDatasPath The file path where the GPSIMU's data is stored.
     */
    public GPSIMU(String poseDatasPath){
        this.poseDatasPath = poseDatasPath;
        currentTick = 0;
        this.poseList = new ArrayList<>();
        this.status = STATUS.UP;
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
        // Parse the JSON array from the file
        JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();

        for (JsonElement element : jsonArray) {
            JsonObject poseObject = element.getAsJsonObject();

            // Extract pose data fields
            int time = poseObject.get("time").getAsInt();
            float x = poseObject.get("x").getAsFloat();
            float y = poseObject.get("y").getAsFloat();
            float yaw = poseObject.get("yaw").getAsFloat();

            // Create a Pose object and add it to the list
            Pose pose = new Pose(x, y, yaw, time);
            poseList.add(pose);
        }
    } catch (IOException e) {
        System.err.println("Error reading pose data JSON file: " + e.getMessage());
        status = STATUS.ERROR; // Update status to error in case of failure
    } catch (Exception e) {
        System.err.println("Unexpected error while parsing pose data: " + e.getMessage());
        status = STATUS.ERROR;
    }
}

    /**
     * Retrieves the pose from the poseList that matches the given time.
     * @param time The current time for which the pose is requested.
     * @return the Pose at the given time or null if not found.
     */
    public Pose getPose() {
        setTime(currentTick + 1);
        for (Pose pose : poseList) {
            if (pose.getTime() == currentTick) {
                return pose;
            }
        }
        return null; // Return null if no matching pose is found
    }
    public void setTime(int currentTick){
        this.currentTick = currentTick;
        if(currentTick > poseList.get(poseList.size() - 1).getTime()){
            status = STATUS.DOWN;
        }
    }

    public STATUS getStatus() {
        return status;
    }
    
    public List<Pose> getPoseList() {
        return poseList;
    }
    

   
}

package bgu.spl.mics.application.config;

import com.google.gson.*;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;

import java.io.*;
import java.util.*;

public class SystemInitializer {
    private int tickTime;
    private int duration;
    private List<Camera> cameras = new ArrayList<>();
    private List<LiDarWorkerTracker> lidars = new ArrayList<>();
    private GPSIMU gpsimu;
    private FusionSlam fusionSlam;

    public SystemInitializer(String configFile) {
        try {
            String basePath = new File(configFile).getParent(); // Path to the input folder

            // Read the configuration file
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(new FileReader(configFile), JsonObject.class);

            // Initialize time
            tickTime = config.get("TickTime").getAsInt();
            duration = config.get("Duration").getAsInt();

            /// Initialize Cameras
            if (config.has("Cameras")) {
                JsonObject camerasConfig = config.getAsJsonObject("Cameras");
                JsonArray cameraConfigs = camerasConfig.getAsJsonArray("CamerasConfigurations");
                for (JsonElement cameraConfig : cameraConfigs) {
                    JsonObject cameraJson = cameraConfig.getAsJsonObject();
                    Camera camera = new Camera(
                        cameraJson.get("id").getAsInt(),
                        cameraJson.get("frequency").getAsInt(),
                        basePath + "/camera_data.json"
                    );
                    cameras.add(camera);
                }
            }

            // Initialize LiDAR Workers
            if (config.has("LiDarWorkers")) {
                JsonObject lidarConfig = config.getAsJsonObject("LiDarWorkers");
                
                if (lidarConfig.has("LidarConfigurations")) {
                    JsonArray lidarConfigs = lidarConfig.getAsJsonArray("LidarConfigurations");
                    for (JsonElement lidarConfigElement : lidarConfigs) {
                        JsonObject lidarJson = lidarConfigElement.getAsJsonObject();
                        int id = lidarJson.get("id").getAsInt();
                        int frequency = lidarJson.get("frequency").getAsInt();
                        lidars.add(new LiDarWorkerTracker(id, frequency, basePath + "/lidar_data.json"));
                    }
                } else {
                    System.err.println("No LidarConfigurations array found in the LiDarWorkers section.");
                }
            }

            // Initialize GPSIMU
            gpsimu = new GPSIMU(basePath + "/pose_data.json");
            

            // Initialize FusionSlam
            int numOfSensors = cameras.size() + lidars.size() + 1; // +1 for GPSIMU
            fusionSlam = FusionSlam.getInstance(numOfSensors);


        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
        }
    }



    // Getters for the private fields
    public int getTickTime() {
        return tickTime;
    }

    public int getDuration() {
        return duration;
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public List<LiDarWorkerTracker> getLidars() {
        return lidars;
    }

    public GPSIMU getGpsimu() {
        return gpsimu;
    }

    public FusionSlam getFusionSlam() {
        return fusionSlam;
    }
}

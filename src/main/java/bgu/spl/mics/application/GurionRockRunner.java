package bgu.spl.mics.application;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");

        /*********************************************** Initialize System *****************************************************/
        String configFile = args[0];
        String basePath = null;
        //Sensors and other Objects:
        List<Camera> cameras = new ArrayList<>();
        List<LiDarWorkerTracker> lidars = new ArrayList<>();
        GPSIMU gpsimu = null;
        StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();
        FusionSlam fusionSlam = null;
        int tickTime = 0;
        int duration = 0;

        //read from json:
        try {
            basePath = new File(configFile).getParent(); //the path to the input folder
            // Read the configuration file
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(new FileReader(configFile), JsonObject.class);
           
            // Initialize time
            tickTime = config.get("TickTime").getAsInt();
            duration = config.get("Duration").getAsInt();
            
            // Initialize Cameras
            JsonObject camerasConfig = config.getAsJsonObject("Cameras");
            JsonArray cameraConfigs = camerasConfig.getAsJsonArray("CamerasConfigurations");
            String cameraDataPath = camerasConfig.get("camera_datas_path").getAsString();
            for (JsonElement cameraConfig : cameraConfigs) {
                JsonObject cameraJson = cameraConfig.getAsJsonObject();
                int id = cameraJson.get("id").getAsInt();
                int frequency = cameraJson.get("frequency").getAsInt();
                cameras.add(new Camera(id, frequency, basePath + cameraDataPath));
            }
            
            // Initialize LiDar Workers
            JsonObject lidarConfig = config.getAsJsonObject("LiDarWorkers");
            JsonArray lidarConfigs = lidarConfig.getAsJsonArray("LidarConfigurations");
            String lidarDataPath = lidarConfig.get("lidars_data_path").getAsString();
            for (JsonElement lidarConfigItem : lidarConfigs) {
                JsonObject lidarJson = lidarConfigItem.getAsJsonObject();
                int id = lidarJson.get("id").getAsInt();
                int frequency = lidarJson.get("frequency").getAsInt();
                lidars.add(new LiDarWorkerTracker(id, frequency,basePath + lidarDataPath));
            }

            // Initialize GPSIMU
            String poseJsonFile = config.get("poseJsonFile").getAsString();
            gpsimu = new GPSIMU(basePath + poseJsonFile);

            // Initialize FusionSlam
            int numOfSensors = cameras.size() + lidars.size() + 1; //plus one is to represent the GPS
            fusionSlam = FusionSlam.getInstance(numOfSensors);

            // Display initialization summary (optional)
            System.out.println("System Initialized:");
            System.out.println("Cameras: " + cameras.size());
            System.out.println("LiDar Workers: " + lidars.size());
            System.out.println("GPSIMU: Initialized");
            System.out.println("FusionSlam: Initialized with " + numOfSensors + " sensors");

        } catch (IOException e) {
            // Handle any issues with reading the configuration file
            System.err.println("Error reading configuration file: " + e.getMessage());
        }

        /*********************************************** Thred Section *****************************************************/
        //Initialize Threds:
        List<Thread> threads = new ArrayList<>();

        //camera thred:
        for(Camera camera : cameras){
            threads.add(new Thread(new CameraService(camera)));
        }

        //lidar thred:
        for(LiDarWorkerTracker lidar : lidars){
            threads.add(new Thread(new LiDarService(lidar)));
        } 

        //pose thred:
        threads.add(new Thread(new PoseService(gpsimu)));

        //fusion-slam thred:
        Thread fusionThread = new Thread(new FusionSlamService(fusionSlam));

        //time thred:
        Thread timeThread = new Thread(new TimeService(tickTime, duration));

        //Start Threds:
        for(Thread thread : threads){
            thread.start();
        }
        fusionThread.start();

        try {
            Thread.sleep(1000); //sleep to give time for all the micro-service to subscribe for tick
        } catch (Exception e) {
            System.out.println("problem with sleping in main");
        }
        timeThread.start();
        
        try {
            fusionThread.join();
        } catch (InterruptedException e) {
            System.out.println("problem with join of fusionThread");
        }

        /************************************************** Output File *****************************************************/
        statisticalFolder.exportToJson(basePath);

    }


}


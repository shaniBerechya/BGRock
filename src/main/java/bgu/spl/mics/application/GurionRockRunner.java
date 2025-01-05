package bgu.spl.mics.application;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.application.config.SystemInitializer;
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
        String basePath = new File(configFile).getParent(); //the path to the input folder
        SystemInitializer initializer = new SystemInitializer(configFile);

        //Sensors and other Objects:
        List<Camera> cameras = initializer.getCameras();
        List<LiDarWorkerTracker> lidars = initializer.getLidars();
        GPSIMU gpsimu = initializer.getGpsimu();
        FusionSlam fusionSlam = initializer.getFusionSlam();
        int tickTime = initializer.getTickTime();
        int duration = initializer.getDuration();
        StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();

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


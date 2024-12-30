//package bgu.spl.mics.application;
package bgu.spl.mics.example;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;
import bgu.spl.mics.example.services.ExampleMessageSenderService;

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
        MicroService handler = new ExampleBroadcastListenerService("Handler", new String[]{"1"});
        MicroService sender = new ExampleMessageSenderService("sender", new String[]{"broadcast"});
        Thread handlerT = new Thread(handler);
        Thread senderT = new Thread(sender);
        handlerT.start();
        // Sleep for 2 seconds (2000 milliseconds)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        senderT.start(); 
    }
}

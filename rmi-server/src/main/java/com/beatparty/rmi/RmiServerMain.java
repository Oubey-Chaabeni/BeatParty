package com.beatparty.rmi;

import com.beatparty.common.rmi.ClubService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiServerMain {

    public static final int RMI_PORT = 1099;
    public static final String SERVICE_NAME = "ClubService";

    public static void main(String[] args) {
        try {
            JmsEventPublisher publisher = new JmsEventPublisher();
            publisher.init();

            ClubService service = new ClubServiceImpl(publisher);
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind(SERVICE_NAME, service);

            System.out.println("==========================================");
            System.out.println(" Beat Party RMI Server running on :" + RMI_PORT);
            System.out.println(" Bound service: " + SERVICE_NAME);
            System.out.println("==========================================");

            Runtime.getRuntime().addShutdownHook(new Thread(publisher::close));

            // Keep JVM alive while RMI daemon threads handle requests
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("[RMI] Fatal server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
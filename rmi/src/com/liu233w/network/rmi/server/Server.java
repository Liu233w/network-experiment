package com.liu233w.network.rmi.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Server {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(8808);
            MeetingServiceImpl meetingService = new MeetingServiceImpl();
            Naming.rebind("//localhost:8808/MeetingService", meetingService);
        } catch (MalformedURLException me) {
            System.out.println("Malformed URL: " + me.toString());
        } catch (RemoteException re) {
            System.out.println("Remote Exception: " + re.toString());
        }
    }
}

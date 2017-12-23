package com.liu233w.network.exam2.server;

import com.liu233w.network.exam2.share.MessageService;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Server {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(8808);
            MessageService messageService = new MessageServiceImpl();
            Naming.rebind("//localhost:8808/MessageService", messageService);
        } catch (MalformedURLException me) {
            System.out.println("Malformed URL: " + me.toString());
        } catch (RemoteException re) {
            System.out.println("Remote Exception: " + re.toString());
        }
    }
}

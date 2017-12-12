package com.liu233w.network.rmi.client;

import com.liu233w.network.rmi.share.MeetingService;

import java.rmi.Naming;
import java.rmi.RemoteException;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("用法： java [clientName] [hostAddress] [portNumber] login|register [username] [password]");
            return;
        }
        try {
            String url = "//" + args[0] + ":" + args[1] + "/MeetingService";
            MeetingService service = (MeetingService) Naming.lookup(url);
            new ClientHandler(service).process(args);
        } catch (RemoteException rex) {
            System.err.println("Error in lookup: " + rex.toString());
            System.exit(1);
        } catch (java.net.MalformedURLException me) {
            System.err.println("Malformed URL: " + me.toString());
            System.exit(1);
        } catch (java.rmi.NotBoundException ne) {
            System.err.println("NotBound: " + ne.toString());
            System.exit(1);
        }
    }
}

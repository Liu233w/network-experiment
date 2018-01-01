package com.liu233w.network.webservice.server;

import javax.xml.ws.Endpoint;

public class Server {
    public static void main(String[] args) {
        Endpoint.publish("http://localhost:8888/TodoList", new TodoListService());
    }
}

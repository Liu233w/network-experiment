package com.liu233w.network.webservice.client;


import com.liu233w.network.webservice.server.TodoListService;
import com.liu233w.network.webservice.server.TodoListServiceService;

import java.io.IOException;

public class Client {
    public static void main(String[] args) {

        TodoListServiceService service = new TodoListServiceService();
        TodoListService port = service.getTodoListServicePort();

        try {
            new ClientHandler(port).start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LoginFailedException e) {
            e.printStackTrace();
        }
    }
}

package ru.geekbrains.server;

import ru.geekbrains.common.ServerConst;
import ru.geekbrains.common.Server_API;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server implements ServerConst, Server_API{
    private Vector<ClientHandler> clients;
    private AuthService authService;
    public AuthService getAuthService(){
        return authService;
    }
    public Server(){
        ServerSocket serverSocket = null;
        Socket socket = null;
        clients = new Vector<>(); //вектор нужно заменять на коллекцию из java.util.concurrent или Collections.synchronized
        try{
            serverSocket = new ServerSocket(PORT);
            authService = new BaseAuthService();
            authService.start(); //placeholder
            System.out.println("Сервер запущен, ждем клиентов");
            while(true){
                socket = serverSocket.accept(); //ждем подключений, сервер становится на паузу
                clients.add(new ClientHandler(this, socket));
                System.out.println("Клиент подключился");
            }
        }catch(IOException e){
            System.out.println("Ошибка инициализации");
        }finally{
            try{
                serverSocket.close();
                authService.stop();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    public void broadcast(String message){
        for(ClientHandler client : clients){
            client.sendMessage(message);
        }
    }
    public void broadcastUsersList(){
        StringBuffer sb = new StringBuffer(USERS_LIST);
        for(ClientHandler client : clients){
            sb.append(" " + client.getNick());
        }
        broadcast(sb.toString());
    }
    public void sendPrivateMessage(ClientHandler from, String to, String msg){
        boolean nickFound = false;
        for(ClientHandler client : clients){
            if(client.getNick().equals(to)){
                nickFound = true;
                client.sendMessage("from: " + from.getNick() + ": " + msg);
                from.sendMessage("to: " + to + " msg: " + msg);
                break;
            }
        }
        if(!nickFound) from.sendMessage("User not found!");
    }
    public void unsubscribeMe(ClientHandler c){
        clients.remove(c);
        broadcastUsersList();
    }
    public boolean isNickBusy(String nick){
        for(ClientHandler client : clients){
            if(client.getNick().equals(nick)) return true;
        }
        return false;
    }

}

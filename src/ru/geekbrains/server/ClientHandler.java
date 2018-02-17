package ru.geekbrains.server;

import ru.geekbrains.common.Server_API;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Server_API {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private static final String UNDEFINED = "undefined";

    public ClientHandler(Server server, Socket socket){
        try{
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            this.nick = UNDEFINED;
        }catch(IOException e){
            e.printStackTrace();
        }
        new Thread(()-> {
            try{
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith(AUTH)) {
                        String[] elements = message.split(" ");
                        if (elements.length > 1) {
                            String nick = server.getAuthService().getNickByLoginPass(elements[1], elements[2]);
                            if (nick != null) {
                                if (!server.isNickBusy(nick)) {
                                    sendMessage(AUTH_SUCCESSFUl + " " + nick);
                                    this.nick = nick;
                                    server.broadcastUsersList();
                                    server.broadcast(this.nick + " has entered the chat room");
                                    break;
                                } else {
                                    sendMessage("This account is already in use!");
                                    disconnectAfterTime();
                                }
                            } else {
                                sendMessage("Wrong login/password!");
                                disconnectAfterTime();
                            }
                        } else {
                            sendMessage("Wrong login/password!");
                            disconnectAfterTime();
                        }
                    } else {
                        sendMessage("You should authorize first!");
                        disconnect();
                    }
                }
                while(true){
                    String message = in.readUTF();
                    if(message.startsWith(SYSTEM_SYMBOL)){
                        if(message.equalsIgnoreCase(CLOSE_CONNECTION))
                            break;
                        else if(message.startsWith(PRIVATE_MESSAGE)){ // /w nick message
                            String nameTo = message.split(" ")[1];
                            String messageText = message.substring(PRIVATE_MESSAGE.length() + nameTo.length() + 2);
                            server.sendPrivateMessage(this, nameTo, messageText);
                        }else sendMessage("Command doesn't exist!");
                    }else {
                        System.out.println("client " + message);
                        server.broadcast(this.nick + " " + message);
                    }
                }
            }catch(IOException e){
            }finally{
                disconnect();
            }
        }).start();
    }
    public void sendMessage(String msg){
        try{
            out.writeUTF(msg);
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void disconnect(){
        sendMessage(CLOSE_CONNECTION + " You have been disconnected!");
        server.unsubscribeMe(this);
        try{
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void disconnectAfterTime(){
        new Thread(()-> {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (nick.equals(UNDEFINED)){
                disconnect();
            }
        }).start();
    }

    public String getNick(){
        return nick;
    }
}

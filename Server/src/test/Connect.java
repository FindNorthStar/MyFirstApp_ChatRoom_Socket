package test;

import java.net.Socket;

/**
 * Created by FindNS on 2016/4/19.
 */
public class Connect {

    private Socket socket;
    private String username;
    private boolean isSendingEstablish;

    public Connect(Socket socket) {
        this.socket = socket;
    }

    public Connect(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean getIsSendingEstablish() {
        return isSendingEstablish;
    }

    public void setSendingEstablish(boolean sendingEstablish) {
        isSendingEstablish = sendingEstablish;
    }
}

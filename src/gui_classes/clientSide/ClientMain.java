package gui_classes.clientSide;

import shared_classes.User;

public class ClientMain implements Runnable{
    User user;

    public ClientMain(User user) {
        this.user = user;
    }

    public void run(){
        new Frame();
    }
}

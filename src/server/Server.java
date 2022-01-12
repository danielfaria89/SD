package server;

import common.Helpers;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    final static int WORKERS_PER_CONNECTION = 3;

    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(Helpers.PORT);
        DataBase db = new DataBase();

        while(true) {
            Socket s = ss.accept();
            ServerConnection sc = new ServerConnection(s,db);

            for (int i = 0; i < WORKERS_PER_CONNECTION; ++i)
                new Thread(sc).start();
        }

    }
}

/*
        try{
            boolean run=true;
            DataBase base=new DataBase();
            base.addClient("teste",new char[]{'c','o','n','a'});
            ServerSocket serverSocket=new ServerSocket(Helpers.PORT);

            while(run){
                Socket clientsocket=serverSocket.accept();
                new ServerConnection(clientsocket,base).run();
            }
        } catch (IOException | AccountException e) {
            e.printStackTrace();
        }
 */

package server;

import common.Frame;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TaggedConnection implements AutoCloseable {
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Socket socket;
    private final Lock r_lock = new ReentrantLock();
    private final Lock w_lock = new ReentrantLock();

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    public TaggedConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    public void send(Frame frame) throws IOException {
        send(frame.getType(), frame.getData());
    }

    public void send(byte tag, List<byte[]> dataN) throws IOException {
        w_lock.lock();
        try {
            out.write(tag);
            out.writeInt(dataN.size());
            for(byte[] data : dataN){
                out.writeInt(data.length);
                out.write(data);
            }
            out.flush();
        }finally {
            w_lock.unlock();
        }
    }

    public Frame receive() throws IOException {
        r_lock.lock();
        try {
            Frame frame=new Frame();
            frame.deserialize(in);
            return frame;
        }finally {
            r_lock.unlock();
        }
    }
}
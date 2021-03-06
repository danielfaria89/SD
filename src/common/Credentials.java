package common;

import common.Exceptions.WrongFrameTypeException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Credentials {

    private String username;
    private char[] password;

    public Credentials(String username,char[] password){
        this.username=username;
        this.password= Arrays.copyOf(password,password.length);
    }

    public Credentials(Frame frame,boolean register) throws WrongFrameTypeException{
        if(register)
            readFrame_Register(frame);
        else
            this.readFrame(frame);
    }

    public Frame createFrame(){
        Frame frame=new Frame(Frame.LOGIN);
        frame.addBlock(username.getBytes(StandardCharsets.UTF_8));
        frame.addBlock(Helpers.charToBytes(password));
        return frame;
    }

    public Frame createFrame_Register(){
        Frame frame=new Frame(Frame.REGISTER);
        frame.addBlock(username.getBytes(StandardCharsets.UTF_8));
        frame.addBlock(Helpers.charToBytes(password));
        return frame;
    }

    public void readFrame_Register(Frame frame) throws WrongFrameTypeException{
        if(frame.getType()!=Frame.REGISTER)throw new WrongFrameTypeException();
        List<byte[]> data=frame.getData();
        username=new String(data.get(0),StandardCharsets.UTF_8);
        password=Helpers.bytesToChar(data.get(1));
    }

    public void readFrame(Frame frame) throws WrongFrameTypeException{
        if(frame.getType()!=Frame.LOGIN)throw new WrongFrameTypeException();
        List<byte[]> data=frame.getData();
        username=new String(data.get(0),StandardCharsets.UTF_8);
        password=Helpers.bytesToChar(data.get(1));
    }

    public String getUsername(){
        return username;
    }

    public char[] getPassword(){
        return Arrays.copyOf(password,password.length);
    }
}

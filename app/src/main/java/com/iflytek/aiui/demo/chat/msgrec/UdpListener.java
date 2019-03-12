package com.iflytek.aiui.demo.chat.msgrec;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by everrise on 2018/11/29.
 */

public class UdpListener implements Runnable {

    public static final int REMOTE_UDP_PORT = 8084;
    private DatagramSocket socket;
    public String ip;

    public UdpListener(int port) throws SocketException {
        // this.device = device;
        Log.i("receive", "0000000000000000000000000000");
        socket = new DatagramSocket(port);
    }

    public void send(byte[] s, String ip) throws IOException {
        DatagramSocket udpSocket = new DatagramSocket();
        InetAddress address;
        if (ip == null) {
            address = InetAddress.getByName("255.255.255.255");
        } else {
            address = InetAddress.getByName(ip);
        }
        DatagramPacket packet = new DatagramPacket(s, s.length, address, REMOTE_UDP_PORT);
        udpSocket.send(packet);
    }


    @Override
    public void run() {
        try {
            while (true) {

                byte[] buf = new byte[5000];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                ip = packet.getAddress().getHostAddress();
                byte[] data = packet.getData();


                String msg = new String(data, 0, data.length);
                String log = "接收到ip地址为：" + ip + "发来的信息：" + msg;
                Log.i("receive", log);

                if (receiveListener != null) {
                    receiveListener.onReceiveListener(msg);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ReceiveListener receiveListener;

    public void setReceiveListener(ReceiveListener receiveListener) {
        this.receiveListener = receiveListener;
    }

    public interface ReceiveListener {
        void onReceiveListener(String content);
    }

    private byte parseTag(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        } else {
            return data[0];
        }
    }
}

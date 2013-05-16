package com.fenrig.denayer.car_android;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.text.TextUtils;

// TODO: ADD 6666 listener, establish TCP connection
/*
public class ConnectThread extends AsyncTask<String, Integer, Boolean>{
	private WifiManager mWifi;
	
	ConnectThread(WifiManager wifi) {
        mWifi = wifi;
    }
	
	@Override
	protected Boolean doInBackground(String... arg0) {
		try{
			DatagramSocket udp_sock;
			udp_sock = new DatagramSocket(6666);
			udp_sock.setBroadcast(true);
			udp_sock.setSoTimeout(1000);
			String strmsg = "DISCOVER-CAR-SERVER";
			strmsg = String.format("%1$-" + 56 + "s", strmsg);
			byte[] bmsg = strmsg.getBytes();
			InetAddress addr = InetAddress.getByName("255.255.255.255");
			DatagramPacket msg = new DatagramPacket (bmsg, 56, addr, 667);
			byte[] buf = new byte[100];
			// byte buf[100];
			DatagramPacket inmsg = new DatagramPacket(buf, buf.length);
			while(true){
				udp_sock.send(msg);
				try{
					udp_sock.receive(inmsg);
				}catch (SocketTimeoutException e) {
					continue;
				}
				
				
				break;
			}
			Socket sock = new Socket(inmsg.getAddress(), 666);
			myUpdateHandler.setSock(m);
			udp_sock.close();
			Log.e("Discovery", "send success");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Discovery", "error", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Discovery", "error", e);
		}
		return null;
	}
	
}
*/
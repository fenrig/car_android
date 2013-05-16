package com.fenrig.denayer.car_android;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class sender extends Thread{
	private WifiManager mWifi;

    sender(WifiManager wifi) {
        mWifi = wifi;
    }
    
	public void run(){
		try{
			DatagramSocket udp_sock;
			try {
				udp_sock = new DatagramSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			//udp_sock.connect(getBroadcastAddress(), 2000);
			udp_sock.setBroadcast(true);
			String strmsg = "DISCOVER-CAR-SERVER";
			byte[] bmsg = strmsg.getBytes();
			InetAddress addr = InetAddress.getByName("192.168.10.255");
			DatagramPacket msg = new DatagramPacket (bmsg, bmsg.length, addr, 667);
			udp_sock.send(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Sending UDP", "error", e);
		}
	}
	
	private InetAddress getBroadcastAddress() throws IOException {
	    //WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	    DhcpInfo dhcp = mWifi.getDhcpInfo();
	    // handle null somehow

	    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) (broadcast >> (k * 8));
	    return InetAddress.getByAddress(quads);
	}
}

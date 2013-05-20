package com.fenrig.denayer.car_android;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

// TODO: authenticate as remote
// TODO: http://www.mkyong.com/android/android-spinner-drop-down-list-example/

public class MainActivity extends Activity{
	private Button btn_connect;
	private TextView text;
	private Socket con;
	private Spinner spin;
	private List<String> spin_list;
	private PrintWriter pw;
	private InputStream nis;
	private Spinner spin_origin;
	private List<String> origin_list;
	private Spinner spin_destination;
	private List<String> destination_list;
	private threadStateMachine tSM;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); // creates layout from xml-file
		// ------------------------------------
		/*
		 // Not preferred
		TextView tv = new TextView(this);
		tv.setText("Jo Hallo");
		setContentView(tv);
		*/
		/*
		btn_connect = new Button(this);
		btn_connect.setText("Connect");
		//btn_connect.setOnClickListener();
		 * 
		 */
		btn_connect = (Button) findViewById(R.id.btn_connect);
		spin = (Spinner) findViewById(R.id.combo);
		spin_list = new ArrayList<String>();
		spin_origin = (Spinner) findViewById(R.id.spin_origin);
		origin_list = new ArrayList<String>();
		spin_destination = (Spinner) findViewById(R.id.spin_destination);
		destination_list = new ArrayList<String>();
		tSM = new threadStateMachine();
		// -------
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void addToCarChooser(String item){
		spin_list.add(item);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spin_list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(dataAdapter);
	}
	
	public void addRouteNode(String item){
		origin_list.add(item);
		destination_list.add(item);
		ArrayAdapter<String> dataAdapter_origin = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, origin_list);
		ArrayAdapter<String> dataAdapter_destination = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, destination_list);
		dataAdapter_origin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dataAdapter_destination.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin_origin.setAdapter(dataAdapter_origin);
		spin_destination.setAdapter(dataAdapter_destination);
	}
	
	Handler myUpdateHandler = new Handler() {
		public void handleMessage(Message msg){
			RelativeLayout phase1;
			RelativeLayout phase2;
			switch(msg.what){
			case 1:
				phase1 = (RelativeLayout) findViewById(R.id.carchooserlayout);
				phase1.setVisibility(View.VISIBLE);
				Log.e("Discovery", "send success");
				break;
			case 2:
				addToCarChooser(msg.getData().getString("str_item"));
				Log.e("add_item", msg.getData().getString("str_item"));
				break;
			case 3:
				phase1 = (RelativeLayout) findViewById(R.id.carchooserlayout);
				phase1.setVisibility(View.GONE);
				phase2 = (RelativeLayout) findViewById(R.id.routechooser);
				phase2.setVisibility(View.VISIBLE);
				Log.e("Discovery", "send success");
				break;
			case 4:
				addRouteNode(msg.getData().getString("str_item"));
				Log.e("add_item", msg.getData().getString("str_item"));
				break;
			case 5:
				phase1 = (RelativeLayout) findViewById(R.id.carchooserlayout);
				phase2 = (RelativeLayout) findViewById(R.id.routechooser);
				phase1.setVisibility(View.GONE);
				phase2.setVisibility(View.GONE);
			case 6:
				Button btnLock = (Button) findViewById(R.id.btn_lock);
				if(btnLock.getText().equals("Lock"))
					btnLock.setText("Unlock");
				else
					btnLock.setText("Lock");
				break;
			case 7:
				Toast.makeText(getApplicationContext(), 
						msg.getData().getString("notification"), Toast.LENGTH_LONG).show();
			case 8:
				spin_list.clear();
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	public class threadStateMachine{
		private boolean fanny;
		private Object lock = new Object();
		
		threadStateMachine(){
			fanny = false;
		}
		
		public boolean getFanny(){
			synchronized (lock){
				return fanny;
			}
		}
		
		public void setFanny(boolean x){
			synchronized (lock){
				fanny = x;
			}
		}
	}
	
	public class ConnectThread extends AsyncTask<String, Integer, Boolean>{
		private WifiManager mWifi;
		private threadStateMachine tSM;
		
		ConnectThread(WifiManager wifi, threadStateMachine x) {
	        mWifi = wifi;
	        tSM = x;
	    }
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			if(tSM.getFanny()) return null;
			tSM.setFanny(true);
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
						Log.e("Discovery", "timeout");
						continue;
					}
					
					break;
				}
				Socket sock = new Socket(inmsg.getAddress(), 666);
				udp_sock.close();
				
				con = sock;
				pw = new PrintWriter(sock.getOutputStream(),true);
			    nis = sock.getInputStream();
				
			    // AUTH
			    pw.write("advan_remote");
			    pw.flush();
			    
			    tSM.setFanny(false);
			    
			    getCarsThread jeej = new getCarsThread(con, pw, nis, tSM);
				jeej.execute();
			    
			    
			    /*
			    Message m = new Message();
				//m.sety
				Bundle b = new Bundle();
				b.putString("str_item", "jeej");
				m.setData(b);
				m.what = 2;
				myUpdateHandler.sendMessage(m);
				m = new Message();
				m.what = 2;
				m.setData(b);
				b.putString("str_item", "appel");
				myUpdateHandler.sendMessage(m);
				m = new Message();
				m.what = 1;
				myUpdateHandler.sendMessage(m);
				*/
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("Discovery", "error", e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("Discovery", "error", e);
			}
			tSM.setFanny(false);
			
			return null;
		}
		
		
		
	}
	
	public void connect(View arg0) throws IOException{
		// sender jeej = new sender();
		// jeej.run();
		ConnectThread jeej = new ConnectThread((WifiManager) getSystemService(Context.WIFI_SERVICE), tSM);
		jeej.execute();
	}
	
	public void refresh(View arg0) throws IOException{
		getCarsThread jeej = new getCarsThread(con, pw, nis, tSM);
		jeej.execute();
	}
	
	public class getCarsThread extends AsyncTask<String, Integer, Boolean>{
		private Socket sock;
		private PrintWriter pw;
		private InputStream nis;
		private threadStateMachine tSM;
		
		getCarsThread(Socket x, PrintWriter x2, InputStream x3, threadStateMachine x4) {
	        sock = x;
	        pw = x2;
	        nis = x3;
	        tSM = x4;
	    }
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			if(tSM.getFanny()) return null;
			tSM.setFanny(true);
			
			clearCarChooser();
			
			// Get controllable cars
		    pw.write("getCar_V2");
		    pw.flush();
		    
		    
		    byte[] in_buf = new byte[256];
		    try {
				nis.read(in_buf, 0, 256);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return null;
			}
		    
		    String answer = new String(in_buf);
		    Log.e("getCar_V2-Response", answer);
		    
		    if(answer.trim().equals(new String("-"))){
		    	addItemToCarChooser("Geen");
		    }else{
		    	String[] parts = answer.trim().split(";");
		    	for(String it : parts){
		    		if(it != "") addItemToCarChooser(it);
		    	}
		    }
		    
		    makeCarChooserVisible(true);
		    
			tSM.setFanny(false);
			return null;
		}
		
		public void addItemToCarChooser(String item){
			Message m = new Message();
			//m.sety
			Bundle b = new Bundle();
			b.putString("str_item", item);
			m.setData(b);
			m.what = 2;
			myUpdateHandler.sendMessage(m);
		}
		
		public void makeCarChooserVisible(Boolean x){
			Message m = new Message();
			m.what = 1;
			myUpdateHandler.sendMessage(m);
		}
		
		public void clearCarChooser(){
			Message m = new Message();
			m.what = 8;
			myUpdateHandler.sendMessage(m);
		}
		
	}
	
	public class ChooseCarThread extends AsyncTask<String, Integer, Boolean>{
		private Socket sock;
		private PrintWriter out;
		private InputStream in;
		private String car;
		private threadStateMachine tSM;
		
		ChooseCarThread(Socket x, PrintWriter x2, InputStream x3, String x4, threadStateMachine x5) {
	        sock = x;
	        out = x2;
	        in = x3;
	        car = x4;
	        tSM = x5;
	    }
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			if(tSM.getFanny()) return null;
			tSM.setFanny(true);
			
			out.write("set_car");
			out.flush();
			out.write(car);
			out.flush();
			byte[] in_buf = new byte[256];
		    try {
				in.read(in_buf, 0, 256);
			} catch (IOException e) {
				Message m = new Message();
				m.what = 5;
				myUpdateHandler.sendMessage(m);
				tSM.setFanny(false);
				return null;
			}
		    String answer = new String(in_buf);
		    if(answer.trim().equals("set_car failed")){
		    	Log.e("Setting Car", "Failed");
		    	tSM.setFanny(false);
		    	return null;
		    }
		    Log.e("Setting Car", "Succeeded");
		    
		    out.write("get_nodes");
		    out.flush();
		    
		    byte[] in_buf2 = new byte[256];
		    try {
				in.read(in_buf2, 0, 256);
			} catch (IOException e) {
				Message m = new Message();
				m.what = 5;
				myUpdateHandler.sendMessage(m);
				tSM.setFanny(false);
				return null;
			}
		    
		    String answer2 = new String(in_buf2);
		    if(answer2.trim().equals("-")){
		    	addItemToNodeChooser("Geen");
		    }else{
		    	String[] parts = answer2.trim().split(";");
		    	for(String it : parts){
		    		if(it != "") addItemToNodeChooser(it);
		    	}
		    }
		    
		    
		    Message m = new Message();
			m.what = 3;
			myUpdateHandler.sendMessage(m);
			tSM.setFanny(false);
			return null;
		}
		
	}
	
	public class SetDestOriginThread extends AsyncTask<String, Integer, Boolean>{
		private Socket sock;
		private PrintWriter out;
		private InputStream in;
		private String origin;
		private String destination;
		private threadStateMachine tSM;
		
		SetDestOriginThread(Socket x, PrintWriter x2, InputStream x3, String x4, String x5, threadStateMachine x6) {
	        sock = x;
	        out = x2;
	        in = x3;
	        origin = x4;
	        destination = x5;
	        tSM = x6;
	    }
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			if(tSM.getFanny()) return null;
			tSM.setFanny(true);
			
			out.write("set_origin");
			out.flush();
			out.write(origin);
			out.flush();
			
			Message m;
			//m.sety
			Bundle b;
			
			byte[] in_buf = new byte[256];
		    try {
				in.read(in_buf, 0, 256);
			} catch (IOException e) {
				m = new Message();
				m.what = 5;
				myUpdateHandler.sendMessage(m);
				tSM.setFanny(false);
				return null;
			}
		    String answer = new String(in_buf);
		    if(answer.trim().equals("set_origin failed")){
		    	Log.e("Setting Origin", "Failed");
		    	notif("Set Origin failed.");
		    	tSM.setFanny(false);
		    	return null;
		    }
		    Log.e("Setting Origin", "Succeeded");
		    notif("Set Origin succeeded.");
			
			out.write("set_destination");
			out.flush();
			out.write(destination);
			out.flush();
			
			byte[] in_buf2 = new byte[256];
		    try {
				in.read(in_buf2, 0, 256);
			} catch (IOException e) {
				m = new Message();
				m.what = 5;
				myUpdateHandler.sendMessage(m);
				tSM.setFanny(false);
				return null;
			}
		    String answer2 = new String(in_buf2);
		    if(answer2.trim().equals("set_destination failed")){
		    	notif("Set Destination failed.");
		    	Log.e("Setting Destination", "Failed");
		    	tSM.setFanny(false);
		    	return null;
		    }
		    Log.e("Setting Destination", "Succeeded");
		    notif("Set Destination succeeded.");
		    
		    notif("Drives!");
		    
		    tSM.setFanny(false);
			return null;
		}
		
		public void notif(String not){
			Message m = new Message();
		    Bundle b = new Bundle();
		    b.putString("notification", not);
			m.setData(b);
			m.what = 7;
			myUpdateHandler.sendMessage(m);
		}
		
	}
	
	public void choose_car(View arg0) throws IOException{
		
		ChooseCarThread jeej = new ChooseCarThread(con, pw, nis, spin.getSelectedItem().toString(), tSM);
		jeej.execute();
	}
	
	public void addItemToNodeChooser(String item){
		Message m = new Message();
		Bundle b = new Bundle();
		b.putString("str_item", item);
		m.setData(b);
		m.what = 4;
		myUpdateHandler.sendMessage(m);
	}
	
	public void set_dest_origin(View arg0) throws IOException{
		SetDestOriginThread jeej = new SetDestOriginThread(con, pw, nis, spin_origin.getSelectedItem().toString(), spin_destination.getSelectedItem().toString(), tSM);
		jeej.execute();
	}
	
	public void lock(View arg0) throws IOException{
		lockThread jeej = new lockThread(con, pw, nis, tSM);
		jeej.execute();
	}
	
	public class lockThread extends AsyncTask<String, Integer, Boolean>{
		private Socket sock;
		private PrintWriter out;
		private InputStream in;
		private threadStateMachine tSM;
		
		lockThread(Socket x, PrintWriter x2, InputStream x3, threadStateMachine x4) {
	        sock = x;
	        out = x2;
	        in = x3;
	        tSM = x4;
	    }
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			if(tSM.getFanny()) return null;
			tSM.setFanny(true);
			
			out.write("lock");
			out.flush();
			Message m = new Message();
			m.what = 6;
			myUpdateHandler.sendMessage(m);
			
			tSM.setFanny(false);
			return null;
		}
		
	}
	

}

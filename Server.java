import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
	public static boolean ServerType = true;
	private static ServerSocket ss;
	public static TCB_Server[] TCBtable;
	public static Thread segHandler;
	public static ListenThread lt;
	private static int socksr;
	private static int sockfd;
	public static ObjectOutputStream output;
	public static ObjectInputStream input;
	public static Object syncObject;

	public static void main(String[] args) throws Exception {
		System.out.println("Server Ready");

		//call the startOverlay method, throw error if it returns -1
		//call the initSRTServer() method, throw error if it returns -1 //create a srt server sock at port 88 using the createSockSRTServer(88)
		//and assign to socksr, throw error if it returns -1
		//connect to srt client using acceptSRTServer(socksr), throw error if it
		//returns -1
		//for now, just use a Thread.sleep(10000) here
		//disconnect using closeSRTServer(sockfd), throw error if it returns -1 //finally, call stopOverlay(), throw error if it returns -1


		if (startOverlay() == -1) {
			throw new Exception();
		}
		System.out.println("startOverlay");

		if (initSRTServer() == -1) {
			throw new Exception();
		}
		System.out.println("initSRTServer");

		int socksr = createSRTServer(88);
		if (socksr == -1) {
			throw new Exception();
		}
		System.out.println("createSRTServer");

		if (acceptSRTServer(socksr) == -1) {
			throw new Exception();
		}
		System.out.println("acceptSRTServer");

		//placeholder
		Thread.sleep(10000);


		if (closeSRTServer(sockfd) == -1) {
			throw new Exception();
		}
		System.out.println("closeSRTServer");

		if (stopOverlay() == -1) {
			throw new Exception();
		}
		System.out.println("stopOverlay");
		while(segHandler.isAlive()){
			try{
				Thread.sleep(1000);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			System.out.println("Thread is alive");
			lt.stop();
			segHandler.interrupt();
		}
		System.out.println("End program");
	}

	private static int startOverlay() throws IOException {

		try {
		//create a server socket using ss = new ServerSocket(59090); and accept the client connection using s = ss.accept();
		ss = new ServerSocket(59090);
		Socket s = ss.accept();

		output = new ObjectOutputStream(s.getOutputStream());
		input = new ObjectInputStream(s.getInputStream());

		System.out.println("Connected");

		}
		catch(Exception e) {
			System.out.println("Exception: "+e);
			return -1;
		}
		return 0;}

	final private static int initSRTServer() {

		try {

		//This method initializes a TCB table containing TCBServer objects. Finally, the method starts the ListenThread to handle the incoming segments. There is only one Listen- Thread object for the server side which handles call connections for the client.
		TCBtable = new TCB_Server[5]; //keep the name

		}

		catch (Exception e) {
			return -1; }
		return 0;
		}

		private static int createSRTServer(final int sockfd) throws IOException {
			try {
				for(int i=0;i<TCBtable.length;i++){
		      if(TCBtable[i]==null){
		        //Create a new TCB_Client object and return the index of the object in the table
		        TCBtable[i]= new TCB_Server(sockfd);
		        return i;
		      }
		    }
				return -1;
			}
			catch (Exception e) {
				return -1;
			}
		}

	private static int acceptSRTServer(int sockfd) {

		//while(TCBtable[socksr].stateServer != TCB_Server.CONNECTED){

		//}

	try {
		lt = new ListenThread(1, sockfd);
		segHandler = new Thread(lt);
		segHandler.start();
		//This method gets the TCBServer entry using sockfd and changes the state of the connection to LISTENING. It then starts a timer to “busy wait” until the TCB entry’s state changes to CONNECTED (the ListenThread object does this when a SYN is received). It waits in an infinite loop for the state transition before proceeding and to return 1 when the state change happens, dropping out of the busy wait loop.
		TCBtable[sockfd].stateServer = TCB_Server.LISTENING; }
	catch (Exception e) {
		return -1;
	}
	return 0;}


	private static int closeSRTServer(int socksr) {
		//This method removes the TCB entry, obtained using socksr. It returns 1 if succeeded (i.e., was in the right state to complete a close) and -1 if fails (i.e., in the wrong state).
		System.out.println("State: "+TCBtable[socksr].stateServer);
		while(TCBtable[socksr].stateServer != 1){
			//System.out.println("State: "+TCBtable[socksr].stateServer);
			try{
				Thread.sleep(100);
			}
			catch(Exception e){}
		}

		/*synchronized(syncObject){
			try{
				syncObject.wait();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}*/
		System.out.println("State: "+TCBtable[socksr].stateServer);
		int getState = 0;

		if (TCBtable[socksr].stateServer == 1) {
			getState = 1;
		}

		else {
			getState = -1;
		}


		TCBtable[socksr] = null;
		return getState;

	}

	private static int stopOverlay() throws IOException {
		try {
			ss.close();
			input.close();
			output.close();
			lt.myTimer.cancel();
			lt.stop();
			segHandler.interrupt();
			return 0; }

		catch (Exception e) {
			return -1;
		}
	}
}

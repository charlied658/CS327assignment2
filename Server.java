import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static boolean ServerType = true;
	private static ServerSocket ss;
	public static TCB_Server[] TCBtable;
	public static Thread SegHandler;
	private static int socksr;
	private static int sockfd;

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

		if (initSRTServer() == -1) {
			throw new Exception();
		}

		int socksr = createSRTServer(88);
		if (socksr == -1) {
			throw new Exception();
		}

		if (acceptSRTServer(socksr) == -1) {
			throw new Exception();
		}

		else {
			//placeholder
			Thread.sleep(10000);
		}


		if (closeSRTServer(sockfd) == -1) {
			throw new Exception();
		}

		if (stopOverlay() == -1) {
			throw new Exception();
		}}

	private static int stopOverlay() throws IOException {

	try {
		ss.close();
		return 0; }

	catch (Exception e) {
		return -1;
	}}

	private static int startOverlay() throws IOException {

		try {
		//create a server socket using ss = new ServerSocket(59090); and accept the client connection using s = ss.accept();
		ss = new ServerSocket(59091);
		Socket s = ss.accept();

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
		ListenThread lt = new ListenThread(1, 59090);
		SegHandler = new Thread(lt);
		SegHandler.start();
		}

		catch (Exception e) {
			return -1; }
		return 0;
		}

	private static int acceptSRTServer(int sockfd) {


	try {
		//This method gets the TCBServer entry using sockfd and changes the state of the connection to LISTENING. It then starts a timer to “busy wait” until the TCB entry’s state changes to CONNECTED (the ListenThread object does this when a SYN is received). It waits in an infinite loop for the state transition before proceeding and to return 1 when the state change happens, dropping out of the busy wait loop.
		TCBtable[sockfd].stateServer = TCB_Server.LISTENING; }
	catch (Exception e) {
		return -1;
	}
	return 0;}

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


	private static int closeSRTServer(int socksr) {
		//This method removes the TCB entry, obtained using socksr. It returns 1 if succeeded (i.e., was in the right state to complete a close) and -1 if fails (i.e., in the wrong state).

		int getState = 0;

		if (TCBtable[socksr].stateServer == 1) {
			getState = 1;
		}

		else {
			getState = -1;
		}


		TCBtable[socksr] = null;
		return getState;

	}}

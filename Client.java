//Author: Charlie Davidson
//CS327 - Networks
//March 17, 2021

import java.net.*;
import java.io.*;
import java.util.*;
public class Client{

  //Initialize final variables
  static final int SYN_TIMEOUT=100;
  static final int SYN_MAX_RETRY=5;
  static final int FIN_TIMEOUT=100;
  static final int FIN_MAX_RETRY=5;

  //Initialize public variables (will be used in multiple methods and/or classes)
  public static ListenThread listenThread;
  public static int retryCount;
  public static Thread segHandler;
  public static Segment segment;
  public static Socket clientSocket;
  public static ObjectOutputStream output;
  public static ObjectInputStream input;
  public static TCB_Client[] table;

  public static void main(String[] args){

    //Wrap everything in a try/catch statement
    try{
      //Run each method, testing if it returns -1. If it does, throw an exception
      if (startOverlay()==-1){
        throw new Exception();
      }
      System.out.println("startOverlay");
      if (initSRTClient()==-1){
        throw new Exception();
      }
      System.out.println("initSRTClient");
      int socksr = createSockSRTClient(87);
      if (socksr==-1){
        throw new Exception();
      }
      System.out.println("createSockSRTClient");
      if (connectSRTClient(socksr,88)==-1){
        throw new Exception();
      }
      System.out.println("connectSRTClient");
      Thread.sleep(10000);
      if (disconnSRTClient(socksr)==-1){
        throw new Exception();
      }
      System.out.println("disconnSRTClient");
      if (closeSRTClient(socksr)==-1){
        throw new Exception();
      }
      System.out.println("closeSRTClient");
      if (stopOverlay()==-1){
        throw new Exception();
      }
      System.out.println("stopOverlay");
      while(segHandler.isAlive()){
        //Debugging - trying to determine why the thread stays alive even after repeated calling of interrupt()
        System.out.println("Client thread alive");
        Thread.sleep(1000);
        segHandler.interrupt();

      }
    } catch (Exception e){
      //Exception handling
      System.out.println("Exception: "+e);
    }
  }

  //Establish the socket connection with the server
  public static int startOverlay(){
    try{
      InetAddress ip = InetAddress.getByName("localhost");
      clientSocket = new Socket(ip, 59091);
      //clientSocket.connect(addr);
      System.out.println("Client Connected");

      //Create input/output streams
      //output = new ObjectOutputStream(clientSocket.getOutputStream());
      //input = new ObjectInputStream(clientSocket.getInputStream());
      //System.out.println("Client Input/Output Streams Connected");

      //Return 1 if successful
      return 1;
    }
    catch(Exception e){
      //If an exception was caught, print the result (for debugging) and return -1
      System.out.println("Exception: "+e);
      return -1;
    }
  }

  //Initialize the TCB table and start the ListenThread
  public static int initSRTClient(){
    //Create a TCB table of length 5 (arbitrary)
    table = new TCB_Client[5];
    for(int i=0;i<5;i++){
      //Set each entry to null
      table[i]=null;
    }
    //Create and start the ListenThread
    listenThread = new ListenThread(0,0);
    segHandler = new Thread(listenThread);
    segHandler.start();
    return 1;
  }

  //Create the entry in the TCB table
  public static int createSockSRTClient(int client_port){
    //Search through table until a null element is found
    for(int i=0;i<table.length;i++){
      if(table[i]==null){
        //Create a new TCB_Client object and return the index of the object in the table
        table[i]= new TCB_Client(client_port);
        return i;
      }
    }
    //If no null elements were found, return -1
    return -1;
  }

  //Move to SYNSENT phase, begin sending SYN's
  public static int connectSRTClient(int socksr, int server_port){
    try {
      //Assign the server port number
      table[socksr].portNumServer=server_port;
      //Create a new SYN segment
      Segment syn = new Segment(0);
      retryCount=0;

      //Create a new TimerTask which will send the SYN repeatedly
      final Timer timer = new Timer();
      final TimerTask task = new TimerTask(){
        public void run(){
          try{
            //Write the SYN to the OutputStream
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.writeObject(syn);
            System.out.println("Sent SYN");
            //System.out.println(retryCount);
            //System.out.println(SYN_MAX_RETRY);
            //Increment the retryCount variable each time
            Client.retryCount+=1;
          }
          catch(Exception e){
            e.printStackTrace();
          }
        };
      };
      //Set the timer to send a SYN once every SYN_TIMEOUT milliseconds
      timer.schedule(task,0,SYN_TIMEOUT);
      //Set the state to 2, SYNSENT
      table[socksr].stateClient=2;

      //Keep sending the SYN until the maximum number of retries is met or exceeded
      while(retryCount<=SYN_MAX_RETRY){
        //System.out.println("while");
        //If the thread is dead
        if(!segHandler.isAlive()){
          //and the ListenThread received a segment of type 1, SYNACK
          System.out.println("segHandler is dead");
          if(segment.type==1){
            //Cancel the timer
            timer.cancel();
            System.out.println("Received SYNACK");
            //Change the state to 3, CONNECTED
            table[socksr].stateClient=3;

            //Restart the ListenThread and return out of the method
            segHandler = new Thread(listenThread);
            segHandler.start();
            return 1;
          }
          else{
            //If ListenTherad found a Segment which wasn't a SYNACK, start the Thread up again
            segHandler = new Thread(listenThread);
            segHandler.start();
          }
        }
      }
      //System.out.println("test");
      timer.cancel();
      //If the number of retrys exceedd SYN_MAX_RETRY, change state to 1, CLOSED, and return -1
      table[socksr].stateClient=1;
      return -1;
    }
    catch (Exception e){
      e.printStackTrace();
      //System.out.println("Exception: "+e);
      return -1;
    }
  }

  //Move to FINWAIT, begin sending FIN's
  public static int disconnSRTClient(int socksr){
    try{
      //Create a FIN segment
      Segment fin = new Segment(2);
      retryCount=0;

      //Create a timer to send FIN segments through the OutputStream
      final Timer timer = new Timer();
      final TimerTask task = new TimerTask(){
        public void run(){
          try{
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.writeObject(fin);
            System.out.println("Sent FIN");
            Client.retryCount+=1;
          }
          catch(Exception e){
            System.out.println("Exception: "+e);
          }
        };
      };

      //Send a FIN once every FIN_TIMEOUT ms
      timer.schedule(task,0,FIN_TIMEOUT);
      table[socksr].stateClient=4;
      while(retryCount<=FIN_MAX_RETRY){
        if(!segHandler.isAlive()){
          if(segment.type==3){
            //If ListenThread received a segment of type 3, FINACK
            timer.cancel();
            System.out.println("Received FINACK");
            //Change state to 1, CLOSED
            table[socksr].stateClient=1;
            segHandler = new Thread(listenThread);
            segHandler.start();
            //Return out of the method
            return 1;
          }
          else{
            //If ListenThread did not find a FINACK, restart ListenThread
            segHandler = new Thread(listenThread);
            segHandler.start();
          }
        }
      }
      //If FIN_MAX_RETRY was exceeded, change state to CLOSED and return -1
      table[socksr].stateClient=1;
      return -1;
    }
    catch(Exception e){
      //System.out.println("Exception: "+e);
      return -1;
    }
  }

  //Remove item from TCB table
  public static int closeSRTClient(int socksr){
    if(table[socksr].stateClient==1){
      //Change value in table to null
      table[socksr]=null;
      return 1;
    }
    else{
      return -1;
    }
  }

  //Terminate the socket connection to the server
  public static int stopOverlay(){
    try{
      //Write a custom Segment of type -1 to shut down the thread, which is listening for a segment (not the best solution)
      output.writeObject(new Segment(-1));
      //Interrupt the thread
      segHandler.interrupt();

      //Close input/output streams and the socket
      output.close();
      input.close();
      clientSocket.close();
      return 1;
    }
    catch(Exception e){
      return -1;
    }
  }

}

import java.util.*;
import java.io.*;


public class ListenThread extends Thread{
  int type;
  int placeHolder;
  Segment currentSeg;

  final static int CLOSE_WAIT_TIMEOUT = 1000; //The static final variable CLOSE WAIT TIMEOUT should be set to 1 second

  public ListenThread(int clientOrServer, int theSockID){
    if(clientOrServer < 0 || clientOrServer > 1){
      System.out.println("Please enter either 0 or 1");
    }//end if

    type = clientOrServer; //if 0, then client. if 1, then server
    placeHolder = theSockID;
  }//end constructor


  public void run(){
    try{
      if (type == 0){
        System.out.println("Type 0");
        //do client run()
        //The run() method handles incoming segments and cancels the thread on receiving SYNACKs and FINACKs
        currentSeg = (Segment) Client.input.readObject();
        Client.segment=currentSeg;
        //SYN 0, SYNACK 1, FIN 2, FINACK 3, DATA 4, DATAACK 5
        if (currentSeg.type == Segment.SYNACK || currentSeg.type == Segment.FINACK){
          //timer.cancel();
          //System.out.println("Received segment of type "+currentSeg.type);
          Thread.currentThread().interrupt();
        }//end if

        //SYN TIMEOUT: the number of milliseconds to wait for SYNACK before retransmitting SYN should be 100 milliseconds
        //SYN MAX RETRY: the max number of SYN retransmissions in srt client connect() should be 5
        //FIN TIMEOUT: the number of milliseconds to wait for FINACK before retransmitting FIN should be 100 milliseconds
        //FIN MAX RETRY: the max number of FIN retransmissions in srt client disconnect() should be 5


      }//end if

      else if(type == 1){
        //do server run()
        //The run() method handles incoming segments and cancels the thread on receiving SYNs and FINs. The thread must also start a CLOSE WAIT TIMEOUT timer on receiving FIN before changing state to CLOSED
        currentSeg = (Segment) Client.input.readObject();
        //SYN 0, SYNACK 1, FIN 2, FINACK 3, DATA 4, DATAACK 5
        if (currentSeg.type == 0){
          Server.TCBtable[placeHolder].stateServer = 3;
          Segment theSynack = new Segment(1);
          System.out.println("Sending SYNACK");
          Client.output.writeObject(theSynack);
          Thread.currentThread().interrupt();
        }//end if

        else if(currentSeg.type == 2){

          //start CLOSE_WAIT_TIMEOUT timer
          Segment theFinnack = new Segment(3);
          System.out.println("Sending FINACK");
          Client.output.writeObject(theFinnack);
          Timer myTimer = new Timer();
          TimerTask myTask = new TimerTask(){
            public void run(){
            //change state to close
            //CLOSED 1, LISTENING 2, CONNECTED 3, CLOSEWAIT 4
            Server.TCBtable[placeHolder].stateServer = 1;
            };//end run
          }; //end timer task
          myTimer.schedule(myTask, CLOSE_WAIT_TIMEOUT);

          Thread.currentThread().interrupt();
        }//end else if

      }//end elseif type = 1

    }
    catch(Exception e){
      e.printStackTrace();
    }
  }//end run


}//end class ListenThread

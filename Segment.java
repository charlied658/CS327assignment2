public class Segment implements java.io.Serializable{
  int src_port;
  int dest_port;
  int seq_num;
  int length;
  public int type; //SYN 0, SYNACK 1, FIN 2, FINACK 3, DATA 4, DATAACK 5
  int rcv_win;
  int checksum;
  char data[];

  final static int SYN = 0;
  final static int SYNACK = 1;
  final static int FIN = 2;
  final static int FINACK = 3;
  final static int DATA = 4;
  final static int DATAACK = 5;

  public Segment(int whatType){
    if(whatType < 0 || whatType > 5){
      System.out.println("Please enter a value between 0 and 5");
    }//end if
    else{
      type = whatType;
    }//end else

  }//end constructor
}//end class segment

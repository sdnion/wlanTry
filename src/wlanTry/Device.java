package wlanTry;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
/**
 * Class of device in WLAN
 * Sending:set value of channel[id] as partner
 * Receiving:
 * 		sense the value of channel[range] is id. 
 * 		The id of device in "range" sending my id is partner.
 * @author Minzhi
 *
 */
public class Device implements Runnable {

	public static int[] channel;
	CyclicBarrier barrier;
	Object key;
	final int id;
	int count;
	int sendState;
	int receiveState;
	int partner;
	final ArrayList<Integer> senseRange;
	
	int state;
	int time;
	int contentionWindow;
	int packetTx,packetRx,packetTxFails,packetRxFails;
	public Device(int id,CyclicBarrier cb,Object o,ArrayList<Integer> range) {
		this.barrier=cb;
		this.key=o;
		this.id=id;
		this.packetRx=0;
		this.packetTx=0;
		this.packetTxFails=0;
		this.packetRxFails=0;
		this.senseRange=range;
		this.state=0;
    } 
	@Override
	public void run() {
		long begintime = System.nanoTime();
		for (time=0;time<5000;time++){
			if (state==0){
				if (checkChannel()){
					receiveInit();
				}
				if (checkRequest()){
					sendInit();
				}
			}
			if (state==1){
				sendNextStep();
			}
			if (state==2){
				receiveNextStep();
			}
			//if (time%100==0){
				try {
					barrier.await();
				} catch (BrokenBarrierException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//}
		}
		long endtime = System.nanoTime();
		double costTime = (endtime - begintime)/1e9;
		System.out.println(costTime);
	}
	// -----------------------RECEIVE------------------------
	/**
	 * Initialize parameters for receiving. 
	 */
	private void receiveInit() {
		System.out.println(this.time+": MT "+id+" receiving initialized");
		state=2;
		receiveState=0;
		partner=receiveSignal()[0];
		count=0;
	}
	/**
	 * check whether there is packet transmitting in channel.
	 * @return boolean (state of channel).
	 */
	private boolean checkChannel() {
		return (receiveSignal()[1]==id);
	}
	/**
	 * received signal from neighbor terminal
	 * @return 
	 * integer[2]=(id, value);
	 */
	private int[] receiveSignal(){
		int id=-1;
		int val=-1;
		int ret[]=new int[2];
		for (int i=0;i<senseRange.size();i++){
			int k=senseRange.get(i);
			int p=channel[k];
			if (p!=-1){
				if (val==-1){
					val=p;
					id=k;
				}else{
					id=0xFFFF;
					val=0xFFFF;
					break;
				}
			}
		}
		ret[0]=id; ret[1]=val;
		return ret;
	}
	private void receiveNextStep() {
		if (count>0){
			count--;
		}else{
			switch (receiveState){
			case 0:
				if (receiveSignal()[1]==-1){
					receiveState=2;
					count=10;
				}
				else if (receiveSignal()[1]!=id)
					receiveState=1;
				break;
			case 1:
				receiveComplete(false);
				break;
			case 2://start sending ACK
				System.out.println(this.time+": MT "+id+" sending ACK");
				receiveState=3;
				count=40;
				synchronized(this.key){
					channel[id]=100;
				}
				break;
			case 3://finished sending ACK
				System.out.println(this.time+": MT "+id+" finished sending ACK");
				synchronized(this.key){
					channel[id]=-1;
				}
				receiveComplete(true);
			}
		}
	}
	private void receiveComplete(boolean success) {
		if (success){
			this.packetRx++;
			System.out.println(this.time+": MT "+id+" receiving Complete");
		}
		else{
			this.packetRxFails++;
			System.out.println(this.time+": MT "+id+" receiving failed");
		}
		receiveState=-1;
		state=0;
		
	}
	// -----------------------RECEIVE END---------------------
	

	// -----------------------SEND------------------------
	/**
	 * check whether there is packet transmitting in channel.
	 * @return boolean (state of channel).
	 */
	private boolean checkRequest() {
		Random r=new Random();
		return r.nextDouble()<0.01;
	}
	/**
	 * Initialize parameters for sending. 
	 */
	private void sendInit(){
		System.out.println(this.time+": MT "+id+" tranmission initialized");
		state=1;
		sendState=0;
		count=34;
		contentionWindow=16;
		partner=id^1;
	}
	private void sendComplete(boolean success){
		if (success){
			this.packetTx++;
			System.out.println(this.time+": MT "+id+" tranmission successful");
		}
		else{
			this.packetTxFails++;
			System.out.println(this.time+": MT "+id+" tranmission failed");
		}
			sendState=-1;
		state=0;
	}
	private void sendInterrupt(){
		System.out.println(this.time+": MT "+id+" tranmission interruptted");
		sendState=-1;
		receiveInit();
	}
	private void sendNextStep(){
		boolean carrierSense=false;
		//for (int i=0;i<100;i++){
			//if (i==this.id) continue;
		if (receiveSignal()[0]==id){
			if (sendState==0 || sendState==1){
				sendInterrupt();
			}
		}
		if (receiveSignal()[0]!=-1) carrierSense=true; 
		//}
		if (carrierSense){
			switch (sendState){
			case 0://DIFS
				count=28;
				break;
			case 2://Transmitting
				count--;
				break;
			case 3:
				if (receiveSignal()[0]==100){
					count=999;
					sendState=4;
					
				}
				break;
			case 4:
				if (receiveSignal()[0]!=100){
					sendState=5;
					count=0;
				}
				break;
			}
		}else{
			switch (sendState){
			case 4:
				count=0;
				
				break;
			default:
				count--;
				break;
			}
		}
		
		if (count<=0){
			synchronized(this.key){
				switch (sendState){
				case 0://DIFS
					sendState=1;
					count=new Random().nextInt(contentionWindow)*9+9;
					System.out.println(this.time+": MT "+id+" backoff "+count);
					break;
				case 1://Back-off
					sendState=2;
					System.out.println(this.time+": MT "+id+" transmitting");
					count=1000;
					channel[id]=partner;
					break;
				case 2://waiting ACK
					System.out.println(this.time+": MT "+id+" waiting ACK");
					channel[id]=-1;
					sendState=3;
					count=90;//EIFS
					break;
				case 3://No ACK
					System.out.println(this.time+": MT "+id+" No ACK");
					sendComplete(false);
					break;
				case 4://receive ACK
					System.out.println(this.time+": MT "+id+" get ACK");
					sendComplete(true);
					break;
				case 5://No ACK
					System.out.println(this.time+": MT "+id+" ACK failed");
					sendComplete(false);
					break;
				}
			}
		}
	}
	// -----------------------SEND------------------------

}

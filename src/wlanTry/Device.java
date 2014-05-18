package wlanTry;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

class Pair{
	public int id;
	public int value;
};
/**
 * Class of device in WLAN
 * Sending:set value of channel[id] as partner
 * Receiving:
 * 		sense the value of channel[range] is id. 
 * 		The id of device in "range" sending my id is partner.
 * @author Minzhi
 *
 */
public class Device implements Callable<Double> {

	public static int[] channel;
	public static double sumTx=0;
	public static double sumRx=0;

	final int timeLength=1000000;
	
	public int AP;
	CyclicBarrier barrier;
	Object key;
	final int id;
	int count;
	int sendState;
	int receiveState;
	int partner;
	final ArrayList<Integer> senseRange;
	TransmissionRequest request;
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
		this.request=new TransmissionRequest();
		this.AP=-1;
    } 
	@Override
	public Double call() throws Exception {
		for (time=0;time<timeLength;time++){
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

		/*System.out.println(
				costTime+
				": MT "+this.id+
				" Tx "+(double)this.packetTx*12000.0/((double)time/1000000.0)/1000000.0+
				", Rx "+(double)this.packetRx*12000.0/((double)time/1000000.0)/1000000.0
				);*/
		return (double)this.packetTx*24000.0/((double)timeLength/1000000.0)/1000000.0;
	}
	// -----------------------RECEIVE------------------------
	/**
	 * Initialize parameters for receiving. 
	 */
	private void receiveInit() {
		DebugOutput.output(this.time+": MT "+id+" receiving initialized");
		state=2;
		receiveState=0;
		partner=receiveSignal().id;
		count=0;
	}
	/**
	 * check whether there is packet transmitting in channel.
	 * @return boolean (state of channel).
	 */
	private boolean checkChannel() {
		return (receiveSignal().value==id);
	}
	/**
	 * received signal from neighbor terminal
	 * @return 
	 * integer[2]=(id, value);
	 */
	private Pair receiveSignal(){
		Pair ret=new Pair();
		int id=-1;
		int val=-1;
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
		ret.id=id; ret.value=val;
		return ret;
	}
	private void receiveNextStep() {
		if (count>0){
			count--;
		}else{
			int signal=receiveSignal().value;
			switch (receiveState){
			case 0:
				if (signal==-1){
					receiveState=2;
					count=10;
				}
				else if (signal!=id)
					receiveState=1;
				break;
			case 1:
				receiveComplete(false);
				break;
			case 2://start sending ACK
				DebugOutput.output(this.time+": MT "+id+" sending ACK");
				receiveState=3;
				count=40;
				synchronized(this.key){
					channel[id]=100;
				}
				break;
			case 3://finished sending ACK
				DebugOutput.output(this.time+": MT "+id+" finished sending ACK");
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
			DebugOutput.output(this.time+": MT "+id+" receiving Complete");
		}
		else{
			this.packetRxFails++;
			DebugOutput.output(this.time+": MT "+id+" receiving failed");
		}
		receiveState=-1;
		state=0;
		
	}
	// -----------------------RECEIVE END---------------------
	

	// -----------------------SEND------------------------
	public void buildRequestList(double pps, int a, int b, int count){
		request.buildRequestList(pps, a, b, count);
	}
	/**
	 * check whether there is packet transmitting in channel.
	 * @return boolean (state of channel).
	 */
	private boolean checkRequest() {
		if (request.getTime()==null) return false;
		return (this.time>request.getTime().time);
	}
	/**
	 * Initialize parameters for sending. 
	 */
	private void sendInit(){
		DebugOutput.output(this.time+": MT "+id+" tranmission initialized");
		state=1;
		sendState=0;
		count=34;
		contentionWindow=16;
		partner=request.getTime().id;
	}
	private void sendComplete(boolean success){
		if (success){
			this.packetTx++;
			DebugOutput.output(this.time+": MT "+id+" tranmission successful");
			request.popFront();
		}
		else{
			this.packetTxFails++;
			DebugOutput.output(this.time+": MT "+id+" tranmission failed");
		}
			sendState=-1;
		state=0;
	}
	private void sendInterrupt(){
		DebugOutput.output(this.time+": MT "+id+" tranmission interruptted");
		sendState=-1;
		receiveInit();
	}
	private void sendNextStep(){
		boolean carrierSense=false;
		int signal=receiveSignal().value;
		//for (int i=0;i<100;i++){
			//if (i==this.id) continue;
		if (signal==id){
			if (sendState==0 || sendState==1){
				sendInterrupt();
			}
		}
		if (signal!=-1) carrierSense=true; 
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
				if (signal==100){
					count=999;
					sendState=4;
					
				}
				break;
			case 4:
				if (signal!=100){
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
					DebugOutput.output(this.time+": MT "+id+" backoff "+count);
					break;
				case 1://Back-off
					sendState=2;
					DebugOutput.output(this.time+": MT "+id+" transmitting");
					count=1000;
					channel[id]=partner;
					break;
				case 2://waiting ACK
					DebugOutput.output(this.time+": MT "+id+" waiting ACK");
					channel[id]=-1;
					sendState=3;
					count=90;//EIFS
					break;
				case 3://No ACK
					DebugOutput.output(this.time+": MT "+id+" No ACK");
					DebugOutput.outputChannel(this.time);
					sendComplete(false);
					break;
				case 4://receive ACK
					DebugOutput.output(this.time+": MT "+id+" get ACK");
					sendComplete(true);
					break;
				case 5://No ACK
					DebugOutput.output(this.time+": MT "+id+" ACK failed");
					sendComplete(false);
					break;
				}
			}
		}
	}
	// -----------------------SEND-----------------------


}

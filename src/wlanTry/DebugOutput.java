package wlanTry;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.print.attribute.standard.MediaSize.Other;

public class DebugOutput {
	FileOutputStream fos;
	OutputStreamWriter osw;
	BufferedWriter bw;
	StringBuilder sb;
	Integer outputTime;
	public DebugOutput(String s){
		if (isDebug){
			try {
				this.fos=new FileOutputStream(s);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.osw=new OutputStreamWriter(fos);
			this.bw=new BufferedWriter(this.osw);
			sb=new StringBuilder();
		}
	}
	public void close(){
		if (isDebug){
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public int time=0;
	public static final boolean isDebug=Param.isDebug;
	public void outputToFile(){
		if (isDebug && sb.length()>0){
			try {
				bw.write(outputTime.toString()+": ");
				sb.append('\n');
				bw.write(sb.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb=new StringBuilder();
		}
	}
	public void outputInit(int time){
		outputTime=time;
	}
	public void output(String s){
		if (isDebug){
			sb.append(s);
		}
	}
	
	public static void outputNeighbor(int id, ArrayList<Integer> al){
		if (isDebug){
			StringBuilder sb=new StringBuilder();
			sb.append("Neighbor of MT "+id+": ");
			for (int i=0;i<al.size();i++){
				sb.append(al.get(i));
				sb.append(' ');
			}
			System.out.println(sb.toString());
		}
	}
	/*
	public void fileDebugInit(String s){
        try {

            System.setOut(new PrintStream(new FileOutputStream(s)));

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return;
        }
	}*/

	public static void outputAlways(String s){
		System.out.println(s);
	}
	public static void outputAlways(Double s){
		System.out.println(s);
	}

}

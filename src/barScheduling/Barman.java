//M. M. Kuttel 2025 mkuttel@gmail.com
package barScheduling;

import java.util.Random;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;

//Additional imports
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

/*
 Barman Thread class.
 */

public class Barman extends Thread {
	

	private CountDownLatch startSignal;
	private BlockingQueue<DrinkOrder> orderQueue;
	int schedAlg =0;
	int q=10000; //really big if not set, so FCFS
	private int switchTime;
	//long time = System.nanoTime();

	public Map<DrinkOrder,Long> startTimes = new ConcurrentHashMap<>();
	private Map<DrinkOrder,Long> addedTimes = new ConcurrentHashMap<>();
	private Map<DrinkOrder,Long> removedTimes = new ConcurrentHashMap<>();
	private Map<DrinkOrder,Long> waitingTimes = new ConcurrentHashMap<>();
	private Map<DrinkOrder,Long> turnaroundTimes = new ConcurrentHashMap<>();
	private Map<DrinkOrder,Long> endTimes = new ConcurrentHashMap<>();
	
	
	Barman(  CountDownLatch startSignal,int sAlg) {
		//which scheduling algorithm to use
		this.schedAlg=sAlg;
		if (schedAlg==1) this.orderQueue = new PriorityBlockingQueue<>(5000, Comparator.comparingInt(DrinkOrder::getExecutionTime)); //SJF
		else this.orderQueue = new LinkedBlockingQueue<>(); //FCFS & RR
	    this.startSignal=startSignal;
	}
	
	Barman(  CountDownLatch startSignal,int sAlg,int quantum, int sTime) { //overloading constructor for RR which needs q
		this(startSignal, sAlg);
		q=quantum;
		switchTime=sTime;
	}

	public void placeDrinkOrder(DrinkOrder order) throws InterruptedException {
        orderQueue.put(order);
		//Order placed on the queue
		addedTimes.put(order, System.currentTimeMillis());
		
    }

	public long averageWaitingTime(Map<DrinkOrder,Long> waitingTimes){
		
		int total=0;

		for (long time:waitingTimes.values()){
			total+=time;
		}

		return total;
	}

	public long averageTurnaroundTime(Map<DrinkOrder,Long> turnaroundTimes){
		
		int total=0;

		for (long time:turnaroundTimes.values()){
			total+=time;
		}

		return total;
	}
	
	public void run() {
		int interrupts=0;
		long waiting=0; //calculates waiting time on the queue

		try {
			DrinkOrder currentOrder;
			
			startSignal.countDown(); //barman ready
			startSignal.await(); //check latch - don't start until told to do so

			if ((schedAlg==0)||(schedAlg==1)) { //FCFS and non-preemptive SJF
				while(true) {
					currentOrder=orderQueue.take();
					//Order taken off the queue
					removedTimes.put(currentOrder, System.currentTimeMillis());
					//Add waiting time to total waiting time for this order
					waiting=waiting+(removedTimes.get(currentOrder)-addedTimes.get(currentOrder)); 
					waitingTimes.put(currentOrder, waiting);

					System.out.println("---Barman preparing drink for patron "+ currentOrder.toString());
					sleep(currentOrder.getExecutionTime()); //processing order (="CPU burst")

					// code that it adds all the times to make the drink

					System.out.println("---Barman has made drink for patron "+ currentOrder.toString());
					currentOrder.orderDone();

					//Record when the order finishes
					endTimes.put(currentOrder, System.currentTimeMillis());

					sleep(switchTime);//cost for switching orders
				}
			}
			else { // RR 
				int burst=0;
				int timeLeft=0;
				

				System.out.println("---Barman started with q= "+q);

				while(true) {
					System.out.println("---Barman waiting for next order ");
					currentOrder=orderQueue.take();

					//Order taken off the queue for RR
					removedTimes.put(currentOrder, System.currentTimeMillis());
					//Add waiting time to total waiting time for this order
					waiting=waiting+(removedTimes.get(currentOrder)-addedTimes.get(currentOrder)); 
					waitingTimes.put(currentOrder, waiting);


					System.out.println("---Barman preparing drink for patron "+ currentOrder.toString() );
					burst=currentOrder.getExecutionTime();
					if(burst<=q) { //within the quantum
						sleep(burst); //processing complete order ="CPU burst"
						System.out.println("---Barman has made drink for patron "+ currentOrder.toString());
						currentOrder.orderDone();

						//Order has completed 
						endTimes.put(currentOrder, System.currentTimeMillis());
					}
					else {
						sleep(q);
						timeLeft=burst-q;
						System.out.println("--INTERRUPT---preparation of drink for patron "+ currentOrder.toString()+ " time left=" + timeLeft);
						interrupts++;
						currentOrder.setRemainingPreparationTime(timeLeft);
						orderQueue.put(currentOrder); //put back on queue at end
						//Order is put back onto the queue
						addedTimes.put(currentOrder, System.currentTimeMillis());

					}
					sleep(switchTime);//switching orders
					Long startTime = startTimes.get(currentOrder);
					Long endTime = endTimes.get(currentOrder);
					//Write results to a file 
			
					if (startTime!= null && endTime!= null) {

						long turnaroundTime = endTime-startTime;
						turnaroundTimes.put(currentOrder, turnaroundTime);

						long totalWaitingTime = waitingTimes.get(currentOrder);
						
					try (PrintWriter out = new PrintWriter(new FileWriter("results.csv", true))) {
						out.println(currentOrder.toString()+","+totalWaitingTime+","+turnaroundTime);
					} 
					catch (IOException e) {
						e.printStackTrace();
					}
					
					} 
					
					else {
						System.out.println("Missing timestamp(s) for " + currentOrder);
					};

					
				}
			
			}
				
		} 
		
		catch (InterruptedException e1) {
			System.out.println("---Barman is packing up ");
			System.out.println("---number interrupts="+interrupts);
		}

		finally{
			try (PrintWriter out = new PrintWriter(new FileWriter("results.csv", true))) {
				long avgWaiting = averageWaitingTime(waitingTimes);
				long avgTurnaround = averageTurnaroundTime(turnaroundTimes);
				out.printf("Average Waiting Time,%.2f\n", (double) avgWaiting / waitingTimes.size());
				out.printf("Average Turnaround Time,%.2f\n", (double) avgTurnaround / turnaroundTimes.size());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
}



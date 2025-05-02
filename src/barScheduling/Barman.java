//M. M. Kuttel 2025 mkuttel@gmail.com
package barScheduling;

import java.util.Random;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

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

	MetricTracker tracker;


	Barman(  CountDownLatch startSignal,int sAlg) {
		//which scheduling algorithm to use
		this.schedAlg=sAlg;
		if (schedAlg==1) this.orderQueue = new PriorityBlockingQueue<>(5000, Comparator.comparingInt(DrinkOrder::getExecutionTime)); //SJF
		else this.orderQueue = new LinkedBlockingQueue<>(); //FCFS & RR
	    this.startSignal=startSignal;
	}
	
	Barman( MetricTracker tracker, CountDownLatch startSignal,int sAlg,int quantum, int sTime) { //overloading constructor for RR which needs q
		this(startSignal, sAlg);
		q=quantum;
		switchTime=sTime;
		this.tracker = tracker;
	}

	public void placeDrinkOrder(DrinkOrder order) throws InterruptedException {
        orderQueue.put(order);
		
    }
	
	public void run() {
		int interrupts=0;

		try {
			DrinkOrder currentOrder;
			
			startSignal.countDown(); //barman ready
			startSignal.await(); //check latch - don't start until told to do so

			if ((schedAlg==0)||(schedAlg==1)) { //FCFS and non-preemptive SJF
				while(true) {
					currentOrder=orderQueue.take();
					//Order taken off the queue
					tracker.removedTimes.put(currentOrder, System.currentTimeMillis());

					//Add waiting time to total waiting time for this order
					tracker.updateWaiting(currentOrder);
					
					System.out.println("---Barman preparing drink for patron "+ currentOrder.toString());
					sleep(currentOrder.getExecutionTime()); //processing order (="CPU burst")

					// code that it adds all the times to make the drink

					System.out.println("---Barman has made drink for patron "+ currentOrder.toString());
					currentOrder.orderDone();

					//Record when the order finishes
					tracker.endTimes.put(currentOrder, System.currentTimeMillis());

					//Write to file
					tracker.writeOrder(currentOrder);

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
					tracker.removedTimes.put(currentOrder, System.currentTimeMillis());

					//Add waiting time to total waiting time for this order				
					tracker.updateWaiting(currentOrder);

					System.out.println("---Barman preparing drink for patron "+ currentOrder.toString() );
					burst=currentOrder.getExecutionTime();
					if(burst<=q) { //within the quantum
						sleep(burst); //processing complete order ="CPU burst"
						System.out.println("---Barman has made drink for patron "+ currentOrder.toString());
						currentOrder.orderDone();

						//Order has completed 						
						tracker.endTimes.put(currentOrder, System.currentTimeMillis());
					}
					else {
						sleep(q);
						timeLeft=burst-q;
						System.out.println("--INTERRUPT---preparation of drink for patron "+ currentOrder.toString()+ " time left=" + timeLeft);
						interrupts++;
						currentOrder.setRemainingPreparationTime(timeLeft);
						orderQueue.put(currentOrder); //put back on queue at end
						//Order is put back onto the queue						
						tracker.addedTimes.put(currentOrder, System.currentTimeMillis());

					}
					sleep(switchTime);//switching orders

					
				}
			
			}
				
		} 
		
		catch (InterruptedException e1) {
			System.out.println("---Barman is packing up ");
			System.out.println("---number interrupts="+interrupts);
		}

	
	}

	
}



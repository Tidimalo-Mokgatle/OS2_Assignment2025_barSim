//M. M. Kuttel 2025 mkuttel@gmail.com
package barScheduling;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/*class for the patrons at the bar*/

public class Patron extends Thread {
	
	private Random random;// for variation in Patron behaviour
	private CountDownLatch startSignal; //all start at once, actually shared
	private Barman theBarman; //the Barman is actually shared though
	private int ID; //thread ID 
	private int numberOfDrinks;
	private long responseTime;

	private DrinkOrder [] drinksOrder;
	public MetricTracker tracker;

	Patron(MetricTracker tracker, int ID,  CountDownLatch startSignal, Barman aBarman, long seed) {
		this.ID=ID;
		this.startSignal=startSignal;
		this.theBarman=aBarman;
		this.numberOfDrinks=5; // number of drinks is fixed
		drinksOrder = new DrinkOrder[numberOfDrinks];
		if (seed>0) random = new Random(seed);// for consistent Patron behaviour
		else random = new Random();
		this.tracker = tracker;	
	}

	public int getPatronID(){
		return ID;
	}

	public long getResponse(){
		return responseTime;
	}
	
	
//this is what the threads do	
	public void run() {
		try {
			
			//Do NOT change the block of code below - this is the arrival times
			startSignal.countDown(); //this patron is ready
			startSignal.await(); //wait till everyone is ready
			int arrivalTime = ID*50; //fixed arrival for testing
	        sleep(arrivalTime);// Patrons arrive at staggered  times depending on ID 
			System.out.println("+new thirsty Patron "+ this.ID +" arrived"); //Patron has actually arrived
			//End do not change

			
	        for(int i=0;i<numberOfDrinks;i++) {

	        	drinksOrder[i]=new DrinkOrder(this.ID,i); //fixed drink order (=CPU burst), useful for testing
				System.out.println("Order placed by " + drinksOrder[i].toString()); //output in standard format  - do not change this
				
				Long time = System.currentTimeMillis();

				//Patron places first order
				tracker.startTimes.put(drinksOrder[i], time);
				tracker.addOrder(drinksOrder[i]);
				theBarman.placeDrinkOrder(drinksOrder[i]);

				drinksOrder[i].waitForOrder();
				
				System.out.println("Drinking patron " + drinksOrder[i].toString());

				//Records the respose time if it is the first drink
				if(i==1){
			
					responseTime=System.currentTimeMillis()-tracker.getStartTime(drinksOrder[i]);
				}

				sleep(drinksOrder[i].getImbibingTime()); //drinking drink = "IO"
				tracker.burstTimes.put(drinksOrder[i], System.currentTimeMillis()); //adds burst times 
				tracker.updateBurst(drinksOrder[i]);

			}

			//Completion time for a patron (last drink received)
			System.out.println("Patron "+ this.ID + " completed ");

			
		} catch (InterruptedException e1) {  //do nothing
		}
}
}
	
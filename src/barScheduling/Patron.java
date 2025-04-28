//M. M. Kuttel 2025 mkuttel@gmail.com
package barScheduling;


import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/*class for the patrons at the bar*/

public class Patron extends Thread {
	
	private Random random;// for variation in Patron behaviour
	private CountDownLatch startSignal; //all start at once, actually shared
	private Barman theBarman; //the Barman is actually shared though
	private long patronArrivalTime; //real system time of arrival
	private long[] drinkOrderPlacedTimes; //real times when orders are placed
	private long[] drinkReceivedTimes;   //real times when drinks are received

	private int ID; //thread ID 
	private int numberOfDrinks;


	private DrinkOrder [] drinksOrder;
	
	Patron( int ID,  CountDownLatch startSignal, Barman aBarman, long seed) {
		this.ID=ID;
		this.startSignal=startSignal;
		this.theBarman=aBarman;
		this.numberOfDrinks=5; // number of drinks is fixed
		this.drinkOrderPlacedTimes = new long[numberOfDrinks];
		this.drinkReceivedTimes = new long[numberOfDrinks];
		drinksOrder = new DrinkOrder[numberOfDrinks];
		if (seed>0) random = new Random(seed);// for consistent Patron behaviour
		else random = new Random();
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

			//Get patron arrival time 
			patronArrivalTime = System.currentTimeMillis();

			
	        for(int i=0;i<numberOfDrinks;i++) {
	        	drinksOrder[i]=new DrinkOrder(this.ID); //order a drink (=CPU burst)	
				
				//Get time that the patron places their order
				//drinkOrderPlacedTimes[i] = System.currentTimeMillis();

	        	//drinksOrder[i]=new DrinkOrder(this.ID,i); //fixed drink order (=CPU burst), useful for testing
				System.out.println("Order placed by " + drinksOrder[i].toString()); //output in standard format  - do not change this
				theBarman.placeDrinkOrder(drinksOrder[i]);
				drinksOrder[i].waitForOrder();
				System.out.println("Drinking patron " + drinksOrder[i].toString());
				sleep(drinksOrder[i].getImbibingTime()); //drinking drink = "IO"

				//**Patron finishes drink */ = turnaround time
			}

			//Get completion time for a patron 
			//
			System.out.println("Patron "+ this.ID + " completed ");
			
		} catch (InterruptedException e1) {  //do nothing
		}
}
}
	


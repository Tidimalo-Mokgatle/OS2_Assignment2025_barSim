package barScheduling;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetricTracker 
{
    public Map<DrinkOrder,Long> startTimes = new ConcurrentHashMap<>();
	public Map<DrinkOrder,Long> addedTimes = new ConcurrentHashMap<>();
	public Map<DrinkOrder,Long> removedTimes = new ConcurrentHashMap<>();
	public Map<DrinkOrder,Long> waitingTimes = new ConcurrentHashMap<>();
	public Map<DrinkOrder,Long> turnaroundTimes = new ConcurrentHashMap<>();
    public Map<DrinkOrder,Long> burstTimes = new ConcurrentHashMap<>();
	public Map<DrinkOrder,Long> endTimes = new ConcurrentHashMap<>();
    private long waiting=0; //calculates waiting time on the queue
    
    private long responseTime;
    private long turnaroundTime;
    private long burst=0;
    private long drinkingTime;
    private long startTime;
    private long endTime;


    public MetricTracker()
    {

    }

    public void addOrder(DrinkOrder order) throws InterruptedException {
		addedTimes.put(order, System.currentTimeMillis());
    }

    public long getStartTime(DrinkOrder order){
        return startTimes.get(order);
    }
	
	public long averageWaitingTime(){ // returns the average waiting time for one order
		
		int total=0;
		for (long time:waitingTimes.values()){
			total+=time;
		}
		return (long)(total/waitingTimes.size());
	}

	public long averageTurnaroundTime(){ // returns the average turnaround time for one order
		
		int total=0;
		for (long time:turnaroundTimes.values()){
			total+=time;
		}
		return (long)(total/turnaroundTimes.size());
	}

    public long averageBurstTime(){ // returns the average burst time for one order
		
		/*int total=0;
		for (long time:burstTimes.values()){
			total+=time;
		}
        */
		return (long)(burst/burstTimes.size());
	}

    public void writeOrder(DrinkOrder currentOrder){ // writes the data of one drink to a file - waiting time, turnaround time, burst time, drinking time

        //Calculates turnaround time
        startTime = startTimes.get(currentOrder);
		endTime = endTimes.get(currentOrder);
        turnaroundTime = endTime - startTime; 
        turnaroundTimes.put(currentOrder, turnaroundTime);
        Long totalWaitingTime = waitingTimes.get(currentOrder);

        try (PrintWriter out = new PrintWriter(new FileWriter("results.csv", true))) {
            out.println(currentOrder.toString()+","+totalWaitingTime+","+turnaroundTime+","+currentOrder.getExecutionTime()+","+currentOrder.getImbibingTime());
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateWaiting(DrinkOrder currentOrder){ //updates the waiting time for a drink order 

        waiting=waiting+(removedTimes.get(currentOrder)-addedTimes.get(currentOrder));
        waitingTimes.put(currentOrder, waiting);

    }

    public void updateBurst(DrinkOrder currentOrder){ //updates the burst time for a drink order 
        burst=burst+currentOrder.getExecutionTime(); 
    }

    public void writeAverages(){ //writes the average turnaround and waiting time to a file

		try (PrintWriter out = new PrintWriter(new FileWriter("results.csv", true))) {
			out.println("Average Waiting Time = "+averageWaitingTime());
			out.println("Average Turnaround Time = "+averageTurnaroundTime());
            out.println("Average CPU Burst Time = "+averageBurstTime());
            out.println("-------------DONE--------------");
        } 
        
        catch (IOException e) {
				e.printStackTrace();
			}
    }

    
}

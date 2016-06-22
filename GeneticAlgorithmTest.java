import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class GeneticAlgorithmTest {

	public static void main(String[] args)
	{
		String[] functions = {"Schwefel", "Rosenbrock", "Rastrigin"};
		double[][] functionsRange = {{-510,510}, {-2.048,2.048}, {-5.12,5.12}};
		final int POP_SIZE = 300;
		final int TOUR_SIZE = 3;
		final int GENERATIONS = 1000;
		long totalTime = 0;
		
		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
		
		for(int i=0; i<functions.length-2; i++)
		{
			//Variables for stuck handling algorithm
			int startStuckCount=0;
			int endStuckCount=0;
			int stuckRangeCount=0;
			boolean stuck = false;
			double previousMin;
			//
			
			System.out.println(functions[i] + "\n");
			
			long startTime = System.currentTimeMillis();
			Population population = new Population(POP_SIZE, functionsRange[i][0], functionsRange[i][1], functions[i]);
			population.initialize(100);
			System.out.println("Generation 1");
			System.out.println(population.toString() + "\n");
			previousMin = population.getMinValue();
			
			for(int count=2; count<=GENERATIONS; count++)
			//for(int count=2; population.getMinValue()>70; count++)
			{
				//population = population.evolve(3, 0.5);
				
				Population newPopulation = new Population(population.getSize(), 
						population.MIN_CELL_VALUE, population.MAX_CELL_VALUE, functions[i]);
				
				//Calculate the mutation value for the generation
				population.calculateMutationValue(0.5);
				
				List<Future<Void>> voidFutureList = new ArrayList<Future<Void>>();
				
				//Selection & Reproduction & Mutation
				for(int j=0; j<population.getSize(); j+=2)
				{
					voidFutureList.add(exec.submit(new EvolutionThread(population, newPopulation, TOUR_SIZE, j)));
				}
				
				//Wait for all task to finish without shutting down the thread pool
				for(Future<Void> voidFuture: voidFutureList)
				{
					try {
						voidFuture.get();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				population = newPopulation;
				population.calculateFitness();
				if(count%500==0)
				{
					//System.out.println(Population.getMutationConst());
				System.out.println("Generation " + count);
				//System.out.println(population.toString() + "\n");
				System.out.println(population.toString() + functions[i]);
				}
				
				/**********************************************/
				//Algorithm to handle when the GA stuck at a local minimal
				if(!stuck)
				{
					//if(Math.floor(oldMin)==Math.floor(population.getMinValue())&&population.getMinValue()>1)
					if(population.getMinValue()>previousMin)
					{
						startStuckCount++;
						stuckRangeCount=0; //Reset the range
						//If up down up down for 10 times means it is stuck
						if(startStuckCount>10)
						{
							stuck = true;
							Individual.setMutationRate(0.02);
							Population.setMutationConst(0.02);
							startStuckCount=0;
						}
					}
					else
					{
						stuckRangeCount++;
						//Detect the up down up down maximum 5 generations ahead
						if(stuckRangeCount>=5)
						{
							//Minus by 1 instead of directly setting back to 0 in case the
							//stuck was not detected even after 5 generations ahead
							startStuckCount--;
							stuckRangeCount=0;
						}
					}
				}
				else //if stuck
				{
					//Check if the GA is still stuck by looking how small the minimum value decreases
					//Unable to check for stuck that happens below 1
					if(Math.floor(previousMin)==Math.floor(population.getMinValue())&&population.getMinValue()>1)
					{
						if(endStuckCount>0)
						{
							endStuckCount--;
						}
					}
					else
					{
						endStuckCount++;
						//If no stuck for 5 generations, then means the GA no longer stuck 
						if(endStuckCount>5)
						{
							stuck=false;
							Individual.setMutationRate(0.01);
							Population.setMutationConst(1);
							endStuckCount=0;
						}
					}
				}
				previousMin = population.getMinValue(); //Get previous min value
				/**********************************************/
			}
			long endTime = System.currentTimeMillis();
			long currentTime = endTime - startTime;
			totalTime += currentTime;
			System.out.printf("Time: %dms\n\n", currentTime);
		}
		
		exec.shutdown();
		
		System.out.printf("Total time taken for all functions: %dms\n", totalTime);
	}
}

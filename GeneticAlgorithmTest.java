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
		final int GENERATIONS = 10000;
		
		for(int i=1; i<functions.length-1; i++)
		{
			System.out.println(functions[i] + "\n");
			
			long startTime = System.currentTimeMillis();
			Population population = new Population(POP_SIZE, functionsRange[i][0], functionsRange[i][1], functions[i]);
			population.initialize(100);
			System.out.println("Generation 1");
			System.out.println(population.toString() + "\n");
	
			ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
			
			for(int count=2; count<=GENERATIONS; count++)
			//for(int count=2; population.getMinValue()>0.000000001; count++)
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
				//if(count%500==0)
				{
				System.out.println("Generation " + count);
				System.out.println(population.toString() + "\n");
				}	
			}
			exec.shutdown();
			long endTime = System.currentTimeMillis();
			
			System.out.printf("time: %d\n\n", endTime-startTime);
		}
	}
}

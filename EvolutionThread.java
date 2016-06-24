import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class EvolutionThread implements Runnable{

	private Population population, newPopulation;
	private int tourSize;
	private int index;
	private CountDownLatch latch;
	
	public EvolutionThread(Population pop, Population newPop, int size, int i, CountDownLatch l)
	{
		population = pop;
		newPopulation = newPop;
		tourSize = size;
		index = i;
		latch = l;
	}
	
	
	@Override
	public void run()
	{
		//Select best parent
		Individual parent1 = population.tournamentSelection(tourSize);
		Individual parent2 = population.tournamentSelection(tourSize);
		
		//Reproduce new children
		Individual[] children = parent1.reproduce(parent2);
		
		for(Individual child: children)
		{
			if(index<newPopulation.getSize()) //Check for odd population size
			{
				//Mutate children
				child.mutate(population.getMutationValue());
				//Add children into new population
				newPopulation.setIndividual(index++, child);
			}
		}
		latch.countDown();
	}
}

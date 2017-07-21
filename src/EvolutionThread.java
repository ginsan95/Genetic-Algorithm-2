import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class EvolutionThread implements Runnable{

	private Population population;
	private Individual[] newIndividuals;
	private int tourSize, index;
	private double mutationValue, mutationRate;
	private CountDownLatch latch;
	
	public EvolutionThread(Population pop, Individual[] newIndivs, int size, double mValue, 
			double mRate, int i, CountDownLatch l)
	{
		population = pop;
		newIndividuals = newIndivs;
		tourSize = size;
		mutationValue = mValue;
		mutationRate = mRate;
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
			if(index<newIndividuals.length) //Check for odd population size
			{
				//Mutate children
				Individual mutatedChild = child.mutate(mutationValue, mutationRate);
				//Add children into the temporary array to store the individuals
				//newPopulation.setIndividual(index++, mutatedChild);
				newIndividuals[index++] = mutatedChild;
			}
		}
		latch.countDown();
	}
}

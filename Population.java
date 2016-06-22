import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Population {
	
	public final double MIN_CELL_VALUE;
	public final double MAX_CELL_VALUE;
	private final String FUNCTION_TYPE;
	private static double mutationConst = 1; //Constant for stuck handling
	private Individual[] individuals; //Individuals inside the population
	private double maxValue, minValue, averageValue;
	//private AtomicInteger indexCount; //the index for next individual in the population
	private double mutationValue;
	
	public Population(int size, double min, double max, String type)
	{
		individuals = new Individual[size];
		MIN_CELL_VALUE = min;
		MAX_CELL_VALUE = max;
		FUNCTION_TYPE = type;
		//indexCount = new AtomicInteger(0);
	}
	
	//Initialize the population with many random new individuals
	public void initialize(int length)
	{
		//Apply threading to create each individual to speed up the process
		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
		List<Future<Individual>> individualFutureList = new ArrayList<Future<Individual>>();
		for(int i=0; i<individuals.length; i++)
		{
			individualFutureList.add(exec.submit(
					new CreateIndividualThread(length, MIN_CELL_VALUE, MAX_CELL_VALUE, FUNCTION_TYPE)));
		}
		for(int i=0; i<individuals.length; i++)
		{
			try {
				individuals[i] = individualFutureList.get(i).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		exec.shutdown();
		calculateFitness(); //Calculate fitness of new population
	}
	
	public void initialize(int length, int a)
	{
		for(int i=0; i<individuals.length; i++)
		{
			individuals[i] = new Individual(length, MIN_CELL_VALUE, MAX_CELL_VALUE, FUNCTION_TYPE);
		}
		calculateFitness(); //Calculate fitness of new population
	}
	
	//Calculate the dynamic mutation value of the population
	public void calculateMutationValue(double value)
	{
		//calculateFitness();
		double k = 0;
		if(minValue > 0.008)
		//if(minValue > 0.007)
		{
			//k = 0.05;
			k=0.08;
			//k = 0.0005;
			//k = 0.04;
			//System.out.println("k = " + 0.1);
		}
		else
		{
			//k = 1.5;
			k=2.0;
			//k = 2.0;
			//System.out.println("k = " + 3.5);
		}
		
		//Use the minValue*k of the population as the rate for mutation if it is bigger than the caller rate
		mutationValue = minValue*k > value? value: minValue*k;
		mutationValue *= mutationConst;
		//trueValue = value;
		
		//System.out.println(mutationValue);
	}
	
	//Tournament selection to select best individual among the players
	public Individual tournamentSelection(int size)
	{
		Random rand = new Random();
		Individual fittest = individuals[rand.nextInt(individuals.length)];
		
		for(int i=1; i<size; i++)
		{
			int selectionIndex = rand.nextInt(individuals.length);
			Individual currentIndividual = individuals[selectionIndex];
			if(currentIndividual.getMinValue() < fittest.getMinValue())
			//if(Math.abs(currentIndividual.getMinValue()) < Math.abs(fittest.getMinValue()))
			{
				fittest = currentIndividual;
			}
		}
		
		return fittest;
	}
	
	//Calculate the fitness of the population
	public void calculateFitness()
	{
		double currentMin = Double.POSITIVE_INFINITY;
		double currentMax = Double.NEGATIVE_INFINITY;
		double total = 0.0;
		
		for(Individual myIndividual: individuals)
		{
			if(myIndividual.getMinValue() < currentMin)
			{
				currentMin = myIndividual.getMinValue();
			}
			if(myIndividual.getMinValue() > currentMax)
			{
				currentMax = myIndividual.getMinValue();
			}
			total += myIndividual.getMinValue();
		}
		
		minValue = currentMin;
		maxValue = currentMax;
		averageValue = total/individuals.length;
	}
	
	/*public void addIndividual(Individual i)
	{
		individuals[indexCount.getAndIncrement()] = i;
	}*/
	
	public void setIndividual(int index, Individual i)
	{
		individuals[index] = i;
	}
	
	public Individual getIndividual(int index)
	{
		return individuals[index];
	}
	
	public int getSize()
	{
		return individuals.length;
	}
	
	public double getMutationValue()
	{
		return mutationValue;
	}
	
	public void setMutationValue(double value)
	{
		mutationValue = value;
	}
	
	public static double getMutationConst()
	{
		return mutationConst;
	}
	
	public static void setMutationConst(double value)
	{
		mutationConst = value;
	}
	
	public double getMinValue()
	{
		return minValue;
	}
	
	public double getMaxValue()
	{
		return maxValue;
	}
	
	public double getAverageValue()
	{
		return averageValue;
	}
	
	@Override
	public String toString()
	{
		return String.format("Min value: %.12f\nMax value: %.12f\nAverage: %.12f", minValue, maxValue, averageValue);
	}
}

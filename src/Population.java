import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class Population {
	
	public final double MIN_CELL_VALUE;
	public final double MAX_CELL_VALUE;
	public final String FUNCTION_TYPE;
	//private static double mutationConst = 1; //Constant for stuck handling
	private final Individual[] individuals; //Individuals inside the population
	private final double minValue, maxValue, averageValue;
	//private AtomicInteger indexCount; //the index for next individual in the population
	//private double mutationValue;
	
	//1st way to create population by specify size and individual size - random
	public Population(int size, int length, double min, double max, String type)
	{
		individuals = new Individual[size];
		MIN_CELL_VALUE = min;
		MAX_CELL_VALUE = max;
		FUNCTION_TYPE = type;
		
		initialize(100); //Initialize the population
		
		//After initialization, directly calculate the fitness the population
		//and these data will not be changed for the population anymore
		double[] data = calculateFitness();
		minValue = data[0];
		maxValue = data[1];
		averageValue = data[2];
		//indexCount = new AtomicInteger(0);
	}
	
	//2nd way to create population by specify its individuals list
	public Population(Individual[] indvs, double min, double max, String type)
	{
		individuals = indvs;
		MIN_CELL_VALUE = min;
		MAX_CELL_VALUE = max;
		FUNCTION_TYPE = type;
		
		//Directly calculate the fitness the population
		//and these data will not be changed for the population anymore
		double[] data = calculateFitness();
		minValue = data[0];
		maxValue = data[1];
		averageValue = data[2];
		//indexCount = new AtomicInteger(0);
	}
	
	//Initialize the population with many random new individuals
	private void initialize(int length)
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
		//calculateFitness(); //Calculate fitness of new population
	}
	
	//Calculate the dynamic mutation value of the population
	public double calculateMutationValue(double value)
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
		return minValue*k > value? value: minValue*k;
		//mutationValue *= mutationConst;
		//trueValue = value;
		
		//System.out.println(mutationValue);
	}
	
	//Overloaded method for stuck handling
	public double calculateMutationValue(double value, double mutationConst)
	{
		return calculateMutationValue(value) * mutationConst;
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
	private double[] calculateFitness()
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
		
		double average = total/individuals.length;
		
		//return all the calculated data to its caller
		return new double[]{currentMin, currentMax,average};
		
		//minValue = currentMin;
		//maxValue = currentMax;
		//averageValue = total/individuals.length;
	}
	
	/*public void addIndividual(Individual i)
	{
		individuals[indexCount.getAndIncrement()] = i;
	}*/
	
	/*
	public void setIndividual(int index, Individual i)
	{
		individuals[index] = i;
	}*/
	
	public Individual getIndividual(int index)
	{
		return individuals[index];
	}
	
	public int getSize()
	{
		return individuals.length;
	}
	
	/*
	public double getMutationValue()
	{
		return mutationValue;
	}
	
	public void setMutationValue(double value)
	{
		mutationValue = value;
	}*/
	
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

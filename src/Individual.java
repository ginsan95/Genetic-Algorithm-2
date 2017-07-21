import java.util.Random;

public final class Individual{
	
	//public static final double MIN_CELL_VALUE = -5.12;
	//public static final double MAX_CELL_VALUE = 5.12;
	//public static final double MIN_CELL_VALUE = -2.048;
	//public static final double MAX_CELL_VALUE = 2.048;
	//public static final double MIN_CELL_VALUE = -510;
	//public static final double MAX_CELL_VALUE = 510;
	public final double MIN_CELL_VALUE;
	public final double MAX_CELL_VALUE;
	public final String FUNCTION_TYPE;
	//private static double mutationRate = 0.01;
	private final Random rand = new Random();
	private final double[] genes; //Actual look of the individual
	private final double minValue; //Represent the fitness of the individual
	
	
	/*private Individual(double min, double max, String type)
	{
		MIN_CELL_VALUE = min;
		MAX_CELL_VALUE = max;
		FUNCTION_TYPE = type;
	}*/
	
	//1st way to create individual by specify size - random genes
	public Individual(int length, double min, double max, String type)
	{
		//this(min, max, type);
		MIN_CELL_VALUE = min;
		MAX_CELL_VALUE = max;
		FUNCTION_TYPE = type;
		
		//Create random genes
		genes = new double[length];
		for(int i=0; i<genes.length; i++)
		{
			genes[i] = MIN_CELL_VALUE + (MAX_CELL_VALUE - MIN_CELL_VALUE) * rand.nextDouble();
		}
		
		minValue = calculateSum();
	}
	
	//2nd way to create individual by specify its genes
	public Individual(double[] g, double min, double max, String type)
	{
		//this(min, max, type);
		MIN_CELL_VALUE = min;
		MAX_CELL_VALUE = max;
		FUNCTION_TYPE = type;
		genes = g;
		minValue = calculateSum();
	}
	
	//Calculate the fitness based on the function
	public double calculateSum()
	{
		double sum = 0;
		/*
		for(int i=1; i<=genes.length; i++)
		{
			sum += i*(genes[i-1]*genes[i-1]);
		}*/
		
		switch(FUNCTION_TYPE)
		{
			case "Schwefel":
				double alpha = 418.982887;
				for(int i=0; i<genes.length; i++)
				{
					sum += -genes[i] * Math.sin(Math.sqrt(Math.abs(genes[i])));
				}
				sum += alpha*genes.length;
				break;
			case "Rosenbrock":
				for(int i=0; i<genes.length-1; i++)
				{
					sum += 100*(Math.pow((Math.pow(genes[i], 2) - genes[i+1]), 2))
							+ Math.pow((1-genes[i]), 2);
				}
				break;
			case "Rastrigin":
				for(int i=0; i<genes.length; i++)
				{
					sum += Math.pow(genes[i], 2) - 10*Math.cos(2*Math.PI*genes[i]);
				}
				sum += 10*genes.length;
				break;
		}
		
		//Rosenbrock
		/*
		for(int i=0; i<genes.length-1; i++)
		{
			sum += 100*(Math.pow((Math.pow(genes[i], 2) - genes[i+1]), 2))
					+ Math.pow((1-genes[i]), 2);
		}*/
		
		//Schwefel
		/*
		double alpha = 418.982887;
		for(int i=0; i<genes.length; i++)
		{
			sum += -genes[i] * Math.sin(Math.sqrt(Math.abs(genes[i])));
		}
		sum += alpha*genes.length;*/
		
		//Rastrigin
		/*
		for(int i=0; i<genes.length; i++)
		{
			sum += Math.pow(genes[i], 2) - 10*Math.cos(2*Math.PI*genes[i]);
		}
		sum += 10*genes.length;*/
		return sum;
	}
	
	//Reproduce with another individual
	public Individual[] reproduce(Individual i2)
	{
		//70% chance to create children, 30% chance to return parents themselves
		if(rand.nextDouble() < 0.7)
		{
			return crossover(i2);
		}
		else
		{
			return new Individual[]{this, i2};
		}
	}
	
	//Create children
	private Individual[] crossover(Individual i2)
	{
		double[] genes1 = new double[genes.length];
		double[] genes2 = new double[genes.length];
		
		//Get a cross point in the array and clone the genes accordingly
		int crossPoint = 1+rand.nextInt(genes.length-1);
		myClone(genes1, genes, 0, crossPoint);
		myClone(genes1, i2.genes, crossPoint, i2.genes.length);
		//myClone(genes2, i2.genes, 0, crossPoint);
		//myClone(genes2, genes, crossPoint, genes.length);
		crossClone(genes2, genes, crossPoint, genes.length, 0);
		crossClone(genes2, i2.genes, 0, crossPoint, genes.length-crossPoint);
		
		//Return the children with those genes
		return new Individual[]{new Individual(genes1, MIN_CELL_VALUE, MAX_CELL_VALUE, FUNCTION_TYPE), 
				new Individual(genes2, MIN_CELL_VALUE, MAX_CELL_VALUE, FUNCTION_TYPE)};
	}
	
	//Mutate the chromosome of the genes
	public Individual mutate(double value, double rate)
	{	
		double[] newGenes = new double[genes.length];
		
		for(int i=0; i<genes.length; i++)
		{
			//Each cell/chromosome has 1% change to mutate
			if(rand.nextDouble()<rate)
			{
				double mutateRand = -value + (value + value) * rand.nextDouble();
				//If exceed range do not mutate
				if(genes[i] + mutateRand >= MIN_CELL_VALUE && genes[i] + mutateRand <= MAX_CELL_VALUE)
				{
					newGenes[i] = genes[i] += mutateRand;
				}
				else //copy original gene
				{
					newGenes[i] = genes[i];
				}
			}
			else //if no mutate then copy original gene
			{
				newGenes[i] = genes[i];
			}
		}
		//Calculate the fitness for the new mutated genes
		//minValue = calculateSum();
		return new Individual(newGenes, MIN_CELL_VALUE, MAX_CELL_VALUE, FUNCTION_TYPE);
	}
	
	
	//Clone genes based on specific range
	private void myClone(double[] array, double[] array2, int start, int end)
	{
		for(int i=start; i<end; i++)
		{
			array[i] = array2[i];
		}
	}
	
	//Clone genes by reversing the position of the chromosome
	private void crossClone(double[] array, double[] array2, int cStart, int cEnd, int start)
	{
		for(int i=cStart; i<cEnd; i++)
		{
			array[start++] = array2[i];
		}
	}
	
	//Get method for minValue
	public double getMinValue()
	{
		return minValue;
	}
	
	//Print the genes for the individual
	public void printGenes()
	{
		for(int i=0; i<genes.length; i++)
		{
			System.out.print(genes[i] + ", ");
			if((i+1)%20==0)
			{
				System.out.println();
			}
		}
	}
}

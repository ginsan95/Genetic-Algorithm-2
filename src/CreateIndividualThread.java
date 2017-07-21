import java.util.concurrent.Callable;


public class CreateIndividualThread implements Callable<Individual>{

	private int length;
	private final double MIN_CELL_VALUE;
	private final double MAX_CELL_VALUE;
	private final String FUNCTION_TYPE;
	
	public CreateIndividualThread(int l, double min, double max, String type)
	{
		length = l;
		MIN_CELL_VALUE = min;
		MAX_CELL_VALUE = max;
		FUNCTION_TYPE = type;
	}

	@Override
	public Individual call() throws Exception
	{
		return new Individual(length, MIN_CELL_VALUE, MAX_CELL_VALUE, FUNCTION_TYPE);
	}
	
}

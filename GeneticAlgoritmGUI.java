import java.awt.EventQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class GeneticAlgoritmGUI {

	private JFrame frame;
	private JTable schwefelTable;
	private JLabel schwefelTimeLabel;
	private JLabel lblRosenbrock;
	private JTable rosenbrockTable;
	private JScrollPane scrollPane_1;
	private JLabel rosenbrockTimeLabel;
	private JLabel lblNewLabel_1;
	private JTable rastriginTable;
	private JLabel rastriginTimeLabel;
	private JScrollPane scrollPane_2;
	private static JLabel totalTimeLabel;
	
	private static DefaultTableModel[] tableModelArray;
	private static JLabel[] timeLabelArray;
	private JButton btnNewButton;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GeneticAlgoritmGUI window = new GeneticAlgoritmGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void startGeneticAlgorithm()
	{
		String[] functions = {"Schwefel", "Rosenbrock", "Rastrigin"};
		double[][] functionsRange = {{-510,510}, {-2.048,2.048}, {-5.12,5.12}};
		final int POP_SIZE = 300;
		final int TOUR_SIZE = 3;
		final int GENERATIONS = 1000;
		long totalTime = 0;
		
		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
		
		for(int i=0; i<functions.length; i++)
		{
			//Variables for stuck handling algorithm
			int startStuckCount=0;
			int endStuckCount=0;
			int stuckRangeCount=0;
			boolean stuck = false;
			double previousMin;
			//
			
			double mutationValue = 0.05;
			double mutationRate = 0.01;
			
			System.out.println(functions[i] + "\n");
			
			long startTime = System.currentTimeMillis();
			Population population = new Population(POP_SIZE, 100, functionsRange[i][0], functionsRange[i][1], functions[i]);
			tableModelArray[i].addRow(new Object[]{
					"Generation 1", population.getMinValue(), 
					population.getMaxValue(), population.getAverageValue()
			});
			previousMin = population.getMinValue();
			
			for(int count=2; count<=GENERATIONS; count++)
			//for(int count=2; population.getMinValue()>70; count++)
			{
				//population = population.evolve(3, 0.5);
				
				//Create a temporary individual array to store the all new children
				Individual[] newIndividuals = new Individual[population.getSize()];
				
				//Calculate the mutation value for the generation
				mutationValue = stuck? population.calculateMutationValue(0.5, 0.02): 
					population.calculateMutationValue(0.5);
				
				//List<Future<Void>> voidFutureList = new ArrayList<Future<Void>>();
				CountDownLatch latch = new CountDownLatch((int)Math.ceil(population.getSize()/2.0));
				
				//Selection & Reproduction & Mutation
				for(int j=0; j<population.getSize(); j+=2)
				{
					exec.execute(new EvolutionThread(population, newIndividuals, 
							TOUR_SIZE, mutationValue, mutationRate, j, latch));
				}
				
				//Wait for all task to finish without shutting down the thread pool
				try {
					latch.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Create a new population based on the individual array which contains all the children
				Population newPopulation = new Population(newIndividuals, population.MIN_CELL_VALUE, 
						population.MAX_CELL_VALUE, population.FUNCTION_TYPE);
				
				//Override the old population with the enw population
				population = newPopulation;
				//if(count%500==0)
				{
					//System.out.println(Population.getMutationConst());
				tableModelArray[i].addRow(new Object[]{
						"Generation " + count, String.format("%.12f", population.getMinValue()), 
						String.format("%.12f", population.getMaxValue()),
						String.format("%.12f", population.getAverageValue())
				});
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
							mutationRate = 0.02;
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
							mutationRate = 0.01;
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
			//System.out.printf("Time: %dms\n\n", currentTime);
			timeLabelArray[i].setText("Time taken : " + currentTime + "ms");
		}
		
		exec.shutdown();
		totalTimeLabel.setText("Total time taken : " + totalTime + "ms");
		//System.out.printf("Total time taken for all functions: %dms\n", totalTime);
	}

	/**
	 * Create the application.
	 */
	public GeneticAlgoritmGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1000, 621);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		//Schwefel
		JLabel lblNewLabel = new JLabel("Schwefel");
		lblNewLabel.setBounds(10, 11, 77, 14);
		frame.getContentPane().add(lblNewLabel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 36, 900, 134);
		frame.getContentPane().add(scrollPane);
		
		DefaultTableModel schwefelModel = new DefaultTableModel(
				new Object[][]{},
				new String[]{"Generations", "Min value", "Max Value", "Average"});
		schwefelTable = new JTable(schwefelModel);
		scrollPane.setViewportView(schwefelTable);
		
		schwefelTimeLabel = new JLabel("Time Taken : ");
		schwefelTimeLabel.setBounds(106, 11, 213, 14);
		frame.getContentPane().add(schwefelTimeLabel);
		
		//Rosenbrock
		lblRosenbrock = new JLabel("Rosenbrock");
		lblRosenbrock.setBounds(10, 180, 77, 14);
		frame.getContentPane().add(lblRosenbrock);
		
		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 205, 900, 134);
		frame.getContentPane().add(scrollPane_1);
		
		DefaultTableModel rosenbrockModel = new DefaultTableModel(
				new Object[][]{},
				new String[]{"Generations", "Min value", "Max Value", "Average"});
		rosenbrockTable = new JTable(rosenbrockModel);
		scrollPane_1.setViewportView(rosenbrockTable);
		
		rosenbrockTimeLabel = new JLabel("Time Taken : ");
		rosenbrockTimeLabel.setBounds(106, 181, 241, 14);
		frame.getContentPane().add(rosenbrockTimeLabel);
		
		//Rastrigin
		lblNewLabel_1 = new JLabel("Rastrigin");
		lblNewLabel_1.setBounds(10, 349, 77, 14);
		frame.getContentPane().add(lblNewLabel_1);
		
		scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(10, 374, 900, 134);
		frame.getContentPane().add(scrollPane_2);
		
		DefaultTableModel rastriginModel = new DefaultTableModel(
				new Object[][]{},
				new String[]{"Generations", "Min value", "Max Value", "Average"});
		rastriginTable = new JTable(rastriginModel);
		scrollPane_2.setViewportView(rastriginTable);
		
		rastriginTimeLabel = new JLabel("Time Taken : ");
		rastriginTimeLabel.setBounds(106, 350, 241, 14);
		frame.getContentPane().add(rastriginTimeLabel);
		
		totalTimeLabel = new JLabel("Total time taken : ");
		totalTimeLabel.setBounds(10, 519, 309, 14);
		frame.getContentPane().add(totalTimeLabel);
		
		//Others
		btnNewButton = new JButton("New button");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Thread timeThread = new Thread(new Runnable() {
				//	public void run() {
				for(DefaultTableModel model: tableModelArray)
				{
					model.getDataVector().clear();
					model.fireTableStructureChanged();
				}
				btnNewButton.setEnabled(false);
				startGeneticAlgorithm();
				btnNewButton.setEnabled(true);
				//	}
				//});
				//timeThread.start();
			}
		});
		btnNewButton.setBounds(10, 548, 188, 23);
		frame.getContentPane().add(btnNewButton);
		
		tableModelArray = new DefaultTableModel[3];
		tableModelArray[0] = schwefelModel;
		tableModelArray[1] = rosenbrockModel;
		tableModelArray[2] = rastriginModel;
		
		timeLabelArray = new JLabel[3];
		timeLabelArray[0] = schwefelTimeLabel;
		timeLabelArray[1] = rosenbrockTimeLabel;
		timeLabelArray[2] = rastriginTimeLabel;
	}
}

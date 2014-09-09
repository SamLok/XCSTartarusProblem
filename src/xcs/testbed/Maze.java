package xcs.testbed;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import xcs.Environment;
import xcs.XCS;
import xcs.XCSConstants.UpdateMethod;
import xcs.XCSConstantsBuilder;
import xcs.stats.StatsLogger;

public class Maze implements Environment<StringState, Maze.Action>{
	public static enum Action{
		NORTH_WEST,
		NORTH,
		NORTH_EAST,
		WEST,
		EAST,
		SOUTH_WEST,
		SOUTH,
		SOUTH_EAST;
	}

	private String[][] maze;
	private List<Point> openPoints;
	private List<Point> finalPoints;
	private int lastX, lastY;
	private int x, y;

//	private static final int trials = 30;
	private static final int trials = 1;
	private static final int captureInterval = 50;

	private static final String O_ENCODING = "00"; //Open points
	private static final String T_ENCODING = "01"; //
	private static final String OOB_ENCODING = "10"; //Out of Bound
	private static final String F_ENCODING = "11"; //Final State

	private int count;

	public Maze(String file){
		openPoints = new ArrayList<Point>();
		finalPoints = new ArrayList<Point>();
		try{
			BufferedReader input = new BufferedReader(new FileReader(new File(file)));
			try{
				maze = new String[Integer.parseInt(input.readLine())][];
				for(int y = 0;y < maze.length;y ++){
					String line = input.readLine();
					maze[y] = new String[line.length()];
					for(int x = 0;x < line.length();x ++){

						//Encode each character
						switch(line.charAt(x)){
						case 'O':
							maze[y][x] = O_ENCODING;
							openPoints.add(new Point(x, y));
							break;
						case 'T':
							maze[y][x] = T_ENCODING;
							break;
						case 'F':
							maze[y][x] = F_ENCODING;
							finalPoints.add(new Point(x, y));
							break;
						default:
							throw new IllegalArgumentException("Maze file contains invalid chars(" + line.charAt(x) + ")");
						}
					}
				}
			}
			finally{
				input.close();
			}
		}
		catch(IOException e){
			System.err.println("Error reader input file: ");
			e.printStackTrace();
		}

		lastX = -1;
		lastY = -1;

		randomizePosition();
	}

	private void randomizePosition(){
		Point random = openPoints.get((int)(Math.random() * openPoints.size()));
		x = random.x;
		y = random.y;
	}

	private String getEncoding(int x, int y){
		if(x < 0 || y < 0 || x >= maze[0].length || y >= maze.length){
			return OOB_ENCODING;
		}

		return maze[y][x];
	}

	private boolean isValidPosition(int x, int y){
		if(x < 0 || y < 0 || x >= maze[0].length || y >= maze.length)return false;
		return !maze[y][x].equals(T_ENCODING);
	}

	@Override
	public StringState getState() {
		return getState(x, y);
	}

	private StringState getState(int x, int y){
		StringBuilder state = new StringBuilder();
		for(int yp = y - 1;yp <= y + 1;yp ++){
			for(int xp = x - 1;xp <= x + 1;xp ++){
				if(xp == x && yp == y)continue;
				state.append(getEncoding(xp, yp));
			}
		}

		return new StringState(state.toString());
	}

	@Override
	public double getReward(StringState state, Action action) {
		count = count + 1;
		if(isFinalState(state)){
			randomizePosition();
		}

		lastX = x;
		lastY = y;

		int moveX = 0;
		int moveY = 0;
		if(action == Action.NORTH_WEST || action == Action.NORTH || action == Action.NORTH_EAST){
			moveY = -1;
		}
		else if(action == Action.SOUTH_WEST || action == Action.SOUTH || action == Action.SOUTH_EAST){
			moveY = 1;
		}

		if(action == Action.NORTH_WEST || action == Action.WEST || action == Action.SOUTH_WEST){
			moveX = -1;
		}
		else if(action == Action.NORTH_EAST || action == Action.EAST || action == Action.SOUTH_EAST){
			moveX = 1;
		}
		if(isValidPosition(x + moveX, y + moveY)){
			x += moveX;
			y += moveY;

			if(maze[y][x].equals(F_ENCODING)){
				count = 0;
				return 1000;
			}
		}

		if(count >= 50){
			randomizePosition();
			count = 0;
		}

		return 0;
	}

	@Override
	public boolean isFinalState(StringState state) {
		int checkX = lastX;
		int checkY = lastY;
		if(state.equals(getState())){
			checkX = x;
			checkY = y;
		}

		for(Point p : finalPoints){
			if(checkX == p.x && checkY == p.y)return true;
		}

		return false;
	}

	public static void main(String[] args){
//		String[] mazeFiles = {"woods1.txt", "maze4.txt", "maze5.txt", "maze6.txt", "woods14.txt", "woods101.txt", "woods102.txt", "maze7.txt", "maze10.txt"};
		String[] mazeFiles = {"woods1.txt"};
//		String[] mazeNames = {"Woods 1", "Maze 4", "Maze 5", "Maze 6", "Woods 14", "Woods 101", "Woods 102", "Maze 7", "Maze 10"};
		String[] mazeNames = {"Woods 1"};
		int[] numberOfProblems = {5000, 7500, 5000, 10000, 20000, 8000, 8000, 8000, 10000};

		XCSConstantsBuilder constants = new XCSConstantsBuilder();
		constants.setSP(500);
		constants.setOmega(1 / 1000);
		constants.setSpecificityProbability(0.4);
		constants.setStateDim(16);
		constants.setBeta(0.2);
		constants.setGamma(0.7);
		constants.setPexp(1);
		constants.setChi(0.8);
		constants.setMu(0.01);
		constants.setThetaNma(8);
		constants.setThetaGA(25);
		constants.setE0(10);
		constants.setThetaDel(20);
		constants.setDoGASubsumption(false);
		constants.setDoActionSetSubsumption(false);

		int[] problems = {0};

		for(int file = 0;file < problems.length;file ++){
			String mazeFile = mazeFiles[problems[file]];
			String mazeName = mazeNames[problems[file]];
			System.out.printf("Running on %s%n", mazeName);
			Maze maze = new Maze("data/" + mazeFile);

			StatsLogger crossTrialStats = new StatsLogger();

//			UpdateMethod[] algos = new UpdateMethod[]{UpdateMethod.NORMAL, UpdateMethod.XCSMU, UpdateMethod.NXCS, UpdateMethod.RXCS};
			UpdateMethod[] algos = new UpdateMethod[]{UpdateMethod.NORMAL};
			
			for(int z = 0;z < algos.length;z ++){
				UpdateMethod type = algos[z];
				String updateMethodName = type.toString();
				StatsLogger logger = new StatsLogger();
				constants.setUpdateMethod(type);
				System.out.println("Type: " + type);

				for(int i = 0;i < trials;i ++){
					XCS<StringState, StringCondition, Action> xcs = new XCS<StringState, StringCondition, Action>(constants.build(), Action.values());
					xcs.runXCSMultistep(maze, numberOfProblems[problems[file]], Maze.captureInterval);
					logger.logRun(xcs.getStats());

					System.out.printf("Run %d completed%n", i + 1);
				}

				crossTrialStats.logTrial(logger.getStatsList());
				try{
					logger.writeLogAndCSVFiles(String.format("log/csv/%s/%s/Trial <TRIAL_NUM>.csv", updateMethodName, mazeName), String.format("log/datadump/%s/<TIMESTEP_NUM>.log", updateMethodName), "Average Number of Steps to Goal");
					logger.writeChartsAsSinglePlot(String.format("log/charts/%s/%s/<CHART_TITLE>.png", updateMethodName, mazeName), String.format("%s on %s", updateMethodName, mazeName), "Average Number of Steps to Goal");
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}

			String[] names = new String[algos.length];
			for(int i = 0;i < names.length;i ++){
				names[i] = algos[i].toString();
			}

			try{
				crossTrialStats.writeChartsAsMultiPlot(String.format("log/charts/%s/<CHART_TITLE>.png", mazeName), mazeName, names, "Average Number of Steps to Goal");
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}

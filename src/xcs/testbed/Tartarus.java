package xcs.testbed;

import java.io.*;
import java.util.*;
import xcs.*;
import xcs.XCSConstants.UpdateMethod;
import xcs.stats.StatsLogger;
import xcs.testbed.Maze.Action;


public class Tartarus implements Environment<StringState, Tartarus.Action>{
	public static enum Action{
		TURNLEFT,
		TURNRIGHT,
		FORWARD
	}

	private enum AgentOrientation {NORTH, EAST, SOUTH, WEST}

	private String[][] world;

	private final String EMPTY = "00";
	private final String BLOCK = "01";
	private final String WALL = "11";
	private final String OOB = "10";

	private int worldSize;
	private int x, y;
	private int numSteps;
	private AgentOrientation orientation;

	/**
	 * Take in a file and number of steps to perform for the Tartarus Board
	 * @param file
	 * @param numSteps
	 */
	public Tartarus(String file, int numSteps){

		try{
			BufferedReader input = new BufferedReader(new FileReader(new File(file)));
			try{
				worldSize = Integer.parseInt(input.readLine());

				//Initialise the size of the Tartarus
				world = new String[worldSize+2][worldSize+2];

				//Surround the Tartarus world with walls
				for (int i = 0; i < worldSize + 2; i++) world[i][0] = world[i][worldSize + 1] = WALL;
				for (int j = 0; j < worldSize + 2; j++) world[0][j] = world[worldSize + 1][j] = WALL;

				//Read the map from a file
				for(int y=1; y<world.length-1; y++){
					String line = input.readLine();
					for(int x=0; x<line.length(); x++){

						switch(line.charAt(x)){
						case 'O':
							world[x+1][y] = EMPTY;
							break;
						case 'B':
							world[x+1][y] = BLOCK;
							break;
						case 'W':
							world[x+1][y] = WALL;
							break;
						default:
							throw new IllegalArgumentException("Map contains invalid characters (" + line.charAt(x) + ")");

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

		//		Random rnd = new Random();
		//
		//		do {
		//			y = 2 + rnd.nextInt(worldSize - 2);
		//			x = 2 + rnd.nextInt(worldSize - 2);
		//
		//		} while (world[y][x] == BLOCK);
		//
		//		switch (rnd.nextInt(4)) {
		//		case 0: orientation = AgentOrientation.NORTH; break;
		//		case 1: orientation = AgentOrientation.EAST; break;
		//		case 2: orientation = AgentOrientation.SOUTH; break;
		//		case 3: orientation = AgentOrientation.WEST; break;
		//		}

		RandomisePosition();

		//Set available number of movements that the agent can have
		this.numSteps = numSteps;
	}

	private void RandomisePosition(){
		Random rnd = new Random();

		do {
			y = 2 + rnd.nextInt(worldSize - 2);
			x = 2 + rnd.nextInt(worldSize - 2);

		} while (world[y][x] == BLOCK);

		switch (rnd.nextInt(4)) {
		case 0: orientation = AgentOrientation.NORTH; break;
		case 1: orientation = AgentOrientation.EAST; break;
		case 2: orientation = AgentOrientation.SOUTH; break;
		case 3: orientation = AgentOrientation.WEST; break;
		}

	}


	/**
	 * Calculate the current Tartarus Board's score
	 */
	public int Score(){
		int score = 0;

		for (int r = 1; r < world.length - 1; ++r) {
			if (world[r][1] == BLOCK) score++;
			if (world[r][world[r].length - 2] == BLOCK) score++;
		}

		for (int c = 1; c < world[0].length - 1; ++c) {
			if (world[1][c] == BLOCK) score++;
			if (world[world.length - 2][c] == BLOCK) score++;
		}

		return score;
	}

	public boolean isValidPosition(int x, int y){
		StringState s = SenseUpperMiddle();
		if(s.getState() == WALL) return false;
		if(s.getState() == BLOCK && SenseBeyond().getState() != EMPTY) return false;

		return true;
	}

	private StringState getState(int x, int y){
		StringBuilder state = new StringBuilder();

		for(int yp = y - 1;yp <= y + 1;yp ++){
			for(int xp = x - 1;xp <= x + 1;xp ++){
				if(xp == x && yp == y) continue;
				state.append(getEncoding(xp, yp));
			}
		}

		return new StringState(state.toString());
	}

	@Override
	public StringState getState() {
		return getState(x, y);
	}

	private String getEncoding(int x, int y){
		if(x < 0 || y < 0 || x >= world[0].length || y >= world.length){
			return OOB;
		}

		return world[y][x];
	}

	public StringState[] MoveForward(){
		//		if(numSteps == 0) return Sensor();
		//
		//		numSteps--;

		PerformMove();
		return Sensor();
	}

	public void PerformMove(){
		StringState s = SenseUpperMiddle();

		if(s.getState() == WALL) return;
		if(s.getState() == BLOCK && SenseBeyond().getState() != EMPTY) return;

		switch(orientation){
		case NORTH: y++; break;
		case EAST:  x++; break;
		case SOUTH: y--; break;
		case WEST:  x--; break;
		}

		if(s.getState() == BLOCK){
			world[y][x] = EMPTY;

			switch(orientation){
			case NORTH: world[y + 1][x] = BLOCK; break;
			case EAST:  world[y][x + 1] = BLOCK; break;
			case SOUTH: world[y - 1][x] = BLOCK; break;
			case WEST:  world[y][x - 1] = BLOCK; break;
			}
		}

	}

	public StringState[] TurnLeft(){
		//		if(numSteps == 0) return Sensor();

		//		numSteps--;

		switch(orientation){
		case NORTH: orientation = AgentOrientation.WEST; break;
		case EAST: orientation = AgentOrientation.NORTH; break;
		case SOUTH: orientation = AgentOrientation.EAST; break;
		case WEST: orientation = AgentOrientation.SOUTH; break;
		}
		return Sensor();

	}

	public StringState[] TurnRight(){
		//		if(numSteps == 0) return Sensor();
		//
		//		numSteps--;

		switch(orientation){
		case NORTH: orientation = AgentOrientation.EAST; break;
		case EAST: orientation = AgentOrientation.SOUTH; break;
		case SOUTH: orientation = AgentOrientation.WEST; break;
		case WEST: orientation = AgentOrientation.NORTH; break;
		}
		return Sensor();
	}

	public StringState[] Sensor(){
		StringState[] state = new StringState[8];

		state[0] = SenseUpperMiddle();
		state[1] = SenseUpperRight();
		state[2] = SenseRight();
		state[3] = SenseLowerRight();
		state[4] = SenseLowerMiddle();
		state[5] = SenseLowerLeft();
		state[6] = SenseLeft();
		state[7] = SenseUpperLeft();

		return state;
	}

	private StringState SenseUpperMiddle(){
		switch (orientation) {
		case NORTH: return new StringState(world[y + 1][x]);
		case EAST:  return new StringState(world[y][x + 1]);
		case SOUTH: return new StringState(world[y - 1][x]);
		case WEST:  default: return new StringState(world[y][x - 1]);
		}
	}

	private StringState SenseUpperLeft(){
		switch (orientation) {
		case NORTH: return new StringState(world[y + 1][x - 1]);
		case EAST:  return new StringState(world[y + 1][x + 1]);
		case SOUTH: return new StringState(world[y - 1][x + 1]);
		case WEST:  default: return new StringState(world[y - 1][x - 1]);
		}
	}

	private StringState SenseUpperRight(){
		switch (orientation) {
		case NORTH: return new StringState(world[y + 1][x + 1]);
		case EAST:  return new StringState(world[y - 1][x + 1]);
		case SOUTH: return new StringState(world[y - 1][x - 1]);
		case WEST:  default: return new StringState(world[y + 1][x - 1]);
		}
	}

	private StringState SenseLeft(){
		switch (orientation) {
		case NORTH: return new StringState(world[y][x - 1]);
		case EAST:  return new StringState(world[y + 1][x]);
		case SOUTH: return new StringState(world[y][x + 1]);
		case WEST:  default: return new StringState(world[y - 1][x]);
		}
	}

	private StringState SenseRight(){
		switch(orientation){
		case NORTH: return new StringState(world[y][x + 1]);
		case EAST:  return new StringState(world[y - 1][x]);
		case SOUTH: return new StringState(world[y][x - 1]);
		case WEST:  default: return new StringState(world[y + 1][x]);
		}
	}

	private StringState SenseLowerLeft(){
		switch(orientation){
		case NORTH: return new StringState(world[y - 1][x - 1]);
		case EAST:  return new StringState(world[y + 1][x - 1]);
		case SOUTH: return new StringState(world[y + 1][x + 1]);
		case WEST:  default: return new StringState(world[y - 1][x + 1]);
		}
	}

	private StringState SenseLowerMiddle(){
		switch(orientation){
		case NORTH: return new StringState(world[y - 1][x]);
		case EAST:  return new StringState(world[y][x - 1]);
		case SOUTH: return new StringState(world[y + 1][x]);
		case WEST:  default: return new StringState(world[y][x + 1]);
		}
	}

	private StringState SenseLowerRight(){
		switch(orientation){
		case NORTH: return new StringState(world[y - 1][x + 1]);
		case EAST:  return new StringState(world[y - 1][x - 1]);
		case SOUTH: return new StringState(world[y + 1][x - 1]);
		case WEST:  default: return new StringState(world[y + 1][x + 1]);
		}
	}

	private StringState SenseBeyond(){
		try {
			switch (orientation) {
			case NORTH: return new StringState(world[y + 2][x]);
			case EAST:  return new StringState(world[y][x + 2]);
			case SOUTH: return new StringState(world[y - 2][x]);
			case WEST:  default: return new StringState(world[y][x - 2]);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return new StringState(WALL);
		}
	}

	public void PrintWorld(PrintStream out){
		for (int r = world.length - 1; r >= 0; --r) {
			for (int c = 0; c < world[r].length; ++c) {
				if (r == y && c == x) {
					switch (orientation) {
					case NORTH: out.print("^"); break;
					case EAST: out.print(">"); break;
					case SOUTH: out.print("v"); break;
					case WEST: out.print("<"); break;
					}
				} else {
					switch (world[r][c]) {
					case EMPTY: out.print("."); break;
					case BLOCK: out.print("*"); break;
					case WALL: out.print("#"); break;
					}
				}
			}
			out.println();
		}

	}


	@Override
	public double getReward(StringState state, Action action) {
		// TODO Auto-generated method stub
		if(isFinalState(state)){
			//			return Score();
			RandomisePosition();
			//			return 0;
		}

		if(action == Action.TURNLEFT ){
			TurnLeft();
			numSteps--;
		}
		else if (action == Action.TURNRIGHT){
			TurnRight();
			numSteps--;
		}
		else if(action == Action.FORWARD){
			MoveForward();
			numSteps--;
		}


		if(numSteps>=0) {
			//			System.out.println(numSteps);

			if(world[1][world.length-2] == BLOCK || world[1][1] == BLOCK
					|| world[world.length-2][1] == BLOCK || world[world.length-2][world.length-2] == BLOCK){
				return 2;
			}
			else {
				for (int r = 2; r < world.length - 1; ++r) {
					if (world[r][2] == BLOCK) return 1;
					if (world[r][world[r].length - 3] == BLOCK) return 1;
				}

				for (int c = 2; c < world[0].length - 1; ++c) {
					if (world[2][c] == BLOCK) return 1;
					if (world[world.length - 3][c] == BLOCK) return 1;
				}
			}
		}
		else{
			PrintWorld(System.out);
			System.out.println(Score());

			RandomisePosition();
			numSteps = 80;

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return 0;
	}

	@Override
	public boolean isFinalState(StringState state) {
		// TODO Auto-generated method stub
		int maxScore = 8 + (worldSize-4);
		if(Score() == maxScore) return true;
		else if(numSteps==0) return true;

		return false;
	}


	public static int trials = 10;
	public static int captureInterval = 50;

	public static void main(String[] args){
		//		Tartarus tartarus = new Tartarus(("data/" + "World01.txt"), 80);

		String[] tartarusFiles = {"World01.txt"};
		String[] tartarusNames = {"6x6"};
		//		int[] numberOfProblems = {5000, 7500, 5000, 10000, 20000, 8000, 8000, 8000, 10000};
		int[] numberOfProblems = {2};

		//===============Algorithm====================
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

		for(int file=0; file<problems.length; file++){
			String tartarusFile = tartarusFiles[problems[file]];
			String tartarusName = tartarusNames[problems[file]];
			System.out.printf("Running on %s%n", tartarusName);

			Tartarus tartarus = new Tartarus(("data/" + tartarusFile), 80);

			tartarus.PrintWorld(System.out);

			StatsLogger crossTrialStats = new StatsLogger();

			UpdateMethod[] algos = new UpdateMethod[]{UpdateMethod.NORMAL};


			UpdateMethod type = UpdateMethod.NORMAL;
			String updateMethodName = type.toString();
			StatsLogger logger = new StatsLogger();
			constants.setUpdateMethod(type);
			System.out.println("Type: " + type);

			for(int i=0; i<trials; i++){
				XCS<StringState, StringCondition, Action> xcs = new XCS<StringState, StringCondition, Action>(constants.build(), Action.values());
				xcs.runXCSMultistep(tartarus, numberOfProblems[problems[file]], Tartarus.captureInterval);
				logger.logRun(xcs.getStats());

				System.out.printf("Run %d completed%n", i + 1);
			}

			crossTrialStats.logTrial(logger.getStatsList());

			try{
				logger.writeLogAndCSVFiles(String.format("log/csv/%s/%s/Trial <TRIAL_NUM>.csv", updateMethodName, tartarusName), String.format("log/datadump/%s/<TIMESTEP_NUM>.log", updateMethodName), "Average Number of Steps to Goal");
				logger.writeChartsAsSinglePlot(String.format("log/charts/%s/%s/<CHART_TITLE>.png", updateMethodName, tartarusName), String.format("%s on %s", updateMethodName, tartarusName), "Average Number of Steps to Goal");
			}
			catch(IOException e){
				e.printStackTrace();
			}

			String[] names = new String[algos.length];
			for(int i = 0;i < names.length;i ++){
				names[i] = type.toString();
			}

			try{
				crossTrialStats.writeChartsAsMultiPlot(String.format("log/charts/%s/<CHART_TITLE>.png", tartarusName), tartarusName, names, "Average Number of Steps to Goal");
			}
			catch(IOException e){
				e.printStackTrace();
			}


		}

	}

}

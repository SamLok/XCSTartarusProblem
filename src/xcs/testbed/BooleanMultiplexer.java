package xcs.testbed;

import java.io.IOException;

import xcs.Environment;
import xcs.XCS;
import xcs.XCSConstants;
import xcs.XCSConstants.UpdateMethod;
import xcs.XCSConstantsBuilder;
import xcs.stats.StatsLogger;

public class BooleanMultiplexer implements Environment<StringState, Integer>{
	private static final int k = 2;
	private static final int numBits = k + (1 << k);

	private static final int trials = 32;
	private static final int learningProblems = 20000;
	private static final int evaluationProblems = 10000;

	public BooleanMultiplexer(){}

	public static void main(String[] args){
		Integer[] actions = {0, 1};

		XCSConstantsBuilder constants = new XCSConstantsBuilder();
		constants.setStateDim(numBits);
		constants.setRho0(1);
		constants.setE0(0.001);
		constants.setSP(250);
		constants.setThetaNma(2);
		constants.setPhi(0);
		constants.setGamma(0);
		constants.setUpdateMethod(UpdateMethod.NORMAL);

		double sum = 0;
		StatsLogger crossTrialStats = new StatsLogger();

		for(UpdateMethod type : UpdateMethod.values()){
			StatsLogger logger = new StatsLogger();
			constants.setUpdateMethod(type);
			System.out.println("Type: " + type);
			XCSConstants cons = constants.build();

			for(int i = 0;i < trials;i ++){
				BooleanMultiplexer problem = new BooleanMultiplexer();
				XCS<StringState, StringCondition, Integer> xcs = new XCS<StringState, StringCondition, Integer>(cons, actions);
				xcs.runXCSSingleStep(problem, learningProblems, 100);
				xcs.printPopulation(16);

				logger.logRun(xcs.getStats());
				double result = xcs.runXCSEvaluationSingleStep(new BooleanMultiplexer(), evaluationProblems);
				sum += result;
				//System.out.printf("Run %d completed%n", i + 1);
				System.out.printf("Run %d: %3.2f/%3.2f%n", i + 1, result, constants.getRho0() * evaluationProblems);
			}

			crossTrialStats.logTrial(logger.getStatsList());

			try{
				logger.writeLogAndCSVFiles("log/csv/" + constants.getUpdateMethod() + "/BooleanMultiplexer/<TRIAL_NUM>.csv", "log/datadump/" + constants.getUpdateMethod() + "/BooleanMultiplexer/<TIMESTEP_NUM>.log", "Correct");
				logger.writeChartsAsSinglePlot("log/charts/" + constants.getUpdateMethod() + "/BooleanMultiplexer/<CHART_TITLE>.png", ""+ constants.getUpdateMethod() + " " + numBits + " bit Boolean Multiplexer", "Correct");
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}

		String[] names = new String[UpdateMethod.values().length];
		for(int i = 0;i < names.length;i ++){
			names[i] = UpdateMethod.values()[i].toString();
		}
		try{
			crossTrialStats.writeChartsAsMultiPlot("log/charts/BooleanMultiplexer/<CHART_TITLE>.png", numBits + " bit Boolean Multiplexer", names, "Correct");
		}
		catch(IOException e){
			e.printStackTrace();
		}

		System.out.printf("Total reward: %3.2f/%3.2f%n", sum, trials * constants.getRho0() * evaluationProblems);
		System.out.printf("Average reward: %3.2f/%3.2f%n", sum / trials, constants.getRho0() * evaluationProblems);
	}

	@Override
	public StringState getState() {
		String state = "";
		for(int i = 0;i < numBits;i ++){
			if(Math.random() > 0.5)state += "0";
			else state += "1";
		}

		return new StringState(state);
	}

	@Override
	public double getReward(StringState state, Integer action) {
		String strState = state.getState();
		String address = "0" + strState.substring(0, k);
		String data = strState.substring(k);

		int decAddress = Integer.parseInt(address, 2);
		if((data.charAt(decAddress) - '0') == action.intValue()){
			return 1.0;
		}
		return 0;
	}

	@Override
	public boolean isFinalState(StringState state) {
		return false;
	}
}

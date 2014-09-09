package xcs.testbed;

import java.io.IOException;

import xcs.Environment;
import xcs.XCS;
import xcs.XCSConstants.UpdateMethod;
import xcs.XCSConstantsBuilder;
import xcs.stats.StatsLogger;

public class RealBooleanMultiplexer implements Environment<RealState, Integer>{

	private static final int k = 2;
	private static final int numBits = k + (1 << k);

	private static final double threshold = 0.6;

	private static double[] maxCap = {1, 1, 1, 1, 1, 1};
	private static double[] minCap = {0, 0, 0, 0, 0, 0};

	private static final int trials = 32;
	private static final int learningProblems = 20000;
	private static final int evaluationProblems = 10000;

	private RealStateBuilder stateBuilder;

	public RealBooleanMultiplexer(){
		stateBuilder = new RealStateBuilder(numBits);
	}


	@Override
	public RealState getState() {
		double[] values = new double[numBits];
		values[0] = (Math.random() < 0.5) ? 0 : 1;
		values[1] = (Math.random() < 0.5) ? 0 : 1;
		for(int i = 2;i < numBits;i ++){
			double min = minCap[i];
			double max = maxCap[i];
			values[i] = Math.random() * (max - min) + min;
		}

		return stateBuilder.makeState(values);
	}

	@Override
	public double getReward(RealState state, Integer action) {
		int address = (int)(state.getBitAt(0) * 2 + state.getBitAt(1));
		double value = state.getBitAt(address + 2);
		double valThreshold = (maxCap[address] - minCap[address]) * threshold + minCap[address];
		if((value > valThreshold) == (action.intValue() == 1)){
			return 1;
		}
		return 0;
	}

	@Override
	public boolean isFinalState(RealState state) {
		return false;
	}

	public static void main(String[] args){
		StatsLogger crossTrialStats = new StatsLogger();
		System.out.println(Math.log(Double.MAX_VALUE));
		Integer[] actions = {0, 1};
		RealBooleanMultiplexer problem = new RealBooleanMultiplexer();
		XCSConstantsBuilder build = new XCSConstantsBuilder();
		build.setStateDim(numBits);
		build.setRho0(1);
		build.setE0(0.001);
		build.setBeta(0.2);
		build.setThetaGA(25);
		build.setChi(0.8);
		build.setMu(0.04);
		build.setSP(300);
		build.setThetaNma(2);
		build.setPhi(0);
		build.setGamma(0);
		build.setUpdateMethod(UpdateMethod.NXCS);

		double sum = 0;

		UpdateMethod[] algos = new UpdateMethod[]{UpdateMethod.NORMAL, UpdateMethod.XCSMU, UpdateMethod.RXCS, UpdateMethod.NXCS};

		for(UpdateMethod type : algos){
			StatsLogger logger = new StatsLogger();
			build.setUpdateMethod(type);
			System.out.println("Type: " + type);
			for(int i = 0;i < trials;i ++){
				XCS<RealState, RangeCondition, Integer> xcs = new XCS<RealState, RangeCondition, Integer>(build.build(), actions);
				xcs.runXCSSingleStep(problem, learningProblems, 100);
				logger.logRun(xcs.getStats());

				problem.stateBuilder.reset();
				//xcs.printPopulation(-1);

				double result = xcs.runXCSEvaluationSingleStep(problem, evaluationProblems);
				sum += result;

				System.out.printf("Run %d: %3.2f/%3.2f%n", i + 1, result, evaluationProblems * build.getRho0());
			}

			crossTrialStats.logTrial(logger.getStatsList());

			try{
				logger.writeLogAndCSVFiles("log/csv/" + build.getUpdateMethod() + "/RealBooleanMultiplexer/<TRIAL_NUM>.csv", "log/datadump/" + build.getUpdateMethod() + "/RealBooleanMultiplexer/<TIMESTEP_NUM>.log", "Correct");
				logger.writeChartsAsSinglePlot("log/charts/" + build.getUpdateMethod() + "/RealBooleanMultiplexer/<CHART_TITLE>.png", ""+ build.getUpdateMethod() + " " + numBits + " bit Real Boolean Multiplexer", "Correct");
			}
			catch(IOException e){
				e.printStackTrace();
			}

			System.out.printf("Average Reward: %3.2f/%3.2f%n", sum / trials, evaluationProblems * build.getRho0());
		}

		String[] names = new String[UpdateMethod.values().length];
		for(int i = 0;i < names.length;i ++){
			names[i] = UpdateMethod.values()[i].toString();
		}
		try{
			crossTrialStats.writeChartsAsMultiPlot("log/charts/RealBooleanMultiplexer/<CHART_TITLE>.png", numBits + " bit Real Boolean Multiplexer", names, "Correct");
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

}

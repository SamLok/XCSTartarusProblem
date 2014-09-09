package xcs;

/**
 * General Bag of constant configuration values used in an XCS system.
 * Generated from a builder.
 *
 * Should be passed to an XCS to initialise it with the following values.
 *
 */
public class XCSConstants {

	public static enum UpdateMethod{
		XCSMU("XCSμ"),
		NXCS("NXCS"),
		NXCS2("NXCS2"),
		RXCS("RXCS"),
		NORMAL("XCS");
		private final String readableName;
		UpdateMethod(String name){this.readableName = name;}
		@Override
		public String toString(){return readableName;}
	}

	public XCSConstants(XCSConstantsBuilder build){
		//Initialise Constants
		stateDim = build.getStateDim();
		specificityProbability = build.getSpecificityProbability();
		pI = build.getPI();
		eI = build.getEI();
		fI = build.getFI();
		thetaI = build.getThetaI();
		rho0 = build.getRho0();
		e0 = build.getE0();
		vi = build.getVi();
		useModifiedActionSetSubsumption = build.useModifiedActionSetSubsumption();
		thetaSub = build.getThetaSub();
		thetaNma = build.getThetaNma();
		thetaGA = build.getThetaGA();
		thetaDel = build.getThetaDel();
		phi = build.getPhi();
		pexp = build.getPexp();
		mu = build.getMu();
		gamma = build.getGamma();
		doGASubsumption = build.doGASubsumption();
		doActionSetSubsumption = build.doActionSetSubsumption();
		delta = build.getDelta();
		chi = build.getChi();
		beta = build.getBeta();
		alpha = build.getAlpha();
		SP = build.getSP();
		updateMethod = build.getUpdateMethod();
		omega = build.getOmega();
	}

	/** The Number of dimensions (bits) in the state space **/
	private final int stateDim;

	/** The probability of not choosing the wildcard for a specific state dimension in a new classifier **/
	private final double specificityProbability;

	/** The initial expected payoff for a new classifier **/
	private final double pI;

	/** The initial prediction error value for a new classifier **/
	private final  double eI;

	/** The initial policy parameter value for new classifiers */
	private final double thetaI;

	/** The initial fitness value for a new classifier **/
	private final double fI;

	/** The maximum reward the system can receive in a single time step */
	private final double rho0;

	/** The accuracy threshold. Any classifier with an accuracy below this is considered to be equally accurate */
	private final double e0;

	private final double omega;

	/**
	 * ===================
	 * Learning Parameters
	 * ===================
	 */

	/** The maximum number of classifiers in the population */
	private final int SP;

	/** The learning rate. Recommended to be 0.1 -> 0.2 */
	private final double beta;

	/** Used in calculating the fitness of a classifier. Default to 0.1 */
	private final double alpha;

	/** The power parameter. Used in the calculating the fitness of a classifier. Default to 5 */
	private final double vi;

	/** The discount factor. Default to 0.71 according to literature, although lee-way is given */
	private final double gamma;

	/** The minimum amount of time between executions of the genetic algorithm.
	  The GA is applied when the average time since the last GA is greater than this. Default 25 -> 50 */
	private final double thetaGA;

	/** The crossover probability. Recomended Range 0.5 -> 1 */
	private final double chi;

	/** The mutation probability. Recommended Range 0.01 -> 0.05 */
	private final double mu;

	/** The deletion threshold. If the experience of a classifier is greater than thetaDel,
	 * its fitness may be considered in its probability of deletion. Default to 20 */
	private final int thetaDel;

	/** The fraction of the mean fitness below which the fitness of a
	 * classifier may be considered in its probability of deletion. */
	private final double delta;

	/** The Subsumption threshold.
	 * If the experience of a classifier is greater than this, it is eligible to subsume
	 * other classifiers. Default to 20. */
	private final double thetaSub;

	/** The probability of using exploration (uniform random action) vs exploitation (Best action) when choosing an action */
	private final double pexp;

	/** Parameter that controls the residual portion of the residual gradient descent updating rule */
	private final double phi;

	/** Flag indicating whether or not to do Genetic Algorithm Subsumption */
	private final boolean doGASubsumption;

	/** Flag indicating whether or not to do action set subsumption */
	private final boolean doActionSetSubsumption;

	/** Flag as to whether or not to use an improved ActionSetSubsumption */
	private final boolean useModifiedActionSetSubsumption;

	/** The minimum number of actions that must be in the match set, before covering occurs */
	private final int thetaNma;

	/** The method to use when updating the set */
	private final UpdateMethod updateMethod;


	/*===================
	 *    Getters
	 *===================*/

	/**
	 * @return The method to use when updating the action set
	 */
	public UpdateMethod getUpdateMethod() {
		return updateMethod;
	}

	/**
	 * @return The minimum number of actions that must be in the match set, before covering occurs
	 */
	public int getThetaNma() {
		return thetaNma;
	}

	/**
	 * @return Whether or not to use a modified verson of action set subsumption in which
	 * the classifiers are sorted according to their fitness before being subsumed
	 */
	public boolean useModifiedActionSetSubsumption() {
		return useModifiedActionSetSubsumption;
	}

	/**
	 * @return Whether classifiers should be subsumed during updating the action set
	 */
	public boolean doActionSetSubsumption() {
		return doActionSetSubsumption;
	}

	/**
	 * @return The probability of not selecting a wildcard for a condition bit
	 */
	public double getSpecificityProbability(){
		return specificityProbability;
	}

	/**
	 * @return Whether classifiers should be subsumed during the genetic algorithm
	 */
	public boolean doGASubsumption() {
		return doGASubsumption;
	}

	public double getOmega(){
		return omega;
	}

	/**
	 * @return Phi, the residual gradient constant
	 */
	public double getPhi() {
		return phi;
	}

	/**
	 * @return The probability of using pure exploitation vs exploration when choosing an action
	 * to perform.
	 */
	public double getPexp() {
		return pexp;
	}

	/**
	 * @return The number of bits in the state
	 */
	public int getStateDim(){
		return stateDim;
	}

	/**
	 * @return The initial expected payoff for new classifiers
	 */
	public double getPI(){
		return pI;
	}

	/**
	 * @return The current initial prediction error assigned to new classifiers
	 */
	public double getEI(){
		return eI;
	}

	/**
	 * @return The initial fitness value assigned to new classifiers
	 */
	public double getFI(){
		return fI;
	}

	/**
	 * @return The initial policy parameter for new classifiers
	 */
	public double getThetaI(){
		return thetaI;
	}

	/**
	 * @return The error threshold. Any classifier with an error below this is considered to have equal error (Essentially 0)
	 */
	public double getE0() {
		return e0;
	}

	/**
	 * @return The maximum number of classifiers in the population
	 */
	public int getSP() {
		return SP;
	}

	/**
	 * @return The learning rate for the experience, error and fitness of classifiers
	 */
	public double getBeta() {
		return beta;
	}

	/**
	 * @return Alpha, a parameter used in calculating the fitness of a classifier
	 */
	public double getAlpha() {
		return alpha;
	}

	/**
	 * @return The power parameter used in calculating the fitness of a classifier
	 */
	public double getVi() {
		return vi;
	}

	/**
	 * @return The subsumption threshold. Any classifier with experience greater than this can subsume others.
	 */
	public double getThetaSub() {
		return thetaSub;
	}

	/**
	 * @return The minimum number of timesteps between executions of the GA
	 */
	public double getThetaGA() {
		return thetaGA;
	}

	/**
	 * @return The probability of applying a crossover during a GA excution
	 */
	public double getChi() {
		return chi;
	}

	/**
	 * @return The probability of applying a mutation in an offpring of a given classifier
	 */
	public double getMu() {
		return mu;
	}

	/**
	 * @return The deletion threshold. If a classifier has an experience greater than this, its fitness is taken into account
	 * in its probability of deletion
	 */
	public int getThetaDel() {
		return thetaDel;
	}

	/**
	 * @return The deletion fraction. If a classifier does not contribute at least this fraction to the mean fitness, its
	 *fitness is considered in its probability of deletion
	 */
	public double getDelta() {
		return delta;
	}

	/**
	 * @return The maximum reward that can be received in a single timestep
	 */
	public double getRho0(){
		return rho0;
	}

	/**
	 * @return The discount factor used in updating classifier predictions
	 */
	public double getGamma() {
		return gamma;
	}
}
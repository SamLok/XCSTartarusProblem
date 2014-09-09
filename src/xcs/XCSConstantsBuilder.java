package xcs;

import xcs.XCSConstants.UpdateMethod;

/**
 * A builder class that allows modifying of XCS parameters
 * before they are made into constants and passed to the XCS.
 *
 */
public class XCSConstantsBuilder {

	/**
	 * Generates a set of constants out of this builder
	 * ready for using in the XCS
	 * @return The XCSConstants generated from this builder
	 */
	public XCSConstants build(){
		return new XCSConstants(this);
	}

	/** The Number of dimensions (bits) in the state space **/
	private int stateDim = 16;

	/** The probability of not choosing the wildcard for a specific state dimension in a new classifier **/
	private double specificityProbability = 0.7;

	/** The initial expected payoff for a new classifier **/
	private double pI = Math.pow(10.0, -10.0);

	/** The initial prediction error value for a new classifier **/
	private  double eI = Math.pow(10.0, -10.0);

	/** The initial fitness value for a new classifier **/
	private double fI = Math.pow(10.0, -10.0);

	/** The initial policy parameter for new classifiers **/
	private double thetaI = 0;

	/** The initial value of w (Compatible Advantage Function Parameter) */
	private double wI = 0;

	/** The maximum reward the system can receive in a single time step */
	private double rho0 = 1000.0;

	/** The accuracy threshold. Any classifier with an accuracy below this is considered to be equally accurate */
	private double e0 = 0.01 * rho0;


	/**
	 * ===================
	 * Learning Parameters
	 * ===================
	 */

	/** The maximum number of classifiers in the population */
	private int SP = 1000;

	private double omega = 1 / rho0;

	/** The learning rate. Recommended to be 0.1 -> 0.2 */
	private double beta = 0.2;

	/** Used in calculating the fitness of a classifier. Default to 0.1 */
	private double alpha = 0.1;

	/** The power parameter. Used in the calculating the fitness of a classifier. Default to 5 */
	private double vi = 5.0;

	/** The discount factor. Default to 0.71 according to literature, although lee-way is given */
	private double gamma = 0.71;

	/** The minimum amount of time between executions of the genetic algorithm.
	  The GA is applied when the average time since the last GA is greater than this. Default 25 -> 50 */
	private double thetaGA = 38;

	/** The crossover probability. Recomended Range 0.5 -> 1 */
	private double chi = 0.75;

	/** The mutation probability. Recommended Range 0.01 -> 0.05 */
	private double mu = 0.03;

	/** The deletion threshold. If the experience of a classifier is greater than thetaDel,
	 * its fitness may be considered in its probability of deletion. Default to 20 */
	private int thetaDel = 20;

	/**
	 * The fraction of the mean fitness below which the fitness of a
	 * classifier may be considered in its probability of deletion. */
	private double delta = 0.1;

	/** The Subsumption threshold.
	 * If the experience of a classifier is greater than this, it is eligible to subsume
	 * other classifiers. Default to 20. */
	private double thetaSub = 20;

	/** The probability of using exploration (uniform random action) vs exploitation (Best action) when choosing an action */
	private double pexp = 0.5;

	/** Parameter that controls the residual portion of the residual gradient descent updating rule */
	private double phi = 1.0;

	/** Flag indicating whether or not to do Genetic Algorithm Subsumption */
	private boolean doGASubsumption = true;

	/** Flag indicating whether or not to do action set subsumption */
	private boolean doActionSetSubsumption = true;

	/** Flag as to whether or not to use an improved ActionSetSubsumption */
	private boolean useModifiedActionSetSubsumption = true;

	/** The method to use when updating the set */
	private UpdateMethod updateMethod = UpdateMethod.NORMAL;

	/** The minimum number of actions that must be in the match set, before covering occurs */
	private int thetaNma = 5;

	/*===================
	 *    Getters
	 *===================*/

	/**
	 * @return The minimum number of actions that must be in the match set, before covering occurs
	 */
	public int getThetaNma() {
		return thetaNma;
	}

	/**
	 * @return The method to use when updating the action set
	 */
	public UpdateMethod getUpdateMethod() {
		return updateMethod;
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
	 * @return The initial value of w (compatible advantage function parameter) for new classifiers
	 */
	public double getWI(){
		return wI;
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

	public double getOmega(){
		return omega;
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

	/*===================
	 *    Setters
	 *===================*/

	/**
	 * Sets the deletion threshold
	 * @param thetaDel The new deletion threshold
	 */
	public void setThetaDel(int thetaDel) {
		this.thetaDel = thetaDel;
	}

	/**
	 * Sets the deletion fraction
	 * @param delta The new deletion fraction
	 */
	public void setDelta(double delta) {
		this.delta = delta;
	}

	/**
	 * Sets the method used to update the action set. If
	 * the argument given is null, default to a normal XCS update
	 * @param type The new method to use for updating
	 */
	public void setUpdateMethod(UpdateMethod method){
		if(method == null)method = UpdateMethod.NORMAL;
		updateMethod = method;
	}

	public void setOmega(double d){
		omega = d;
	}

	/**
	 * Sets the minimum number of classifiers that can exist in the match set
	 * before covering occurs.
	 * @param thetaNma The new minimum number of classifiers in the match set
	 */
	public void setThetaNma(int thetaNma) {
		this.thetaNma = thetaNma;
	}

	/**
	 * Sets whether to use a modified version of action set subsumption in which
	 * classifiers are sorted by their fitness before being subsumed
	 * @param mass Whether to use this modified version of action set subsumption
	 */
	public void setUseModifiedActionSetSubsumption(boolean mass) {
		this.useModifiedActionSetSubsumption = mass;
	}

	/**
	 * Sets whether or not to subsume
	 * @param doActionSetSubsumption
	 */
	public void setDoActionSetSubsumption(boolean doActionSetSubsumption) {
		this.doActionSetSubsumption = doActionSetSubsumption;
	}

	/**
	 * Sets the probability of not selecting a wildcard for a condition bit
	 * @param sp The new Speciicity Probability
	 * @throws IllegalArgumentException If sp < 0 || sp > 1
	 */
	public void setSpecificityProbability(double sp){
		if(sp < 0 || sp > 1)throw new IllegalArgumentException("Specificity Probability must be >= 0 and <= 1");
		specificityProbability = sp;
	}

	/**
	 * Sets whether to subsume classifiers in the Genetic Algorithm
	 * @param doGASubsumption Whether to perform subsumption in the GA
	 */
	public void setDoGASubsumption(boolean doGASubsumption) {
		this.doGASubsumption = doGASubsumption;
	}

	/**
	 * Sets Phi. The parameter that controls the residual part of the residual gradient updating method.
	 * @throws IllegalArgumentException If the new phi is not 0 < phi < 1.
	 * @param phi The new phi constant.
	 */
	public void setPhi(double phi) {
		if(phi < 0 || phi > 1)throw new IllegalArgumentException("Phi must be between 0 and 1");
		this.phi = phi;
	}

	/**
	 * Sets the probability of using pure exploitation vs exploration when choosing an action
	 * @throws IllegalArgumentException If the new pexp is not 0 < pexp < 1.
	 * @param pexp The new probability
	 */
	public void setPexp(double pexp) {
		if(pexp < 0 || pexp > 1)throw new IllegalArgumentException("pexp must be 0 <= pexp <= 1");
		this.pexp = pexp;
	}

	/**
	 * Sets the number of bits in the state
	 * @param sd The new stateDim
	 */
	public void setStateDim(int sd){
		if(sd <= 0)throw new IllegalArgumentException("Number of state bits must be > 0");
		stateDim = sd;
	}

	/**
	 * Sets the initial expected payoff for new classifiers
	 * @param pI The new initial expected payoff
	 */
	public void setPI(double pI){
		this.pI = pI;
	}

	/**
	 * Sets the initial prediction error for new classifiers
	 * @param eI The new prediction error
	 */
	public void setEI(double eI){
		this.eI = eI;
	}

	/**
	 * Sets the initial fitness parameter used when generating new classifiers
	 * @param fI The new initial fitness
	 */
	public void setFI(double fI){
		this.fI = fI;
	}

	/**
	 * Sets the initial policy parameter used when creating new classifiers
	 * @param thetaI The new initial policy parameter
	 */
	public void setThetaI(double thetaI){
		this.thetaI = thetaI;
	}

	public void setWI(double wI){
		this.wI = wI;
	}

	/**
	 * Sets the error threshold. Any classifier with an error below this is considered to have equal error
	 * @param e0 The new error threshold
	 */
	public void setE0(double e0) {
		if(e0 <= 0)throw new IllegalArgumentException("E0 cannot be <= 0");
		this.e0 = e0;
	}

	/**
	 * Sets the maximum number of classifiers allowed in the population
	 * @throws IlegalArgumentException If the new SP is <= 0
	 * @param SP The new maximum number of classifiers in the population
	 */
	public void setSP(int SP) {
		if(SP <= 0)throw new IllegalArgumentException("Maximum number of classifiers in population must be > 0");
		this.SP = SP;
	}

	/**
	 * Sets the learning rate for the experience, error and fitness of classifiers.
	 * @param beta The new learning rate
	 */
	public void setBeta(double beta) {
		if(beta <= 0)throw new IllegalArgumentException("Beta must be > 0");
		this.beta = beta;
	}

	/**
	 * Sets alpha, a parameter used in calculating the fitness of a classifier
	 * @param alpha The new value of alpha
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	/**
	 * Sets te power parameter used in calculating the fitness of classifier
	 * @param vi The new power parameter value
	 */
	public void setVi(double vi) {
		this.vi = vi;
	}

	/**
	 * Sets the subsumption threshold. Any classifier with experience greater than this can subsume others.
	 * @param thetaSub The new subsumption threshold
	 */
	public void setThetaSub(double thetaSub) {
		this.thetaSub = thetaSub;
	}

	/**
	 * Sets the minimum number of timesteps between executions of the GA
	 * @param thetaGA The new thetaGA
	 */
	public void setThetaGA(double thetaGA) {
		this.thetaGA = thetaGA;
	}

	/**
	 * Sets the probability of applying a crossover during the GA
	 * @throws IllegalArgumentException If the new chi is not 0 <= mu <= 1
	 * @param chi The new chi value
	 */
	public void setChi(double chi) {
		if(chi < 0 || chi > 1)throw new IllegalArgumentException("Chi must be >= 0 and <= 1");
		this.chi = chi;
	}

	/**
	 * Sets the probability of applying a mutation in an offpring of a given classifier
	 * @throws IllegalArgumentException If the new mu is not 0 <= mu <= 1
	 * @param mu The new mutation probability
	 */
	public void setMu(double mu) {
		if(mu < 0 || mu > 1)throw new IllegalArgumentException("Mutation Probability must be >= 0 and <= 1");
		this.mu = mu;
	}

	/**
	 * Sets the maximum reward that can be received from the environment in a single timestep
	 * @param rho0 The new maximum reward
	 */
	public void setRho0(double rho0){
		this.rho0 = rho0;
	}

	/**
	 * Sets the discount factor used in updating classifier predictions
	 * @param gamma The new discount factor
	 */
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}
}

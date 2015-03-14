package de.jungblut.antcolony;

public final class MetaParameters {

  // greedy
  public static final double ALPHA = 0d; // >= 0
  // rapid selection
  public static final double BETA = 9d; // >= 1

  // heuristic parameters
  public static final double Q = 0.0001; // somewhere between 0 and 1
  public static final double PHEROMONE_PERSISTENCE = 0.5d; // between 0 and 1

  public static final int NUM_AGENTS = 1_000 * 200;

  private double alpha;
  private double beta;
  private double q;
  private double pheromonePersistence;
  private int numAgents;

  private MetaParameters(double alpha, double beta, double q,
      double pheromonePersistence, int numAgents) {
    this.alpha = alpha;
    this.beta = beta;
    this.q = q;
    this.pheromonePersistence = pheromonePersistence;
    this.numAgents = numAgents;
  }

  public static MetaParameters defaults() {
    return from(ALPHA, BETA, Q, PHEROMONE_PERSISTENCE, NUM_AGENTS);
  }

  public static MetaParameters from(double alpha, double beta, double q,
      double pheromonePersistence, int numAgents) {
    return new MetaParameters(alpha, beta, q, pheromonePersistence, numAgents);
  }

  public double getAlpha() {
    return alpha;
  }

  public double getBeta() {
    return beta;
  }

  public double getQ() {
    return q;
  }

  public double getPheromonePersistence() {
    return pheromonePersistence;
  }

  public int getNumAgents() {
    return numAgents;
  }

  @Override
  public String toString() {
    return "[alpha=" + alpha + ", beta=" + beta + ", q=" + q
        + ", pheromonePersistence=" + pheromonePersistence + ", numAgents="
        + numAgents + "]";
  }

}

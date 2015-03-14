package de.jungblut.antcolony;

import java.util.Random;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * Contains the "Pheromones" and abstractions to access them in a multithreaded
 * setting.
 * 
 * @author thomas.jungblut
 *
 */
public final class WeightMatrix {

  private final double pheromonePersistence;
  // TODO this is not super efficient for very large matrices, since we want to
  // enforce a sparse matrix for weights
  private final AtomicDouble[][] weights;

  public WeightMatrix(int length, double pheromonePersistence) {
    this.pheromonePersistence = pheromonePersistence;
    weights = new AtomicDouble[length][length];
    initializeWeights();
  }

  public double readWeight(int x, int y) {
    return weights[x][y].get();
  }

  public void adjustWeight(int x, int y, double weightUpdate) {
    while (true) {
      final double val = weights[x][y].get();
      final double result = weightDecay(val, weightUpdate);
      boolean success = false;
      if (result >= 0.0d) {
        success = weights[x][y].compareAndSet(val, result);
      } else {
        success = weights[x][y].compareAndSet(val, 0);
      }
      if (success) {
        break;
      }
    }
  }

  public int numRows() {
    return weights.length;
  }

  private double weightDecay(double current, double weightUpdate) {
    return weightUpdate + (1d - pheromonePersistence) * current;
  }

  private void initializeWeights() {
    final int rows = weights.length;
    Random rand = new Random();
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < rows; column++) {
        weights[row][column] = new AtomicDouble(rand.nextDouble());
      }
    }

  }

}

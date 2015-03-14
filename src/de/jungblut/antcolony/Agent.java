package de.jungblut.antcolony;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.util.FastMath;

import de.jungblut.antcolony.AntColonyOptimization.Path;

public final class Agent implements Callable<Path> {

  private static final ThreadLocalRandom RANDOM_UNIFORM = ThreadLocalRandom
      .current();

  private final WeightMatrix weightMatrix;
  private final MetaParameters parameters;
  private final double[][] distanceMatrix;

  private final int start;
  private final boolean[] visited;
  private final int[] path;

  private int toVisit;

  public Agent(MetaParameters parameters, WeightMatrix weights,
      double[][] distanceMatrix) {
    this.parameters = parameters;
    this.weightMatrix = weights;
    this.distanceMatrix = distanceMatrix;
    this.start = RANDOM_UNIFORM.nextInt(weights.numRows());
    this.visited = new boolean[weights.numRows()];
    this.visited[start] = true;
    this.toVisit = visited.length - 1;
    this.path = new int[visited.length];
  }

  private int getNextProbableNode(int x) {
    if (toVisit > 0) {
      int danglingUnvisited = -1;
      final double[] transitionProbabilities = new double[visited.length];

      double allowedYSum = 0d;
      for (int y = 0; y < visited.length; y++) {
        if (!visited[y]) {
          double p = computeNumerator(x, y);
          transitionProbabilities[y] = p;
          allowedYSum += p;
          danglingUnvisited = y;
        }
      }

      if (allowedYSum == 0d) {
        return danglingUnvisited;
      }

      double sum = 0d;
      for (int y = 0; y < visited.length; y++) {
        double weighted = transitionProbabilities[y] / allowedYSum;
        transitionProbabilities[y] = weighted;
        sum += weighted;
      }

      double random = RANDOM_UNIFORM.nextDouble() * sum;
      for (int y = 0; y < visited.length; y++) {
        random -= transitionProbabilities[y];
        if (random <= 0) {
          return y;
        }
      }
    }
    return -1;
  }

  /*
   * (pheromone(row,col) ^ ALPHA) * ((1/distance(row,col)) ^ BETA)
   */
  private final double computeNumerator(int row, int column) {
    if (row != column) {
      double weight = weightMatrix.readWeight(column, row);
      if (weight != 0d) {
        double alpha = FastMath.pow(weight, parameters.getAlpha());
        double beta = FastMath.pow(1d / distanceMatrix[row][column],
            parameters.getBeta());
        return (alpha * beta);
      }
    }
    return 0d;
  }

  @Override
  public final Path call() throws Exception {

    int lastNode = start;
    int next = start;
    int i = 0;
    double distance = 0d;
    while ((next = getNextProbableNode(lastNode)) != -1) {
      path[i++] = lastNode;
      distance += distanceMatrix[lastNode][next];
      final double dampened = parameters.getQ() / distance;
      weightMatrix.adjustWeight(lastNode, next, dampened);
      visited[next] = true;
      lastNode = next;
      toVisit--;
    }
    distance += distanceMatrix[lastNode][start];
    path[i] = lastNode;

    return new Path(path, distance);
  }

}

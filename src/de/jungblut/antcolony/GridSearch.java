package de.jungblut.antcolony;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.ComparisonChain;

public class GridSearch {

  public static class GridSearchResult implements Comparable<GridSearchResult> {

    final double distanceToBestSolution;
    final double mean;
    final double stddev;
    final MetaParameters params;

    public GridSearchResult(double distanceToBestSolution, double mean,
        double stddev, MetaParameters params) {
      this.distanceToBestSolution = distanceToBestSolution;
      this.mean = mean;
      this.stddev = stddev;
      this.params = params;
    }

    @Override
    public int compareTo(GridSearchResult o) {
      // we want to favour the one with the smallest mean and stddev, with the
      // least distance to the best solution
      return ComparisonChain.start().compare(o.mean, mean)
          .compare(o.stddev, stddev)
          .compare(o.distanceToBestSolution, distanceToBestSolution).result();
    }

    @Override
    public String toString() {
      return "[distanceToBestSolution=" + distanceToBestSolution + ", mean="
          + mean + ", stddev=" + stddev + "]";
    }
  }

  public static void main(String[] args) throws IOException,
      InterruptedException, ExecutionException {

    final String berlin = "files/berlin52.tsp";
    final double bestValueBerlin = 7542; // according to
                                         // http://www.iwr.uni-heidelberg.de/groups/comopt/software/TSPLIB95/STSP.html
    final int numBestParametersToKeep = 10;
    final int numRepetitions = 5;
    double[] alphas = from(0d, 1d, 0.1d).toArray();
    double[] betas = from(1d, 10d, 1d).toArray();
    double[] persistence = from(0d, 1d, 0.1d).toArray();

    PriorityQueue<GridSearchResult> queue = new PriorityQueue<>();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(
        "files/parameters_2.csv"))) {

      writer.write("alpha,beta,p,distance_to_best,mean,stddev\n");

      for (int a = 0; a < alphas.length; a++) {
        for (int b = 0; b < betas.length; b++) {
          for (int p = 0; p < persistence.length; p++) {

            MetaParameters params = MetaParameters.from(alphas[a], betas[b],
                MetaParameters.Q, persistence[p], MetaParameters.NUM_AGENTS);

            DescriptiveStatistics stats = new DescriptiveStatistics();

            for (int i = 1; i <= numRepetitions; i++) {
              System.out.println(i + "/" + numRepetitions + " " + params);
              AntColonyOptimization antColonyOptimization = new AntColonyOptimization(
                  new File(berlin), params);
              double result = antColonyOptimization.start();
              stats.addValue(result);
            }

            double bestValue = stats.getMin() - bestValueBerlin;
            double mean = stats.getMean();
            double stddev = stats.getStandardDeviation();

            queue.add(new GridSearchResult(bestValue, mean, stddev, params));

            writer.write(params.getAlpha() + "," + params.getBeta() + ","
                + params.getPheromonePersistence() + "," + bestValue + ","
                + mean + "," + stddev + "\n");
            writer.flush(); // for tailing the process

            // drop of the worst configs
            while (queue.size() > numBestParametersToKeep) {
              queue.poll();
            }

            outputTop(queue, numBestParametersToKeep);
          }
        }
      }
    }

    outputTop(queue, numBestParametersToKeep);
  }

  public static void outputTop(PriorityQueue<GridSearchResult> queue, int topX) {
    List<GridSearchResult> buffer = new ArrayList<>();
    System.out.println("-----------------------------------------");
    System.out.println();
    while (!queue.isEmpty()) {
      GridSearchResult poll = queue.poll();
      System.out.println("Top " + topX + ": " + poll + " with parameters: "
          + poll.params);
      buffer.add(poll);
      topX--;
    }
    System.out.println();

    // add it back again
    queue.addAll(buffer);
  }

  public static DoubleStream from(double start, double endExclusive,
      double stepSize) {
    return DoubleStream.iterate(start, (s) -> s + stepSize).limit(
        (int) ((endExclusive - start) / stepSize));
  }

  public static IntStream from(int start, int endExclusive, int stepSize) {

    return IntStream.iterate(start, (s) -> s + stepSize).limit(
        (int) ((endExclusive - start) / stepSize));
  }

}

package de.jungblut.antcolony;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AntColonyOptimization {

  private static final int POOL_SIZE = Runtime.getRuntime()
      .availableProcessors();
  // we are queuing this amount of extra work to the CompletionService.
  // the reason is throughput: dequeuing internally has much less overhead
  // than this class waiting on a result to and requeing more work.
  private static final int POOL_OVERCOMMIT = 10;

  private final ExecutorService threadPool = Executors
      .newFixedThreadPool(POOL_SIZE);
  private final ExecutorCompletionService<Path> agentCompletionService = new ExecutorCompletionService<Path>(
      threadPool);

  private final double[][] distanceMatrix;
  private final WeightMatrix weights;

  private final MetaParameters parameters;

  public AntColonyOptimization(File file, MetaParameters parameters)
      throws IOException {
    this.parameters = parameters;
    this.distanceMatrix = TSPFileReader.readDistanceMatrixFromFile(file);
    this.weights = new WeightMatrix(distanceMatrix.length,
        parameters.getPheromonePersistence());
  }

  public double start() throws InterruptedException, ExecutionException {

    Path bestDistance = null;

    int agentsSend = 0;
    int agentsDone = 0;
    int agentsWorking = 0;
    for (int agentNumber = 0; agentNumber < parameters.getNumAgents(); agentNumber++) {
      agentCompletionService.submit(new Agent(parameters, weights,
          distanceMatrix));
      agentsSend++;
      agentsWorking++;
      while (agentsWorking >= POOL_SIZE + POOL_OVERCOMMIT) {
        Path way = agentCompletionService.take().get();
        if (bestDistance == null || way.distance < bestDistance.distance) {
          bestDistance = way;
          System.out.println("Agent returned with new best distance of: "
              + way.distance);
        }
        agentsDone++;
        agentsWorking--;
      }
    }
    final int left = agentsSend - agentsDone;
    System.out.println("Waiting for " + left
        + " agents to finish their random walk!");

    for (int i = 0; i < left; i++) {
      Path path = agentCompletionService.take().get();
      if (bestDistance == null || path.distance < bestDistance.distance) {
        bestDistance = path;
        System.out.println("Agent returned with new best distance of: "
            + path.distance);
      }
    }

    threadPool.shutdownNow();
    System.out.println("Found best so far: " + bestDistance.distance);
    System.out.println(Arrays.toString(bestDistance.path));

    return bestDistance.distance;

  }

  static class Record {
    double x;
    double y;

    public Record(double x, double y) {
      super();
      this.x = x;
      this.y = y;
    }
  }

  static class Path {
    final int[] path;
    final double distance;

    public Path(int[] path, double distance) {
      super();
      this.path = path;
      this.distance = distance;
    }
  }

  public static void main(String[] args) throws IOException,
      InterruptedException, ExecutionException {

    String file = "files/berlin52.tsp";
    if (args.length > 0) {
      file = args[0];
      System.out.println("Using " + args[0]);
    }

    MetaParameters defaults = MetaParameters.defaults();

    long start = System.currentTimeMillis();
    AntColonyOptimization antColonyOptimization = new AntColonyOptimization(
        new File(file), defaults);
    double result = antColonyOptimization.start();
    System.out
        .println("Took: " + (System.currentTimeMillis() - start) + " ms!");
    System.out.println("Result was: " + result);
  }

}

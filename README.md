# Ant Colony Optimization for TSP problems

This repository contains an implementation for solving TSP problems with the famous meta-heuristics ACO (Ant Colony Optimization). 
It runs several agents (Ants) through a weighted random walk until it converges to a (hopefully) good minimum. 

This features a fully multi-threaded (and lock-free) implementation of ACO and contains a GridSearch to optimize the hyper parameters for a problem.
The repository includes the Berlin52 problem, but can be adapted to virtually every TSP file [from the University of Heidelberg's TSP group](http://www.iwr.uni-heidelberg.de/groups/comopt/software/TSPLIB95/tsp/).

The throughput on my machine (i7-5820k @ 3.3ghz and ddr4 RAM) is roughly 13 agents per millisecond (or ~1 agent per logical core per millisecond) and it scales linearly with more cores.

For more details, consult [Wikipedia](http://en.wikipedia.org/wiki/Ant_colony_optimization_algorithms) or [my blog post from 2011](http://codingwiththomas.blogspot.co.uk/2011/08/ant-colony-optimization-for-tsp.html). 

Initially this was a prototype for a paper in Software Engineering (second semester undergraduation). 
Since Google Code closed it's gates, I decided to rewrite major parts and port it to Java 8- although for the most part, it should work with Java 6 without major modifications.

Build
-----

To build locally, you will need at least Java 8 to build this library.

You can simply build with:
 
> mvn clean package install

The created jars contains debuggable code + sources.

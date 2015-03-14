package de.jungblut.antcolony;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.jungblut.antcolony.AntColonyOptimization.Record;

public class TSPFileReader {

  public static final double[][] readDistanceMatrixFromFile(File f)
      throws IOException {

    final List<Record> records = new ArrayList<>();
    try (final BufferedReader br = new BufferedReader(new FileReader(f))) {

      boolean readAhead = false;
      String line;
      while ((line = br.readLine()) != null) {

        if (line.equals("EOF")) {
          break;
        }

        if (readAhead) {
          String[] split = sweepNumbers(line.trim());
          records.add(new Record(Double.parseDouble(split[1].trim()), Double
              .parseDouble(split[2].trim())));
        }

        if (line.equals("NODE_COORD_SECTION")) {
          readAhead = true;
        }
      }
    }

    final double[][] localMatrix = new double[records.size()][records.size()];

    int rIndex = 0;
    for (Record r : records) {
      int hIndex = 0;
      for (Record h : records) {
        localMatrix[rIndex][hIndex] = calculateEuclidianDistance(r.x, r.y, h.x,
            h.y);
        hIndex++;
      }
      rIndex++;
    }

    return localMatrix;
  }

  private static final double calculateEuclidianDistance(double x1, double y1,
      double x2, double y2) {
    final double xDiff = x2 - x1;
    final double yDiff = y2 - y1;
    return Math.abs((Math.sqrt((xDiff * xDiff) + (yDiff * yDiff))));
  }

  private static final String[] sweepNumbers(String input) {
    String[] arr = new String[3];
    int currentIndex = 0;
    for (int i = 0; i < input.length(); i++) {
      final char c = input.charAt(i);
      if ((c) != 32) {
        for (int f = i + 1; f < input.length(); f++) {
          final char x = input.charAt(f);
          if ((x) == 32) {
            arr[currentIndex] = input.substring(i, f);
            currentIndex++;
            break;
          } else if (f == input.length() - 1) {
            arr[currentIndex] = input.substring(i, input.length());
            break;
          }
        }
        i = i + arr[currentIndex - 1].length();
      }
    }
    return arr;
  }

}

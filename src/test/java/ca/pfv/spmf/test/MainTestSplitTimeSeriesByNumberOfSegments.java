package ca.pfv.spmf.test;

import ca.pfv.spmf.NoExceptionAssertion;
import ca.pfv.spmf.algorithms.timeseries.TimeSeries;
import ca.pfv.spmf.algorithms.timeseries.split.AlgoSplitTimeSeries;
import org.junit.Test;

/**
 * Example of how to split a time-series by number of segments using
 * the source code of SPMF
 *
 * @author Philippe Fournier-Viger, 2016.
 */
public class MainTestSplitTimeSeriesByNumberOfSegments {

    @Test
    public void main() {
        NoExceptionAssertion.assertDoesNotThrow(() -> {

            // the number of time series that we want
            int numberOfSeries = 4;

            // Create a time series
            double[] dataPoints = new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
            TimeSeries timeSeries = new TimeSeries(dataPoints, "SERIES1");

            // Applying the  algorithm
            AlgoSplitTimeSeries algorithm = new AlgoSplitTimeSeries();
            TimeSeries[] splittedTimeSeries = algorithm.runAlgorithm(numberOfSeries, timeSeries);
            algorithm.printStats();

            // Print the sax sequences
            System.out.println(" Splitted time series: ");
            for (int i = 0; i < splittedTimeSeries.length; i++) {
                System.out.println("Time series " + i + " " + splittedTimeSeries[i]);
            }

        });
    }
}

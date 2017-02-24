package ca.pfv.spmf.test;


import ca.pfv.spmf.NoExceptionAssertion;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.AlgoCloSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator_Qualitative;
import org.junit.Test;

/**
 * Example of how to use the algorithm PrefixSpan but executing a
 * postprocessing step at the end in order to find only the closed
 * frequent patterns. The output is saved in the main memory
 *
 * @author agomariz
 */
public class MainTestPrefixSpan_PostProcessingStepForClosedMining_saveToMemory {

    /**
     */
    @Test
    public void main() {
        NoExceptionAssertion.assertDoesNotThrow(() -> {
            // Load a sequence database
            double support = (double) 180 / 360;

            boolean keepPatterns = true;
            boolean verbose = false;
            boolean findClosedPatterns = true;
            boolean executePruningMethods = false;

            // if you set the following parameter to true, the sequence ids of the sequences where
            // each pattern appears will be shown in the result
            boolean outputSequenceIdentifiers = true;

            AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();

            SequenceDatabase sequenceDatabase = new SequenceDatabase();

            sequenceDatabase.loadFile("contextPrefixSpan.txt", support);
            //sequenceDatabase.loadFile("contextCloSpan.txt", support);
            //sequenceDatabase.loadFile("gazelle.txt", support);

            //System.out.println(sequenceDatabase.toString());

            AlgoCloSpan algorithm = new AlgoCloSpan(support, abstractionCreator, findClosedPatterns, executePruningMethods);

            algorithm.runAlgorithm(sequenceDatabase, keepPatterns, verbose, null, outputSequenceIdentifiers);
            System.out.println(algorithm.getNumberOfFrequentPatterns() + " pattern found.");

            if (keepPatterns) {
                System.out.println(algorithm.printStatistics());
            }
        });
    }
}

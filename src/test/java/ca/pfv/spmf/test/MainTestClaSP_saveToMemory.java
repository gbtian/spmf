package ca.pfv.spmf.test;

import ca.pfv.spmf.NoExceptionAssertion;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.AlgoClaSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreatorStandard_Map;
import org.junit.Test;

/**
 * Example of how to use the algorithm ClaSP, saving the results in the
 * main memory
 *
 * @author agomariz
 */
public class MainTestClaSP_saveToMemory {

    /**
     */
    @Test
    public void main() {

        NoExceptionAssertion.assertDoesNotThrow(() -> {
            // Load a sequence database
            double support = 0.5;

            boolean keepPatterns = true;
            boolean verbose = true;
            boolean findClosedPatterns = true;
            boolean executePruningMethods = true;
            // if you set the following parameter to true, the sequence ids of the sequences where
            // each pattern appears will be shown in the result
            boolean outputSequenceIdentifiers = false;

            AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
            IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

            SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

            //double relativeSupport = sequenceDatabase.loadFile("contextClaSP.txt", support);
            double relativeSupport = sequenceDatabase.loadFile("contextPrefixSpan.txt", support);
            //double relativeSupport = sequenceDatabase.loadFile("gazelle.txt", support);

            AlgoClaSP algorithm = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns, executePruningMethods);


            //System.out.println(sequenceDatabase.toString());
            algorithm.runAlgorithm(sequenceDatabase, keepPatterns, verbose, null, outputSequenceIdentifiers);
            System.out.println("Minsup (relative) : " + support);
            System.out.println(algorithm.getNumberOfFrequentPatterns() + " patterns found.");

            if (verbose && keepPatterns) {
                System.out.println(algorithm.printStatistics());
            }

            //uncomment if we want to see the Trie graphically
//        ShowTrie.showTree(algorithm.getFrequentAtomsTrie());
        });
    }
}

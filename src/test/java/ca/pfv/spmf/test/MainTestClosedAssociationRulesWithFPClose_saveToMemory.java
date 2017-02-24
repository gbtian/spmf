package ca.pfv.spmf.test;

import ca.pfv.spmf.NoExceptionAssertion;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRules;
import ca.pfv.spmf.algorithms.associationrules.closedrules.AlgoClosedRules_UsingFPClose;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPClose;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Example of how to mine closed association rules using FPClose from the source code and save the result to memory
 *
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestClosedAssociationRulesWithFPClose_saveToMemory {

    @Test
    public void main() {
        NoExceptionAssertion.assertDoesNotThrow(() -> {
            // input file path
            String input = "contextZart.txt";

            // the threshold
            double minsup = 0.60;
            double minconf = 0.60;

            // Loading the transaction database
            TransactionDatabase database = new TransactionDatabase();
            try {
                database.loadFile(input);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//		database.printDatabase();

            // STEP 1: Applying the Charm algorithm to find frequent closed itemsets
            AlgoFPClose algo = new AlgoFPClose();
            // Run the algorithm
            // Note that here we use "null" as output file path because we want to keep the results into memory instead of saving to a file
            Itemsets patterns = algo.runAlgorithm(input, null, minsup);

            // Show the CFI-Tree for debugging!
//		System.out.println(algo.cfiTree);

            // show the execution time and other statistics
            algo.printStats();
            // print the patterns to System.out
            patterns.printItemsets(algo.getDatabaseSize());

            // STEP 2: Generate all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
            AlgoClosedRules_UsingFPClose algoClosedRules = new AlgoClosedRules_UsingFPClose();
            AssocRules rules = algoClosedRules.runAlgorithm(patterns, null, database.size(), minconf, algo.cfiTree);
            algoClosedRules.printStats();
            rules.printRules(database.size());
        });
    }
}

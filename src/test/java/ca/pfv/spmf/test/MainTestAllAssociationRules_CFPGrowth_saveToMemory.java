package ca.pfv.spmf.test;

import ca.pfv.spmf.NoExceptionAssertion;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRules;
import ca.pfv.spmf.algorithms.frequentpatterns.cfpgrowth.AlgoCFPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import org.junit.Test;

/**
 * Example of how to mine all association rules with CFPGROWTH,
 * from the source code.
 *
 * @author Philippe Fournier-Viger (Copyright 2014)
 */
public class MainTestAllAssociationRules_CFPGrowth_saveToMemory {

    @Test
    public void main() {

        NoExceptionAssertion.assertDoesNotThrow(() -> {
            String input = "contextIGB.txt";
            String MISfile = "MIS.txt";

            // STEP 1: Applying the CFP-GROWTH algorithm to find frequent itemsets
            AlgoCFPGrowth cfpgrowth = new AlgoCFPGrowth();
            Itemsets patterns = cfpgrowth.runAlgorithm(input, null, MISfile);
//		patterns.printItemsets(database.size());
            int databaseSize = cfpgrowth.getDatabaseSize();
            cfpgrowth.printStats();

            // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
            double minconf = 0.90;
            AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
            // the next line run the algorithm.
            // Note: we pass null as output file path, because we don't want
            // to save the result to a file, but keep it into memory.
            AssocRules rules = algoAgrawal.runAlgorithm(patterns, null, databaseSize, minconf);
            rules.printRules(databaseSize);

            //System.out.println("DATABASE SIZE :" + databaseSize);

        });
    }
}

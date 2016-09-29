package ca.pfv.spmf.algorithms.frequentpatterns.upgrowth_ihup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This is an implementation of the UPGrowthPlus algorithm.<\br><\br>
 *
 * Copyright (c) 2015 Prashant Barhate <\br><\br>
 *
 * The UP-GrowthPlus algorithm was proposed in this paper: <\br><\br>
 *
 * V. S. Tseng, B.-E. Shie, C.-W. Wu, and P. S. Yu. Efficient ca.pfv.spmf.algorithms for mining high
 *  utility itemsets from transactional databases. 
 * IEEE Transactions on Knowledge and Data Engineering, 2012, doi: 10.1109/TKDE.2012.59.<\br><\br>
 *
 * This file is part of the SPMF DATA MINING SOFTWARE *
 * (http://www.philippe-fournier-viger.com/spmf). <\br><\br>
 *
 * SPMF is free software: you can redistribute it and/or modify it under the *
 * terms of the GNU General Public License as published by the Free Software *
 * Foundation, either version 3 of the License, or (at your option) any later *
 * version. SPMF is distributed in the hope that it will be useful, but WITHOUT ANY *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details. <\br><\br>
 *
 * You should have received a copy of the GNU General Public License along with SPMF.
 * If not, see <http://www.gnu.org/licenses/>. <\br><\br>
 *
 * @author Prashant Barhate
 */

public class AlgoUPGrowthPlus {

	// variable for statistics
	private double maxMemory = 0; // the maximum memory usage
	private long startTimestamp = 0; // the time the algorithm started
	private long endTimestamp = 0; // the time the algorithm terminated
	private int huiCount = 0; // the number of HUIs generated
	private int phuisCount; // the number of PHUIs generated

	// map for minimum node utility during DLU(Decreasing Local Unpromizing
	// items) strategy
	private Map<Integer, Integer> mapMinimumItemUtility;

	// writer to write the output file
	private BufferedWriter writer = null;

	// Structure to store the potential HUIs
	private List<Itemset> phuis = new ArrayList<Itemset>();

	// To activate debug mode
	private final boolean DEBUG = false;

	/**
	 * Method to run the algorithm
	 *
	 * @param input path to an ca.pfv.spmf.input file
	 * @param output  path for writing the output file
	 * @param minUtility  the minimum utility threshold
	 * @throws IOException  exception if error while reading or writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility)
			throws IOException {

		maxMemory = 0;

		startTimestamp = System.currentTimeMillis();

		writer = new BufferedWriter(new FileWriter(output));

		// We create a map to store the TWU of each item
		final Map<Integer, Integer> mapItemToTWU = new HashMap<Integer, Integer>();

		// ******************************************
		// First database scan to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%' || thisLine.charAt(0) == '@') {
					continue;
				}

				// split the transaction according to the : separator
				String split[] = thisLine.split(":");
				// the first part is the list of items
				String items[] = split[0].split(" ");
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);

				// for each item, we add the transaction utility to its TWU
				for (int i = 0; i < items.length; i++) {
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);
					// add the utility of the item in the current transaction to its twu
					twu = (twu == null) ? transactionUtility : twu	+ transactionUtility;
					mapItemToTWU.put(item, twu);
				}
			}
		} catch (Exception e) {
			// catches exception if error while reading the ca.pfv.spmf.input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}

		// ******************************************
		// second database scan  generate revised transaction and global UP-Tree
		// and calculate the minimum utility of each item
		// (required by the DLU(Decreasing Local Unpromizing items) strategy)
		mapMinimumItemUtility = new HashMap<Integer, Integer>();

		try {
			UPTreePlus tree = new UPTreePlus();

			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));

			// Transaction ID to track transactions
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%' || thisLine.charAt(0) == '@') {
					continue;
				}

				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");

				int remainingUtility = 0;

				// Create a list to store items
				List<Item> revisedTransaction = new ArrayList<Item>();
				// for each item
				for (int i = 0; i < items.length; i++) {
					// convert values to integers

					int itm = Integer.parseInt(items[i]);
					int utility = Integer.parseInt(utilityValues[i]);

					if (mapItemToTWU.get(itm) >= minUtility) {
						Item element = new Item(itm, utility);
						// add it
						revisedTransaction.add(element);
						remainingUtility += utility;

						// get the current Minimum Item Utility of that item
						Integer minItemUtil = mapMinimumItemUtility.get(itm);

						// Minimum Item Utility is utility of Transaction T if there
						// does not exist Transaction T' such that utility(T')<
						// utility(T)
						if ((minItemUtil == null) || (minItemUtil >= utility)) {
							mapMinimumItemUtility.put(itm, utility);
						}

						// prepare object for garbage collection
						element = null;
					}
				}

				// revised transaction in desceding order of TWU
				Collections.sort(revisedTransaction, new Comparator<Item>() {
					public int compare(Item o1, Item o2) {
						return compareItemsDesc(o1.name, o2.name, mapItemToTWU);
					}
				});

				// add transaction to the global UP-Tree
				tree.addTransaction(revisedTransaction, remainingUtility);
			}

			// We create the header table for the global UP-Tree
			tree.createHeaderList(mapItemToTWU);

			// check the memory usage
			checkMemory();

			if(DEBUG) {
				System.out.println("GLOBAL TREE" + "\nmapITEM-TWU : " +mapItemToTWU +
					"\nmapITEM-MINUTIL : " +mapMinimumItemUtility + "\n" + tree.toString());
			}

			// Mine tree with UPGrowthPlus with 2 strategies DLU and DLN
			upgrowthPlus(tree, minUtility, new int[0]);

			// check the memory usage again and close the file.
			checkMemory();

		} catch (Exception e) {
			// catches exception if error while reading the ca.pfv.spmf.input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}

		// save the number of candidate found
		phuisCount = phuis.size();

		// ******************************************
		// Third database scan to calculate the
		// exact utility of each PHUIs and output those that are HUIS.

		// First sort the PHUIs by size for optimization
		Collections.sort(phuis, new Comparator<Itemset>() {
			public int compare(Itemset arg0, Itemset arg1) {
				return arg0.size() - arg1.size();
			}}
		);

		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));

			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%' || thisLine.charAt(0) == '@') {
					continue;
				}

				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");

				// Create a list to store items
				List<Item> revisedTransaction = new ArrayList<Item>();
				// for each item
				for (int i = 0; i < items.length; i++) {
					// / convert values to integers
					int item = Integer.parseInt(items[i]);
					int utility = Integer.parseInt(utilityValues[i]);

					Item element = new Item(item, utility);
					if (mapItemToTWU.get(item) >= minUtility) {
						revisedTransaction.add(element);
					}
				}

				// sort the transaction by lexical order
				// for faster comparison since PHUIs have been sorted
				// by lexical order and this will make faster
				// comparison
				Collections.sort(revisedTransaction, new Comparator<Item>() {
					public int compare(Item o1, Item o2) {
						return o1.name - o2.name;
					}});

				//  Compare each itemset with the transaction
				for(Itemset itemset : phuis){
					// OPTIMIZATION:
					// if this itemset is larger than the current transaction
					// it cannot be included in the transaction, so we stop
					// and we don't need to consider the folowing itemsets
					// either since they are ordered by increasing size.
					if(itemset.size() > revisedTransaction.size()) {
						break;
					}

					// Now check if itemset is included in the transaction
					// and if yes, update its utility
					updateExactUtility(revisedTransaction, itemset);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// OUTPUT ALL HUIs
		for(Itemset itemset : phuis) {
			if(itemset.getExactUtility() >= minUtility) {
				writeOut(itemset);
			}
		}

		// check the memory usage again
		checkMemory();

		// record end time
		endTimestamp = System.currentTimeMillis();

		// Release some memory
		phuis.clear();
		mapMinimumItemUtility = null;
		// CLOSE OUTPUT FILE
		writer.close();
	}


	/**
	 * Compare two items according to the ascending order of TWU.
	 * @param item1 the first item
	 * @param item2 the second item
	 * @param mapItemEstimatedUtility the map indicating the TWU of each item
	 * @return -1 if item1 is smaller than 2. 1 if item 1 is greater than 2. Otherwise 0.
	 */
	private int compareItemsDesc(int item1, int item2, Map<Integer, Integer> mapItemEstimatedUtility) {
		int compare = mapItemEstimatedUtility.get(item2) - mapItemEstimatedUtility.get(item1);
		// if the same, use the lexical order otherwise use the TWU
		return (compare == 0) ? item1 - item2 : compare;
	}

	/**
	 * Mine UP Tree recursively
	 *
	 * @param tree UPTree to mine
	 * @param minUtility minimum utility threshold
	 * @param prefix the prefix itemset
	 */
	private void upgrowthPlus(UPTreePlus tree, int minUtility, int[] prefix) throws IOException {

		// For each item in the header table list of the tree in reverse order.
		for (int i = tree.headerList.size() - 1; i >= 0; i--) {
			// get the item
			Integer item = tree.headerList.get(i);

			// ===== CREATE THE LOCAL TREE =====
			UPTreePlus localTree = createLocalTree(minUtility, tree, item);
			// NEXT LINE IS FOR DEBUGING:
			if(DEBUG) {
				System.out.println("LOCAL TREE for projection by:" + ((prefix== null)?"": Arrays.toString(prefix)+ ",")  + item +
					"\n" + localTree.toString());
			}

			// ===== CALCULATE SUM OF ITEM NODE UTILITY =====
			// take node from bottom of header table
			UPNodePlus pathCPB = tree.mapItemNodes.get(item);
			// take item
//			int itemCPB = pathCPB.itemID;
			int pathCPBUtility = 0;
			while (pathCPB != null) {
				// sum of items node utility
				pathCPBUtility += pathCPB.nodeUtility;
				pathCPB = pathCPB.nodeLink;
			}

			// if path utility of 'item' in header table is greater than
			// minUtility
			// then 'item' is a PHUI (Potential high utility itemset)
			if (pathCPBUtility >= minUtility) {
				// Create the itemset by appending the item to the current prefix
				// This gives us a PHUI
				int[] newPrefix = new int[prefix.length+1];
				System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
				newPrefix[prefix.length] = item;

				// Save the PHUI
				savePHUI(newPrefix);

				// Make a recursive call to the UPGrowthPlus procedure to explore
				// other itemsets that are extensions of the current PHUI
				if(localTree.headerList.size() >0) {

					upgrowthPlus(localTree, minUtility, newPrefix);
				}
			}
		}
	}

	private UPTreePlus createLocalTree(int minUtility, UPTreePlus tree, Integer item) {

		// === Construct conditional pattern base ===
		// It is a subdatabase which consists of the set of prefix paths
		List<List<UPNodePlus>> prefixPaths = new ArrayList<List<UPNodePlus>>();
		UPNodePlus path = tree.mapItemNodes.get(item);

		// map to store path utility of local items in CPB
		final Map<Integer, Integer> itemPathUtility = new HashMap<Integer, Integer>();
		while (path != null) {

			// get the Node Utiliy of the item
			int nodeutility = path.nodeUtility;
			// if the path is not just the root node
			if (path.parent.itemID != -1) {
				// create the prefixpath
				List<UPNodePlus> prefixPath = new ArrayList<UPNodePlus>();
				// add this node.
				prefixPath.add(path); // NOTE: we add it just to keep its 								// utility,
				// actually it should not be part of the prefixPath

				// Recursively add all the parents of this node.
				UPNodePlus parentnode = path.parent;
				while (parentnode.itemID != -1) {
					prefixPath.add(parentnode);

					// pu - path utility
					Integer pu = itemPathUtility.get(parentnode.itemID);
					pu = (pu == null) ? nodeutility : pu + nodeutility;

					itemPathUtility.put(parentnode.itemID, pu);
					parentnode = parentnode.parent;
				}
				// add the path to the list of prefixpaths
				prefixPaths.add(prefixPath);
			}
			// We will look for the next prefixpath
			path = path.nodeLink;
		}

		if(DEBUG) {
			System.out.println("\n\n\nPREFIXPATHS:");
			for (List<UPNodePlus> prefixPath : prefixPaths) {
				for(UPNodePlus node : prefixPath) {
					System.out.println("    " +node);
				}
				System.out.println("    --");
			}
		}

		// Calculate the Utility of each item in the prefixpath
		UPTreePlus localTree = new UPTreePlus();
		
		Map<Integer,Integer> mapMiniNodeUtility = new HashMap<Integer,Integer>();
		
		//  ======= Calculate the minimum utility of each item =====
		// for each prefixpath
		for (List<UPNodePlus> prefixPath : prefixPaths) {
			// for each node in the prefixpath, except the first one
			for (int j = 1; j < prefixPath.size(); j++) {
				UPNodePlus node = prefixPath.get(j);

				// Here is DLU Strategy  #################
				// we check whether local item is promising or not
				if (itemPathUtility.get(node.itemID) >= minUtility) {
					Integer util = mapMiniNodeUtility.get(node.itemID);
					if(util == null) {
						mapMiniNodeUtility.put(node.itemID, node.minimalNodeUtility);  // MOVED BY PHILIPPE 
					}else if(node.minimalNodeUtility < util) {
						mapMiniNodeUtility.put(node.itemID, node.minimalNodeUtility);
					}
				}
			}
		}
		// ======  End of minimum utility calculation ===== 

		// for each prefixpath
		for (List<UPNodePlus> prefixPath : prefixPaths) {
			// the Utility of the prefixpath is the node utility of its
			// first node.
			int pathCount = prefixPath.get(0).count;
			int pathUtility = prefixPath.get(0).nodeUtility;

			List<Integer> localPath = new ArrayList<Integer>();
			// for each node in the prefixpath,
			// except the first one, we count the frequency
			for (int j = 1; j < prefixPath.size(); j++) {

				int itemValue = 0; // It store multiplication of minimum
									// item utility and pathcount
				// for each node in prefixpath
				UPNodePlus node = prefixPath.get(j);

				// Here is DLU Strategy  #################
				// we check whether local item is promising or not
				if (itemPathUtility.get(node.itemID) >= minUtility) {
					localPath.add(node.itemID);
				} else { // If item is unpromising then we recalculate path
							// utillity
					// Here we requre to calculate path utility by
					// subctracting minimal node utility of unpromising item
					itemValue = node.minimalNodeUtility * pathCount;
				}
				pathUtility = pathUtility - itemValue;

			}
			if(DEBUG) {
				System.out.println("  path utility after DGU,DGN,DLU: " + pathUtility);
			}

			// we reorganize local path in decending order of path utility
			Collections.sort(localPath, new Comparator<Integer>() {

				public int compare(Integer o1, Integer o2) {
					// compare the TWU of the items
					return compareItemsDesc(o1, o2, itemPathUtility);
				}
			});

			// create tree for conditional pattern base
			localTree.addLocalTransaction(localPath, pathUtility, mapMiniNodeUtility, pathCount);
		}

		// We create the local header table for the tree item - CPB
		localTree.createHeaderList(itemPathUtility);
		return localTree;
	}


	/**
	 * Save a PHUI in the list of PHUIs
	 * @param itemset the itemset
	 */
	private void savePHUI(int[] itemset)  {
		// Create an itemset object and store it in the list of pHUIS
		Itemset itemsetObj = new Itemset(itemset);
		// Sort the itemset by lexical order to faster calculate its
		// exact utility later on.
		Arrays.sort(itemset);
		// add the itemset to the list of PHUIs
		phuis.add(itemsetObj);
	}


	/**
	 * Update the exact utility of an itemset given a transaction
	 * It assumes that itemsets are sorted according to the lexical order.
	 * @param itemset1 the first itemset
	 * @param itemset2 the second itemset
	 * @return true if the first itemset contains the second itemset
	 */
	public void updateExactUtility(List<Item> transaction, Itemset itemset){
		int utility = 0;
			// for each item in the  itemset
loop1:		for(int i =0; i < itemset.size(); i++){
				Integer itemI = itemset.get(i);
				// for each item in the transaction
				for(int j =0; j < transaction.size(); j++){
					Item itemJ = transaction.get(j);
					// if the current item in transaction is equal to the one in itemset
					// search for the next one in itemset1
					if(itemJ.name == itemI){
						utility += transaction.get(j).utility;
						continue loop1;
					}
				    // if the current item in itemset1 is larger
					// than the current item in itemset2, then
					// stop because of the lexical order.
					else if(itemJ.name  > itemI){
						return;
					}
				}
				// means that an item was not found
				return;
			}
			// if all items were found, increase utility.
	 		itemset.increaseUtility(utility);
	}


	/**
	 * Write a HUI to the output file
	 * @param HUI
	 * @param utility
	 * @throws IOException
	 */
	private void writeOut(Itemset HUI) throws IOException {
		huiCount++; // increase the number of high utility itemsets found

		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		//Append each item
		for (int  i = 0; i < HUI.size(); i++) {
			buffer.append(HUI.get(i));
			buffer.append(' ');
		}
		buffer.append("#UTIL: ");
		buffer.append(HUI.getExactUtility());

		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * Method to check the memory usage and keep the maximum memory usage.
	 */
	private void checkMemory() {
		// get the current memory usage
		double currentMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d;
		// if higher than the maximum until now  replace the maximum with the current memory usage
		if (currentMemory > maxMemory) {
			maxMemory = currentMemory;
		}
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  UP-GROWTH+ ALGORITHM v96r17 - STATS =============");
		System.out.println(" PHUIs (candidates) count: " + phuisCount);
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ " + maxMemory + " MB");
		System.out.println(" HUIs count : " + huiCount);
		System.out.println("===================================================");
	}

}
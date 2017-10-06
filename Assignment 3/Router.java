/* Fall 2014 CSci4211: Introduction to Computer Networks
** This program serves as the Router.
** Written in Java.
** Creation: 11/05/2014	Modify: 11/05/2014 */

import java.io.*;
import java.net.*;
import java.util.*;

class Router {
	public static void main(String[] args) throws Exception {
		String host = "localhost";
		int port = 5001;

		Socket cSock = null;
		DataOutputStream sendOut = null;
		BufferedReader readFrom = null;

		/*-----------------  First Phase, Initialization ------------- */

		try{
			cSock = new Socket(host, port);
			sendOut = new DataOutputStream(cSock.getOutputStream());
			readFrom = new BufferedReader(new InputStreamReader(cSock.getInputStream()));
		} catch (Exception e) {
			System.out.println("Error: cannot open socket");
			System.exit(1); // Handle exceptions.
		}

		String N = ""; // N is the number of total routers (it is a string).
		N = readFrom.readLine();
		sendOut.writeBytes("ACK" + "\n"); // An ACK must be sent.
		System.out.println("Received the total number of routers.");

		String costMatrix = ""; // costMatrix is the corresponding cost matrix (it is one dimensional, and it is a string).
		costMatrix = readFrom.readLine();
		sendOut.writeBytes("ACK" + "\n");
		System.out.println("Received the cost matrix.");

		String ipRange = ""; // ipRange is the IP range for which each route servers. It is a string, please refer to its format in the project description.
		ipRange = readFrom.readLine();
		sendOut.writeBytes("ACK" + "\n");
		System.out.println("Received the IP range.");
		int num = Integer.parseInt(N);
		int[][] costs = new int[num][num];
		String[] tokens = costMatrix.split(",");
		for(int i = 0; i < tokens.length; i++) {
			tokens[i] = tokens[i].trim();
		}
		for(int i = 0; i < num; i++) {
			for(int j = 0; j < num; j++) {
				int index = (i * num) + j;
				int intToken = Integer.parseInt(tokens[index]);
				costs[i][j] = intToken;
			}
		}
		int[] myCosts = new int[num];
		boolean[] unvisited = new boolean[num];
		String[] paths = new String[num];
		String[] nextHops = new String[num];
		for(int i = 0; i < num; i++) {
			myCosts[i] = -1;
			unvisited[i] = true;
			paths[i] = "";
			nextHops[i] = "";
		}
		int currentlyVisiting = -1;
		/*	
			Note:
				1. You need to do type casting to convert the above three variables into correct primitive types.
				2. You also need to parse the ipRange string to get the IP address range for each router.
		*/

		/*	---------------------------- Second Phase, Find the Shortest Path -------------------------

			1. Implement Dijkstra's Algorithm and find the shortest paths from your router, i.e., Router 0.
		*/
		
		do {
			if(currentlyVisiting == -1) {
				currentlyVisiting = 0;
			}
			else {
				int minVal = -1;
				for(int i = 0; i < num; i++) {
					if(unvisited[i]) {
						int cost = myCosts[i];
						if(cost < 0) {
							continue;
						}
						if(cost < minVal || minVal < 0) {
							minVal = cost;
							currentlyVisiting = i;
						}
					}
				}
				if(minVal < 0) {
					System.err.println("minVal is -1 after scanning, I think this means router 0 isn't connected to any routers");
					System.exit(1);
				}
			}
			for(int i = 0; i < num; i++) {
				int newCost = costs[currentlyVisiting][i];
				if(newCost < 0) {
					continue;
				}
				if(myCosts[currentlyVisiting] < 0) {
					
				}
				else {
					newCost = newCost + myCosts[currentlyVisiting];
				}
				if(newCost < myCosts[i] || myCosts[i] < 0) {
					myCosts[i] = newCost;
					if(currentlyVisiting != i) {
						paths[i] = paths[currentlyVisiting] + Integer.toString(i);
					}
				}
			}
			unvisited[currentlyVisiting] = false;
		} while(checkUnvisitedBools(unvisited));
		paths[0] = "0";
		for(int i = 0; i < num; i++) {
			if(!paths[i].isEmpty()) {
				nextHops[i] = paths[i].substring(0,1);
			}
		}
		/*	----------------------------- Third Phase, Routing/Forwarding --------------------------

			1. After 10 seconds, you will receive a string, which is an IP address, e.g. "192.168.0.38".
			You need to figure out to which router you need to send it. For example, if you need to send it to Router 3 (Recall you are always Router 0), you should send a string "3".

			2. Attention, every message is of string type!
		*/
		
		String[] tempTokens = ipRange.split("[\\[\\] ]");
		String[] ipTokens = new String[num];
		long[] startingAddresses = new long[num];
		long[] endingAddresses = new long[num];
		int x = 0;
		for(int i = 0; i < tempTokens.length; i++) {
			if(!tempTokens[i].isEmpty()) {
				String[] addresses = tempTokens[i].split("-");
				long startingAddress = convertAddressToInt(addresses[0]);
				long endingAddress = convertAddressToInt(addresses[1]);
				startingAddresses[x] = startingAddress;
				endingAddresses[x] = endingAddress;
				x++;
			}
		}
		String IP = "";
		while(!(IP = readFrom.readLine()).equals("END")) {
			System.out.println("Received " + IP + ". Forwarding...");
			long IPAddr = convertAddressToInt(IP);
			for(int i = 0; i < num; i++) {
				if(IPAddr >= startingAddresses[i] && IPAddr < endingAddresses[i]) {
					sendOut.writeBytes(nextHops[i] + "\n");
					break;
				}
			}
		}
		System.out.println("Received END. Exit.");
		
		/*
			Note:
				You will receive some requests from the TestCase. The number is not fixed so please do not take it for granted. I may use another test case to grade.
				When you receive a string called "END", this indicated the end of the testing process.
		*/
	}
	
	public static boolean checkUnvisitedBools(boolean[] bools) {
		boolean result = false;
		for(int i = 0; i < bools.length; i++) {
			result = result | bools[i];
		}
		return result;
	}
	
	public static long convertAddressToInt(String address) {
		//System.out.println(address);
		String[] addressChunks = address.split("\\.");
		//for(int i = 0; i < addressChunks.length; i++) {
		//	System.out.println(addressChunks[i]);
		//}
		//System.out.flush();
		long highestByte = Long.parseLong(addressChunks[0]) << 24;
		long nextHighestByte = Long.parseLong(addressChunks[1]) << 16;
		long nextLowestByte = Long.parseLong(addressChunks[2]) << 8;
		long lowestByte = Long.parseLong(addressChunks[3]);
		return highestByte | nextHighestByte | nextLowestByte | lowestByte;
	}
}

/* Author: Guobao Sun
** Fall 2014 CSci4211: Introduction to Computer Networks
** This program serves as the client of URL query.
** Written in Java.
** Creation: 09/19/2014	Modify: 09/19/2014 */

import java.io.*;
import java.net.*;

class Client {
	public static void main(String[] args) throws Exception {
		String host = "localhost"; // Remote hostname. It can be changed to anything you desire.
		int port = 5001; // Port number.
		String sentence; // Store user input.
		String data; // Store the server's feedback.

		Socket cSock = null;
		DataOutputStream sendOut = null;
		BufferedReader readFrom = null;

		try{
			cSock = new Socket(host, port); // Initialize the socket.
			sendOut = new DataOutputStream(cSock.getOutputStream()); // The output stream to server.
			readFrom = new BufferedReader(new InputStreamReader(cSock.getInputStream())); // The input stream from server.
		} catch (Exception e) {
			System.out.println("Error: cannot open socket");
			System.exit(1); // Handle exceptions.
		}

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); // Get input from user.
		while (true) {
			
			System.out.println("Type in a domain name to query, or 'q' to quit:");
			sentence = inFromUser.readLine(); // Prompt to get user's input.
			try {
				if (sentence.equalsIgnoreCase("q")) {
					cSock.close();
					System.exit(0); // Quit the program.
				} else {			
					sendOut.writeBytes(sentence + "\n");
									System.out.println("Sent data, waiting for data.");
					data = readFrom.readLine(); // Send and receive.
					System.out.println("Received: " + data);
				}
			}
			catch(Exception e) {
				System.out.println("Network error. Disconnected from server.");
			}
		}
	}
}
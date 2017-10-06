/* Author: Guobao Sun
** Fall 2014 CSci4211: Introduction to Computer Networks
** This program serves as the server of URL query.
** Written in Java.
** Creation: 09/19/2014	Modify: 09/19/2014 */

/* The places that you need to fill codes in are marked with ............ */

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class Server {
	public static void main(String[] args) throws Exception {
		int port = 5001;
		ServerSocket sSock = null;
                ConcurrentHashMap<String, String> hashmap = new ConcurrentHashMap<>();
		try {
                    sSock = new ServerSocket(port);
			//............ // Try to open server socket at port 5001.
		} catch (Exception e) {
			System.out.println("Error: cannot open socket");
			System.exit(1); // Handle exceptions.
		}

		System.out.println("Server is listening...");
		new monitorQuit().start(); // Start a new thread to monitor exit signal.

		while (true) {
			new urlQuery(sSock.accept(), hashmap).start();
		} // Start a new thread to handle queries.
	}
}

class urlQuery extends Thread {
	Socket sSock = null;
        DataOutputStream sendOut;
        BufferedReader readFrom;
        String data;
        String HTMLString;
        ConcurrentHashMap<String, String> hashmap;
    urlQuery(Socket sSock, ConcurrentHashMap hashmap) {
    	this.sSock = sSock; // We can use sSock to refer to the socket we use.
        this.hashmap = hashmap;
    }
	@Override
	public void run(){

		//............
		/* There are several things you need to do here */
		//............
		/* Step 1: Create corresponding input and output stream for the socket, i.e., sSock. */
            try {
                sendOut = new DataOutputStream(sSock.getOutputStream()); // The output stream to client.
                readFrom = new BufferedReader(new InputStreamReader(sSock.getInputStream())); // The input stream from client.
                while(true) {
                    data = readFrom.readLine();
                    System.out.println(data);
					int pointer = 0;
					String http = "http://";
					String www = "www.";
					if (data.contains("http://")) {
						pointer = pointer + 7;
					}
					else if (data.contains("https://")) {
						pointer = pointer + 8;
						http = "https://";
					}
					if (data.contains("www.")) {
						pointer = pointer + 4;
					}
					String dataContents = data.subSequence(pointer, data.length()).toString();
					data = http + www + dataContents;
					if (!data.endsWith("/")) {
						data = data + "/";
					}
					System.out.println(data);
                    HTMLString = hashmap.get(data);
                    if (HTMLString == null) {
                        HTMLString = getHTMLFromURL(data);
                        hashmap.put(data, HTMLString);
                        System.out.println("Had to retrieve HTML from remote server.");
                    }
                    
                    System.out.println("Sending HTMLString");
                    
                    sendOut.writeBytes(HTMLString + "\n");
                    System.out.println("Wrote HTMLString");
                    
                }
            }
            catch(Exception e) {
                System.out.println("Client likely closed the connection");; // Like a boss
                try {
                    sendOut.close();
                    readFrom.close();
                    sSock.close();
                    System.out.println("Data Streams and sockets have been closed.");
                }
                catch(IOException ie) {
                    System.out.println("Data Streams and Sockets have already been closed");
                }
                
            }
		//............
		/* Step 2: Fetch message from the client side. */
            
		//............
		/* Step 3: Use getHTMLFromURL function to get content of the given URL. */
		//............
		/* Step 4: Check if the query is cached in the local file. If it has been cached, you can directly send the local result to the client. Otherwise, you need to connect to the remote server to get it. */
		//............
	}

	public static String getHTMLFromURL (String src) {
		try {
			String htmlData;
			StringBuilder copiedHTMLResponse = new StringBuilder();
				
			URL obj = new URL(src);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");			
			int responseCode = con.getResponseCode();
			BufferedReader in = null;

			try {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				while ((htmlData = in.readLine()) != null){
					System.out.println(htmlData);
					copiedHTMLResponse.append(htmlData);
				}
				return copiedHTMLResponse.toString();
			} catch (UnknownHostException e) {
				htmlData = "URL Not Found";
				return htmlData;
			}
		} catch (Exception e) {

		}
		return "URL Not Found";
	}
}

class monitorQuit extends Thread {
	@Override
	public void run() {
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(System.in)); // Get input from user.
		String st = null;
		while(true){
			try{
				st = inFromClient.readLine();
			} catch (IOException e) {
			}
            if(st.equalsIgnoreCase("exit")){
                System.exit(0);
            }
        }
	}
}
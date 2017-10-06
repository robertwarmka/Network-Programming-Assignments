import java.io.*;
import java.net.*;
import java.util.*;
import java.security.MessageDigest;

class Client {
	public static void main(String[] args) throws Exception {
		String host = "localhost"; // Remote hostname. It can be changed to anything you desire.
		int port = 5001; // Port number.
		String sentence; // Store user input.
		String data; // Store the server's feedback.
		String message = "";
		String packet = "";
		byte[] b = new byte[467];
		char[] c = new char[467];
		boolean resend = false;
		boolean skip_next = false;
		char seqno = '0';
		//char large, medium, small;
		String data_size = "";
		char is_last = 'y';
		String expected_acknum = "0";

		Socket cSock = null;
		DataOutputStream sendOut = null;
		BufferedReader readFrom = null;
		BufferedReader inFromUser = null;
		FileInputStream fin = null;
		String filename = "";;
		if(args.length > 0) {
			filename = args[0];
		}
		try{
			fin = new FileInputStream(filename);
			inFromUser = new BufferedReader(new InputStreamReader(System.in));
			cSock = new Socket(host, port); // Initialize the socket.
			sendOut = new DataOutputStream(cSock.getOutputStream()); // The output stream to server.
			readFrom = new BufferedReader(new InputStreamReader(cSock.getInputStream())); // The input stream from server.
		} catch (Exception e) {
			System.out.println("Error: cannot open socket");
			System.exit(1); // Handle exceptions.
		}
		/* Following part is how to handle timeout event. Please pay attention! */

		while(true){
			cSock.setSoTimeout(1500); // Set the time out in milliseconds.
			try{
				if(skip_next) {
					skip_next = false;
				}
				else if(resend) {
					System.out.println("Resending");
					sendOut.writeBytes(packet);
					resend = false;
				}
				else {
					int avail = fin.available();
					if (avail == 0) {
						break;
					}
					System.out.println("Getting next chunk of data");
					message = "";
					for(int i = 0; i < b.length; i++) {
						b[i] = ' ';
					}
					fin.read(b);
					if (avail >= 467) {
						data_size = "467";
					}
					else {
						data_size = Integer.toString(avail / 100);
						avail = avail % 100;
						data_size = data_size + Integer.toString(avail / 10);
						avail = avail % 10;
						data_size = data_size + Integer.toString(avail);
						is_last = 'z';
					}
					for(int i = 0; i < b.length; i++) {
						c[i] = (char)b[i];
					}
					message = new String(c);
					packet = seqno + data_size + is_last + message;
					String checksum = SHA1(packet);
					packet = checksum + packet;
					sendOut.writeBytes(packet);
					if(seqno == '0') {
						seqno = '1';
					}
					else {
						seqno = '0';
					}
				}
				data = readFrom.readLine();
				// Potential to mangle or drop the ACK here. Do for extra credit.
				String real_data = replace(data, 1, 10, 10);
				System.out.println("ACK after replace: " + real_data);
				if(real_data.isEmpty()) {
					System.out.println("ACK got lost, we'll call this a timeout. Resending");
					resend = true;
					continue;
				}
				if(!real_data.equals("ACK0") && !real_data.equals("ACK1")) {
					System.out.println("ACK got mangled, we'll call this a timeout. Resending");
					resend = true;
					continue;
				}
				String ack_num = data.substring(3);
				System.out.println("ACK number is: " + ack_num);
				if(!ack_num.equals(expected_acknum)) {
					System.out.println("Wrong ACK received. Skipping next transmission and waiting for next ACK (until timeout).");
					skip_next = true;
					continue;
				}
				if(expected_acknum.equals("0")) {
					expected_acknum = "1";
				}
				else {
					expected_acknum = "0";
				}
				System.out.println(data);
			} catch (Exception e) {
				String classType = e.getClass().toString();
				//SocketTimeoutException
				if(classType.contains("SocketTimeoutException")) {
					//System.out.println("SocketTimeoutException found");
					System.out.println("Timeout! Retransmitting...");
					resend = true;
				}
				else {
					System.out.println("The final ACK was dropped or mangled, but the server socket closed because it received the last piece of data.");
					System.out.println("The client tried to re-send the data to receive the final ACK that was lost, but it doesn't need it.");
					break;
				}
			}
		}
	}
	
	public static String SHA1(String inputText) {
		byte arr[] = null;

		try{
			MessageDigest m = MessageDigest.getInstance("SHA-1");  
			m.update(inputText.getBytes("UTF8"));  
			arr = m.digest();
		} catch (Exception e){
		}
		
		StringBuffer sb = new StringBuffer();  
		for (int i = 0; i < arr.length; ++i) {  
			sb.append(Integer.toHexString((arr[i] & 0xFF) | 0x100).substring(1,3));  
		}  
		return sb.toString();  
	}
	
	public static String replace(String packet, int delay, int drop, int mangle){
		try{
			Thread thread = Thread.currentThread();
  			thread.sleep(delay * 1000);
		} catch (Exception e) {

		}
	
		String result = "";

		Random rand = new Random();
        int prob = rand.nextInt(100);

		if (prob < drop + mangle){
			if (prob < drop) {
				return result;
			} else {
				for (int i = 0; i < packet.length(); i++){
					int charInt = rand.nextInt(94) + 32;
					char[] character = Character.toChars(charInt);
					String temp = Character.toString(character[0]);
					result = result + temp;
				}
				return result;
			}
		} else {
			return packet;
		}
	}
}
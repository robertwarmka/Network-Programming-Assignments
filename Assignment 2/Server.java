import java.io.*;
import java.net.*;
import java.util.*;
import java.security.MessageDigest;

class Server {
	public static void main(String[] args) throws Exception {
		int port = 5001;
		ServerSocket welcomeSock = null;
		byte[] data = new byte[512];
		char[] c = new char[512];
		String is_last = "";
		String expected_seqno = "0";
		File file = new File("server_save_file.txt");
		if(file.exists()) {
			file.delete();
		}
		file.createNewFile();
		try {
			welcomeSock = new ServerSocket(port);
		} catch (Exception e) {
			System.out.println("Error: cannot open socket");
			System.exit(1); // Handle exceptions.
		}

		System.out.println("Server is listening on port 5001...");

        Socket sSock = welcomeSock.accept();
		try {
			DataInputStream inFromClient = new DataInputStream(sSock.getInputStream());
            PrintWriter sendOut = new PrintWriter(sSock.getOutputStream(), true);
			PrintWriter fwriter = new PrintWriter(file);
            while(!is_last.equals("z")) {
                for(int i = 0; i < 512; i++) {
					data[i] = inFromClient.readByte();
					c[i] = (char)data[i];
				}
				String packet = new String(c);
				String actualPacket = replace(packet, 1, 10, 25);
				if(actualPacket.isEmpty()) {
					System.out.println("Packet dropped, waiting for client timeout.");
					continue;
				}
				String checksum = actualPacket.substring(0, 40);
				String seqno = actualPacket.substring(40, 41);
				String data_size = actualPacket.substring(41, 44);
				String temp_is_last = actualPacket.substring(44, 45);
				String packet_data = actualPacket.substring(45);
				String actual_data = "";
				String packet_contents = actualPacket.substring(40);
				String checksum_test = SHA1(packet_contents);
				if(!checksum.equals(checksum_test)) {
					System.out.println("Packet mangled, waiting for client timeout to resend.");
					continue;
				}
				if(!seqno.equals(expected_seqno)) {
					System.out.println("Wrong packet sequence number. Possible duplicate packet transmission due to timeout on client side.");
					System.out.println("Re-transmitting ACK" + seqno);
					sendOut.println("ACK" + seqno);
					continue;
				}
				int d_s = Integer.parseInt(data_size);
				if(d_s < 467) {
					actual_data = packet_data.substring(0, d_s);
				}
				else {
					actual_data = packet_data;
				}
				is_last = temp_is_last;
                //Random rand = new Random();
                //int pm = rand.nextInt(100);
                /*if(pm<=0){
                    System.out.println("ACK is lost on the way.");
                } else {
                    System.out.println("ACK" + seqno + " is transmitted.");
                    sendOut.println("ACK" + seqno);
                }*/
				//Write packet data to a file
				fwriter.print(actual_data);
				if(expected_seqno.equals("0")) {
					expected_seqno = "1";
				}
				else {
					expected_seqno = "0";
				}
            }
			System.out.println("Exiting gracefully.");
			fwriter.close();
			sendOut.close();
			inFromClient.close();
        } catch(Exception e){
			e.printStackTrace();
            // Do nothing here.
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

		if (packet.length() < 512) return result;

		Random rand = new Random();
        int prob = rand.nextInt(100);

		if (prob < drop + mangle){
			if (prob < drop) {
				return result;
			} else {
				for (int i = 0; i < 512; i++){
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
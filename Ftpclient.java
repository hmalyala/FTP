import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Ftpclient {
	Socket requestSocket;           //socket connect to the server
	static ObjectOutputStream os;         //stream write to the socket
	static ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
	static String server_name = null;
	static int port_no = 0;
	static Scanner scan = new Scanner (System.in);
	
	public void Ftpclient() {}

	void run()
	{
		try{													
			
			//create a socket to connect to the server			
			read_server_info();
			requestSocket = new Socket(server_name,port_no);

			System.out.println("Connected to localhost in port -> 8001");
			
			// Verify the login credentials
			login();
			
			boolean flag = true;

			// Call appropriat5e method based on the imput from user
			while(flag){
				String cmd = input_command();

				switch(cmd){	
				// Disconnect with Server			
				case "Exit" :
					flag = false;
					System.out.println("Disconnected with the Server");
					break;

				/*
				* Calls method upload_file in case of any of the 
				* dir, get <filename> or upload <filename> commands
				*/
				default:
					if((cmd.contains("upload")) || (cmd.contains("dir") || (cmd.contains("get")))){
						upload_file(requestSocket,cmd);						
					}				
					else {
						System.out.println("Invalid option");
					}
				}											

			}
		}

		catch (ConnectException e) {
			System.err.println("Connection refused. You need to initiate a server first.");
			run();
		} 
		catch(SocketException e){
			System.err.println("Server is unreachable");
			run();
		}
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
			run();
		}

		catch(IOException ioException){
			System.err.println("Error - please try again");
			run();
		}

		finally{
			//Close all the streams and connections
			try{
				if(requestSocket!=null) {
					requestSocket.close();
				}
				if(in != null){
					in.close();
				}
				if(os != null){
					os.close();
				}				
			}
			catch(Exception ioException){
				System.out.println("Encountered the exception - "+ioException);
			}
		}
	}

	//main method
	public static void main(String args[])
	{
		Ftpclient client = new Ftpclient();
		client.run();
	}

	// Method that verifies the login credentials of the user
	public static void login(){
		System.out.println("Please enter username and password");		

		String username = scan.nextLine();
		String pwd = scan.nextLine();

		if(!(username.equals("admin") && pwd.equals("1234"))){
			System.out.println("Invalid credentials, please try again");
			login();
		}					
	}	

	// Method to take imput from the user
	public static String input_command(){

		//Scanner scan = new Scanner (System.in);

		System.out.println("Please select a operation");
		System.out.println("\t 1. dir ");
		System.out.println("\t 2. get <filename>"); 
		System.out.println("\t 3. upload <filename>");
		System.out.println("\t 4. Exit");

		String input = scan.nextLine();
		//scan.close();
		return(input);
	}	
	
	/* 
	 * This method performs the dir, get and upload operations
	 * based on the command given by the user
	 */
	public static void upload_file(Socket sock, String cmd){


//		String filename = "C:\\Users\\mhema\\Desktop\\Client\\";
		String filename = "";	
		try {
			if(os == null) {
				os = new ObjectOutputStream(sock.getOutputStream());
				in = new ObjectInputStream(sock.getInputStream());
			}

			switch(cmd) {
			/*
			 *  Requests the list of files from  Server
			 */
				
			case "dir":
				os.writeObject(cmd);
				System.out.println("Files in the Server");
				try {					
					String str = null;
					
					do {
						str = (String) in.readObject();
						if((str.equals("EOF"))) break;
						System.out.println("\t"+str);
				      } while(true);
				}
				catch(Exception e) {
					
				}
				break;

			default:
			/*
			 * If user wants to upload the file to Server
			 */
			if(cmd.contains("upload")) {
		//		os.writeObject(cmd);
				String temp = cmd;				
				cmd = cmd.substring(cmd.lastIndexOf(" ")+1);
				filename += cmd;
				File myFile = new File( filename );
				if(myFile.exists()){				
				try{
					
                	byte[] mybytearray = new byte[(int) myFile.length()];					
					FileInputStream fis = new FileInputStream(myFile);
					BufferedOutputStream outToClient = new BufferedOutputStream(sock.getOutputStream());
					BufferedInputStream bis = new BufferedInputStream(fis);
					os.writeObject(temp);					
					bis.read(mybytearray, 0, mybytearray.length);
                    outToClient.write(mybytearray, 0, mybytearray.length);
                    outToClient.flush();
					// outToClient.close();
					
	/* 				String data = new String(Files.readAllBytes(Paths.get(filename)));
					os.writeObject(data);
					TimeUnit.SECONDS.sleep(1);
					os.flush(); */
					fis.close();
					System.out.println("Done.");
					break;
				} catch (IOException e) {
					System.out.println("File doesn't exist");
	//				e.printStackTrace();
				}
			}
			else{
				System.out.println("The file is not present at the client...!!!");
			}
			} 
			/*
			 * If user wants to get file to Server
			 */
			else{
				System.out.println("Reading the file from Server");
				List<String> l = new ArrayList<String>();
				os.writeObject("dir");
				try {					
					String str = null;
					
					do {
						str = (String) in.readObject();
						if((str.equals("EOF"))) break;
						l.add(str);
				      } while(true);
				}
				catch(Exception e) {
					System.out.println("Data Corrupted Exception");
				}

				String temp = cmd;
				cmd = cmd.substring(cmd.lastIndexOf(" ")+1);
				if(l.contains(cmd)){
					filename += cmd;
					os.writeObject(temp);

				
					try {	
															
						InputStream is = sock.getInputStream();												
						FileOutputStream fos = new FileOutputStream( filename );
						BufferedOutputStream bos = new BufferedOutputStream(fos);	
						ByteArrayOutputStream byteaos = new ByteArrayOutputStream();						
						int len;
						byte[] br = new byte[1];
						len = is.read(br, 0, br.length);
						do {
								byteaos.write(br);
								len = is.read(br);
						} while (is.available()>0);

						bos.write(byteaos.toByteArray());
						bos.flush();
						bos.close();
						fos.close();

						System.out.println("File Received of bytes "+(new File(filename)).length());
					}	catch (ClassCastException  e) {
							System.out.println("File not present in the Server");
					}

							
				}
				else{
					System.out.println("File does not exist in the Server");
				}
				break;
		}
	}
}
		catch(java.lang.ClassCastException ex)
		{
			System.out.println("File not present at Server");
			Thread.currentThread().interrupt();
		}
		catch (FileNotFoundException e) {
			System.out.println("File not found!!!");
//			e.printStackTrace();
		} catch (RuntimeException e){
			System.out.println("Run time exception");
//			e.printStackTrace();
		}


		catch(Exception e){
			System.out.println("Encountered the exception "+e);
//			e.printStackTrace();
		}			


	}
	
	
	public static void read_server_info() {
		
		try {
			System.out.println("Please Enter Server name and Port number - ftpclient <IP port>");
			String msg = scan.nextLine();
			
			String[] details = msg.split("\\s+");
			
			if(details.length!=3){
				System.out.println("Invalid Entry");
				details = null;
				read_server_info();
				return;
			}
			
			String dummy = details[0];
			if(!dummy.equals("ftpclient")){
				System.out.println("Please enter in the format - ftpclient <IP port>");
				read_server_info();
				return;
			}
			server_name = details[1];
			port_no = Integer.parseInt(details[2]);
			if(port_no > 65535) throw new StringIndexOutOfBoundsException();
		}
		catch(StringIndexOutOfBoundsException e) {
			System.out.println("Invalid Server/Port number - Please try again");
			read_server_info();		
		}
		catch(NumberFormatException e){
			System.out.println("Invalid Port number");
			read_server_info();
		}
		
	}
}









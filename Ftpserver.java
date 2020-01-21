import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Ftpserver {

	private static final int sPort = 8001;   //The server will be listening on this port number

	public static void main(String[] args) throws Exception {
		
		ServerSocket listener	= null;	
		try{
			listener = new ServerSocket(sPort);
		}
		catch(java.net.BindException e){
			System.out.println("This Server is already running !!!");
			return;
		}
		System.out.println("The server is running.");
		int clientNum = 1;
		try {
			while(true) {
				new Handler(listener.accept(),clientNum).start();
				System.out.println("Client "  + clientNum + " is connected!");
				clientNum++;

			}
		} finally {
			listener.close();
		} 

	}

	/**
	 * A handler thread class.  Handlers are spawned from the listening
	 * loop and are responsible for dealing with a single client's requests.
	 */
	private static class Handler extends Thread {
		private String message;    //message received from the client
//		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
		private ObjectInputStream in;	//stream read from the socket
		private ObjectOutputStream out;    //stream write to the socket
		private int no;		//The index number of the client
		private InputStream is = null;
		public Handler(Socket connection, int no) {
			this.connection = connection;
			this.no = no;
		}

		public void run() {
			try{
				//initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				try {
					in = new ObjectInputStream(connection.getInputStream());
					is = connection.getInputStream();
				}

				catch(Exception e) {
					System.out.println("Disconnected with client "+ no);
					Thread.currentThread().interrupt();
				}

				try{
					while(true)
					{
						if(connection.isClosed()){
							System.out.println("Connection closed");
						}
						//receive the message sent from the client
						String filename = "C:\\Users\\mhema\\Desktop\\server\\";
						try {
							message = (String)in.readObject();
							System.out.println("Received message "+message);
						}
						catch(EOFException  ex) {
							continue;
						}
						catch(Exception e){
						}
						
						/*
						 * 	Displays all the files in the server to the client
						 */
						if(message.equals("dir")) {
							list_of_file();
						}
						
						/*
						 *  Receives the file being uploaded from the server
						 */
						
						else if(message.contains("upload")) {
							System.out.println("Receiving the file from Client"+no);

							try {	
								message = message.substring(message.lastIndexOf(" ")+1);
								filename += message;
																						
								FileOutputStream fos = new FileOutputStream( filename );
								BufferedOutputStream bos = new BufferedOutputStream(fos);
								ByteArrayOutputStream byteaos = new ByteArrayOutputStream();
								byte[] br = new byte[1];	

								int bytesRead;
								bytesRead = is.read(br, 0, br.length);
								do {
										byteaos.write(br);
										bytesRead = is.read(br);
								} while (is.available()>0);

								bos.write(byteaos.toByteArray());
								bos.flush();
								bos.close();
								fos.close();
								System.out.println("File Received");
							}	
							catch(java.nio.file.NoSuchFileException e){
								System.out.println("File does not exist in Client-");
							}
							catch (IOException e) {
								System.out.println("Exception Occurred:");
								//e.printStackTrace();								
							}
							catch(ClassCastException e){
								System.out.println("File does not exist in Client_:");
							}
							

						}
						
						/*
						 *  Send the file requested by the Client 
						 */
						
						else {
							if(message.contains("get")){
								String temp = message;				
								message = message.substring(message.lastIndexOf(" ")+1);
								filename += message;
								System.out.println("Sending file "+filename+" to client");
								BufferedOutputStream outToClient = new BufferedOutputStream(connection.getOutputStream());
								try{
									File myFile = new File( filename );
									byte[] mybytearray = new byte[(int) myFile.length()];					
									FileInputStream fis = new FileInputStream(myFile);									
									BufferedInputStream bis = new BufferedInputStream(fis);
									System.out.println("Initiating file transfer");			
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
								}								
								catch(java.nio.file.NoSuchFileException e){
									System.out.println("File does not exist in server");									
								}
								catch(Exception e){
									System.out.println("File does not exist in the server");
									outToClient.write(new byte[0]);
								}
						}
						}						

					}
				}

				/* catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				} */				

				// Terminates the current thread gracefully
				catch(Exception e){					
				}
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client bcoz of exception 1 " + no);
				ioException.printStackTrace();
			}
			/*
			 * finally{ //Close connections try{ in.close(); out.close();
			 * connection.close(); } catch(IOException ioException){
			 * System.out.println("Disconnect with Client bcoz of exception 2 " + no);
			 * ioException.printStackTrace(); } }
			 */
		}

		//send a message to the output stream
		public void sendMessage(String msg)
		{
			try{
				out.writeObject(msg);
				out.flush();
				System.out.println("Send message: " + msg + " to Client " + no);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}

		public void list_of_file(){

			//get Input from standard inputC:\\Users\\mhema\\Desktop\\Desktop\\CN\\Multithreaded FTP Server and Client\\Multithreaded FTP Server and Client
			File folder = new File("C:\\Users\\mhema\\Desktop\\server");

			File[] files_list = folder.listFiles();
			try {
				for (int i = 0; i < files_list.length; i++) {
					if (files_list[i].isFile()) {						
						String str = files_list[i].getName();
						out.writeObject(str);
					} else if (files_list[i].isDirectory()) {
						out.writeObject(files_list[i].getName());
					}
				}
				out.writeObject("EOF");
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}

	}

}

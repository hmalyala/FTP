CNT 5106 Computer Networks

Project Title: Implementation of FTP client and server

Hemanth Kumar Malyala

Description:
FTP is used to transfer files between computers over the network.
Through this project you will be able to transfer/receive files between server and client connected through internet.
This project also supports multi-client connections i.e. two or more clients can simultaneously communicate
with the server independent of what the other client is involved in.

You can perform the following operations:
1) Establishing connection with the server.
2) Retrieve the list of files present in the server.
3) Upload a file from Client to the Server.
4) Retrieve a file that is present in the Server.

Further, this project allows you to transfer of any format i.e. .jpg, .pdf, .ex and .rar etc.

Steps to run the program:

As soon as you run the Ftpclient.java

1) To establish connection with the server, enter command in the following format:
	ftpclient servername/IP address portno.
	Example: ftpclient localhost 8001

2) In order to get the list of file spresent in the server use command "dir"
	Example: dir

3) TO upload a file to the server:
	upload <filename>
	Example: upload Report.pdf
	
	Note: If the file you are trying to upload is not present in the client, an error message is displayed 
		   "File not present in the Client". Also, if you are trying to upload file that is already present 
		   in the server, it will be overwritten.

4) To get a file from the server:
	get <filename>
	Example: get Report.pdf
	
	Note: If the file you are trying to request is not present in the server, an error message is displayed 
		   "File not present in the Server". Also, if you are trying to get a file that is already present 
		   in the server, it will be overwritten.

5) Command to disconnect with the Server:
	Exit
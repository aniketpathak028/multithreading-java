import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
    private static final String FILE_PATH = "./file.txt";
    
    private String readFileContent() throws IOException {
        return new String(Files.readAllBytes(Paths.get(FILE_PATH)));
    }
    
    public void run() throws IOException {
        int port = 8000;
        // Bind to all interfaces for WSL
        ServerSocket socket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
        socket.setSoTimeout(60000 * 10); // server timeout after 10 minutes
        System.out.println("Server is listening on port " + port);
        
        while (true) {
            try {
                Socket acceptedConnection = socket.accept();
                System.out.println("Connection accepted from client " + acceptedConnection.getRemoteSocketAddress());
                
                PrintWriter toClient = new PrintWriter(acceptedConnection.getOutputStream(), true);
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(acceptedConnection.getInputStream()));
                
                // Read file content for each client connection
                // This is a blocking call, so it will wait for the file to be read
                // before sending it to the client
                String fileContent = readFileContent();

                // Send file content with newline
                toClient.print(fileContent + "\n");
                toClient.flush();
                
                String line = fromClient.readLine();
                System.out.println("Response from client is: " + line);
            
                toClient.close();
                fromClient.close();
                acceptedConnection.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String []args){
        Server server= new Server();
        try{
            server.run();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
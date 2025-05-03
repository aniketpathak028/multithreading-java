import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server{
    private static final String FILE_PATH = "../file.txt";
    private final ExecutorService threadPool;

    public Server(int poolSize) {
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    private String readFileContent() throws IOException {
        return new String(Files.readAllBytes(Paths.get(FILE_PATH)));
    }

    public Runnable getRunnable(Socket acceptedConnection){
        return new Runnable() {
            @Override
            public void run(){
                try{
                    System.out.println("Connection accepted from client " + acceptedConnection.getRemoteSocketAddress());
                    
                    PrintWriter toClient= new PrintWriter(acceptedConnection.getOutputStream(), true);
                    BufferedReader fromClient= new BufferedReader(new InputStreamReader(acceptedConnection.getInputStream()));
                    
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
                } catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        };
    }

    public static void main(String args[]){
        int port= 8000;
        Server server= new Server(100);// Create a thread pool with 100 threads
        // The server will use this thread pool to handle incoming connections
        // and process requests concurrently
        // The server will not block waiting for a client to connect
        // and will continue to accept new connections while the previous connections are
        // being handled in separate threads
        try{
            ServerSocket socket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
            socket.setSoTimeout(60000 * 10); // server timeout after 10 minutes
            System.out.println("Server is listening on port " + port);
            while(true){
                Socket acceptedConnection = socket.accept();
                // Accept a new connection
                // and create a new thread to handle it
                // The thread will be created from the thread pool
                server.threadPool.execute(server.getRunnable(acceptedConnection));
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
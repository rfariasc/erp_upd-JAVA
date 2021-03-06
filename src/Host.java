import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by beto on 19-11-14.
 */
public class Host {

    private int port;
    private DatagramSocket socket;
    private InetAddress IPAddress;

    Host(DatagramSocket socket){
        this.socket = socket;
    }

    ////GET////

    public int getPort() {
        return port;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public InetAddress getIPAddress() {
        return IPAddress;
    }

    /////SET///////

    public void setPort(int port) {
        this.port = port;
    }

    public void setIPAddress(InetAddress IPAddress) {
        this.IPAddress = IPAddress;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }


}

import java.net.InetAddress;

public class Paquete {
    byte[] data;
    int datalengh;
    InetAddress IPAddress;
    int port;

    Paquete(byte[] data, int datalengh, InetAddress IPAddress, int port){
        this.data = data;
        this.datalengh = datalengh;
        this.IPAddress = IPAddress;
        this.port = port;
    }

    ///GET///

    public byte[] getData() {
        return data;
    }
    public InetAddress getIPAdress() {
        return IPAddress;
    }
    public int getDatalengh() {
        return datalengh;
    }
    public int getPort() {
        return port;
    }

    ///SET///

    public void setData(byte[] data) {
        this.data = data;
    }
    public void setDatalengh(int datalengh) {
        this.datalengh = datalengh;
    }
    public void setIPAdress(InetAddress IPAdress) {
        this.IPAddress = IPAdress;
    }
    public void setPort(int port) {
        this.port = port;
    }

}
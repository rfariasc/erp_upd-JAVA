import java.io.IOException;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

public class erp_udp {

    public static void main (String args[]) throws SocketException {

        ////PARAMETROS////
        String server_hostname = "127.0.0.1";
        int puerto_srv = 12345;
        int puerto_cli = 12346;
        //////////////////

        DatagramSocket server_Socket = new DatagramSocket(puerto_srv);
        DatagramSocket client_socket = new DatagramSocket();

        Comunicante srv = new Comunicante(server_Socket);
        Comunicante cli = new Comunicante(client_socket);

        //por que usar LinkedBlockingQueue??
        //http://stackoverflow.com/questions/2695426/are-linkedblockingqueues-insert-and-remove-methods-thread-safe

        LinkedBlockingQueue<DatagramPacket> toServer_packages = new LinkedBlockingQueue<DatagramPacket>();
        LinkedBlockingQueue<DatagramPacket> toClient_packages = new LinkedBlockingQueue<DatagramPacket>();

        LinkedBlockingQueue<Long> toServer_package_delay = new LinkedBlockingQueue<Long>();
        LinkedBlockingQueue<Long> toClient_package_delay = new LinkedBlockingQueue<Long>();


        srv.setPort(puerto_srv);
        cli.setPort(puerto_cli);

        try {
            InetAddress client_IPaddress = InetAddress.getByName(server_hostname);
            cli.setIPAddress(client_IPaddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ///////////////////////////////// Threads que intervienen en el flujo Cliente -> Servidor

        //Los que llegan al servidor en este programa y salen al servidor remoto
        receptor from_client_RX = new receptor(toServer_packages, toServer_package_delay, srv);
        Thread T1 = new Thread(from_client_RX);
        T1.start();

        //Los que salen del cliente en este programa y salen al servidor remoto  //envio al servidor
        transmisor from_client_TX = new transmisor(toServer_packages, toServer_package_delay, cli);
        Thread T2 =  new Thread(from_client_TX);
        T2.start();

        //////////////////////////////////////////////////////////////////////////////////////////


        ///////////////////////////////// Threads que intervienen en el flujo Servidor -> Cliente

        //Los que llegan al cliente en este programa y salen al cliente
        receptor from_server_RX = new receptor(toClient_packages, toClient_package_delay, cli);
        Thread T3 = new Thread(from_server_RX);
        T3.start();

        //Los que salen del servidor en este programa y salen al cliente  //envio al cliente
        transmisor from_server_TX = new transmisor(toClient_packages, toClient_package_delay, srv);
        Thread T4 = new Thread(from_server_TX);
        T4.start();

        //////////////////////////////////////////////////////////////////////////////////////////
    }
}

class receptor implements Runnable {

    int MTU = 1024;
    private LinkedBlockingQueue<DatagramPacket> received_packages;
    private LinkedBlockingQueue<Long> delay;
    private Comunicante comunicante;

    receptor(LinkedBlockingQueue<DatagramPacket> received_packages, LinkedBlockingQueue<Long> delay, Comunicante comunicante){
        this.received_packages = received_packages;
        this.delay = delay;
        this.comunicante = comunicante;
    }


    @Override
    public void run() {

        //al primer mensaje recibido se completa toda la info;
        byte[] comunicante_data = new byte[MTU];
        DatagramPacket recieved_data = new DatagramPacket(comunicante_data, comunicante_data.length);
        try {
            comunicante.getSocket().receive(recieved_data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        delay.add(new Long(System.nanoTime()));

        comunicante.setIPAddress(recieved_data.getAddress());
        comunicante.setPort(recieved_data.getPort());

        received_packages.add(recieved_data);

        while(true){

            byte[] recievedData = new byte[MTU];
            DatagramPacket recievedPacket = new DatagramPacket(recievedData, recievedData.length);
            try {
                comunicante.getSocket().receive(recievedPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            delay.add(new Long(System.nanoTime()));
            System.out.println("lo reibido fue: " + new String(recievedPacket.getData()) + "\nPor parte de: " + recievedPacket.getAddress() + ":" + recievedPacket.getPort() + "\n================");

            System.out.println("ahora se va a proceder a mandar...");
            received_packages.add(recievedPacket);
        }
    }
}


class transmisor implements Runnable {

    private Comunicante comunicante;
    private LinkedBlockingQueue<DatagramPacket> packages_toSend;
    private LinkedBlockingQueue<Long> delay;

    transmisor(LinkedBlockingQueue<DatagramPacket> packages_toSend, LinkedBlockingQueue<Long> delay, Comunicante comunicante){
        this.comunicante  = comunicante;
        this.packages_toSend = packages_toSend;
        this.delay = delay;
    }

    @Override
    public void run() {

        while(true){

            try {
                DatagramPacket retrieved_packet = packages_toSend.take();
                System.out.println("El contenido que se va a eviar es: " + new String(retrieved_packet.getData()));


                Thread.sleep(2000);

                long startTime = delay.take();
                long estimatedTime = System.nanoTime() - startTime;

                System.out.println("el tiempo que se esperó fue de: " + new Long(estimatedTime/1000).intValue() );

                DatagramPacket toSend_packet = new DatagramPacket(retrieved_packet.getData(), retrieved_packet.getLength(), comunicante.getIPAddress(), comunicante.getPort());
                comunicante.getSocket().send(toSend_packet);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
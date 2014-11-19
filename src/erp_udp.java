import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class erp_udp {

    public static void main (String args[]) throws SocketException {

//        ////PARAMETROS////
//        String server_hostname = "127.0.0.1";
//        int srv_port = 12345;
//        int cli_port = 12346;
//        Global.delay=0;
//        Global.delay_variation=0;
//        Global.expected_loss=0;
//        //////////////////

        String server_hostname;
        int cli_port;

        switch(args.length){
            case 5:
                server_hostname = "127.0.0.1";
                cli_port = Integer.parseInt(args[4]);
                break;
            case 6:
                server_hostname = args[4];
                cli_port = Integer.parseInt(args[5]);
                break;
            default:
                System.out.println("Modo de uso:\nerp_udp <retardo_promedio> <variación_retardo> <porcentaje_pérdida> <puerto_local> [host_remoto] <puerto_remoto>");
                return;
        }
        if(Integer.parseInt(args[2])> 100 || Integer.parseInt(args[2]) < 0 ){
            System.out.println("Porcentaje de pérdida debe estar entre [0,100]");
            return;
        }

        int srv_port = Integer.parseInt(args[3]);
        Global.delay=Integer.parseInt(args[0]);
        Global.delay_variation=Integer.parseInt(args[1]);
        Global.expected_loss=Integer.parseInt(args[2]);


        DatagramSocket server_Socket = new DatagramSocket(srv_port);
        DatagramSocket client_socket = new DatagramSocket();

        Host srv = new Host(server_Socket);
        Host cli = new Host(client_socket);

        //por que usar LinkedBlockingQueue??
        //http://stackoverflow.com/questions/2695426/are-linkedblockingqueues-insert-and-remove-methods-thread-safe

        LinkedBlockingQueue<DatagramPacket> toServer_packages = new LinkedBlockingQueue<DatagramPacket>();
        LinkedBlockingQueue<DatagramPacket> toClient_packages = new LinkedBlockingQueue<DatagramPacket>();

        LinkedBlockingQueue<Long> toServer_package_delay = new LinkedBlockingQueue<Long>();
        LinkedBlockingQueue<Long> toClient_package_delay = new LinkedBlockingQueue<Long>();

        ///////////////////////////////////////////////////////////

        srv.setPort(srv_port);
        cli.setPort(cli_port);

        try {
            InetAddress client_IPaddress = InetAddress.getByName(server_hostname);
            cli.setIPAddress(client_IPaddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ///////////////////////////////// Threads que intervienen en el flujo Cliente -> Servidor

        //Los que llegan al servidor en este programa y salen al servidor remoto
        Receiver from_client_RX = new Receiver(toServer_packages, toServer_package_delay, srv);
        Thread T1 = new Thread(from_client_RX);
        T1.start();

        //Los que salen del cliente en este programa y salen al servidor remoto  //envio al servidor
        Transmitter from_client_TX = new Transmitter(toServer_packages, toServer_package_delay, cli);
        Thread T2 =  new Thread(from_client_TX);
        T2.start();

        //////////////////////////////////////////////////////////////////////////////////////////


        ///////////////////////////////// Threads que intervienen en el flujo Servidor -> Cliente

        //Los que llegan al cliente en este programa y salen al cliente
        Receiver from_server_RX = new Receiver(toClient_packages, toClient_package_delay, cli);
        Thread T3 = new Thread(from_server_RX);
        T3.start();

        //Los que salen del servidor en este programa y salen al cliente  //envio al cliente
        Transmitter from_server_TX = new Transmitter(toClient_packages, toClient_package_delay, srv);
        Thread T4 = new Thread(from_server_TX);
        T4.start();

        //////////////////////////////////////////////////////////////////////////////////////////
    }
}

class Receiver implements Runnable {

    int MTU = Global.MTU;
    private LinkedBlockingQueue<DatagramPacket> received_packages;
    private LinkedBlockingQueue<Long> delay;
    private Host comunicante;

    Receiver(LinkedBlockingQueue<DatagramPacket> received_packages, LinkedBlockingQueue<Long> delay, Host comunicante){
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
            received_packages.add(recievedPacket);
        }
    }
}


class Transmitter implements Runnable {

    private Host comunicante;
    private LinkedBlockingQueue<DatagramPacket> packages_toSend;
    private LinkedBlockingQueue<Long> delay;

    Random rand = new Random();

    Transmitter(LinkedBlockingQueue<DatagramPacket> packages_toSend, LinkedBlockingQueue<Long> delay, Host comunicante){
        this.comunicante  = comunicante;
        this.packages_toSend = packages_toSend;
        this.delay = delay;
    }

    @Override
    public void run() {

        while(true){

            try {
                DatagramPacket retrieved_packet = packages_toSend.take();

//                System.out.println("El contenido que se va a enviar es: " + new String(retrieved_packet.getData()) +"\nA la dirección: " + comunicante.getIPAddress() + ":" + comunicante.getPort() );

                long startTime = delay.take();
                long estimatedTime = System.nanoTime() - startTime;
//                System.out.println("el tiempo que se esperó fue de: " + new Long(estimatedTime/1000000).intValue() );

                if(estimatedTime < Global.delay){

                    Thread.sleep(Global.delay + (int)rand.nextDouble()*Global.delay_variation - new Long(estimatedTime/1000000));
                }


                if ( (rand.nextDouble()*100) > Global.expected_loss){ //Si el número que sale es mayor que la probabilidad,se envía

                    DatagramPacket toSend_packet = new DatagramPacket(retrieved_packet.getData(), retrieved_packet.getLength(), comunicante.getIPAddress(), comunicante.getPort());
                    comunicante.getSocket().send(toSend_packet);
                }else{
                    System.out.println("el paquete se pierde =(");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
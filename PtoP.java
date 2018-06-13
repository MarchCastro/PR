import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;
import java.net.Inet6Address;
import java.io.*;
import java.net.*;
import java.util.*;
import static java.lang.System.out;


public class PtoP {
    String host_local = "";
    final getLista lista = new getLista();
    String buscador = "";
    int flag = 0;

    static String displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        String sub = "";
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            if(inetAddress.toString().charAt(2) == '9' || inetAddress.toString().charAt(2) == '0'){
                out.printf("Display name: %s\n", netint.getDisplayName());
                out.printf("Name: %s\n", netint.getName());
                //out.printf("%s ", inetAddress);
                sub = inetAddress.toString().substring(1,inetAddress.toString().length());
                System.out.println("Address: "+sub);
                return sub;
            }
        }       
        return "";
    }
    
    class getURLS{
        public void solicitaDatos(){
            //BufferedReader datos = null;
            try {   
                //OBTENGO HOST LOCAL
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface netint : Collections.list(nets)){
                    host_local = displayInterfaceInformation(netint);
                    break;
                }
                //System.out.println("Puerto local "+ host_local);
                //System.out.println("");
                System.out.println("ENTRE CLIENTE");
                System.out.println(""); 
                System.out.println("");    
                System.out.println("<---------------- Bienvenido! --------------->");
                System.out.println("Introduce a continuación una palabra para buscar en tus documentos...");
                System.out.print("Buscador: ");
                BufferedReader datos = new BufferedReader(new InputStreamReader(System.in));
                buscador = datos.readLine();                            
                //HILOS PARA C/S
                Hilo objHilo = new Hilo();
                Hilo1 objHilo1 = new Hilo1();
                //Hilo2 objHilo2 = new Hilo2();
                Hilo3 objHilo3 = new Hilo3();
                new Thread(objHilo).start();
                new Thread(objHilo1).start();
                //new Thread(objHilo2).start();
                new Thread(objHilo3).start();
            } catch (Exception e) {
               e.printStackTrace();
            }
        }    
    }

    //SERVIDOR
    class Hilo implements Runnable, InterfazRemota{
        
        public ArrayList <String> nombre(String response) {

            //Enlisto archivos del fichero
            String sDirectorio = "/home/marce/Documents/Redes/TercerParcial/Practica/Archivos";
            File f = new File(sDirectorio);
            
            if (f.exists()){ // Directorio existe 
                File[] ficheros = f.listFiles();
                if (ficheros == null){
                    System.out.println("No hay ficheros en el directorio especificado");
                }else { 
                    for (int x=0;x<ficheros.length;x++){
                      //System.out.println(ficheros[x].getName());
                        for (int j = 0; j < ficheros[x].getName().length(); j++) {
                            if(ficheros[x].getName().charAt(j) == '.'){
                                String [] sin_fin = ficheros[x].getName().split("\\.");
                                //System.out.println(sin_fin[0] + "  " + sin_fin[1]);
                                
                                for (int k = 0; k < sin_fin[0].length(); k++) {
                                    if(sin_fin[0].charAt(k) == '_'){
                                        String [] file_parts = sin_fin[0].split("_");
                                        //System.out.println(file_parts[0] + "  " + file_parts[1]);
                                        if(file_parts[0].toLowerCase().compareTo(response.toLowerCase()) == 0 || file_parts[1].toLowerCase().compareTo(response.toLowerCase()) == 0 ){
                                            lista.nombres_coincidencia.add(ficheros[x].getName());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                System.out.println("No existe ese directorio :c");
            }

            for (int x = 0; x < lista.nombres_coincidencia.size(); x++) {
                //System.out.println("Archivo existente: "+ lista.nombres_coincidencia.get(x));
                //return nombres_coincidencia.get(x);
            }

            //return name;
            return lista.nombres_coincidencia;
        }

        public String archivos(String archivo) {
            System.out.println("Pide archivo: "+archivo);
            flag = 1;
            //ENVIO ARCHIVO
            try{
                String dst = "127.0.0.1";
                int pto = 7000;
                //JFileChooser jf = new JFileChooser();
                //int r = jf.showOpenDialog(null);
                //System.out.println("R. "+ r );
                int porcentaje = 0;
                int n = 0;
                //if(n == JFileChooser.APPROVE_OPTION){
                    File f = new File("/home/march/Downloads/P6/Archivos/"+archivo);
                    String path = f.getAbsolutePath();
                    String nombre = f.getName();
                    long tam = f.length();
                    Socket cl = new Socket(dst,pto);
                    System.out.println("Conexion establecida ... iniciando envío del archivo" + nombre + "\n\n");
                    long enviados = 0;
                    byte [] b = new byte [1500];
                    //Uso Data porque voy a mandar diferentes tipos de datos, si es puro texto es Print y si es bytes es Buffer
                    DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                    DataInputStream dis = new DataInputStream(new FileInputStream(path));
                    dos.writeUTF(nombre);
                    dos.flush();
                    dos.writeLong(tam);
                    dos.flush();
    
                    while(enviados < tam){
                        n = dis.read(b);
                        dos.write(b,0,n);
                        enviados += n;
                        porcentaje = (int)((enviados * 100)/tam);
                        System.out.println("\r Se ha enviado el " + porcentaje + "% del archivo");
                    }
    
                    System.out.println("\n Se ha completado el envio");
                    dis.close();
                    dos.close();
                    cl.close();
               // }
            }catch(Exception e){
                e.printStackTrace();
            }

            return archivo;
        }

        @Override
        public void run(){
            for(;;){

                try {
                    java.rmi.registry.LocateRegistry.createRegistry(1099); //puerto default del rmiregistry
                    System.out.println("Puerto local "+ host_local);
                    System.setProperty("java.rmi.server.hostname", host_local);
                    System.out.println("RMI registry ready.");
                } catch (Exception e) {
                    //System.out.println("Exception starting RMI registry:");
                    //e.printStackTrace();
                }//catch
                
                try {
                System.setProperty("java.rmi.server.codebase","file:///home/marce/Documents/Redes/TercerParcial/Practica/");
                    Hilo obj = new Hilo();
                    InterfazRemota stub = (InterfazRemota) UnicastRemoteObject.exportObject(obj, 0);
            
                    // Bind the remote object's stub in the registry
                    Registry registry = LocateRegistry.getRegistry();
                    registry.bind("InterfazRemota", stub);
            
                    System.err.println("Servidor listo...");
                } catch (Exception e) {
                    //System.err.println("Server exception: " + e.toString());
                    //e.printStackTrace();
                }
            }
            
        }
    }

    class Hilo1 implements Runnable{
        @Override
        public void run(){
            String host;
            try {
                ArrayList<String> temp = new ArrayList<String>();                
                for (int i = 65; i < 69; i++) {
                    //host = "192.168.1."+i;
                    host = "10.100.68.1"+i;
                    //if(host.compareTo(host_local) == 0){
                      //  System.out.println("No busco en mis propios archivos >:C");
                    //}else{
                    try{
                        Registry registry = LocateRegistry.getRegistry(host);	
                        InterfazRemota stub = (InterfazRemota) registry.lookup("InterfazRemota");
                        ArrayList<String> response = stub.nombre(buscador);
                        System.out.println(host + ":  responde  : " + response);
                        
                        if(host.compareTo(host_local) != 0){
                            //for(int x = 0; x < lista.nombres_coincidencia.size(); x++){
                            for (int y = 0; y < response.size(); y++) {
                                if(lista.nombres_coincidencia.contains(response.get(y))){
                                    System.out.println("Ya existe: "+response.get(y));
                                }else{
                                    temp.add(response.get(y));
                                }
                            }
                        }

                        
                    }catch(Exception e){
                        System.out.println(host+" no está vivo x_x");
                    }
                }
            } catch (Exception e) {
                System.err.println("Client exception: " + e.toString());
                e.printStackTrace();
            }
        }    
    }

    class Hilo3 implements Runnable{
        @Override
        public void run(){
            if(flag == 1){
                try{
                    int pto = 7000;
                    ServerSocket s = new ServerSocket(pto);
                    System.out.println("Servidor iniciado, esperando archivos...");
                    for(;;){
                        Socket cl = s.accept();
                        DataInputStream dis = new DataInputStream(cl.getInputStream());
                        String nombre = dis.readUTF();
                        long tam = dis.readLong();
                        
                        System.out.println("Inicia recepción del archivo: " + nombre + " de tamaño " + tam + " desde " + cl.getInetAddress() + ":" + cl.getPort());
                        DataOutputStream dos = new DataOutputStream(new FileOutputStream(nombre));
                        long recibidos = 0;
                        int n,porcentaje = 0;
                        
                        while(recibidos < tam){
                            byte [] b = new byte[1500];
                            n = dis.read(b);
                            recibidos += n;
                            dos.write(b,0,n);
                            dos.flush();
                            porcentaje = (int)((recibidos * 100)/tam);
                            System.out.println("\r Recibido el " + porcentaje + "% del archivo");
                        }
        
                        System.out.println("Archivo recibido");
                        dos.close();
                        dis.close();
                        cl.close();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    class getLista{
        public volatile ArrayList<String> ip_activa = new ArrayList<String>();
        public volatile ArrayList<String> nombres_coincidencia = new ArrayList<String>();
    }

    private void test() {
        getURLS objURL = new getURLS();
        objURL.solicitaDatos();
    }
    
    public static void main(String[] args) throws IOException {
        try {
            PtoP test = new PtoP();
            test.test();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

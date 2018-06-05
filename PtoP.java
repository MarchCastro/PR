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
                new Thread(objHilo).start();
                new Thread(objHilo1).start();   
            } catch (Exception e) {
               e.printStackTrace();
            }
        }    
    }

    //SERVIDOR
    class Hilo implements Runnable, InterfazRemota{
        
        public ArrayList <String> nombre(String response) {

            //Enlisto archivos del fichero
            String sDirectorio = "/home/marce/Documents/Redes/TercerParcial/Practica6/Archivos";
            File f = new File(sDirectorio);
            
            ArrayList <String> nombres_coincidencia = new ArrayList <String>();
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
                                System.out.println(sin_fin[0] + "  " + sin_fin[1]);
                                
                                for (int k = 0; k < sin_fin[0].length(); k++) {
                                    if(sin_fin[0].charAt(k) == '_'){
                                        String [] file_parts = sin_fin[0].split("_");
                                        System.out.println(file_parts[0] + "  " + file_parts[1]);
                                        if(file_parts[0].toLowerCase().compareTo(response.toLowerCase()) == 0 || file_parts[1].toLowerCase().compareTo(response.toLowerCase()) == 0 ){
                                            nombres_coincidencia.add(ficheros[x].getName());
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

            for (int x = 0; x < nombres_coincidencia.size(); x++) {
                System.out.println("Archivo existente: "+nombres_coincidencia.get(x));
                //return nombres_coincidencia.get(x);
            }

            //return name;
            return nombres_coincidencia;
        }

        @Override
        public void run(){
            try {
                java.rmi.registry.LocateRegistry.createRegistry(1099); //puerto default del rmiregistry
                System.out.println("Puerto local "+ host_local);
                System.setProperty("java.rmi.server.hostname", host_local);
                System.out.println("RMI registry ready.");
             } catch (Exception e) {
                System.out.println("Exception starting RMI registry:");
                e.printStackTrace();
             }//catch
           
           try {
               System.setProperty("java.rmi.server.codebase","file:///home/marce/Documents/Redes/TercerParcial/Practica6/");
               Hilo obj = new Hilo();
               InterfazRemota stub = (InterfazRemota) UnicastRemoteObject.exportObject(obj, 0);
       
               // Bind the remote object's stub in the registry
               Registry registry = LocateRegistry.getRegistry();
               registry.bind("InterfazRemota", stub);
       
               System.err.println("Servidor listo...");
           } catch (Exception e) {
               System.err.println("Server exception: " + e.toString());
               e.printStackTrace();
           }
        }
    }

    class Hilo1 implements Runnable{
        @Override
        public void run(){
            String host;
            try {
                                
                for (int i = 65; i < 69; i++) {
                    host = "192.168.1."+i;
                    //host = "10.100.68.1"+i;
                    //if(host.compareTo(host_local) == 0){
                      //  System.out.println("No busco en mis propios archivos >:C");
                    //}else{
                        try{
                            Registry registry = LocateRegistry.getRegistry(host);	
                            InterfazRemota stub = (InterfazRemota) registry.lookup("InterfazRemota");
                            ArrayList<String> response = stub.nombre(buscador);
                            System.out.println(host + ":  responde  : " + response);
                        }catch(Exception e){
                            System.out.println("No esta vivo el servidor :C");
                        }
                    //}
                }

                /*for (int i = 65; i < 69; i++) {
                    host = "192.168.1."+i;
                    //host = "10.100.68.1"+i;
                    if(host.compareTo(host_local) == 0){
                        System.out.println("No busco en mis propios archivos >:C");
                    }else{
                        try {
                            //System.out.println(host);
                            Registry registry = LocateRegistry.getRegistry(host);	
                            //LocateRegistry.getRegistry(host, 1099);//
                            //tambien puedes usar getRegistry(String host, int port)
                            
                            InterfazRemota stub = (InterfazRemota) registry.lookup("InterfazRemota");
                            //int x=5,y=4;                        
                            String response = stub.nombre(buscador);
                            System.out.println(host + ":  responde  : " + response);
    
                            //Enlisto archivos del fichero
                            String sDirectorio = "/home/marce/Documents/Redes/TercerParcial/Practica6/Archivos";
                            File f = new File(sDirectorio);
                            
                            ArrayList <String> nombres_coincidencia = new ArrayList <String>();
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
                                                            nombres_coincidencia.add(ficheros[x].getName());
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
    
                            for (int x = 0; x < nombres_coincidencia.size(); x++) {
                                System.out.println("Archivo existente: "+nombres_coincidencia.get(x));
                            }
                            
                        } catch (Exception e) {
                            System.out.println(host+" no está vivo x_x");
                        }
                    }
                }*/
            } catch (Exception e) {
                System.err.println("Client exception: " + e.toString());
                e.printStackTrace();
            }
        }    
    }

    class getLista{
        public volatile ArrayList<String> ip_activa = new ArrayList<String>();
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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface InterfazRemota extends Remote {
    //int suma(int a,int b) throws RemoteException;
    ArrayList<String> nombre(String name) throws RemoteException;
}

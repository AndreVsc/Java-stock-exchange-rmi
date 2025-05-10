package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InvestidorRemote extends Remote {
    void notificarMudancaPreco(String simboloAcao, double precoAntigo, double novoPreco) throws RemoteException;
    void notificarAlteracaoBook(String simboloAcao) throws RemoteException;
    String getId() throws RemoteException;
}
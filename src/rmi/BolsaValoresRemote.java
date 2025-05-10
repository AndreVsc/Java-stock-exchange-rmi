package rmi;

import model.Acao;
import model.Ordem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import interfaces.InvestidorRemote;

public interface BolsaValoresRemote extends Remote {
    Map<String, Acao> listarAcoes() throws RemoteException;
    Acao obterAcao(String simbolo) throws RemoteException;
    void enviarOrdem(Ordem ordem) throws RemoteException;
    List<Ordem> listarOrdensCompra(String simboloAcao) throws RemoteException;
    List<Ordem> listarOrdensVenda(String simboloAcao) throws RemoteException;
    void registrarListener(InvestidorRemote investidor) throws RemoteException;
    void cancelarRegistroListener(InvestidorRemote investidor) throws RemoteException;
}
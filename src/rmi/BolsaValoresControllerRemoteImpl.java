package rmi;

import controller.BolsaValoresController;
import interfaces.InvestidorRemote;
import model.Acao;
import model.Ordem;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

public class BolsaValoresControllerRemoteImpl extends UnicastRemoteObject implements BolsaValoresControllerRemote {
    private static final long serialVersionUID = 1L;
    private final BolsaValoresController controller;

    public BolsaValoresControllerRemoteImpl(BolsaValoresController controller) throws RemoteException {
        super();
        this.controller = controller;
    }

    @Override
    public Map<String, Acao> listarAcoes() throws RemoteException {
        return controller.getAcoes();
    }

    @Override
    public Acao obterAcao(String simbolo) throws RemoteException {
        return controller.getAcao(simbolo);
    }

    @Override
    public void enviarOrdem(Ordem ordem) throws RemoteException {
        controller.enviarOrdem(ordem);
    }

    @Override
    public List<Ordem> listarOrdensCompra(String simboloAcao) throws RemoteException {
        return controller.listarOrdensCompra(simboloAcao);
    }

    @Override
    public List<Ordem> listarOrdensVenda(String simboloAcao) throws RemoteException {
        return controller.listarOrdensVenda(simboloAcao);
    }

    @Override
    public void registrarListener(InvestidorRemote investidor) throws RemoteException {
        // Você pode adaptar para listeners se necessário
    }

    @Override
    public void cancelarRegistroListener(InvestidorRemote investidor) throws RemoteException {
        // Você pode adaptar para listeners se necessário
    }
}

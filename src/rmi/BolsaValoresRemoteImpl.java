package rmi;

import model.Acao;
import model.Ordem;
import service.BolsaValoresService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import interfaces.BookDeOfertasListener;
import interfaces.InvestidorRemote;
import interfaces.PrecoAcaoListener;

public class BolsaValoresRemoteImpl extends UnicastRemoteObject implements BolsaValoresRemote {
    private static final long serialVersionUID = 1L;
    
    private final BolsaValoresService service;
    private final Map<String, InvestidorRemote> investidores = new ConcurrentHashMap<>();
    
    public BolsaValoresRemoteImpl(BolsaValoresService service) throws RemoteException {
        super();
        this.service = service;
        this.service.adicionarListener(new AtualizadorInvestidores());
        this.service.getBookDeOfertas().adicionarListener(new BookListener());
    }
    
    @Override
    public Map<String, Acao> listarAcoes() throws RemoteException {
        return service.getAcoes();
    }
    
    @Override
    public Acao obterAcao(String simbolo) throws RemoteException {
        return service.getAcao(simbolo);
    }
    
    @Override
    public void enviarOrdem(Ordem ordem) throws RemoteException {
        service.getBookDeOfertas().adicionarOrdem(ordem);
    }
    
    @Override
    public List<Ordem> listarOrdensCompra(String simboloAcao) throws RemoteException {
        return service.getBookDeOfertas().getOrdensCompra(simboloAcao);
    }
    
    @Override
    public List<Ordem> listarOrdensVenda(String simboloAcao) throws RemoteException {
        return service.getBookDeOfertas().getOrdensVenda(simboloAcao);
    }
    
    @Override
    public void registrarListener(InvestidorRemote investidor) throws RemoteException {
        investidores.put(investidor.getId(), investidor);
    }
    
    @Override
    public void cancelarRegistroListener(InvestidorRemote investidor) throws RemoteException {
        investidores.remove(investidor.getId());
    }
    
    private class AtualizadorInvestidores implements PrecoAcaoListener {
        @Override
        public void atualizacaoPreco(String simboloAcao, double precoAntigo, double novoPreco) {
            List<String> investidoresParaRemover = new ArrayList<>();
            
            for (Map.Entry<String, InvestidorRemote> entry : investidores.entrySet()) {
                try {
                    entry.getValue().notificarMudancaPreco(simboloAcao, precoAntigo, novoPreco);
                } catch (RemoteException e) {
                    investidoresParaRemover.add(entry.getKey());
                }
            }
            
            for (String id : investidoresParaRemover) {
                investidores.remove(id);
            }
        }
    }
    
    private class BookListener implements BookDeOfertasListener {
        @Override
        public void bookAlterado(String simboloAcao) {
            List<String> investidoresParaRemover = new ArrayList<>();
            
            for (Map.Entry<String, InvestidorRemote> entry : investidores.entrySet()) {
                try {
                    entry.getValue().notificarAlteracaoBook(simboloAcao);
                } catch (RemoteException e) {
                    investidoresParaRemover.add(entry.getKey());
                }
            }
            
            for (String id : investidoresParaRemover) {
                investidores.remove(id);
            }
        }
    }
}
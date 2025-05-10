package app;

import model.Acao;
import model.Ordem;
import rmi.BolsaValoresRemote;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import interfaces.InvestidorRemote;

public class InvestidorApp {
    
    private static class InvestidorImpl extends UnicastRemoteObject implements InvestidorRemote {
        private static final long serialVersionUID = 1L;
        
        private final String id;
        private final Map<String, Acao> acoesSeguidas;
        private final BolsaValoresRemote bolsa;
        private final Random random = new Random();
        
        public InvestidorImpl(String id, Map<String, Acao> acoes, BolsaValoresRemote bolsa) throws RemoteException {
            super();
            this.id = id;
            this.acoesSeguidas = acoes;
            this.bolsa = bolsa;
            
            Thread threadReacao = new Thread(new ReatorDePrecos());
            threadReacao.setDaemon(true);
            threadReacao.start();
        }
        
        @Override
        public void notificarMudancaPreco(String simboloAcao, double precoAntigo, double novoPreco) throws RemoteException {
            if (acoesSeguidas.containsKey(simboloAcao)) {
                System.out.printf("[ATUALIZAÇÃO] %s: R$%.2f -> R$%.2f (Variação: %.2f%%)\n", 
                    simboloAcao, precoAntigo, novoPreco, ((novoPreco / precoAntigo) - 1) * 100);
            }
        }
        
        @Override
        public void notificarAlteracaoBook(String simboloAcao) throws RemoteException {
            if (acoesSeguidas.containsKey(simboloAcao)) {
                System.out.println("[BOOK] Alteração no book de ofertas para " + simboloAcao);
                
                List<Ordem> compras = bolsa.listarOrdensCompra(simboloAcao);
                List<Ordem> vendas = bolsa.listarOrdensVenda(simboloAcao);
                
                System.out.println("  COMPRAS:");
                for (int i = 0; i < Math.min(3, compras.size()); i++) {
                    System.out.println("    " + compras.get(i));
                }
                
                System.out.println("  VENDAS:");
                for (int i = 0; i < Math.min(3, vendas.size()); i++) {
                    System.out.println("    " + vendas.get(i));
                }
                System.out.println();
            }
        }
        
        @Override
        public String getId() throws RemoteException {
            return id;
        }
        
        private class ReatorDePrecos implements Runnable {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000 + random.nextInt(15000));
                        
                        String[] simbolos = acoesSeguidas.keySet().toArray(new String[0]);
                        if (simbolos.length > 0) {
                            String simboloEscolhido = simbolos[random.nextInt(simbolos.length)];
                            Acao acao = bolsa.obterAcao(simboloEscolhido);
                            
                            Ordem.TipoOrdem tipoOrdem = random.nextBoolean() ? Ordem.TipoOrdem.COMPRA : Ordem.TipoOrdem.VENDA;
                            
                            double precoBase = acao.getPreco();
                            double variacao = precoBase * (random.nextDouble() * 0.06 - 0.03);
                            double preco = Math.max(0.01, precoBase + variacao);
                            
                            int quantidade = (random.nextInt(10) + 1) * 100;
                            
                            Ordem ordem = new Ordem(id, simboloEscolhido, tipoOrdem, preco, quantidade);
                            bolsa.enviarOrdem(ordem);
                            
                            System.out.println("[ORDEM ENVIADA] " + ordem);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (RemoteException e) {
                        System.err.println("Erro ao interagir com a bolsa: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Digite o endereço do servidor da bolsa (ou deixe em branco para localhost): ");
            String serverAddress = scanner.nextLine().trim();
            if (serverAddress.isEmpty()) {
                serverAddress = "localhost";
            }
            
            System.out.println("Conectando ao servidor em " + serverAddress + "...");
            
            Registry registry = LocateRegistry.getRegistry(serverAddress, 1099);
            BolsaValoresRemote bolsa = (BolsaValoresRemote) registry.lookup("BolsaValores");
            
            String investidorId = "INV-" + UUID.randomUUID().toString().substring(0, 8);
            System.out.println("Investidor inicializado com ID: " + investidorId);
            
            Map<String, Acao> acoes = bolsa.listarAcoes();
            System.out.println("\nAções disponíveis para negociação:");
            for (Acao acao : acoes.values()) {
                System.out.println("  " + acao);
            }
            
            InvestidorImpl investidor = new InvestidorImpl(investidorId, acoes, bolsa);
            bolsa.registrarListener(investidor);
            
            System.out.println("\nInvestidor conectado à bolsa. Pressione ENTER para sair.");
            scanner.nextLine();
            
            try {
                bolsa.cancelarRegistroListener(investidor);
                UnicastRemoteObject.unexportObject(investidor, true);
            } catch (NoSuchObjectException e) {
            }
            
            System.out.println("Investidor desconectado. Encerrando...");
            
        } catch (Exception e) {
            System.err.println("Erro na aplicação do investidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
/*
 * Bolsa de Valores RMI - Java
 *
 * Este projeto simula uma bolsa de valores distribuída usando Java RMI.
 * Investidores se conectam ao servidor, acompanham ações, recebem notificações de preço e book,
 * e enviam ordens de compra/venda. Toda a comunicação entre app e serviço é feita via controller remoto.
 *
 * Principais responsabilidades:
 * - Atualização automática de preços de ações (multi-threaded)
 * - Book de ofertas concorrente (compra/venda)
 * - Notificações reativas para investidores
 * - Consistência de dados com concorrência
 * - Separação clara entre app, controller e service
 */
package app;

import model.Acao;
import model.Ordem;
import rmi.BolsaValoresControllerRemote;

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

/**
 * Classe principal do cliente Investidor.
 * Conecta-se ao controller remoto, recebe notificações e envia ordens automaticamente.
 */
public class InvestidorApp {
    /**
     * Implementação remota do investidor, que recebe notificações do servidor.
     */
    private static class InvestidorImpl extends UnicastRemoteObject implements InvestidorRemote {
        private static final long serialVersionUID = 1L;
        // Identificador único do investidor
        private final String id;
        // Ações que o investidor acompanha
        private final Map<String, Acao> acoesSeguidas;
        // Referência remota ao controller
        private final BolsaValoresControllerRemote bolsa;
        private final Random random = new Random();

        /**
         * Cria um investidor remoto e inicia a thread de reação automática.
         */
        public InvestidorImpl(String id, Map<String, Acao> acoes, BolsaValoresControllerRemote bolsa) throws RemoteException {
            super();
            this.id = id;
            this.acoesSeguidas = acoes;
            this.bolsa = bolsa;
            // Thread que envia ordens aleatórias periodicamente
            Thread threadReacao = new Thread(new ReatorDePrecos());
            threadReacao.setDaemon(true);
            threadReacao.start();
        }

        /**
         * Notificação de mudança de preço de uma ação acompanhada.
         */
        @Override
        public void notificarMudancaPreco(String simboloAcao, double precoAntigo, double novoPreco) throws RemoteException {
            if (acoesSeguidas.containsKey(simboloAcao)) {
                System.out.printf("[ATUALIZAÇÃO] %s: R$%.2f -> R$%.2f (Variação: %.2f%%)\n", 
                    simboloAcao, precoAntigo, novoPreco, ((novoPreco / precoAntigo) - 1) * 100);
            }
        }

        /**
         * Notificação de alteração no book de ofertas de uma ação acompanhada.
         */
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

        /**
         * Retorna o identificador remoto do investidor.
         */
        @Override
        public String getId() throws RemoteException {
            return id;
        }

        /**
         * Thread interna que envia ordens de compra/venda aleatórias periodicamente.
         */
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

    /**
     * Ponto de entrada do app Investidor.
     * Solicita endereço do servidor, conecta ao controller remoto, registra o investidor e inicia as notificações.
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Digite o endereço do servidor da bolsa (ou deixe em branco para localhost): ");
            String serverAddress = scanner.nextLine().trim();
            if (serverAddress.isEmpty()) {
                serverAddress = "localhost";
            }
            System.out.println("Conectando ao servidor em " + serverAddress + "...");
            Registry registry = LocateRegistry.getRegistry(serverAddress, 1099);
            BolsaValoresControllerRemote bolsa = (BolsaValoresControllerRemote) registry.lookup("BolsaValores");
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
                // Ignora se já foi removido
            }
            System.out.println("Investidor desconectado. Encerrando...");
        } catch (Exception e) {
            System.err.println("Erro na aplicação do investidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
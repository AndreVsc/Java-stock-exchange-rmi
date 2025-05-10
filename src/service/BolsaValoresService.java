package service;

import model.Acao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import interfaces.PrecoAcaoListener;

/*
 * Serviço principal da Bolsa de Valores.
 *
 * Gerencia as ações, listeners de preço e book de ofertas.
 * Responsável por iniciar a simulação de preços e garantir concorrência.
 */
public class BolsaValoresService {
    private final Map<String, Acao> acoes = new ConcurrentHashMap<>();
    private final List<PrecoAcaoListener> listeners = new ArrayList<>();
    private final Random random = new Random();
    private final BookDeOfertas bookDeOfertas;
    
    /**
     * Inicializa o serviço com ações de exemplo e book de ofertas.
     */
    public BolsaValoresService() {
        acoes.put("PETR4", new Acao("PETR4", "Petrobras", 28.50));
        acoes.put("VALE3", new Acao("VALE3", "Vale", 68.20));
        acoes.put("ITUB4", new Acao("ITUB4", "Itaú Unibanco", 32.90));
        acoes.put("BBDC4", new Acao("BBDC4", "Bradesco", 20.15));
        acoes.put("ABEV3", new Acao("ABEV3", "Ambev", 14.80));
        
        bookDeOfertas = new BookDeOfertas();
    }
    
    /**
     * Inicia threads para simulação de atualização de preços das ações.
     */
    public void iniciarSimulacao() {
        for (String simbolo : acoes.keySet()) {
            Thread atualizadorPreco = new Thread(new AtualizadorPreco(simbolo));
            atualizadorPreco.setDaemon(true);
            atualizadorPreco.start();
        }
    }
    
    /**
     * Retorna a ação pelo símbolo.
     */
    public Acao getAcao(String simbolo) {
        return acoes.get(simbolo);
    }

    /**
     * Retorna o book de ofertas.
     */
    public BookDeOfertas getBookDeOfertas() {
        return bookDeOfertas;
    }
    
    /**
     * Retorna todas as ações disponíveis.
     */
    public Map<String, Acao> getAcoes() {
        return new HashMap<>(acoes);
    }
    
    /**
     * Adiciona um listener para notificações de preço.
     */
    public void adicionarListener(PrecoAcaoListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove um listener de notificações de preço.
     */
    public void removerListener(PrecoAcaoListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Thread interna que simula a atualização de preços de uma ação.
     */
    private class AtualizadorPreco implements Runnable {
        private final String simboloAcao;
        
        public AtualizadorPreco(String simboloAcao) {
            this.simboloAcao = simboloAcao;
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    // Aguarda entre 1 e 5 segundos para atualizar
                    Thread.sleep(1000 + random.nextInt(4000));
                    
                    Acao acao = acoes.get(simboloAcao);
                    if (acao != null) {
                        double precoAtual = acao.getPreco();
                        // Variação entre -2% e +2%
                        double variacao = precoAtual * (random.nextDouble() * 0.04 - 0.02);
                        double novoPreco = Math.max(0.01, precoAtual + variacao);
                        
                        acao.setPreco(novoPreco);
                        
                        // Notificar todos os listeners sobre a mudança de preço
                        synchronized (listeners) {
                            for (PrecoAcaoListener listener : listeners) {
                                listener.atualizacaoPreco(simboloAcao, precoAtual, novoPreco);
                            }
                        }
                        
                        // Verificar se alguma ordem pode ser executada com o novo preço
                        bookDeOfertas.verificarExecucaoOrdens(simboloAcao, novoPreco);
                        
                        System.out.println("[ATUALIZAÇÃO] " + acao);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
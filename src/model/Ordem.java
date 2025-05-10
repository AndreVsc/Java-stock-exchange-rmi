/*
 * Modelo de Ordem de compra/venda da bolsa.
 *
 * Representa uma ordem enviada por um investidor, com tipo, ação, preço, quantidade e status.
 * Serializable para uso em RMI.
 */
package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Ordem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * Enumeração para os tipos de ordem: COMPRA ou VENDA.
     */
    public enum TipoOrdem {
        COMPRA, VENDA
    }
    
    private final String id;
    private final String investidorId;
    private final String simboloAcao;
    private final TipoOrdem tipo;
    private final double preco;
    private final int quantidade;
    private final LocalDateTime dataCriacao;
    private boolean executada;
    
    /**
     * Cria uma nova ordem de compra ou venda.
     * @param investidorId ID do investidor
     * @param simboloAcao Código da ação
     * @param tipo Tipo da ordem (COMPRA/VENDA)
     * @param preco Preço da ordem
     * @param quantidade Quantidade de ações
     */
    public Ordem(String investidorId, String simboloAcao, TipoOrdem tipo, double preco, int quantidade) {
        this.id = UUID.randomUUID().toString();
        this.investidorId = investidorId;
        this.simboloAcao = simboloAcao;
        this.tipo = tipo;
        this.preco = preco;
        this.quantidade = quantidade;
        this.dataCriacao = LocalDateTime.now();
        this.executada = false;
    }
    
    /**
     * Retorna o ID único da ordem.
     * @return ID da ordem
     */
    public String getId() { return id; }
    
    /**
     * Retorna o ID do investidor que criou a ordem.
     * @return ID do investidor
     */
    public String getInvestidorId() { return investidorId; }
    
    /**
     * Retorna o símbolo da ação associada à ordem.
     * @return Símbolo da ação
     */
    public String getSimboloAcao() { return simboloAcao; }
    
    /**
     * Retorna o tipo da ordem (COMPRA ou VENDA).
     * @return Tipo da ordem
     */
    public TipoOrdem getTipo() { return tipo; }
    
    /**
     * Retorna o preço da ordem.
     * @return Preço da ordem
     */
    public double getPreco() { return preco; }
    
    /**
     * Retorna a quantidade de ações na ordem.
     * @return Quantidade de ações
     */
    public int getQuantidade() { return quantidade; }
    
    /**
     * Retorna a data de criação da ordem.
     * @return Data de criação
     */
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    
    /**
     * Verifica se a ordem foi executada.
     * @return true se executada, false caso contrário
     */
    public boolean isExecutada() { return executada; }
    
    /**
     * Define o status de execução da ordem.
     * @param executada true para marcar como executada, false caso contrário
     */
    public void setExecutada(boolean executada) { this.executada = executada; }
    
    /**
     * Retorna uma string legível para exibição da ordem.
     * @return Representação textual da ordem
     */
    @Override
    public String toString() {
        return String.format("Ordem[%s] %d x %s @ R$%.2f", 
            tipo, quantidade, simboloAcao, preco);
    }
}
package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Ordem implements Serializable {
    private static final long serialVersionUID = 1L;
    
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
    
    public String getId() {
        return id;
    }
    
    public String getInvestidorId() {
        return investidorId;
    }
    
    public String getSimboloAcao() {
        return simboloAcao;
    }
    
    public TipoOrdem getTipo() {
        return tipo;
    }
    
    public double getPreco() {
        return preco;
    }
    
    public int getQuantidade() {
        return quantidade;
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public boolean isExecutada() {
        return executada;
    }
    
    public void setExecutada(boolean executada) {
        this.executada = executada;
    }
    
    @Override
    public String toString() {
        return tipo + " " + quantidade + " " + simboloAcao + " @ R$" + 
               String.format("%.2f", preco) + " [" + (executada ? "EXECUTADA" : "PENDENTE") + "]";
    }
}
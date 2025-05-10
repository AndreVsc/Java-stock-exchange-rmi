package model;

import java.io.Serializable;

public class Acao implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String simbolo;
    private final String nome;
    private double preco;
    
    public Acao(String simbolo, String nome, double precoInicial) {
        this.simbolo = simbolo;
        this.nome = nome;
        this.preco = precoInicial;
    }
    
    public String getSimbolo() {
        return simbolo;
    }
    
    public String getNome() {
        return nome;
    }
    
    public synchronized double getPreco() {
        return preco;
    }
    
    public synchronized void setPreco(double novoPreco) {
        this.preco = novoPreco;
    }
}
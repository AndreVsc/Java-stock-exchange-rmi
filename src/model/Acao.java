/*
 * Modelo de Ação negociada na bolsa.
 *
 * Representa uma ação com símbolo, nome e preço atual.
 * Serializable para uso em RMI.
 */
package model;

import java.io.Serializable;

public class Acao implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String simbolo;
    private final String nome;
    private double preco;

    /**
     * Cria uma nova ação.
     * @param simbolo Código da ação (ex: PETR4)
     * @param nome Nome da empresa
     * @param precoInicial Preço inicial da ação
     */
    public Acao(String simbolo, String nome, double precoInicial) {
        this.simbolo = simbolo;
        this.nome = nome;
        this.preco = precoInicial;
    }

    public String getSimbolo() { return simbolo; }
    public String getNome() { return nome; }
    public synchronized double getPreco() { return preco; }
    public synchronized void setPreco(double novoPreco) { this.preco = novoPreco; }

    /**
     * Retorna uma string legível para exibição da ação.
     */
    @Override
    public String toString() {
        return String.format("%s (%s): R$%.2f", simbolo, nome, preco);
    }
}
/*
 * Controller da Bolsa de Valores
 *
 * Centraliza todas as operações de consulta e envio de ordens.
 * É a única ponte entre a camada de aplicação (app) e a camada de serviço (service).
 *
 * Responsável por:
 * - Consultar ações e ordens
 * - Enviar ordens para o book
 * - Gerenciar listeners de preço
 * - Garantir separação de camadas
 */
package controller;

import model.Acao;
import model.Ordem;
import service.BolsaValoresService;
import interfaces.PrecoAcaoListener;
import service.BookDeOfertas;

import java.util.List;
import java.util.Map;

/**
 * Controller central da bolsa de valores.
 * Fornece métodos para consulta e operações de ordens, sempre delegando ao service.
 */
public class BolsaValoresController {
    private final BolsaValoresService service;

    /**
     * Construtor padrão: cria um novo service.
     */
    public BolsaValoresController() {
        this.service = new BolsaValoresService();
    }

    /**
     * Construtor para injeção de dependência (usado pelo servidor).
     */
    public BolsaValoresController(BolsaValoresService service) {
        this.service = service;
    }

    /**
     * Inicia a simulação de atualização de preços.
     */
    public void iniciarSimulacao() {
        service.iniciarSimulacao();
    }

    /**
     * Consulta uma ação pelo símbolo.
     */
    public Acao getAcao(String simbolo) {
        return service.getAcao(simbolo);
    }

    /**
     * Retorna todas as ações disponíveis.
     */
    public Map<String, Acao> getAcoes() {
        return service.getAcoes();
    }

    /**
     * Adiciona um listener para notificações de preço.
     */
    public void adicionarListener(PrecoAcaoListener listener) {
        service.adicionarListener(listener);
    }

    /**
     * Remove um listener de notificações de preço.
     */
    public void removerListener(PrecoAcaoListener listener) {
        service.removerListener(listener);
    }

    /**
     * Retorna o book de ofertas (para uso interno/controlado).
     */
    public BookDeOfertas getBookDeOfertas() {
        return service.getBookDeOfertas();
    }

    /**
     * Envia uma ordem para o book de ofertas.
     */
    public void enviarOrdem(Ordem ordem) {
        service.getBookDeOfertas().adicionarOrdem(ordem);
    }

    /**
     * Lista as ordens de compra para uma ação.
     */
    public List<Ordem> listarOrdensCompra(String simboloAcao) {
        return service.getBookDeOfertas().getOrdensCompra(simboloAcao);
    }

    /**
     * Lista as ordens de venda para uma ação.
     */
    public List<Ordem> listarOrdensVenda(String simboloAcao) {
        return service.getBookDeOfertas().getOrdensVenda(simboloAcao);
    }
}
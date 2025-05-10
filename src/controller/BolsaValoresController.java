package controller;

import model.Acao;
import model.Ordem;
import service.BolsaValoresService;

import java.util.List;
import java.util.Map;

public class BolsaValoresController {
    private final BolsaValoresService service;

    public BolsaValoresController(BolsaValoresService service) {
        this.service = service;
    }

    public Map<String, Acao> listarAcoes() {
        return service.getAcoes();
    }

    public Acao obterAcao(String simbolo) {
        return service.getAcao(simbolo);
    }

    public void enviarOrdem(Ordem ordem) {
        service.getBookDeOfertas().adicionarOrdem(ordem);
    }

    public List<Ordem> listarOrdensCompra(String simboloAcao) {
        return service.getBookDeOfertas().getOrdensCompra(simboloAcao);
    }

    public List<Ordem> listarOrdensVenda(String simboloAcao) {
        return service.getBookDeOfertas().getOrdensVenda(simboloAcao);
    }
}
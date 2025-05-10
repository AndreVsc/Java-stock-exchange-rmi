/*
 * Book de Ofertas da Bolsa de Valores
 *
 * Gerencia ordens de compra e venda concorrentes para cada ação.
 * Responsável por casar ordens, limpar ordens executadas e notificar listeners.
 * Garante consistência dos dados com uso de estruturas concorrentes e sincronização.
 */
package service;

import model.Ordem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import interfaces.BookDeOfertasListener;

public class BookDeOfertas {
    // Mapas concorrentes para ordens de compra e venda por ação
    private final Map<String, List<Ordem>> ordensCompra = new ConcurrentHashMap<>();
    private final Map<String, List<Ordem>> ordensVenda = new ConcurrentHashMap<>();
    private final List<BookDeOfertasListener> listeners = new ArrayList<>();

    /**
     * Adiciona uma ordem de compra ou venda ao book e tenta casar ordens.
     */
    public void adicionarOrdem(Ordem ordem) {
        String simbolo = ordem.getSimboloAcao();
        if (ordem.getTipo() == Ordem.TipoOrdem.COMPRA) {
            ordensCompra.computeIfAbsent(simbolo, k -> Collections.synchronizedList(new ArrayList<>())).add(ordem);
            ordensCompra.get(simbolo).sort(Comparator.comparing(Ordem::getPreco).reversed());
        } else {
            ordensVenda.computeIfAbsent(simbolo, k -> Collections.synchronizedList(new ArrayList<>())).add(ordem);
            ordensVenda.get(simbolo).sort(Comparator.comparing(Ordem::getPreco));
        }
        verificarExecucaoOrdens(simbolo);
        notificarAlteracaoBook(simbolo);
    }

    /**
     * Verifica se há ordens de compra e venda que podem ser executadas (casadas).
     * Remove ordens executadas e notifica listeners se houver mudanças.
     */
    public void verificarExecucaoOrdens(String simboloAcao) {
        List<Ordem> compras = ordensCompra.getOrDefault(simboloAcao, Collections.emptyList());
        List<Ordem> vendas = ordensVenda.getOrDefault(simboloAcao, Collections.emptyList());
        boolean mudanca = false;
        synchronized (compras) {
            synchronized (vendas) {
                for (Ordem compra : new ArrayList<>(compras)) {
                    if (compra.isExecutada()) continue;
                    for (Ordem venda : new ArrayList<>(vendas)) {
                        if (venda.isExecutada()) continue;
                        if (compra.getPreco() >= venda.getPreco()) {
                            compra.setExecutada(true);
                            venda.setExecutada(true);
                            System.out.println("[EXECUÇÃO] Ordem de compra " + compra.getId() +
                                " casada com ordem de venda " + venda.getId() +
                                " - " + compra.getQuantidade() + " " + simboloAcao +
                                " @ R$" + String.format("%.2f", venda.getPreco()));
                            mudanca = true;
                        }
                    }
                }
            }
        }
        if (mudanca) {
            limparOrdensExecutadas();
            notificarAlteracaoBook(simboloAcao);
        }
    }

    /**
     * Sobrecarga para verificar ordens com preço atual (usado pela simulação de preço).
     */
    public void verificarExecucaoOrdens(String simboloAcao, double precoAtual) {
        verificarExecucaoOrdens(simboloAcao);
    }

    /**
     * Remove ordens executadas do book de ofertas.
     */
    private void limparOrdensExecutadas() {
        for (String simbolo : ordensCompra.keySet()) {
            List<Ordem> compras = ordensCompra.get(simbolo);
            synchronized (compras) {
                compras.removeIf(Ordem::isExecutada);
            }
        }
        for (String simbolo : ordensVenda.keySet()) {
            List<Ordem> vendas = ordensVenda.get(simbolo);
            synchronized (vendas) {
                vendas.removeIf(Ordem::isExecutada);
            }
        }
    }

    /**
     * Retorna as ordens de compra não executadas para uma ação.
     */
    public List<Ordem> getOrdensCompra(String simboloAcao) {
        return ordensCompra.getOrDefault(simboloAcao, Collections.emptyList())
                .stream()
                .filter(o -> !o.isExecutada())
                .collect(Collectors.toList());
    }

    /**
     * Retorna as ordens de venda não executadas para uma ação.
     */
    public List<Ordem> getOrdensVenda(String simboloAcao) {
        return ordensVenda.getOrDefault(simboloAcao, Collections.emptyList())
                .stream()
                .filter(o -> !o.isExecutada())
                .collect(Collectors.toList());
    }

    /**
     * Adiciona um listener para alterações no book de ofertas.
     */
    public void adicionarListener(BookDeOfertasListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Remove um listener do book de ofertas.
     */
    public void removerListener(BookDeOfertasListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Notifica todos os listeners sobre alteração no book de uma ação.
     */
    private void notificarAlteracaoBook(String simboloAcao) {
        synchronized (listeners) {
            for (BookDeOfertasListener listener : listeners) {
                listener.bookAlterado(simboloAcao);
            }
        }
    }
}
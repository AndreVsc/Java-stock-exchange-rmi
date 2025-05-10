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
    private final Map<String, List<Ordem>> ordensCompra = new ConcurrentHashMap<>();
    private final Map<String, List<Ordem>> ordensVenda = new ConcurrentHashMap<>();
    private final List<BookDeOfertasListener> listeners = new ArrayList<>();
    
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
    
    public void verificarExecucaoOrdens(String simboloAcao, double precoAtual) {
        verificarExecucaoOrdens(simboloAcao);
    }
    
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
    
    public List<Ordem> getOrdensCompra(String simboloAcao) {
        return ordensCompra.getOrDefault(simboloAcao, Collections.emptyList())
                .stream()
                .filter(o -> !o.isExecutada())
                .collect(Collectors.toList());
    }
    
    public List<Ordem> getOrdensVenda(String simboloAcao) {
        return ordensVenda.getOrDefault(simboloAcao, Collections.emptyList())
                .stream()
                .filter(o -> !o.isExecutada())
                .collect(Collectors.toList());
    }
    
    public void adicionarListener(BookDeOfertasListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removerListener(BookDeOfertasListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private void notificarAlteracaoBook(String simboloAcao) {
        synchronized (listeners) {
            for (BookDeOfertasListener listener : listeners) {
                listener.bookAlterado(simboloAcao);
            }
        }
    }
}
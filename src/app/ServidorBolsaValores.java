package app;

import rmi.BolsaValoresRemote;
import rmi.BolsaValoresRemoteImpl;
import service.BolsaValoresService;
import controller.BolsaValoresController;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorBolsaValores {
    public static void main(String[] args) {
        try {
            BolsaValoresService service = new BolsaValoresService();
            BolsaValoresController controller = new BolsaValoresController(service);

            controller.listarAcoes().forEach((k, v) -> System.out.println(v));

            service.iniciarSimulacao();
            
            BolsaValoresRemote bolsaRemota = new BolsaValoresRemoteImpl(service);
            
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("BolsaValores", bolsaRemota);
            
            System.out.println("Servidor da Bolsa de Valores iniciado!");
            System.out.println("Aguardando conexões dos investidores...");
            
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
/*
 * Servidor da Bolsa de Valores - Java RMI
 *
 * Inicializa o serviço da bolsa, expõe o controller remoto via RMI,
 * e inicia a simulação de preços das ações.
 *
 * Responsável por:
 * - Criar o serviço e controller
 * - Expor o controller remoto para clientes
 * - Iniciar threads de atualização de preços
 */
package app;

import rmi.BolsaValoresControllerRemote;
import rmi.BolsaValoresControllerRemoteImpl;
import service.BolsaValoresService;
import controller.BolsaValoresController;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Classe principal do servidor da bolsa de valores.
 * Inicia o serviço, expõe o controller remoto e aguarda conexões de investidores.
 */
public class ServidorBolsaValores {
    public static void main(String[] args) {
        try {
            // Inicializa o serviço de negócios da bolsa
            BolsaValoresService service = new BolsaValoresService();
            // Controller centraliza operações e é a ponte entre app e service
            BolsaValoresController controller = new BolsaValoresController(service);

            // Exibe as ações disponíveis no início
            controller.getAcoes().forEach((k, v) -> System.out.println(v));

            // Inicia threads de atualização de preços
            service.iniciarSimulacao();
            // Expondo o controller remoto via RMI
            BolsaValoresControllerRemote controllerRemoto = new BolsaValoresControllerRemoteImpl(controller);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("BolsaValores", controllerRemoto);
            System.out.println("Servidor da Bolsa de Valores iniciado!");
            System.out.println("Aguardando conexões dos investidores...");
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
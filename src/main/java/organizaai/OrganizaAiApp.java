package organizaai;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import organizaai.gerenciamento.GerenciadorAgenda;
import organizaai.interfacegrafica.TelaPrincipal;

/**
 * Classe responsável pelo ciclo de vida da interface gráfica JavaFX.
 * Herda de Application para gerir a abertura e fechamento da janela.
 */
public class OrganizaAiApp extends Application {

    private GerenciadorAgenda gerenciador;

    /**
     * Método chamado ao iniciar a interface.
     * 
     * @param primaryStage O palco principal (janela) da aplicação.
     */
    @Override
    public void start(Stage primaryStage) {
        // Inicializa o gerenciador (que já carrega o XML no construtor)
        this.gerenciador = new GerenciadorAgenda();

        // Cria a interface principal
        TelaPrincipal telaPrincipal = new TelaPrincipal(gerenciador);
        
        Scene scene = new Scene(telaPrincipal.getRoot(), 800, 600);
        
        primaryStage.setTitle("OrganizaAí - Agenda Pessoal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Método chamado automaticamente pelo JavaFX quando o usuário fecha a janela.
     * Garante o cumprimento do requisito de persistência ao salvar os dados no XML.
     */
    @Override
    public void stop() throws Exception {
        if (gerenciador != null) {
            gerenciador.salvarDados();
            System.out.println("Dados salvos com sucesso ao encerrar a aplicação.");
        }
        super.stop();
    }
}
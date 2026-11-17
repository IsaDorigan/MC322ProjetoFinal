package organizaai.interfacegrafica;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Caixas de diálogo reutilizadas pelas telas.
 */
final class Dialogos {

    private Dialogos() {
    }

    /**
     * Exibe uma mensagem de erro com um único botão OK.
     * Usa AlertType.NONE (em vez de ERROR) para não mostrar o ícone vermelho com X,
     * que parecia um botão mas não fazia nada.
     *
     * @param titulo    Título da janela.
     * @param cabecalho Texto de destaque no topo da caixa.
     * @param mensagem  Explicação do erro para o usuário.
     */
    static void mostrarErro(String titulo, String cabecalho, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.NONE, mensagem, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.showAndWait();
    }
}

package organizaai.excecoes;

/**
 * Exceção quando a tentativa de criar ou editar uma atividade
 * com uma data inválida (exemplo: data no passado).
 */
public class DataInvalidaException extends Exception {
    
    /**
     * Constrói a exceção com uma mensagem de erro.
     * 
     * @param mensagem A descrição do erro ocorrido.
     */
    public DataInvalidaException(String mensagem) {
        super(mensagem);
    }
}
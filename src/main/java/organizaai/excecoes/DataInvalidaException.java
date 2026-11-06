package src.main.java.organizaai.excecoes;

/**
 * Exceção lançada quando há tentativa de criar ou editar uma atividade
 * com uma data inválida (exemplo: data no passado).
 */
public class DataInvalidaException extends Exception {
    
    /**
     * Constrói a exceção com uma mensagem de erro detalhada.
     * 
     * @param mensagem A descrição do erro ocorrido.
     */
    public DataInvalidaException(String mensagem) {
        super(mensagem);
    }
}
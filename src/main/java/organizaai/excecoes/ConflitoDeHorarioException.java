package organizaai.excecoes;

/**
 * Excecaoo lançada quando a tentativa de agendar um evento com horário
 * em um intervalo de tempo que bate com outro evento já existente.
 */
public class ConflitoDeHorarioException extends Exception {
    
    /**
     * Constroi a exceção com uma mensagem sobre o conflito.
     * 
     * @param mensagem A descrição do choque de horários.
     */
    public ConflitoDeHorarioException(String mensagem) {
        super(mensagem);
    }
}
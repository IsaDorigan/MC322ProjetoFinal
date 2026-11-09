package organizaai.excecoes;

/**
 * Exceção lançada quando há tentativa de agendar um EventoComHorario
 * em um intervalo de tempo que conflita com outro evento já existente na agenda.
 */
public class ConflitoDeHorarioException extends Exception {
    
    /**
     * Constrói a exceção com uma mensagem detalhada do conflito.
     * 
     * @param mensagem A descrição do choque de horários.
     */
    public ConflitoDeHorarioException(String mensagem) {
        super(mensagem);
    }
}
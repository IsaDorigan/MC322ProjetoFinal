package organizaai.modelo;

/**
 * Representa os níveis de prioridade que uma atividade pode ter.
 * Auxilia na filtragem e na exibição de urgência na interface gráfica.
 */
public enum Prioridade {
    /** Prioridade baixa, para atividades não urgentes. */
    BAIXA,
    
    /** Prioridade média, para atividades de importância padrão. */
    MEDIA,
    
    /** Prioridade alta, para atividades urgentes ou críticas. */
    ALTA
}
package organizaai.modelo;
/**
 * Status
 * 
 * Representa o status (andamento) de uma atividade
 * Mostra ao usuário em que momento a atividade está
 */
public enum Status {
    //Quando a atividade criada ainda não foi iniciada
    PENDENTE,
    //Quando a atividade está em execução/acontecendo
    EM_ANDAMENTO,
    //Quando a atividade foi concluída
    CONCLUIDA,
    //Quando uma atividade passa do prazo e ainda não foi concluída
    EM_ATRASO
}

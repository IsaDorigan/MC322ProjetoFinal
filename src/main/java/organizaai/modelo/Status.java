package organizaai.modelo;
/**
 * Status
 * 
 * Representa o status (andamento) de uma atividade
 * Mostra ao usuário em que momento a atividade está
 */
public enum Status {
    //Quando a atividade criada ainda não foi iniciada
    PENDENTE("Pendente"),
    //Quando a atividade está em execução/acontecendo
    EM_ANDAMENTO("Em andamento"),
    //Quando a atividade foi concluída
    CONCLUIDA("Concluída"),
    //Quando uma atividade passa do prazo e ainda não foi concluída
    //(o nome EM_ATRASO é mantido para não quebrar arquivos XML já salvos)
    EM_ATRASO("Atrasado");

    private final String rotulo;

    Status(String rotulo) {
        this.rotulo = rotulo;
    }

    /**
     * Texto amigável (com acento e espaços) para mostrar ao usuário.
     *
     * @return O nome do status pronto para exibição.
     */
    public String getRotulo() {
        return rotulo;
    }

    /**
     * Usa o rótulo amigável na exibição (listas, caixas de escolha e textos).
     * A gravação no XML continua usando name(), então não é afetada.
     */
    @Override
    public String toString() {
        return rotulo;
    }
}

package organizaai.modelo;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Classe que representa a estrutura base de qualquer compromisso na agenda.
 * Define os atributos comuns e a obrigatoriedade de implementação de metodos específicos
 */
public abstract class Atividade {
    
    private String id;
    private String titulo;
    private String descricao;
    private LocalDate data;
    private Categoria categoria;
    private Prioridade prioridade;
    private Status status;

    /**
     * Construtor para inicializar uma atividade genérica.
     * O ID é gerado automaticamente para garantir a unicidade no sistema.
     * 
     * @param titulo     O título descritivo da atividade.
     * @param descricao  Os detalhes ou anotações da atividade.
     * @param data       A data em que a atividade ocorrerá ou deverá ser entregue. Formato: (ano-mês-dia)
     * @param categoria  A classificação da atividade (Pessoal, Faculdade, etc).
     * @param prioridade O nível de urgência (Baixa, Média, Alta).
     * @param status     O status da atividade (Pendente, Em andamento, Concluída, Em atraso)
     */
    public Atividade(String titulo, String descricao, LocalDate data, Categoria categoria, Prioridade prioridade, Status status) {
        this.id = UUID.randomUUID().toString();
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.categoria = categoria;
        this.prioridade = prioridade;
        this.status = status; // Toda atividade nasce como pendente
    }

    // Getters e Setters

    public String getId() { 
        return id; 
    }
    
    public String getTitulo() { 
        return titulo; 
    }
    public void setTitulo(String titulo) { 
        this.titulo = titulo; 
    }
    
    public String getDescricao() { 
        return descricao; 
    }
    public void setDescricao(String descricao) { 
        this.descricao = descricao; 
    }
    
    public LocalDate getData() { 
        return data; 
    }
    public void setData(LocalDate data) { 
        this.data = data; 
    }
    
    public Categoria getCategoria() { 
        return categoria; 
    }
    public void setCategoria(Categoria categoria) { 
        this.categoria = categoria; 
    }
    
    public Prioridade getPrioridade() { 
        return prioridade; 
    }
    public void setPrioridade(Prioridade prioridade) { 
        this.prioridade = prioridade; 
    }
    
    public Status getStatus() { 
        return status; 
    }
    public void setConcluida(Status status) { 
        this.status = status; 
    }

    /**
     * Método abstrato para exibir os detalhes específicos da atividade.
     * O polimorfismo garantirá que Tarefas e Eventos sejam exibidos da maneira correta.
     * 
     * @return String formatada com os dados da atividade.
     */
    public abstract String exibirDetalhes();

    /**
     * Define o ID da atividade
     * 
     * @param id O identificador único recuperado do arquivo.
     */
    public void setId(String id) {
        this.id = id;
    }

}

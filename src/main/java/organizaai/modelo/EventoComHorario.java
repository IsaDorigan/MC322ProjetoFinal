package organizaai.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa um evento na agenda que possui um horário fixo de início e de fim.
 * Herda as características de Atividade.
 */
public class EventoComHorario extends Atividade {
    
    private LocalTime horaInicio;
    private LocalTime horaFim;

    /**
     * Construtor de EventoComHorario.
     * 
     * @param titulo     Título do evento.
     * @param descricao  Descrição ou local do evento.
     * @param data       Data do evento.
     * @param horaInicio Horário programado para o inicio
     * @param horaFim    Horário programado para o termino.
     * @param categoria  Categoria do evento.
     * @param prioridade Prioridade do evento.
     */
    public EventoComHorario(String titulo, String descricao, LocalDate data, LocalTime horaInicio, LocalTime horaFim, Categoria categoria, Prioridade prioridade) {
        super(titulo, descricao, data, categoria, prioridade);
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public LocalTime getHoraInicio() { 
        return horaInicio; 
    }
    public void setHoraInicio(LocalTime horaInicio) { 
        this.horaInicio = horaInicio; 
    }

    public LocalTime getHoraFim() { 
        return horaFim; 
    }
    public void setHoraFim(LocalTime horaFim) { 
        this.horaFim = horaFim; 
    }

    /**
     * Retorna os detalhes formatados especificamente para um Evento.
     * 
     * @return String com os dados do evento, incluindo horários.
     */
    @Override
    public String exibirDetalhes() {
        return String.format("[EVENTO] %s - %s das %s às %s | Categoria: %s", 
                getTitulo(), getData().toString(), horaInicio.toString(), horaFim.toString(), getCategoria());
    }
}
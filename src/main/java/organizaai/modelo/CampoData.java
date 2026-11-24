package organizaai.modelo;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Cria e lê DatePickers que aceitam data digitada (dd/MM/aaaa) além da escolha pelo ícone.
 * <p>
 * O DatePicker do JavaFX só atualiza getValue() quando o texto digitado é "confirmado"
 * (Enter ou perda de foco, e o conversor padrão depende do idioma do sistema). Aqui o formato
 * é fixo e, ao salvar, o texto do campo é lido diretamente com {@link #ler(DatePicker)}.
 */
public final class CampoData {

    private static final DateTimeFormatter FMT_EXIBIR = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    // Na leitura aceita também "5/1/2027" (sem zero à esquerda)
    private static final DateTimeFormatter FMT_LER = DateTimeFormatter.ofPattern("d/M/uuuu");

    private CampoData() {
    }

    /**
     * Cria um DatePicker editável no formato dd/MM/aaaa.
     *
     * @param inicial Data inicialmente selecionada (pode ser null).
     */
    public static DatePicker criar(LocalDate inicial) {
        DatePicker dp = new DatePicker();
        dp.setEditable(true);
        dp.setPromptText("dd/mm/aaaa");
        dp.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : FMT_EXIBIR.format(data);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.trim().isEmpty()) {
                    return null;
                }
                return LocalDate.parse(texto.trim(), FMT_LER);   // lança DateTimeParseException se inválido
            }
        });
        dp.setValue(inicial);   // depois do conversor, para o texto já aparecer no formato certo

        // Confirma o que foi digitado quando o campo perde o foco
        dp.getEditor().focusedProperty().addListener((obs, tinhaFoco, temFoco) -> {
            if (!temFoco) {
                try {
                    dp.setValue(dp.getConverter().fromString(dp.getEditor().getText()));
                } catch (DateTimeParseException ex) {
                    // texto inválido: mantém o campo como está; o erro é mostrado ao salvar
                }
            }
        });
        return dp;
    }

    /**
     * Lê a data que está <b>de fato escrita</b> no campo (digitada ou escolhida no calendário).
     * Use este método no botão Salvar no lugar de {@code dp.getValue()}.
     *
     * @return A data, ou null se o campo estiver vazio.
     * @throws DateTimeParseException Se o texto não for uma data válida (ex.: 31/02/2026).
     */
    public static LocalDate ler(DatePicker dp) {
        LocalDate data = dp.getConverter().fromString(dp.getEditor().getText());
        dp.setValue(data);   // mantém o valor do DatePicker coerente com o texto
        return data;
    }
}
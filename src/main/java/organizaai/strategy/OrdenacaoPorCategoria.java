package organizaai.strategy;

import organizaai.modelo.Atividade;
import java.util.Comparator;
import java.util.List;

/**
 * Estratégia concreta que agrupa as atividades ordenando-as 
 * alfabeticamente pelo nome da categoria.
 */
public class OrdenacaoPorCategoria implements EstrategiaOrdenacao {

    @Override
    public void ordenar(List<Atividade> atividades) {
        // Compara o nome em String (ex: "FACULDADE", "PESSOAL") do enum Categoria
        atividades.sort(Comparator.comparing(a -> a.getCategoria().name()));
    }
}
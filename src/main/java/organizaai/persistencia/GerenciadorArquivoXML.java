package organizaai.persistencia;

import organizaai.modelo.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por ler e gravar as atividades da agenda em um arquivo XML local.
 */
public class GerenciadorArquivoXML {

    /**
     * Varre a lista de objetos cadastrados e salva todos os atributos no arquivo XML.
     * 
     * @param atividades A lista de atividades atual do sistema.
     * @param caminhoArquivo O nome/caminho do arquivo local (ex: "agenda.xml").
     */
    public void salvarAgenda(List<Atividade> atividades, String caminhoArquivo) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // Elemento raiz <agenda>
            Element rootElement = doc.createElement("agenda");
            doc.appendChild(rootElement);

            for (Atividade atv : atividades) {
                Element atividadeNode = doc.createElement("atividade");
                
                // Define o atributo "tipo" para sabermos qual subclasse instanciar depois
                if (atv instanceof TarefaComPrazo) {
                    atividadeNode.setAttribute("tipo", "TarefaComPrazo");
                } else if (atv instanceof EventoComHorario) {
                    atividadeNode.setAttribute("tipo", "EventoComHorario");
                }

                criarElemento(doc, atividadeNode, "id", atv.getId());
                criarElemento(doc, atividadeNode, "titulo", atv.getTitulo());
                criarElemento(doc, atividadeNode, "descricao", atv.getDescricao());
                criarElemento(doc, atividadeNode, "data", atv.getData().toString());
                criarElemento(doc, atividadeNode, "categoria", atv.getCategoria().name());
                criarElemento(doc, atividadeNode, "prioridade", atv.getPrioridade().name());
                criarElemento(doc, atividadeNode, "concluida", String.valueOf(atv.isConcluida()));

                // Atributos especificos do EventoComHorario
                if (atv instanceof EventoComHorario) {
                    EventoComHorario evento = (EventoComHorario) atv;
                    criarElemento(doc, atividadeNode, "horaInicio", evento.getHoraInicio().toString());
                    criarElemento(doc, atividadeNode, "horaFim", evento.getHoraFim().toString());
                }

                rootElement.appendChild(atividadeNode);
            }

            // Grava o conteúdo no arquivo XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Formata o XML para ficar legível
            
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(caminhoArquivo));
            transformer.transform(source, result);

        } catch (Exception e) {
            System.err.println("Erro ao salvar a agenda: " + e.getMessage());
        }
    }

    /**
     * Faz a leitura automática do arquivo local reconstruindo os objetos Atividade
     * 
     * @param caminhoArquivo O nome/caminho do arquivo local.
     * @return A lista de atividades recuperada, ou uma lista vazia se o arquivo não existir.
     */
    public List<Atividade> carregarAgenda(String caminhoArquivo) {
        // Cria uma lista para guardar as atividades
        List<Atividade> listaCarregada = new ArrayList<>();
        // abre o arquivo usando o caminho recebido
        File arquivo = new File(caminhoArquivo);

        // Se o arquivo não existir, retorna a lista vazia
        if (!arquivo.exists()) {
            return listaCarregada; 
        }

        // Tenta carregar a agenda
        try {
            // Faz a leitura do arquivo XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(arquivo);
            doc.getDocumentElement().normalize();

            // Pega as atividades existentes no arquivo
            NodeList atividades = doc.getElementsByTagName("atividade");

            // Percorre as atividades
            for (int temp = 0; temp < atividades.getLength(); temp++) {
                Node node = atividades.item(temp);

                // Verifica se o elemento é uma atividade
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elemento = (Element) node;

                    // Verifica o tipo da atividade
                    String tipo = elemento.getAttribute("tipo");

                    // Le as informaç~oes da atividade
                    String id = lerElemento(elemento, "id");
                    String titulo = lerElemento(elemento, "titulo");
                    String descricao = lerElemento(elemento, "descricao");
                    LocalDate data = LocalDate.parse(lerElemento(elemento, "data"));
                    Categoria categoria = Categoria.valueOf(lerElemento(elemento, "categoria"));
                    Prioridade prioridade = Prioridade.valueOf(lerElemento(elemento, "prioridade"));
                    boolean concluida = Boolean.parseBoolean(lerElemento(elemento, "concluida"));


                    // Cria a atividade de acordo com seu tipo
                    Atividade atividadeRecuperada = null;

                    // Polimorfismo na reconstrução
                    if (tipo.equals("TarefaComPrazo")) {
                        atividadeRecuperada = new TarefaComPrazo(titulo, descricao, data, categoria, prioridade);
                        
                    } else if (tipo.equals("EventoComHorario")) {
                        LocalTime horaInicio = LocalTime.parse(lerElemento(elemento, "horaInicio"));
                        LocalTime horaFim = LocalTime.parse(lerElemento(elemento, "horaFim"));
                        atividadeRecuperada = new EventoComHorario(titulo, descricao, data, horaInicio, horaFim, categoria, prioridade);
                    }

                    // Se a atividade foi criada, adiciona na lista
                    if (atividadeRecuperada != null) {
                        atividadeRecuperada.setId(id); // Restaura o ID original
                        atividadeRecuperada.setConcluida(concluida);
                        listaCarregada.add(atividadeRecuperada);
                    }
                }
            }
        } catch (Exception e) {
            // Se der erro, chama um catch
            System.err.println("Erro ao carregar a agenda: " + e.getMessage());
        }

        // retonra uma lista com as atividades que foram carregadas
        return listaCarregada;
    }



    private void criarElemento(Document doc, Element pai, String nome, String valor) {
        Element node = doc.createElement(nome);
        node.appendChild(doc.createTextNode(valor));
        pai.appendChild(node);
    }

    private String lerElemento(Element pai, String nomeTag) {
        return pai.getElementsByTagName(nomeTag).item(0).getTextContent();
    }
}
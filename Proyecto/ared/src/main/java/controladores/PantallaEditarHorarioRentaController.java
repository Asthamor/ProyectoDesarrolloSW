/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import clasesApoyo.Mapas;
import clasesApoyo.Mensajes;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import static controladores.PantallaGruposController.crearArchivoXML;
import interfaces.IControladorRentas;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import modelo.RentaXML;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * FXML Controller class
 *
 * @author alonso
 */
public class PantallaEditarHorarioRentaController implements Initializable, IControladorRentas {

    @FXML
    private GridPane gridHorario;
    @FXML
    private JFXButton btnAceptar;
    @FXML
    private Label lblHorario;
    private ArrayList<Integer> horarioRenta;
    private Mapas mapa;
    private Document document;
    private RentaXML rentaXML;
    @FXML
    private JFXDatePicker txtDia;
    private String dia;
    @FXML
    private ScrollPane scrollHorario;
    private boolean cambioDia = false;
    private PantallaEditarRentaController controlador;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        crearArchivoXML();
        scrollHorario.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollHorario.setVvalue(0.5);
        SAXReader reader = new SAXReader();
        try {
            document = reader.read(System.getProperty("user.home") + "/.ared/horariosAred.xml");
        } catch (DocumentException ex) {
            Logger.getLogger(PantallaGruposController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (mapa == null) {
            mapa = new Mapas();
        }
    }

    public void cargarDatos() {
        mostrarFecha();
        mostrarRenta();
    }

    public void setRentaXML(RentaXML rentaXML) {
        this.rentaXML = rentaXML;
        lblHorario.setText(this.rentaXML.getHorario());
        txtDia.setValue(LocalDate.parse(this.rentaXML.getDia()));
    }

    public void setHorarioRenta(ArrayList<Integer> horarioRenta) {
        this.horarioRenta = horarioRenta;
        obtenerHorario();
    }

    public void setControlador(PantallaEditarRentaController controlador) {
        this.controlador = controlador;
    }

    public void obtenerHorario() {
        int horaInicio = Integer.parseInt(mapa.getMapaHoras().get(lblHorario.getText().split("-")[0]).toString());
        int horaFin = Integer.parseInt(mapa.getMapaHoras().get(lblHorario.getText().split("-")[1]).toString());
        for (int i = horaInicio; i < horaFin; i++) {
            horarioRenta.add(i);
        }
    }

    @FXML
    private void mostrarEventos(ActionEvent event) {
        lblHorario.setText("");
        horarioRenta.clear();
        mostrarFecha();
    }

    @Override
    public ArrayList<Integer> getHorarioRenta() {
        return this.horarioRenta;
    }

    @Override
    public void agregarHora(int fila) {
        this.horarioRenta.add(fila);
        Collections.sort(horarioRenta);
    }

    @Override
    public void mostrarHorarios() {
        if (mapa == null) {
            mapa = new Mapas();
        }
        String horario = "";
        if (!horarioRenta.isEmpty()) {
            int ultimoElemnto = horarioRenta.size() - 1;
            horario = mapa.getMapaFilas().get(horarioRenta.get(0)) + "-"
                    + mapa.getMapaFilas().get(horarioRenta.get(ultimoElemnto) + 1);
        }
        lblHorario.setText(horario);
    }

    public void mostrarFecha() {
        dia = String.valueOf((txtDia.getValue().getDayOfWeek().getValue()) - 1);
        gridHorario.getChildren().clear();
        agregarAgenda();
        mostrarGrupos();
        mostrarTodasLasRentas();
        
    }

    public void agregarAgenda() {
        gridHorario.setVgap(1);
        gridHorario.setHgap(2);
        int hora = 0;
        for (int i = 0; i < 48; i += 2) {
            StackPane horasEnteras = new StackPane();
            Label lbHoras = new Label(hora + ":00");
            hora++;
            horasEnteras.getChildren().add(lbHoras);
            horasEnteras.setAlignment(lbHoras, Pos.TOP_RIGHT);
            gridHorario.add(horasEnteras, 0, i);
        }

        for (int i = 0; i < 48; i++) {
            gridHorario.add(crearTarjetaHorario("", "", true, false, i), 1, i);
        }
    }

    public void mostrarGrupos() {
        Element root = document.getRootElement();
        List<org.dom4j.Node> grupoXML = root.selectNodes("/ared/grupos/grupo[horario/dia/@num = '" + dia + "']");
        String nombreGrupo;
        String colorGrupo;

        for (org.dom4j.Node grupo : grupoXML) {
            Element grupoElemnt = (Element) grupo;
            nombreGrupo = grupoElemnt.attributeValue("nombreGrupo");
            colorGrupo = grupoElemnt.attributeValue("color");
            Element horario = grupoElemnt.element("horario");
            for (Iterator<Element> itr3 = horario.elementIterator("dia"); itr3.hasNext();) {
                Element dia = itr3.next();
                int columna = Integer.valueOf(dia.attributeValue("num"));
                for (Iterator<Element> itr4 = dia.elementIterator("hora"); itr4.hasNext();) {
                    int horaInicio = (Integer) mapa.getMapaHoras().get((String.valueOf(((Element) itr4.next()).getData())).trim());
                    int horaFin = (Integer) mapa.getMapaHoras().get((String.valueOf(((Element) itr4.next()).getData())).trim());
                    Parent pantalla = crearTarjetaHorario(nombreGrupo, colorGrupo, false, true, horaInicio);
                    gridHorario.add(pantalla, 1, horaInicio, 1, (horaFin - horaInicio));
                }
            }
        }
    }

    public void mostrarRenta() {
        int horaInicio = Integer.parseInt(mapa.getMapaHoras().get(lblHorario.getText().split("-")[0]).toString());
        int horaFin = Integer.parseInt(mapa.getMapaHoras().get(lblHorario.getText().split("-")[1]).toString());
        for (int i = horaInicio; i < horaFin; i++) {
            Parent pantalla = crearTarjetaHorario("Reservado", "#ffe6fd", true, true, i);
            gridHorario.add(pantalla, 1, i);
        }
    }

    public void mostrarTodasLasRentas() {
        
        List<org.dom4j.Node> rentas = document.selectNodes("/ared/rentas/renta[@dia = '"
                + txtDia.getValue().toString() + "' and @id != '" + rentaXML.getId() + "']");
        for (org.dom4j.Node renta : rentas) {
            String id = renta.valueOf("@id");
            String nombreCliente = renta.selectSingleNode("cliente").getText();
            String horario = renta.selectSingleNode("horario").getText();
            int horaInicio = Integer.parseInt(mapa.getMapaHoras().get(horario.split("-")[0]).toString());
            int horaFin = Integer.parseInt(mapa.getMapaHoras().get(horario.split("-")[1]).toString());
            Parent pantalla = crearTarjetaHorario(nombreCliente, "#D7BDE2", false, true, horaInicio);
            gridHorario.add(pantalla, 1, horaInicio, 1, (horaFin - horaInicio));

        }
    }

    public Parent crearTarjetaHorario(String nombreEvento, String colorEvento, boolean ocupado, boolean esEvento, int fila) {
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(PantallaRegistrarRentaController.class.getResource("/fxml/TarjetaHorarioRenta.fxml"));
        try {
            root = (Parent) loader.load();
        } catch (IOException ex) {
            Logger.getLogger(PantallaDefinirHorarioGrupoController.class.getName()).log(Level.SEVERE, null, ex);
        }
        TarjetaHorarioRentaController controlador = loader.getController();
        controlador.setColorEvento(colorEvento);
        controlador.setFila(fila);
        controlador.setControlador(this);
        controlador.setNombreEvento(nombreEvento);
        controlador.setEsGrupo(esEvento);
        controlador.setHoraLibre(ocupado);
        return root;
    }

    @FXML
    private void cambiarHorario(ActionEvent event) {
        Stage mainStage = (Stage) btnAceptar.getScene().getWindow();
        if (lblHorario.getText().equals("")) {
            Mensajes.mensajeAlert("Debe elegir un horario para la renta");
        } else {
            controlador.cambiarHorario(txtDia.getValue().toString(), lblHorario.getText());
            mainStage.close();
        }

    }

}

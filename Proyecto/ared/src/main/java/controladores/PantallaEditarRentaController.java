/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import clasesApoyo.JFXLimitedTextField;
import clasesApoyo.Mensajes;
import com.jfoenix.controls.JFXButton;
import static controladores.PantallaPrincipalDirectorController.crearPantalla;
import interfaces.Controlador;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import modelo.PagoRenta;
import modelo.Renta;
import modelo.RentaXML;
import org.controlsfx.control.Notifications;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * FXML Controller class
 *
 * @author alonso
 */
public class PantallaEditarRentaController implements Initializable, Controlador {

    @FXML
    private Label lblFecha;
    @FXML
    private Label lblNombreCliente;
    @FXML
    private Label lblHorario;
    @FXML
    private JFXLimitedTextField txtMonto;
    @FXML
    private JFXButton btnGuardar;
    @FXML
    private JFXButton btnCancelarRenta;
    @FXML
    private JFXButton btnEditar;
    
    private HBox pantallaDividida;
    private StackPane pnlPrincipal;
    private RentaXML rentaXML;
    private Renta renta;
    private ArrayList<Integer> horarioRenta;
    private Document document;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        horarioRenta = new ArrayList();
        SAXReader reader = new SAXReader();
        try {
            document = reader.read(System.getProperty("user.home") + "/.ared/horariosAred.xml");
        } catch (DocumentException ex) {
            Logger.getLogger(PantallaEditarRentaController.class.getName()).log(Level.SEVERE, null, ex);
        }

        txtMonto.setRequired(true);
        txtMonto.setCurrencyFilter();
        txtMonto.setText("$");

    }

    @FXML
    private void actualizarRenta(ActionEvent event) {
        boolean seActualizo = true;
        if (!existenCamposVacios()) {
            if (!rentaXML.getMonto().equals(txtMonto.getText().replace("$", ""))) {
                renta.getPagoRenta().setMonto(Integer.valueOf(txtMonto.getText().replace("$", "")));
                if (!renta.getPagoRenta().actualizarPago()) {
                    Mensajes.mensajeAlert("La renta no se pudo actualizar");
                    seActualizo = false;
                }
            }

            if (seActualizo) {
                guardarObjetoXML(crearObjetoXML());
                Notifications.create()
                        .title("¡Exito!")
                        .text("La renta se actualizo correctamente")
                        .showInformation();
                pnlPrincipal.getChildren().clear();
                pnlPrincipal.getChildren().add(crearPantalla("/fxml/PantallaRentas.fxml",
                        this.pnlPrincipal, this.pantallaDividida));
                pantallaDividida.getChildren().clear();
                pantallaDividida.getChildren().add(pnlPrincipal);
            }
        }
    }

    @FXML
    private void cancelarRenta(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que desea cancelar la renta?",
                ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            if (renta.eliminarRenta()) {
                eliminarObjetoXML();
                Notifications.create()
                        .title("¡Exito!")
                        .text("La renta se ha cancelado correctamente")
                        .showInformation();
                pnlPrincipal.getChildren().clear();
                pnlPrincipal.getChildren().add(crearPantalla("/fxml/PantallaRentas.fxml",
                        this.pnlPrincipal, this.pantallaDividida));
                pantallaDividida.getChildren().clear();
                pantallaDividida.getChildren().add(pnlPrincipal);
            }
        }
    }

    public Element crearObjetoXML() {
        Document document = DocumentHelper.createDocument();
        Element rentaXML = document.addElement("renta");
        rentaXML.addAttribute("dia", lblFecha.getText());
        rentaXML.addAttribute("id", this.rentaXML.getId());
        rentaXML.addElement("monto").addText(txtMonto.getText().replace("$", ""));
        rentaXML.addElement("horario").addText(lblHorario.getText());
        rentaXML.addElement("cliente").addText(lblNombreCliente.getText());

        return rentaXML;

    }

    public void guardarObjetoXML(Element renta) {
        org.dom4j.Node rentasXML = document.getRootElement().selectSingleNode("/ared/rentas/renta[@id = '" + rentaXML.getId() + "']");
        Element rentas = document.getRootElement().element("rentas");
        rentas.remove((Element) rentasXML);
        rentas.add(renta);
        try {
            XMLWriter writer = new XMLWriter(
                    new FileWriter(System.getProperty("user.home") + "/.ared/horariosAred.xml"));
            writer.write(this.document);
            writer.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void eliminarObjetoXML() {
        org.dom4j.Node rentasXML = document.getRootElement().selectSingleNode("/ared/rentas/renta[@id = '" + rentaXML.getId() + "']");
        Element rentas = document.getRootElement().element("rentas");
        rentas.remove((Element) rentasXML);
        try {
            XMLWriter writer = new XMLWriter(
                    new FileWriter(System.getProperty("user.home") + "/.ared/horariosAred.xml"));
            writer.write(this.document);
            writer.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void editarHorario(ActionEvent event) {
        rentaXML.setDia(lblFecha.getText());
        rentaXML.setHorario(lblHorario.getText());
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(PantallaEditarRentaController.class.getResource("/fxml/PantallaEditarHorarioRenta.fxml"));
        try {
            root = (Parent) loader.load();
        } catch (IOException ex) {
            Logger.getLogger(PantallaDefinirHorarioGrupoController.class.getName()).log(Level.SEVERE, null, ex);
        }
        PantallaEditarHorarioRentaController controlador = loader.getController();
        controlador.setRentaXML(rentaXML);
        controlador.setHorarioRenta(horarioRenta);
        controlador.setControlador(this);
        controlador.cargarDatos();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();

    }

    public void cambiarHorario(String dia, String horario) {
        lblFecha.setText(dia);
        lblHorario.setText(horario);
    }

    public void setRentaXML(RentaXML rentaXML) {
        this.rentaXML = rentaXML;
        lblFecha.setText(this.rentaXML.getDia());
        lblNombreCliente.setText(this.rentaXML.getNombreCliente());
        lblHorario.setText(this.rentaXML.getHorario());
        txtMonto.setText(this.rentaXML.getMonto());
        renta = new Renta();
        renta = renta.buscarRenta(Integer.parseInt(this.rentaXML.getId()));
    }

    @Override
    public void setPantallaDividida(HBox pantallaDividida) {
        this.pantallaDividida = pantallaDividida;
    }

    @Override
    public void setPnlPrincipal(StackPane pnlPrincipal) {
        this.pnlPrincipal = pnlPrincipal;
    }

    public boolean existenCamposVacios() {
        boolean huboError = false;
        boolean errorDeMonto = false;

        if (txtMonto.getText().replace("$", "").trim().isEmpty()) {
            txtMonto.setTextFormatter(null);
            txtMonto.setText("");
            errorDeMonto = true;
            huboError = true;
        }
        if (!txtMonto.validate()) {
            huboError = true;
        }
        if (errorDeMonto) {
            txtMonto.setText("$");
            txtMonto.setCurrencyFilter();
        }
        return huboError;
    }

}

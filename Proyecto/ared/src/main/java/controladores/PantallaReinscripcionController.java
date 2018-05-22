/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import clasesApoyo.JFXLimitedTextField;
import clasesApoyo.Mensajes;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.validation.NumberValidator;
import com.jfoenix.validation.RequiredFieldValidator;
import com.jfoenix.validation.base.ValidatorBase;
import interfaces.Controlador;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import modelo.Alumno;
import modelo.Grupo;
import modelo.Maestro;
import modelo.PagoAlumno;
import modelo.Persona;
import modelo.Usuario;

/**
 *
 * @author alonso
 */
public class PantallaReinscripcionController implements Initializable, Controlador {

    @FXML
    private ListView<String> lstGrupo;
    @FXML
    private ListView<String> lstAlumnos;
    @FXML
    private JFXLimitedTextField txtMonto;
    @FXML
    private JFXComboBox<?> comboPromocion;
    @FXML
    private Label labelMontoFinal;
    private HBox pantallaDividida;
    private StackPane pnlPrincipal;
    @FXML
    private JFXButton btnReinscribir;

    private Persona persona;
    private Usuario usuario;
    private ArrayList<String> nombresAlumnos;
    private List<Alumno> alumnos;
    private ArrayList<String> nombresGrupos;
    private List<Grupo> grupos;
    @FXML
    private Label lblNombreAlumno;

    public boolean setUserData() {
        String nombreUsuario = System.getProperty("nombreUsuario");
        usuario = new Usuario();
        usuario = usuario.buscar(nombreUsuario);
        if (usuario.getTipoUsuario().equals("maestro")) {
            this.persona = (Maestro) usuario.getMaestroCollection().toArray()[0];
        }
        if (usuario.getTipoUsuario().equals("director")) {
            this.persona = (Maestro) usuario.getMaestroCollection().toArray()[0];
        }
        return true;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblNombreAlumno.setText("");
        txtMonto.setRequired(true);
        txtMonto.setNumLimiter(6);
        setUserData();
        setListGrupos();
        ValidatorBase numVal = new NumberValidator();
        numVal.setMessage("Inserte el monto");
        ValidatorBase req = new RequiredFieldValidator();
        req.setMessage("Inserte el monto de inscripción");
        txtMonto.setValidators(numVal, req);

        txtMonto.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                labelMontoFinal.setText("$" + newValue);
            }
        });
    }

    private boolean setListGrupos() {
        Grupo g = new Grupo();
        nombresGrupos = new ArrayList();
        if (usuario.getTipoUsuario().equals("director")) {
            grupos = g.obtenerTodosLosGrupos();
        } else {
            grupos = g.obtenerGruposMaestro(((Maestro) persona).getMaestroPK().getIdMaestro());
        }

        grupos.forEach((grupo) -> {
            nombresGrupos.add(grupo.getNombre());
        });
        ObservableList<String> items = FXCollections.observableArrayList();
        items.addAll(nombresGrupos);
        lstGrupo.setItems(items);
        lstGrupo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                lstAlumnos.setItems(null);
                nombresAlumnos = new ArrayList();
                int grpIndex = lstGrupo.getSelectionModel().getSelectedIndex();
                if (grpIndex != -1) {
                    alumnos = new ArrayList(grupos.get(grpIndex).getAlumnoCollection());
                    alumnos.forEach((alumno) -> {
                        nombresAlumnos.add(alumno.getNombre() + " " + alumno.getApellidos());
                    });
                    ObservableList<String> items = FXCollections.observableArrayList();
                    items.addAll(nombresAlumnos);
                    lstAlumnos.setItems(items);
                    lstAlumnos.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            if (lstAlumnos.getSelectionModel().getSelectedIndex() != -1) {
                                Alumno alumn = alumnos.get(lstAlumnos.getSelectionModel().getSelectedIndex());
                                lblNombreAlumno.setText(alumn.getNombre() + " " + alumn.getApellidos());                                
                            }
                        }

                    });
                }
            }
        });
        return true;
    }

    @Override

    public void setPantallaDividida(HBox pantallaDividida) {
        this.pantallaDividida = pantallaDividida;
    }

    @Override
    public void setPnlPrincipal(StackPane pnlPrincipal) {
        this.pnlPrincipal = pnlPrincipal;
    }

    @FXML
    private void reinscribir(ActionEvent event) {
        int grpIndex = lstGrupo.getSelectionModel().getSelectedIndex();
        int almIndex = lstAlumnos.getSelectionModel().getSelectedIndex();
        if (txtMonto.validate() && almIndex != -1 && grpIndex != -1) {

            Grupo grupo = grupos.get(grpIndex);
            Alumno alumn = alumnos.get(almIndex);
            PagoAlumno pago = new PagoAlumno();
            pago.setAlumno(alumn);
            pago.setGrupo(grupo);
            pago.setMonto(Integer.parseInt(txtMonto.getText()));
            pago.setFechaPago(new Date());
            Date vence = java.sql.Date.valueOf(LocalDate.now().plusMonths(1));
            pago.setFechaVencimiento(vence);
            pago.registrarPago();

            setListGrupos();
            Mensajes.mensajeExitoso(alumn.getNombre() + " " + alumn.getApellidos()
                    + " reinscrito en el grupo " + grupo.getNombre());
        } else if (almIndex == -1) {
            Mensajes.mensajeAlert("Selecciona un alumno");
        } else if (grpIndex == -1) {
            Mensajes.mensajeAlert("Selecciona un grupo");
        }
    }

}
package application.Factures;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import application.Config;

public class FactureController implements Initializable {

	public static Connection conn = null;
	public static Statement st = null;
	public static ResultSet rs = null;

	static int numCom;
	static double montantTTC = 0.0;

	Alert alerte = new Alert(Alert.AlertType.WARNING, "", ButtonType.OK);
	ObservableList<Facture> liste;

	// Req pr recuperer l'id du client a partir de son nom
	String req_idCli = "SELECT numcl FROM client WHERE nom= '";

	String fichierJRXML = ".\\FactureComCli.jrxml";
	String fichierPDF = ".\\FactureComCli.pdf";

	@FXML
	private Button btnAchat;

	@FXML
	private Button btnImprimer;

	@FXML
	private Button btnMontant;
	
	@FXML 
	private Button btnFermer;

	@FXML
	private TableColumn<Facture, String> designation;

	@FXML
	private ComboBox<String> laDateCom;

	@FXML
	private ComboBox<String> leClient;

	@FXML
	private TableView<Facture> listAchats;

	@FXML
	private TableColumn<Facture, Integer> mt;

	@FXML
	private TableColumn<Facture, Integer> pu;

	@FXML
	private TableColumn<Facture, Integer> qte;

	@FXML
	private Label totalHT;

	@FXML
	private Label totalTTC;

	@FXML
	private Label labelPasAchats;

	@FXML
	private TextField tva;
	
	
	

	@Override
	public void initialize(URL localisation, ResourceBundle res) {
		leClient.setItems(FXCollections.observableArrayList(remplirComboBox("SELECT nom FROM client;")));
		laDateCom.setItems(
				FXCollections.observableArrayList(remplirComboBox("SELECT DISTINCT(dateCom) FROM commande;")));
		btnFermer.setVisible(true);

	}
	
	@FXML
	void fermer(ActionEvent event) {
		System.exit(0);
	}

	@FXML
	void affichAchats(ActionEvent event) {

		listAchats.setItems(null); // Vider la taleView si des données y figurent
		labelPasAchats.setVisible(false);
		totalHT.setText(null);
		totalTTC.setText(null);

		// On teste si un element est selectionner dans les comboBox
		if (leClient.getSelectionModel().isEmpty() || laDateCom.getSelectionModel().isEmpty()) {

			// Aucun client selectionner dans le comboBox on affiche une erreur
			alerte.setHeaderText(null);
			alerte.setContentText("Veuillez selectionner un element dans chacune des listes deroulantes");
			alerte.setTitle("Attention: Mauvaise validation");
			alerte.showAndWait();
		} else { // On recherche les achats effectues par le client selectionne

			// On recupere l'id du client selectionne dans le comboBox
			int id_Cli = idSelected(req_idCli, leClient.getSelectionModel().getSelectedItem());

			// Req pr recuperer l'id de la commande d'un client
			String req_idCom = "SELECT numCom FROM commande WHERE numCl= '" + id_Cli + "' AND dateCom ='";
			numCom = idSelected(req_idCom, laDateCom.getSelectionModel().getSelectedItem());

			// Si numCom est egale zero cad que le rs.next() n'a rien retourne comme donnees
			if (numCom <= 0) {

				labelPasAchats.setVisible(true);
			} else {
				String selectComClient = "SELECT designation, prixUnit, qteCom, prixUnit*qteCom\r\n"
						+ "			FROM client cl, commande co, detailcom de, produit pr\r\n"
						+ "			WHERE cl.numCl=co.numCl AND \r\n"
						+ "			co.numCom=de.numCom AND \r\n"
						+ "			de.numProd=pr.numProd AND\r\n"
						+ "			co.numCom = " + numCom + ";";

				designation.setCellValueFactory(new PropertyValueFactory<Facture, String>("desA"));
				pu.setCellValueFactory(new PropertyValueFactory<Facture, Integer>("puA"));
				qte.setCellValueFactory(new PropertyValueFactory<Facture, Integer>("qteA"));
				mt.setCellValueFactory(new PropertyValueFactory<Facture, Integer>("mtA"));

				// On ajoute les commandes du client donné dans la TableView
				liste = Config.getFactures(selectComClient);
				listAchats.setItems(liste);
			}

		}
	}

	@FXML
	void calculMontant(ActionEvent event) {
		
		if (tva.getText().isBlank() | Config.isLettre(tva.getText())) {
			alerte.setHeaderText(null);
			alerte.setContentText("Veuillez entrer un nombre pour la TVA");
			alerte.setTitle("Attention: Mauvaise saisie");
			alerte.showAndWait();
			
		}else {
			if (!(Config.isLettre(tva.getText())) & numCom > 0) {
				List<Integer> mt_list = new ArrayList<Integer>();
				int montantHT = 0;
				double vleurTVA = 0.0;

				String montantComClient = "SELECT prixUnit*qteCom\r\n"
						+ "			FROM client cl, commande co, detailcom de, produit pr\r\n"
						+ "			WHERE cl.numCl=co.numCl AND \r\n"
						+ "			co.numCom=de.numCom AND \r\n"
						+ "			de.numProd=pr.numProd AND\r\n"
						+ "			co.numCom = " + numCom + ";";

				conn = Config.connectBD();
				try {
					st = conn.createStatement();
					rs = st.executeQuery(montantComClient);

					while (rs.next()) {

						mt_list.add(rs.getInt(1)); // Ajout des differents montants dans la liste
					}

				} catch (Exception ex) {
					System.out.println("Impossible de recuperer les montants " + ex);
				}
				// On calcule le totalHT
				for (int i = 0; i < mt_list.size(); i++) {
					montantHT += Integer.valueOf(mt_list.get(i));

				}

				// On calcule le montant TTC avec la valeur de la TVA
				vleurTVA = (Double.valueOf(tva.getText()) / 100);
				montantTTC = montantHT * (1 + vleurTVA);
				
				// On les affiche sur l'interface de facture
				totalHT.setText("Total HT en Franc CFA: " + montantHT);
				totalTTC.setText("Total TTC en Franc CFA: " + montantTTC);

			}
		}

		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" }) // a cause de Map et HashMap
	@FXML
	void genererPDF(ActionEvent event) {

		if (!(Config.isLettre(tva.getText()))) {

			JasperDesign jasperDesign;
			try {

				Driver monDriver = new com.mysql.jdbc.Driver();
				DriverManager.registerDriver(monDriver);
				conn = DriverManager.getConnection(Config.url, Config.nomUser, Config.motP);

				jasperDesign = JRXmlLoader.load(fichierJRXML);
				JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

				// Paramétres à envoyer a l'etat
				Map parameters = new HashMap();
				parameters.put("numeroCom", Integer.valueOf(numCom));
				parameters.put("tva", Double.valueOf(tva.getText()));

				// substring pour mettre en majuscule la premiere lettre du montant
				String montantEnLettres = "Arrete la presente facture à la somme de : "
						+ (ChiffreEnLettres.convert(montantTTC)).substring(0, 1).toUpperCase() + ""
						+ (ChiffreEnLettres.convert(montantTTC)).substring(1).toLowerCase()
						+ " francs CFA toutes taxes comprises./.";

				parameters.put("montantLettres", montantEnLettres);

				// Execution de l'etat
				JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
				// Creation de l'etat au format PDF
				JasperExportManager.exportReportToPdfFile(jasperPrint, fichierPDF);
				// Ouvrir le fichier PDF
				try {
					Desktop d = Desktop.getDesktop();
					d.open(new File("FactureCommande" + numCom + ".pdf"));
				} catch (IOException err) {
					System.out.println("err = " + err);
				}
			} catch (JRException | SQLException e) {
				System.out.println("Fichier non trouve " + e);

			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("Echec de fermeture de la connexion a la base de donnees " + e);
				}
			}
		} else {
			alerte.setHeaderText(null);
			alerte.setContentText("Veuillez entrer un nombre pour la TVA");
			alerte.setTitle("Attention: Mauvaise saisie");
			alerte.showAndWait();
		}
	}

	// Fonction pour la recuperation de l'id de l'element selectionne
	public int idSelected(String larequete, String elecherch) {
		conn = Config.connectBD();

		int id = 0;
		larequete = larequete + elecherch + "';";
		try {
			st = conn.createStatement();
			rs = st.executeQuery(larequete);
			if (rs.first()) {
				id = rs.getInt(1);
			}
			rs.close();
			st.close();
		} catch (Exception exception) {
			System.out.println(exception);
		}
		return id;

	}

	// Fonction de type liste de chaine de caracteres pour remplir le comboBox
	public List<String> remplirComboBox(String requete) {

		// Declaration d'une liste de chaine de caracteres
		List<String> options = new ArrayList<>();

		try {
			conn = Config.connectBD();
			try {
				st = conn.createStatement();
				rs = st.executeQuery(requete);

				while (rs.next()) {
					options.add(rs.getString(1));

				}
				st.close();
				rs.close();
				conn.close();

			} catch (Exception sqlExcptn) {
				System.out.println("Requete non executée");
			}

		} catch (Exception ex) {
			System.out.println("Connexion echouée à la base de données.");

		}
		return options; // retourne la liste des donnees de l'exécution de la requete
	}

}

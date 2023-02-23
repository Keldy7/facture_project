package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import application.Factures.Facture;


public class Config {

	public Connection conn = null;
    static PreparedStatement st;
    public static ResultSet rs = null;
    
    public static String dbnom = "bdgsf";
	public static String driver ="com.mysql.jdbc.Driver";
    public static String url = "jdbc:mysql://localhost:3306/" + dbnom + "?useUnicode=true&characterEncoding=utf8";
    public static String nomUser = "root";
    public static String motP = "";
    
    //Fonction pour checker la connection à la BD
	public static Connection connectBD(){
				        
		try {
            Class.forName(driver);
            Connection conn = (Connection) DriverManager.getConnection(url, nomUser, motP);
	        //System.out.println("Connection reuissie à notre base de données: " + dbnom);
	        return conn;

		} catch (Exception e) {
	        //System.out.println("Connection échouée à notre base de données: " + dbnom);
			return null;
		}
		
	}
	
	
	//Tester si un champ de saisie recoit que des lettres
    public static boolean isLettre(String texField) {
    	Pattern p = Pattern.compile("[a-zA-Z]+");
    	Matcher m = p.matcher(texField);
    	if (m.find() && m.group().equals(texField)) {
    		Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention : Saisie de lettres");
            alert.setHeaderText(null);
            alert.setContentText("S'il vous plait veuiller saisir un nombre pour la valeur de la TVA.");
            alert.showAndWait();
    		return true;

    	}else{
    		
    		return false;
    	}    	
    }
    
  
    //Fonction de type liste observable pour récupérer les données d'une requete
    public static ObservableList<Facture> getFactures(String selectCmdClient){
		Connection conn = connectBD();
		ObservableList<Facture> listeFour = FXCollections.observableArrayList();

		try {
			st = conn.prepareStatement(selectCmdClient);
			rs = st.executeQuery();
	        
	        while(rs.next()) {
				   listeFour.add(new Facture(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4)));
	        }
			
		} catch (Exception e) {
			System.out.println("Erreur dans getFactures() " + e);
		}

		return listeFour;
		
	}
}

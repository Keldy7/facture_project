package application.Factures;

import java.text.*;

public class ChiffreEnLettres {
  private static final String[] dizaines = {
      "",
      "",
      "vingt",
      "trente",
      "quarante",
      "cinquante",
      "soixante",
      "soixante",
      "quatre-vingt",
      "quatre-vingt"
  };

  private static final String[] unites1 = {
      "",
      "un",
      "deux",
      "trois",
      "quatre",
      "cinq",
      "six",
      "sept",
      "huit",
      "neuf",
      "dix",
      "onze",
      "douze",
      "treize",
      "quatorze",
      "quinze",
      "seize",
      "dix-sept",
      "dix-huit",
      "dix-neuf"
  };

  private static final String[] unites2 = {
      "",
      "",
      "deux",
      "trois",
      "quatre",
      "cinq",
      "six",
      "sept",
      "huit",
      "neuf",
      "dix"
  };

  private ChiffreEnLettres() {
  }

  private static String convertZeroACent(int nombre) {

    int laDizaine = nombre / 10;
    int lUnite = nombre % 10;
    String resultat = "";

    switch (laDizaine) {
      case 1:
      case 7:
      case 9:
        lUnite = lUnite + 10;
        break;
      default:
    }

    // séparateur "-" "et ""
    String laLiaison = "";
    if (laDizaine > 1) {
      laLiaison = "-";
    }
    // cas particuliers
    switch (lUnite) {
      case 0:
        laLiaison = "";
        break;
      case 1:
        if (laDizaine == 8) {
          laLiaison = "-";
        } else {
          laLiaison = " et ";
        }
        break;
      case 11:
        if (laDizaine == 7) {
          laLiaison = " et ";
        }
        break;
      default:
    }

    // dizaines en lettres
    switch (laDizaine) {
      case 0:
        resultat = unites1[lUnite];
        break;
      case 8:
        if (lUnite == 0) {
          resultat = dizaines[laDizaine];
        } else {
          resultat = dizaines[laDizaine]
              + laLiaison + unites1[lUnite];
        }
        break;
      default:
        resultat = dizaines[laDizaine]
            + laLiaison + unites1[lUnite];
    }
    return resultat;
  }

  private static String convertNbrePlusdeMille(int nombre) {

    int lesCentaines = nombre / 100;
    int leReste = nombre % 100;
    String sReste = convertZeroACent(leReste);

    String resultat;
    switch (lesCentaines) {
      case 0:
        resultat = sReste;
        break;
      case 1:
        if (leReste > 0) {
          resultat = "cent " + sReste;
        } else {
          resultat = "cent";
        }
        break;
      default:
        if (leReste > 0) {
          resultat = unites2[lesCentaines] + " cent " + sReste;
        } else {
          resultat = unites2[lesCentaines] + " cents";
        }
    }
    return resultat;
  }

  public static String convert(double nbre) {
    // 0 à 999 999 999 999
    if (nbre == 0) {
      return "zero";
    }

    String snombre = Long.toString((long) nbre);

    String mask = "000000000000";
    DecimalFormat df = new DecimalFormat(mask);
    snombre = df.format(nbre);

    // XXXnnnnnnnnn
    int lesMilliards = Integer.parseInt(snombre.substring(0, 3));
    // nnnXXXnnnnnn
    int lesMillions = Integer.parseInt(snombre.substring(3, 6));
    // nnnnnnXXXnnn
    int lesCentMille = Integer.parseInt(snombre.substring(6, 9));
    // nnnnnnnnnXXX
    int lesMille = Integer.parseInt(snombre.substring(9, 12));

    String tradMilliards;
    switch (lesMilliards) {
      case 0:
        tradMilliards = "";
        break;
      case 1:
        tradMilliards = convertNbrePlusdeMille(lesMilliards)
            + " milliard ";
        break;
      default:
        tradMilliards = convertNbrePlusdeMille(lesMilliards)
            + " milliards ";
    }
    String resultat = tradMilliards;

    String tradMillions;
    switch (lesMillions) {
      case 0:
        tradMillions = "";
        break;
      case 1:
        tradMillions = convertNbrePlusdeMille(lesMillions)
            + " million ";
        break;
      default:
        tradMillions = convertNbrePlusdeMille(lesMillions)
            + " millions ";
    }
    resultat = resultat + tradMillions;

    String tradCentMille;
    switch (lesCentMille) {
      case 0:
        tradCentMille = "";
        break;
      case 1:
        tradCentMille = "mille ";
        break;
      default:
        tradCentMille = convertNbrePlusdeMille(lesCentMille)
            + " mille ";
    }
    resultat = resultat + tradCentMille;

    String tradMille;
    tradMille = convertNbrePlusdeMille(lesMille);
    resultat = resultat + tradMille;

    return resultat;
  }

}

package org.colombe.glossaries;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

public class Glossary {

	public static Map<String, String> abrev = new LinkedHashMap<String, String>();

	private static Map<String, String> glossary = new HashMap<String, String>();
	private static Map<String, Integer> bookNumbers = new HashMap<String, Integer>();
	public static Map<String, Integer> bookAbrevNumbers = new HashMap<String, Integer>();
	private static Set<String> history = new HashSet<String>();
	private static String sourcePath;
	private static String bibleRef = null;

	static {
		/*  Ancien Testament  */

		// Le pentateuque
		abrev.put("Gen",    "Gn");
		abrev.put("Exod",   "Ex");
		abrev.put("Lev",    "Lv");
		abrev.put("Num",    "Nb");
		abrev.put("Deut",   "Dt");

		// Les livres historiques
		abrev.put("Josh",   "Jos");
		abrev.put("Judg",   "Jg");
		abrev.put("Ruth",   "Rt");
		abrev.put("1Sam",   "1 S");
		abrev.put("2Sam",   "2 S");
		abrev.put("1Kgs",   "1 R");
		abrev.put("2Kgs",   "2 R");
		abrev.put("1Chr",   "1 Ch");
		abrev.put("2Chr",   "2 Ch");
		abrev.put("Ezra",   "Esd");
		abrev.put("Neh",    "Né");
		abrev.put("Esth",   "Est");

		// Les livres poetiques
		abrev.put("Job",    "Jb");
		abrev.put("Ps",     "Ps");
		abrev.put("Prov",   "Pr");
		abrev.put("Eccl",   "Ec");
		abrev.put("Song",   "Ct");

		// Les prophètes
		abrev.put("Isa",    "Es");
		abrev.put("Jer",    "Jr");
		abrev.put("Lam",    "Lm");
		abrev.put("Ezek",   "Ez");
		abrev.put("Dan",    "Dn");
		abrev.put("Hos",    "Os");
		abrev.put("Joel",   "Jl");
		abrev.put("Amos",   "Am");
		abrev.put("Obad",   "Ab");
		abrev.put("Jonah",  "Jon");
		abrev.put("Mic",    "Mi");
		abrev.put("Nah",    "Na");
		abrev.put("Hab",    "Ha");
		abrev.put("Zeph",   "So");
		abrev.put("Hag",    "Ag");
		abrev.put("Zech",   "Za");
		abrev.put("Mal",    "Ml");

		/*  Nouveau Testament  */

		abrev.put("Matt",   "Mt");
		abrev.put("Mark",   "Mc");
		abrev.put("Luke",   "Lc");
		abrev.put("John",   "Jn");

		abrev.put("Acts",   "Ac");

		abrev.put("Rom",    "Rm");
		abrev.put("1Cor",   "1 Co");
		abrev.put("2Cor",   "2 Co");
		abrev.put("Gal",    "Ga");
		abrev.put("Eph",    "Ep");
		abrev.put("Phil",   "Ph");
		abrev.put("Col",    "Col");
		abrev.put("1Thess", "1 Th");
		abrev.put("2Thess", "2 Th");
		abrev.put("1Tim",   "1 Tm");
		abrev.put("2Tim",   "2 Tm");
		abrev.put("Titus",  "Tt");
		abrev.put("Phlm",   "Phm");

		abrev.put("Heb",    "Hé");

		abrev.put("Jas",    "Jc");

		abrev.put("1Pet",   "1 P");
		abrev.put("2Pet",   "2 P");

		abrev.put("1John",  "1 Jn");
		abrev.put("2John",  "2 Jn");
		abrev.put("3John",  "3 Jn");

		abrev.put("Jude",   "Jude");
		abrev.put("Rev",    "Ap");

		int i = 1;
		for (Entry<String, String> ab: abrev.entrySet()) {
			bookNumbers.put(ab.getKey(), i);
			bookAbrevNumbers.put(ab.getValue(), i);
			i++;
		}
	}

	public static void setSourcePath(String value) {
		sourcePath = value;
	}

	public static String getGlossaryLink(String hrefLink, boolean fromGlossary)
	{
		Document doc = Jsoup.parse(hrefLink);
		String word = doc.text();

		String[] fileKey = getHrefParams(hrefLink);
		String definition = getDefinition(fileKey[0], fileKey[1], fromGlossary);
		if (fromGlossary) {

			definition = definition.replace("%",  "%25");
			definition = definition.replace("\"", "%27");
			definition = definition.replace("'",  "%27");
			definition = definition.replace(">",  "%3E");

			return "<a href=\"r" + definition + "\">" + word + "</a>";
		}

		return "<RF q=*><b>" + word + "</b>:" + definition + "<Rf>" + word;
		//return "<RF q=" + word + "><b>" + word + "</b>:" + definition + "<Rf>"; // Bonne idée, mais pb d'espace
	}

	private static String[] getHrefParams(String hrefLink)
	{
		String hrefParams = hrefLink.substring(hrefLink.indexOf("href=") + 6); // href="
		hrefParams = hrefParams.substring(0, hrefParams.indexOf('"')); // Pour enlever le dernier "

		return hrefParams.split("#");
	}

	private static String getDefinition(String xmlFile, String key, boolean fromGlossary)
	{
		if (! glossary.containsKey(key)) {

			StringBuilder result = new StringBuilder();
			history.add(key); // Pour ne pas entrer en référence circulaire (Esprit -> Vent -> Esprit... )

			try {
				File f = new File(sourcePath + File.separatorChar + xmlFile);
				Document doc = Jsoup.parse(f, "UTF-8");
				Element parent = doc.select("div.item > span.label > a[id=" + key + "]").parents().get(1);
				List<Node> itemList = parent.childNodes();
				for (Node p: itemList) {

					String suite = p.toString();
					if (suite.trim().isEmpty() || suite.startsWith("<span class=\"label\">"))
						continue;

					if (suite.startsWith(",") && Character.isDigit(suite.charAt(1)) && bibleRef != null) {

						// Nous étions dans une référence, et nous avons là des versets.
						// Par exemple: 1 S 10:6,10
						// La référence sur 1 S 10:6 a déjà été faite,
						// on va ajouter maintenant la référence vers 1 S 10:6:10

						String[] tabRef = suite.split(",");
						for (String ref: tabRef) {
							if (ref.isEmpty())
								continue;

							if (! Character.isDigit(ref.charAt(0))) {
								result.append(StringEscapeUtils.unescapeHtml4(ref));
								continue;
							}

							StringBuilder text = new StringBuilder(ref);
							StringBuilder verseNumber = new StringBuilder();

							for (int i = 0; i < text.length(); i++) {
								char c = text.charAt(i);
								if (Character.isDigit(c) || c == '-')
									verseNumber.append(c);
							}

							text.delete(0, verseNumber.length()); // Pour garder le texte "non numéro de verset"

							// Création de la nouvelle référence
							StringBuilder newRef = new StringBuilder(bibleRef.substring(0, bibleRef.lastIndexOf('.') +1));
							newRef.append(verseNumber).append("\">").append(verseNumber).append("</a>");

							result.append(",").append(newRef).append(text);
						}

						bibleRef = null;
						continue;
					}

					if (suite.startsWith("<a class=\"reference\"")) {
						bibleRef = getBibleLink(suite);
						result.append(bibleRef);
						continue;
					}

					if (suite.contains("<a class=\"w-gloss\"")) {
						String[] refs = getHrefParams(suite);
						String nextKey = refs[1];

						if (history.contains(nextKey)) {
							Document d = Jsoup.parse(suite);
							result.append(" *").append(d.text()).append(" "); // On coupe la référence circulaire
							continue;
						}

						suite = getGlossaryLink(suite, true);
					}

					result.append(StringEscapeUtils.unescapeHtml4(suite));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			history.remove(key);
			if (fromGlossary)
				return result.toString(); // Pour ne pas mémoriser (et ressortir) une définition éventuellement limitée du aux ref. circulaires

			glossary.put(key, result.toString()); // Pour aller plus vite la prochaine fois...
		}

		return glossary.get(key);
	}

	private static String getBibleLink(String hrefLink)
	{
		// Correction des erreurs de référence dans le glossaire
		if (hrefLink.equals("<a class=\"reference\" href=\"2John.xml\">2</a>"))
			hrefLink = "<a class=\"reference\" href=\"2John.xml\">2 Jn</a>";

		StringBuilder result = new StringBuilder("<a class=\"bible\" href=\"#b");

		// On extrait le nom du livre à partir du href
		String part1 = hrefLink.split("href=\"")[1];
		String book = part1.split(".xml")[0];
		if (part1.contains("-"))
			book = book.split("-")[0];

		result.append(bookNumbers.get(book)); // <a class="bible" href="#b19

		String part2 = hrefLink.substring(hrefLink.indexOf('>') +1, hrefLink.indexOf("</"));
		if (Character.isDigit(part2.charAt(part2.length() -1))) {

			// Si le dernier caractère est un chiffre, on complète le lien
			result.append('.');
			if (part2.contains(" ")) {

				String[] ref = part2.split(" ");
				if (isSingleChapter(part2))
					result.append("1."); // Le chapitre 1 est sous-entendu

				result.append(ref[ref.length -1]); // Dans 2 Tim 2.3, ce qui nous interresse, c'est 2.3
			} else
				result.append(part2); // 3.14 (renvoie au chapitre 3, verset 14 du livre courrant)
		}

		result.append("\">"); // Termine la balise <a href> commencé dès le début

		// Partie visible du lien
		if (! part2.contains(" "))
			result.append(abrev.get(book)).append(" "); // on ajoute le nom du livre

		part2 = part2.replace(".", ":");
		result.append(part2).append("</a>");

		return result.toString();
	}

	public static boolean isSingleChapter(String ref)
	{
		return  ref.startsWith("Obad") ||
				ref.startsWith("Phlm") ||
				ref.startsWith("2 Jn") ||
				ref.startsWith("3 Jn") ||
				ref.startsWith("Jude");
	}
}

package org.colombe.glossaries;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

public class Glossary {

	public static Map<String, String> abrev = new LinkedHashMap<String, String>();

	private static Map<String, Integer> bookNumbers = new HashMap<String, Integer>();
	private static String sourcePath;

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
			i++;
		}
	}

	public static void setSourcePath(String value) {
		sourcePath = value;
	}

	public static String getGlossaryLink(String hrefLink)
	{
		Document d = Jsoup.parse(hrefLink);
		String s1 = d.text();

		String s2 = hrefLink.substring(hrefLink.indexOf("href=") + 6); // href="
		s2 = s2.substring(0, s2.indexOf('"')); // Pour enlever le dernier "
		String[] refs = s2.split("#");
		String file = refs[0];
		String key = refs[1];

		String definition = getDefinition(sourcePath + File.separatorChar + file, key);
		s1 = "<RF q=*><b>" + s1 + "</b>: " + definition + "<Rf>" + s1;

		return s1;
	}

	private static boolean isMonoChapter(String ref)
	{
		return ref.startsWith("Obad") ||
				ref.startsWith("Phlm") ||
				ref.startsWith("2 Jn") ||
				ref.startsWith("3 Jn") ||
				ref.startsWith("Jude");
	}

	private static String getBibleLink(String hrefLink)
	{
		StringBuilder result = new StringBuilder("<a class=\"bible\" href=\"#b");

		String part1 = hrefLink.split("href=\"")[1];
		String sep = "-"; // Comme dans 2Cor-3.xml
		if (! part1.contains(sep))
			sep = ".xml"; // Concerne les livres sans chapitre (bug de l'editeur, aurait du etre jude-1.xml par exemple)

		String book = part1.substring(0, part1.indexOf(sep));
		result.append(bookNumbers.get(book));

		String part2 = hrefLink.substring(hrefLink.indexOf('>') +1, hrefLink.indexOf("</"));
		if (Character.isDigit(part2.charAt(part2.length() -1))) {
			// Si le dernier caractère est un chiffre, on complète le lien

			result.append('.');
			if (part2.contains(" ")) {

				String[] ref = part2.split(" ");
				if (isMonoChapter(part2))
					result.append("1."); // Le chapitre 1 est sous-entendu

				result.append(ref[ref.length -1]); // Dans 2 Tim 2.3, ce qui nous interresse, c'est 2.3
			} else {
				result.append(part2); // 3.14 (renvoie au chapitre 3, verset 14 du livre courrant)
			}
		}

		result.append("\">"); // Termine la balise <a href> commencé dès le début

		// Partie visible du lien
		if (! part2.contains(" "))
			result.append(abrev.get(book)).append(" "); // on affiche le livre s'il n'y était pas déjà

		part2 = part2.replace(".", ":");
		result.append(part2).append("</a>");

		return result.toString();
	}

	public static String getDefinition(String xmlFile, String key)
	{
		StringBuilder result = new StringBuilder();

		try {
			File f = new File(xmlFile);
			Document doc = Jsoup.parse(f, "UTF-8");
			Element parent = doc.select("div.item > span.label > a[id=" + key + "]").parents().get(1);
			List<Node> itemList = parent.childNodes();
			for (Node p: itemList) {

				String s = p.toString();
				if (s.startsWith("<span class=\"label\">"))
					continue;

				if (s.startsWith("<a class=\"reference\"")) {
					result.append(getBibleLink(s));
					continue;
				}

				if (s.contains("<a class=\"w-gloss\"")) {
					Document d = Jsoup.parse(s);
					result.append(" *").append(d.text()).append(" ");
					continue;
				}

				String suite = s.trim();

				result.append(StringEscapeUtils.unescapeHtml4(suite));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result.toString();
	}
}

package org.colombe.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.colombe.glossaries.Glossary;

public class ReferenceUtil {

	private static String bookName;
	private static int bookNum;
	private static String chapterNum;
	private static String[] separator = new String[] {
			";",
			"et",
			"comp.",
			", ",
			" ",
			","
	};

	public static String decode(String references) {

		StringBuilder result = new StringBuilder();

		if (references.contains("12.15-34 1 Tm 6.9-10")) // Il manque un ;
			references = references.replace("12.15-34 1 Tm 6.9-10", "12.15-34 ; 1 Tm 6.9-10");

		bookName = "";
		bookNum = 0;
		chapterNum = "";

		// On a ça par exemple: 1 Ch 13 ; 15 et 16

		String[] tabRef = references.split(";|et|comp.|, ");
		for (String ref: tabRef) {
			ref = ref.trim();
			System.out.println(ref);
			String[] tRef = ref.split(" "); // Pour pouvoir récupérer le nom du livre (et donc, son numéro)
			List<String> aRef = new ArrayList<>();
			aRef.addAll(Arrays.asList(tRef));

			if (result.length() > 0)
				result.append(getSeparator(references, ref));

			if (aRef.size() > 1)
				findNumBook(aRef);

			findNumChapter(aRef.get(0));
			addLink(result, aRef.remove(0));
		}

		return result.toString();
	}

	private static void findNumChapter(String reference) {

		if (Glossary.isSingleChapter(bookName)) {
			chapterNum = "1";
			return;
		}

		if (! reference.contains("."))
			return; // On est sur le meme chapitre

		StringBuilder ref = new StringBuilder(reference);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < ref.length(); i++) {
			char c = ref.charAt(i);
			if (c == '.' || c == ',')
				break;

			result.append(c);
		}

		chapterNum = result.toString();
	}

	private static void addLink(StringBuilder result, String reference) {

		String[] refTab = reference.split(",");
		for (String ref: refTab) {
			if (ref.startsWith("v."))
				ref = ref.replace("v.", ""); // Bug sur Jude. Dans 2 Pierre 3, on a la ref: "Jude v.17-23"

			if (! ref.contains(".")) {
				if (! "".equals(chapterNum)) // Si le numero de chapitre est vide, c'est que la ref est le chapitre
					ref = chapterNum + "." + ref;
//				else
//					ref += ".1-"; // On veut tout le chapitre...
			}

			result.append(getBibleLink(ref)).append(" ; ");
		}

		result.delete(result.length() - 3, result.length()); // On enleve le dernier " ; "
		System.out.println(result);
	}

	private static String getSeparator(String references, String ref) {

		for (String sep: separator) {
			if (references.contains(sep + " " + ref))
				return " " + sep + " ";
		}

		return "";
	}

	private static void findNumBook(List<String> ref) {

		bookName = ref.remove(0);
		if (bookName.startsWith("Comp."))
			bookName = ref.remove(0);

		if (Character.isDigit(bookName.charAt(0)))
			bookName += " " + ref.remove(0); // 1 P 1.10-21 (il faut aller jusqu'au P pour comprendre 1 Pierre)

		bookNum = Glossary.bookAbrevNumbers.get(bookName);
	}

	private static String getBibleLink(String ref) {

		// Format attendu: <em><RX 41.16.12-13></em>
		StringBuilder result = new StringBuilder("<em><RX ").
				append(bookNum).append(".").
				append(ref).
				append("></em>");

		return result.toString();
	}
}

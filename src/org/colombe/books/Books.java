package org.colombe.books;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Stack;

import org.colombe.glossaries.Glossary;
import org.colombe.utils.ReferenceUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class Books {

	private LinkedHashMap<String, LinkedHashSet<BookStructure>> books = new LinkedHashMap<String, LinkedHashSet<BookStructure>>();

	private String sourcePath;

	private int numBook;
	private int numChapter;
	private int numVerse;

	private boolean fromGlossary;

	private LinkedHashSet<BookStructure> data;
	private BookStructure oldBS = null;

	public Books(String sourcePath)
	{
		this.sourcePath = sourcePath;
		Glossary.setSourcePath(sourcePath);

		for (Entry<String, String> b: Glossary.abrev.entrySet())
			books.put(b.getKey(), null);

//		books.put("2Sam", null);
//		books.put("1Chr", null);
	}

	public LinkedHashMap<String, LinkedHashSet<BookStructure>> getBooks()
	{
		return books;
	}

	public void createBooks()
	{
		numBook = 0;
		for (Entry<String, LinkedHashSet<BookStructure>> e: books.entrySet()) {

			numBook++;
			data = new LinkedHashSet<BookStructure>();
			String book = e.getKey();
			createBook(book);
			e.setValue(data);
		}
	}

	private void createBook(String name)
	{
		File[] bookFiles = findXMLFiles(name);

		numChapter = 0;
		for (File fChapter: bookFiles) {

			numChapter++;
			System.out.println(fChapter);

			try {
				Document doc = Jsoup.parse(fChapter, "UTF-8");
				Elements verses = doc.select("p.p , div.lg");
				numVerse = 0;
				StringBuilder verse = new StringBuilder();
				Stack<String> subTitles = new Stack<>(); // Comme on récupere les sous-titres à l'envers, on les stocke dans une stack pour les récupérer à l'endroit
				for (Element xmlVerse: verses) {

					// Recherche des sous-titre(s) accolés au verset courant
					Element title = xmlVerse.previousElementSibling();
					boolean hasTitle = title != null && title.hasClass("title");
					while (hasTitle) {

						subTitles.push(title.toString());
						title = title.previousElementSibling(); // On recule d'un élément
						hasTitle = title != null && title.hasClass("title");
					}

					fromGlossary = false;
					for (Node child: xmlVerse.childNodes()) {

						String xml = child.toString();
						if (xml.startsWith("<div class=\"osis_l")) { // verset poetique, indenté dans le livre

							for (Node divChildren: child.childNodes()) {

								xml = divChildren.toString();
								decodeVerse(xml, subTitles, verse);
							}

							continue;
						}

						decodeVerse(xml, subTitles, verse);
					}

					printVerse(verse);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void addSpace(String s1, StringBuilder verse)
	{
		if (! s1.isEmpty() &&     	  // Gen 26.29: le mot du glossaire est le dernier mot du verset précédent
				verse.length() > 0) { // Nb  23.19: le mot du glossaire est le premier mot du verset courant.

			char c1 = s1.charAt(0);
			char c2 = verse.charAt(verse.length() -1);

			if ((Character.isLetter(c1) ||
					c1 == '(')          &&    // 3 Jn 1:7 : *Nom (du Seigneur  au lieu de  *Nom(du Seigneur
					Character.isLetter(c2)) {

				verse.append(" ");
			}
		}
	}

	private void decodeVerse(String xml, Stack<String> subTitles, StringBuilder verse)
	{
		boolean citation = xml.startsWith("<span class=\"otPassage\"");
		if (citation) {

			String sep = "";
			if (verse.length() > 0) {

				char c = verse.charAt(verse.length() -1);
				if (Character.isLetter(c))
					sep = " ";
			}

			verse.append(sep).append("<i>"); // On met les citations de l'ancien testament en italique
		} else if (xml.startsWith("<span class="))
			return;

		if (xml.startsWith("<a class=\"verse\"")) {

			// Cloture du verset précédent
			if (verse.length() > 0)
				printVerse(verse);

			numVerse++;
			return;
		}

		// insertion des sous-titres
		while (! subTitles.isEmpty()) {

			String title = subTitles.pop();
			boolean isReference = title.contains("<span class=\"reference\">");
			Document t = Jsoup.parse(title);
			title = isReference ? ReferenceUtil.decode(t.text()) : t.text();

			verse.append("<TS>").append(title).append("<Ts>");
		}

		Document doc = Jsoup.parse(xml);
		String text = doc.text();
		if (xml.startsWith("<a class=\"w-gloss\"") || fromGlossary) {

			if (! fromGlossary)
				text = Glossary.getGlossaryLink(xml, false);

			addSpace(text, verse);
			fromGlossary = ! fromGlossary;
		}

		verse.append(text);
		if (citation)
			verse.append("</i> ");
	}

	private void printVerse(StringBuilder verse)
	{
		if (oldBS == null || oldBS.getNumVerse() != numVerse) {

			BookStructure bs = new BookStructure(numBook, numChapter, numVerse, verse);
			data.add(bs);
			oldBS = bs;
		} else {

			// Suite du verset précédent
			verse.insert(0, oldBS.getVerse());
			oldBS.setVerse(verse);
		}

		verse.setLength(0);
	}

	/*
	 * Revoie la liste des fichiers XML d'un livre, trié par numéro de chapitre
	 */
	private File[] findXMLFiles(final String book)
	{
		File dir = new File(sourcePath);
		File[] result = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {

				return filename.startsWith(book + "-");
			}
		});

		Comparator<File> comparator = new Comparator<File>() {

			private Long getNumChapter(String value) {

				String s = value.split("-")[1]; // Pour éviter de prendre le 1 de 1Chr-12.xml par exemple
				return Long.valueOf(s.replaceAll("[^0-9]", "")); // Renvoie le numéro de chapitre du livre
			}

	        @Override
	        public int compare(File f1, File f2) {

	        	Long i1 = getNumChapter(f1.getName());
	        	Long i2 = getNumChapter(f2.getName());
	        	return i1.compareTo(i2);
	        }
	    };

		Arrays.sort(result, comparator);
		return result;
	}
}

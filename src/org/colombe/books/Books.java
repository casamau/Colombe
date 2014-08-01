package org.colombe.books;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.colombe.glossaries.Glossary;
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
/**/
		for (Entry<String, String> b: Glossary.abrev.entrySet())
			books.put(b.getKey(), null);

//		books.put("Gen", null);
	}

	public LinkedHashMap<String, LinkedHashSet<BookStructure>> getBooks()
	{
		return books;
	}

	public void createBooks()
	{
		numBook = 0;
		for (Entry<String, LinkedHashSet<BookStructure>> e: books.entrySet())
		{
			numBook++;
			String book = e.getKey();
			data = new LinkedHashSet<BookStructure>();
			createBook(book);
			e.setValue(data);
		}
	}

	private void createBook(String name)
	{
		File[] bookFiles = findXMLFiles(name);

		numChapter = 0;
		for (File f: bookFiles)
		{
			numChapter++;
			System.out.println(f);

			try {
				Document doc = Jsoup.parse(f, "UTF-8");
				Elements ps = doc.select("p.p , div.lg");
				numVerse = 0;
				StringBuilder verse = new StringBuilder();
				for (Element p: ps) {

					fromGlossary = false;
					for (Node child: p.childNodes()) {

						String s = child.toString();
						if (s.startsWith("<div class=\"osis_l")) {

							for (Node divChildren: child.childNodes()) {

								s = divChildren.toString();
								decodeVerse(s, verse);
							}

							continue;
						}

						decodeVerse(s, verse);
					}

					printVerse(verse.toString());
					verse.setLength(0);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

//			if (books.size() == 1)
//				break; // Code de débogage: si on ne génère qu'un seul livre, on s'arrete au premier chapitre
		}
	}

	private void addSpace(String s1, StringBuilder verse)
	{
		if (! s1.isEmpty() &&     	  // Gen 26.29: le mot du glossaire est le dernier mot du verset précédent
				  verse.length() > 0) // Nb  23.19: le mot du glossaire est le premier mot du verset courant.
		{
			char c1 = s1.charAt(0);
			char c2 = verse.charAt(verse.length() -1);

			if ((Character.isLetter(c1) ||
					c1 == '(') // 3 Jn 1:7 : *Nom (du Seigneur  au lieu de  *Nom(du Seigneur
					&& Character.isLetter(c2)) {
				verse.append(" ");
			}
		}
	}

	private void decodeVerse(String s, StringBuilder verse)
	{
		boolean citation = s.startsWith("<span class=\"otPassage\"");
		if (citation) {
			String sep = "";
			if (verse.length() > 0) {
				char c = verse.charAt(verse.length() -1);
				if (Character.isLetter(c))
					sep = " ";
			}

			verse.append(sep).append("<i>"); // On met les citations de l'ancien testament en italique
		} else if (s.startsWith("<span class="))
			return;

		if (s.startsWith("<a class=\"verse\"")) {

			// Cloture du verset précédent
			if (verse.length() > 0) {
				printVerse(verse.toString());
				verse.setLength(0);
			}

			numVerse++;
			return;
		}

		Document d = Jsoup.parse(s);
		String s1 = d.text();
		if (s.startsWith("<a class=\"w-gloss\"") || fromGlossary) {

			if (! fromGlossary)
				s1 = Glossary.getGlossaryLink(s, false);

			addSpace(s1, verse);
			fromGlossary = ! fromGlossary;
		}

		verse.append(s1);
		if (citation)
			verse.append("</i> ");
	}

	private void printVerse(String verse)
	{
		if (oldBS == null || oldBS.getNumVerse() != numVerse) {
			BookStructure b = new BookStructure(numBook, numChapter, numVerse, verse);
			data.add(b);
			oldBS = b;
		} else {
			// Suite du verset précédent
			StringBuilder old = new StringBuilder(oldBS.getVerse()).append(verse);
			oldBS.setVerse(old.toString());
		}
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

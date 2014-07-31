package org.colombe.books;

import java.util.ArrayList;
import java.util.List;


public class BookStructure {

	private int numBook;
	private int numChapter;
	private int numVerse;
	private String verse;

	public BookStructure(int numBook, int numChapter, int numVerse, String verse)
	{
		this.numBook = numBook;
		this.numChapter = numChapter;
		this.numVerse = numVerse;
		setVerse(verse);
	}

	public int getNumBook() {
		return numBook;
	}

	public int getNumChapter() {
		return numChapter;
	}

	public int getNumVerse() {
		return numVerse;
	}

	public String getVerse() {
		return verse;
	}

	private boolean isInValidCharacter(char c)
	{
		return ! ( Character.isWhitespace(c) ||
				   Character.isUpperCase(c)  ||
				   c == '’'  ||
				   c == '\'' ||
				   c == '<'  || //c == '>'  ||
				   c == '('  || c == ')'  ||
				   c == '['  || c == ']'  ||
				   c == '-'
				 );
	}

	private boolean intervallContains(List<int[]> ranges, int index) {

		int low; int high;

		for (int[] r : ranges) {
			low  = r[0];
			high = r[1];
			if (index >= low && index <= high)
				return true;
		}

	    return false;
	}

	private List<int[]> getBaliseInterval(String value)
	{
		ArrayList<int[]> result = new ArrayList<int[]>();
		int start = value.indexOf("<RF");
		while (start != -1) {
			int end = value.indexOf("<Rf>", start) + 4;
			result.add(new int[] { start, end });
			start = value.indexOf("<RF", end);
		}

		return result;
	}

	private String checkSpace(String value)
	{
		StringBuilder result = new StringBuilder();

		List<int[]> ignoreIndexes = getBaliseInterval(value);
		int lenI = value.length();
		for (int i = 0; i < lenI; i++) {
			char c = value.charAt(i);
			result.append(c);
			if (intervallContains(ignoreIndexes, i)) // On bypass tout ce qu'il y a entre les balises <RF= ...> et <Rf>, ce sont des notes du glossaire.
				continue;

			if (i < (lenI -1)) {
				// S'il y a un caractère après c

				char n = value.charAt(i +1);
				if (isInValidCharacter(c)) {
					if (Character.isUpperCase(n))
						result.append("<br>");
				}
			}
		}

		String r = result.toString().replace("<br><br>", "<br>");
		return r;
	}

	public void setVerse(String value) {
		verse = checkSpace(value);
	}

	@Override
	public String toString() {
		return numBook + " " + numChapter + "." + numVerse + " " + verse;
	}
}

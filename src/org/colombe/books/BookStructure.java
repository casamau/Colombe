package org.colombe.books;

import java.util.ArrayList;
import java.util.List;


public class BookStructure {

	private int numBook;
	private int numChapter;
	private int numVerse;
	private String verse;

	public BookStructure(int numBook, int numChapter, int numVerse, StringBuilder verse)
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
				   c == '<'  || //c == '>'   ||
				   c == '('  || c == ')'     ||
				   c == '['  || c == ']'     ||
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

	private List<int[]> getBaliseInterval(String key, StringBuilder value)
	{
		char[] cKey = key.toCharArray();
		String startKey = "<" + key; // <RF
		String endKey   = "<" + cKey[0] + Character.toLowerCase(cKey[1]) + ">"; // <Rf>
		ArrayList<int[]> result = new ArrayList<int[]>();
		int start = value.indexOf(startKey);
		while (start != -1) {

			int end = value.indexOf(endKey, start) + endKey.length();
			result.add(new int[] { start, end });
			start = value.indexOf(startKey, end);
		}

		return result;
	}

	private String checkSpace(StringBuilder value)
	{
		StringBuilder result = new StringBuilder();

		List<int[]> ignoreIndexes = getBaliseInterval("RF", value); // Balises des liens glossaires
		ignoreIndexes.addAll(getBaliseInterval("TS", value)); // Balise des sous-titres
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

		return result.toString().replace("<br><br>", "<br>");
	}

	public void setVerse(StringBuilder value) {
		verse = checkSpace(value);
	}

	@Override
	public String toString() {
		return numBook + " " + numChapter + "." + numVerse + " " + verse;
	}
}

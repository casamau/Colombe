package org.colombe;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.colombe.utils.BibleCreator;

public class CreateColombe {

	private static String bibleName = "colombe.bbl.mybible";

	public static void main(String[] args) {

		/* Chemin des fichiers
		 *
		 * arg0: fichier epub
		 * arg1: fichier destination (optionnel, colombe.bbl.mybible par defaut)
		 */

		if (args.length != 1) {
			System.out.println("Usage: ");
			System.out.println("CreateColombe <fichier_epub>");

			System.exit(-1);
		}

		File epub = new File(args[0]);
		if (! epub.exists()) {
			System.out.println("Le fichier epub n'existe pas");
			System.exit(-1);
		}

		bibleName = System.getenv("HOME") + File.separatorChar + bibleName;
		File bible = new File(bibleName);
		if (bible.exists())
			bible.delete();

		Path tempDir = null;
		try {
			tempDir = Files.createTempDirectory("epub");
			unzip(epub.getAbsolutePath(), tempDir.toString());

			BibleCreator bc = new BibleCreator(tempDir.toString(), bibleName);
			bc.createBible();
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			removeRecursive(tempDir);
		}

		System.exit(0);
	}

	public static void unzip(String source, String destination)
	{
	    try {
	         ZipFile zipFile = new ZipFile(source);
	         zipFile.extractAll(destination);
	    } catch (ZipException e) {
	        e.printStackTrace();
	    }
	}

	public static void removeRecursive(Path path)
	{
		try {
		    Files.walkFileTree(path, new SimpleFileVisitor<Path>()
		    {
		        @Override
		        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		                throws IOException
		        {
		            Files.delete(file);
		            return FileVisitResult.CONTINUE;
		        }

		        @Override
		        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
		        {
		            // try to delete the file anyway, even if its attributes
		            // could not be read, since delete-only access is
		            // theoretically possible
		            Files.delete(file);
		            return FileVisitResult.CONTINUE;
		        }

		        @Override
		        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
		        {
		            if (exc == null)
		            {
		                Files.delete(dir);
		                return FileVisitResult.CONTINUE;
		            }
		            else
		            {
		                // directory iteration failed; propagate exception
		                throw exc;
		            }
		        }
		    });
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
}

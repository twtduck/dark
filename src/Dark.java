import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Dark {
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		Scanner in = new Scanner(System.in);

		// Get input folder path
		System.out.println("Enter input file folder absolute path: ");
		String folderPath = in.nextLine();
		System.out.print("Checking valid file path... ");
		File inputFolder = new File(folderPath);
		if (inputFolder.exists() && inputFolder.isDirectory()) {
			System.out.println("yes");
		} else {
			System.out.println("no");
			System.out
					.println("Invalid path or path is not a directory. Exiting now.");
			System.exit(-1);
		}

		// Get output file path
		System.out.println("Enter output csv file absolute path: ");
		String csvPath = in.nextLine();
		System.out.print("Checking valid file path... ");
		File csvFile = new File(csvPath);
		if (!csvFile.exists() && csvFile.getParentFile().exists()
				&& csvFile.getParentFile().isDirectory()) {
			System.out.println("yes");
		} else {
			System.out.println("no");
			System.out.println("Invalid csv file path. Exiting now.");
			System.exit(-1);
		}

		// Get recursive search setting
		boolean recursiveSearch = false;
		boolean retry = false;
		do {
			System.out.println("Do you want to search subfolders? [y/n]");

			String parseBool = in.nextLine();
			if (parseBool.toLowerCase().equals("y")
					|| parseBool.toLowerCase().equals("yes")) {
				retry = false;
				recursiveSearch = true;
			} else if (parseBool.toLowerCase().equals("n")
					|| parseBool.toLowerCase().equals("no")) {
				retry = false;
				recursiveSearch = false;
			} else {
				System.out.println("Invalid answer. Try again.");
				retry = true;
			}
		} while (retry);

		// Start csv doc
		PrintWriter out = new PrintWriter(csvFile, "UTF-8");
		out.print("Threshold,");
		for (int threshold = 0; threshold <= 255; threshold += 1) {
			out.print(threshold + ",");
		}
		out.println();

		// Search for files, and add them to an ArrayList<File> and
		// corresponding ArrayList<String> for paths
		String prefix = "";
		ArrayList<File> scanFiles = new ArrayList<File>();
		ArrayList<String> filePaths = new ArrayList<String>();
		scanDirectory(inputFolder, prefix, scanFiles, filePaths,
				recursiveSearch);

		for (int i = 0; i < scanFiles.size(); i++) {
			File imgFile = scanFiles.get(i);
			String imgPath = filePaths.get(i);
			out.print(imgPath + ",");
			BufferedImage img = null;
			try {
				img = ImageIO.read(imgFile);
				System.out.println("Reading " + imgFile.getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int totalPixels = img.getWidth() * img.getHeight();

			for (int threshold = 0; threshold <= 255; threshold += 1) {
				float imageValue;
				int passedPixels = 0;
				for (int y = 0; y < img.getHeight(); y++) {
					for (int x = 0; x < img.getWidth(); x++) {
						Color pixelColor = new Color(img.getRGB(x, y));
						int pixelValue = (int) (0.2126 * pixelColor.getRed()
								+ 0.7152 * pixelColor.getGreen() + 0.0722 * pixelColor
								.getBlue());
						if (pixelValue < threshold)
							passedPixels++;

					}
				}
				imageValue = ((float) passedPixels) / ((float) totalPixels);
				out.print(imageValue + ",");
				System.out.println("Image " + (i+1) + " of " + scanFiles.size()
						+ ": Threshold " + threshold + "/255");
			}
			out.println();
			img.flush();

		}

		out.close();
		in.close();
	}

	public static void scanDirectory(File directory, String prefix,
			ArrayList<File> scanFiles, ArrayList<String> filePaths,
			boolean recursive) {
		File[] files = directory.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				String newPrefix = prefix;
				newPrefix += f.getName();
				if (recursive)
					scanDirectory(f, newPrefix, scanFiles, filePaths, recursive);
			} else {
				if(f.getName().endsWith(".jpg")) {
					scanFiles.add(f);
					filePaths.add(prefix + "/" + f.getName());
				}
			}
		}
	}
}

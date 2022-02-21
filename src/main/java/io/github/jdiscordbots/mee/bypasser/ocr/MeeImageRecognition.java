package io.github.jdiscordbots.mee.bypasser.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;

public class MeeImageRecognition {
	private static final Tesseract tesseract = new Tesseract();

	static {
		File trainingPath = new File(".tesseract");

		trainingPath.mkdirs();
		File trainingDataFile = new File(trainingPath, "eng.traineddata");
		if(!trainingDataFile.exists()) {
			try(BufferedInputStream bis = new BufferedInputStream(new URL("https://github.com/tesseract-ocr/tessdata/blob/main/eng.traineddata?raw=true").openStream())) {
				Files.copy(bis, trainingDataFile.toPath());
			} catch(IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		tesseract.setDatapath(trainingPath.getAbsolutePath());
		tesseract.setVariable("tessedit_char_whitelist", "RANK#LEV0123456789");
		tesseract.setVariable("user_defined_dpi", "300");

	}

	public static LevelInfo loadDataFromImage(InputStream imageInput) throws IOException, TesseractException {
		BufferedImage img = ImageIO.read(imageInput);
		return new LevelInfo(loadRankTextFromImage(img), loadDiscrimminatorFromImage(img));
	}

	private static int loadDiscrimminatorFromImage(BufferedImage img) throws TesseractException {
		String seperator = "#";
		Rectangle rect = new Rectangle(252, 122, img.getWidth() - 252 - 45, 75);
		return loadIntFromImage(img, seperator, rect);
	}

	private static int loadRankTextFromImage(BufferedImage img) throws TesseractException {
		String prelevel = "LEVEL";
		Rectangle rect = new Rectangle(569, 0, img.getWidth() - 569, 118);
		return loadIntFromImage(img, prelevel, rect);
	}

	private static int loadIntFromImage(BufferedImage img, String seperator, Rectangle rect) throws TesseractException {
		String infoText = tesseract.doOCR(img, rect);
		int levelIndex = infoText.indexOf(seperator);
		if(levelIndex == -1) {
			return -1;
		} else {
			try {
				int beginIndex = levelIndex + seperator.length();
				return Integer.parseInt(infoText.substring(beginIndex, Math.min(beginIndex + 4, infoText.length())).replace(" ", "").trim());
			} catch(NumberFormatException e) {
				return -1;
			}
		}
	}

	public static class LevelInfo {
		private final int level;
		private final int discrimminator;

		public LevelInfo(int level, int discrimminator) {
			this.level = level;
			this.discrimminator = discrimminator;
		}

		public int getLevel() {
			return level;
		}

		public int getDiscrimminator() {
			return discrimminator;
		}
	}
}

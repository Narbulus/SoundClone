package main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.imageio.ImageIO;

import GsonObjects.Configuration;
import GsonObjects.TrackInfo;

import com.mpatric.mp3agic.ID3Wrapper;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v23Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

/**
 * Handles the low-level downloading of song files. Also
 * updates the mp3 file tag with song information and 
 * album artwork.
 * @author Spencer Anderson
 *
 */
public class Mp3Downloader {

	private ID3v2 template;
	private Configuration config;
	private String tempDir;

	/**
	 * Constructs a new mp3 downloader based on the given user
	 * configuration, mp3 tag template, and temporary download
	 * path directory.
	 * @param template The template mp3 file tag to base new tags off of.
	 * @param config The current session's configuration
	 * @param tempDir The directory to download the song files to temporarily
	 */
	public Mp3Downloader(ID3v2 template, Configuration config, String tempDir) {
		this.template = template;
		this.config = config;
		this.tempDir = tempDir;
	}

	/**
	 * Takes media locations and track information and downloads the mp3 file to
	 * a temporary location. Formats the resulting mp3 file and embeds track
	 * information and album artwork before saving to the specified download
	 * path.
	 * 
	 * @param mediaPath
	 *            - url path of the mp3 file to be downloaded
	 * @param downloadPath
	 *            - desination of the formatted mp3 filed
	 * @param title
	 *            - title of the track
	 * @param artworkPath
	 *            - url path of the artwork jpg to be downloaded
	 * @return Whether or not the download was successful
	 * @throws IOException
	 * @throws UnsupportedTagException
	 * @throws InvalidDataException
	 * @throws NotSupportedException
	 * @throws InvalidAudioFrameException
	 * @throws ReadOnlyFileException
	 * @throws TagException
	 * @throws CannotReadException
	 * @throws CannotWriteException
	 */
	public boolean generateMp3(String mediaPath, TrackInfo track)
			throws IOException, UnsupportedTagException, InvalidDataException,
			NotSupportedException {

		if (mediaPath != null) {

			// Download the mp3 file
			URL website = new URL(mediaPath);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			String title = track.getTitle();

			// Remove special, code breaking characters from the track title
			String fuzzTitle = track.getTitle();
			fuzzTitle = fuzzTitle.replaceAll("[<>?*:|/\\\\]", " ");
			fuzzTitle = fuzzTitle.replaceAll("\"", "'");
			
			// Generate the temporary and final song download directory paths
			String tempPath = tempDir + "/" + fuzzTitle + ".mp3";
			String finalPath = config.getDownloadPath() + "/"
					+ config.getUsername() + "/" + fuzzTitle + ".mp3";
			File finalDir = new File(finalPath);
			finalDir.getParentFile().mkdirs();
			
			// Read the file from the website stream into the temporary path
			FileOutputStream fos = new FileOutputStream(tempPath);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();

			File f = new File(tempPath);
			
			// Open the file as an Mp3File object for tag editing
			Mp3File mp3file = new Mp3File(tempPath);
			mp3file.setId3v2Tag(template);
			ID3v2 id3v2Tag = mp3file.getId3v2Tag();

			// If the file name has a parseable title and artist,
			// update tag with new info
			if (title.contains(" - ")) {
				String[] halves = title.split(" - ");
				if (halves.length == 2) {
					id3v2Tag.setArtist(halves[0]);
					id3v2Tag.setTitle(halves[1]);
				}
			}

			// If the track has artwork, embed it in the Mp3
			if (track.getArtworkURL() != null) {
				URL artworkURL = new URL(track.getArtworkURL().replace("large",
						"t500x500"));

				BufferedImage image = ImageIO.read(artworkURL.openStream());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write(image, "jpg", out);
				out.flush();
				byte[] bytes = out.toByteArray();
				out.close();

				ID3Wrapper newId3Wrapper = new ID3Wrapper(new ID3v1Tag(),
						new ID3v23Tag());
				newId3Wrapper.setAlbumImage(bytes, "image/jpeg");
				id3v2Tag.setAlbumImage(bytes, 2, "image/jpeg");
			}

			mp3file.save(finalPath);
			f.delete();

			return true;
		}
		return false;
	}

}

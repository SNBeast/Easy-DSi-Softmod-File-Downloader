/*
	Copyright (C) 2020 SNBeast 

	You should have received a copy of the GNU General Public License
	along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package gfx;

import java.awt.Dimension;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FileUtils;

import net.FileRetriever;
import net.GithubLatestRetriever;

public class Window {
	private JFileChooser chooser = new JFileChooser("Select your SD card's root");
	private JProgressBar downloadBar = new JProgressBar(0, 0);
	private JFrame frame = new JFrame("Downloading...");
	
	public static final String[] staticFiles = new String[] {"pitOther.bin", "pit4.bin", "dumpTool.nds", "UNLAUNCH.DSI"};
	public static final String[] staticNames = new String[] {"pit.bin", "pit.bin", null, null};
	public static final String[] staticLocations = new String[] {"private/ds/app/484E494A/", "", ""}; //only one pit because the section that parses this will only know of one
	public static final Object[][] githubFiles = new Object[][] {{"https://api.github.com/repos/DS-Homebrew/TWiLightMenu/releases/latest", 0}};
	public static final String[] githubNames = new String[] {"twilightmenu.7z"};
	
	public static final String[] twilightMenuDirectories = new String[] {"DSi&3DS - SD card users/_nds", "DSi - CFW users/SDNAND root/hiya", "DSi - CFW users/SDNAND root/title", "_nds", "roms"};
	public static final String[] twilightMenuFiles = new String[] {"DSi&3DS - SD card users/BOOT.NDS"};
	
	public Window () { //i hope you like messy monolithic programs
		downloadBar.setPreferredSize(new Dimension(400, 50));
		downloadBar.setStringPainted(true);
		frame.add(downloadBar);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JOptionPane.showMessageDialog(null,
				"Easy DSi Softmod File Downloader, Copyright (C) 2020 SNBeast\n"
				+ "This program's license is in LICENSE.txt, bundled with this program.\n\n"
				+ "None of the files downloaded here are authored by me.");
		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File sd = getFolder();
		
		int region = JOptionPane.showOptionDialog(null, "Is your DSi Korean/Chinese region?", "Select DSi region", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		int ignore = 1;
		if (region == 1) {
			ignore = JOptionPane.showOptionDialog(null, "Does your DSi have a firmware version of at least 1.4?", "Select DSi firmware", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		}
		else if (region == -1) System.exit(0); //-1 being an error (probably clicking the close button)
		
		int unlaunch = JOptionPane.showOptionDialog(null, "Do you want to also download Unlaunch?", "Also include Unlaunch?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (unlaunch == -1) System.exit(0);
		
		int twilight = JOptionPane.showOptionDialog(null, "Do you want to also download and extract TWiLightMenu?", "Also include TWiLightMenu?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (twilight == -1) System.exit(0);
		
		downloadBar.setMaximum(staticFiles.length + githubFiles.length - twilight - unlaunch - 1); //minus one copy of pit.bin, and optionally minus twilightmenu and unlaunch
		frame.setVisible(true);
		
		ArrayList<File> files = new ArrayList<File>();
		String name;
		for (int i = 0; i < staticFiles.length; i++) {
			if (i != ignore && (i != staticFiles.length - 1 || unlaunch == 0)) {
				name = staticNames[i] == null ? staticFiles[i] : staticNames[i];
				downloadBar.setString("Downloading " + name);
				files.add(FileRetriever.getNetFile("https://github.com/SNBeast/Easy-DSi-Softmod-File-Downloader/raw/master/rsc/" + staticFiles[i], name));
				downloadBar.setValue(downloadBar.getValue() + 1);
			}
		}
		
		File twilightMenu = null;
		for (int i = 0; i < githubFiles.length; i++) {
			if (twilight == 0 || i != 0) {
				name = githubNames[i];
				downloadBar.setString("Downloading " + name);
				if (i == 0) {
					twilightMenu = GithubLatestRetriever.getReleaseFile((String)githubFiles[i][0], name, ((Integer)githubFiles[i][1]).intValue());
				}
				downloadBar.setValue(downloadBar.getValue() + 1);
			}
		}
		
		if (twilightMenu != null) {
			try {
				File targetDir = new File("twilightmenu");
				frame.setTitle("Extracting...");
				downloadBar.setValue(0);
				downloadBar.setMaximum(1);
				downloadBar.setString("Extracting TWiLight Menu++");
				if (!targetDir.isDirectory()) {
					targetDir.mkdirs();
				}
				SevenZFile archive = new SevenZFile(twilightMenu);
				SevenZArchiveEntry entry = null;
				byte[] content;
				while ((entry = archive.getNextEntry()) != null) {
					name = targetDir.getName() + "/" + entry.getName();
					File f = new File(name);
					if (entry.isDirectory()) {
						if (!f.isDirectory()) {
							f.mkdirs();
						}
					}
					else {
						File parent = f.getParentFile();
						if (!parent.isDirectory()) {
							parent.mkdirs();
						}
						if (f.exists()) f.delete();
						f.createNewFile();
						content = new byte[(int)entry.getSize()];
						archive.read(content);
						Files.write(f.toPath(), content);
					}
				}
				downloadBar.setValue(1);
				archive.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "An error occurred during the extraction of TWiLight Menu. Report this issue immediately.", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(4);
			}

			frame.setTitle("Copying...");
			
			downloadBar.setValue(0);
			downloadBar.setMaximum(twilightMenuDirectories.length + twilightMenuFiles.length);
			try {
				for (String path : twilightMenuDirectories) {
					if (path.equals("_nds")) {
						downloadBar.setString("Copying " + path + " (this may take a while)");
					}
					else {
						downloadBar.setString("Copying " + path);
					}
					FileUtils.copyDirectory(new File("twilightmenu/" + path), new File(sd.getAbsolutePath() + "/" + new File(path).getName()));
					downloadBar.setValue(downloadBar.getValue() + 1);
				}
				for (String file : twilightMenuFiles) {
					downloadBar.setString("Copying " + file);
					FileUtils.copyFile(new File("twilightmenu/" + file), new File(sd.getAbsolutePath() + "/" + new File(file).getName()));
					downloadBar.setValue(downloadBar.getValue() + 1);
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "An error occurred while copying TWiLight Menu. Check if the SD is still mounted. Otherwise, report this issue immediately.", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(5);
			}
		}
		frame.setTitle("Copying...");
		downloadBar.setValue(0);
		downloadBar.setMaximum(files.size());
		try {
			for (int i = 0; i < files.size(); i++) {
				File f = files.get(i);
				downloadBar.setString("Copying " + f.getName());
				if (!staticLocations[i].equals("")) {
					new File(sd.getAbsolutePath() + "/" + staticLocations[i]).mkdirs();
				}
				Path destination = Paths.get(sd.getAbsolutePath() + "/" + staticLocations[i] + f.getName());
				Path source = f.toPath();
				Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
				downloadBar.setValue(downloadBar.getValue() + 1);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "An error occurred during file copy. Check that the SD is still mounted. Otherwise, report this issue immediately.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(3);
		}
		frame.setVisible(false);
		JOptionPane.showMessageDialog(null, "Completed successfully!");
		System.exit(0);
	}
	private File getFolder () {
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if (!f.isDirectory()) {
				JOptionPane.showMessageDialog(null, "Selected file is not a folder or drive", "Error", JOptionPane.ERROR_MESSAGE);
				return getFolder();
			}
			if (!f.exists()) {
				JOptionPane.showMessageDialog(null, "Selected location does not exist", "Error", JOptionPane.ERROR_MESSAGE);
				return getFolder();
			}
			return f;
		}
		else if (returnVal == JFileChooser.CANCEL_OPTION) {
			System.exit(0);
			return null; //apparently java's compiler isn't smart enough to realize that exit terminates
		}
		else {
			JOptionPane.showMessageDialog(null, "An unknown error occurred.", "Error", JOptionPane.ERROR_MESSAGE); //idk when this'd happen, maybe a cli-only environment?
			System.exit(1);
			return null;
		}
	}
}

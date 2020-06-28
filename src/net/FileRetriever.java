/*
	Copyright (C) 2020 SNBeast 

	You should have received a copy of the GNU General Public License
	along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package net;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.JOptionPane;

public abstract class FileRetriever {
	public static File getNetFile (String url, String name) {
		try {
			InputStream in = new URL(url).openStream();
			Files.copy(in, Paths.get(name), StandardCopyOption.REPLACE_EXISTING);
			return new File(name);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "An error occurred during file download. Check your internet condition.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
			return null;
		}
	}
}

/*
	Copyright (C) 2020 SNBeast 

	You should have received a copy of the GNU General Public License
	along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package net;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public abstract class GithubLatestRetriever {
	public static File getReleaseFile (String url, String name, int index) {
		File json = FileRetriever.getNetFile(url, "release.json");
		try {
			JSONObject jo = (JSONObject)new JSONParser().parse(new FileReader(json));
			JSONArray assets = (JSONArray)jo.get("assets");
			@SuppressWarnings("unchecked")
			String location = (String)((Map<String, Object>)assets.get(index)).get("browser_download_url");
			json.delete();
			return FileRetriever.getNetFile(location, name);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "An error occurred during JSON decoding. Report this issue immediately.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(3);
			return null;
		}
	}
}

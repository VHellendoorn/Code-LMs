package org.microg.networklocation.backends.opencellid;

import android.content.Context;
import android.util.Log;

import org.microg.networklocation.data.CellSpec;
import org.microg.networklocation.data.LocationSpec;
import org.microg.networklocation.helper.Networking;
import org.microg.networklocation.source.LocationSource;
import org.microg.networklocation.source.OnlineDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class OpenCellIdLocationSource extends OnlineDataSource implements LocationSource<CellSpec> {

	private static final String TAG = "nlp.OpenCellIdLocationSource";
	private static final String NAME = "opencellid.org";
	private static final String DESCRIPTION = "Retrieve cell locations from opencellid.org when online";
	private static final String COPYRIGHT = "© OpenCellID.org\nLicense: CC BY-SA 3.0";
	private static final String SERVICE_URL =
			"http://www.opencellid.org/cell/get?fmt=xml&mcc=%d&mnc=%d&lac=%d&cellid=%d&key=%s";
	private final Context context;
	private final String API_KEY = "36e1c4ff-fb91-46d8-b9ff-c480c35292e5";
	
	public OpenCellIdLocationSource(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Collection<LocationSpec<CellSpec>> retrieveLocation(Collection<CellSpec> cellSpecs) {
		Collection<LocationSpec<CellSpec>> locationSpecs = new ArrayList<LocationSpec<CellSpec>>();
		for (CellSpec cellSpec : cellSpecs) {
			try {
				URL url = new URL(String.format(SERVICE_URL, cellSpec.getMcc(), cellSpec.getMnc(), cellSpec.getLac(),
												cellSpec.getCid(), API_KEY));
				URLConnection connection = url.openConnection();
				Networking.setUserAgentOnConnection(connection, context);
				connection.setDoInput(true);
				InputStream inputStream = connection.getInputStream();
				String result = new String(Networking.readStreamToEnd(inputStream));
				
				if ((result == null) || !result.isEmpty()) {
					// Add xml tag because it's sometimes not includes in the returned result
					if(!result.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
						result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + result;
					}
					LocationSpec<CellSpec> data = parseXMLData(cellSpec, result);
					if(data != null) {
						locationSpecs.add(data);
					}
				}
			} catch (Throwable t) {
				Log.w(TAG, t);
			}
		}
		return locationSpecs;
	}
		
	private LocationSpec<CellSpec> parseXMLData(CellSpec cellSpec, String data) throws ParserConfigurationException, SAXException, IOException{		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(data)));

		NodeList nodeList = doc.getElementsByTagName("cell");
		if(nodeList.getLength() >= 1){
			Node node = nodeList.item(0);
			NamedNodeMap attributes = node.getAttributes();
			return new LocationSpec<CellSpec>(cellSpec,
					Double.parseDouble(attributes.getNamedItem("lat").getNodeValue()),
					Double.parseDouble(attributes.getNamedItem("lon").getNodeValue()),
					Double.parseDouble(attributes.getNamedItem("range").getNodeValue()));
		}else{
			return null;
		}		
	}
}

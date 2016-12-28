import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

/**
 * Created by jorgen on 22.12.16.
 */
public class XMLUtil {
    static String documentToText(Document doc) {
        Writer out = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(out, null);

        try {
            serializer.serialize(doc);
        } catch (IOException e) {
            System.err.println("Error serializing document");
            e.printStackTrace();
        }

        return out.toString();
    }

    static String getResultOf(String xml) {
        String result = null;

        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = bf.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            Node params = doc.getElementsByTagName("param").item(0);
            result = params.getFirstChild().getTextContent();
        } catch (Exception e) {
            System.err.println("XML parse error: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}

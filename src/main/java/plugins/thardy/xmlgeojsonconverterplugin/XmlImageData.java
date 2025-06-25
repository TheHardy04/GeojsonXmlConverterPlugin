package plugins.thardy.xmlgeojsonconverterplugin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the XML structure for image data, including metadata and regions of interest (ROIs).
 * It provides methods to read from an XML file and write to an XML file.
 *
 * @author thardy
 */

public class XmlImageData {
    String name;
    Meta meta;
    List<Roi> rois;

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }
    public List<Roi> getRois() { return rois; }
    public void setRois(List<Roi> rois) { this.rois = rois; }

    static String indent(int lvl)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lvl; i++) {
            sb.append("  "); // 2 spaces for each level of indentation
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("XmlImageData: \n");
        sb.append("Name: ").append(name).append("\n");
        if (meta != null) {
            sb.append("Meta: ").append(meta.toString());
        } else {
            sb.append("Meta: null\n");
        }
        return sb.toString();
    }

    public String roisString(int n) {
        StringBuilder sb = new StringBuilder();
        if (rois == null || rois.isEmpty()) {
            sb.append("No ROIs found.\n");
        } else {
            sb.append("ROIs:\n");
            for (int i = 0; i < Math.min(n, rois.size()); i++) {
                sb.append(rois.get(i).toString()).append("\n");
            }
            if (rois.size() > n) {
                sb.append("... and ").append(rois.size() - n).append(" more ROIs.\n");
            }
        }
        return sb.toString();
    }

    /**
     * Utility method to get the text content of an element by tag name.
     * @param parent the parent element
     * @param tagName the tag name to search for
     * @return the text content of the first matching element, or an empty string if not found
     */
    private static String getElementTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return "";
    }

    /**
     * Utility method to create an element with text content.
     * @param doc the document to create the element in
     * @param parentElement the parent element to append the new element to
     * @param tagName the tag name of the new element
     * @param textContent the text content of the new element
     */
    private static void createElementWithTextContent(Document doc, Element parentElement, String tagName, String textContent) {
        if (textContent != null) {
            Element element = doc.createElement(tagName);
            element.appendChild(doc.createTextNode(textContent));
            parentElement.appendChild(element);
        }
    }

    /**
     * Writes the XmlImageData object to an XML file.
     * @param filePath the path to the XML file to be created/overwritten.
     * @throws IOException if an error occurs while writing the file.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created.
     * @throws TransformerException if an error occurs during the transformation process.
     */
    public void toXmlFile(String filePath) throws IOException, ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // Root element <image>
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("root");
        doc.appendChild(rootElement);

        // <name> element
        if (this.name != null) {
            createElementWithTextContent(doc, rootElement, "name", this.name);
        }

        // <meta> element
        if (this.meta != null) {
            rootElement.appendChild(this.meta.toXmlElement(doc));
        }

        // <rois> element
        // If meta exists add <rois> element
        // It not just add the <roi> elements
        if (this.rois != null && !this.rois.isEmpty()) {
            if(this.meta != null)
            {
                for(Roi roi : this.rois) {
                    rootElement.appendChild(roi.toXmlElement(doc));
                }
            }
            else {
                Element roisElement = doc.createElement("rois");
                rootElement.appendChild(roisElement);
                for (Roi roi : this.rois) {
                    roisElement.appendChild(roi.toXmlElement(doc));
                }
            }
        }

        // Write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // Indentation size
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));

        transformer.transform(source, result);
    }

    /**
     * Reads an XML file and returns an XmlImageData object.
     * @param filePath the path to the XML file
     * @return XmlImageData object containing the parsed data
     * @throws IOException if an error occurs while reading the file
     */
    public static XmlImageData fromXml(String filePath) throws IOException
    {
        try{
            File file = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            XmlImageData xmlImageData = new XmlImageData();

            // Get the root element
            Element root = doc.getDocumentElement();
            xmlImageData.setName(getElementTextContent(root, "name"));

            // Parse the meta data
            NodeList metaNodes = root.getElementsByTagName("meta");
            if (metaNodes.getLength() > 0) {
                Node metaNode = metaNodes.item(0);
                if (metaNode.getNodeType() == Node.ELEMENT_NODE) {
                    Meta meta = Meta.getMeta((Element) metaNode);
                    xmlImageData.setMeta(meta);
                }
            }

            // Parse the ROIs
            NodeList roiNodes = root.getElementsByTagName("rois");
            if( roiNodes.getLength() > 0){
                Node roiNode = roiNodes.item(0);
                if (roiNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element roiElement = (Element) roiNode;
                    NodeList roiList = roiElement.getElementsByTagName("roi");
                    xmlImageData.rois = new ArrayList<>();
                    for (int i = 0; i < roiList.getLength(); i++) {

                        Element roiElem = (Element) roiList.item(i);
                        Roi roi = Roi.getRoi(roiElem);
                        xmlImageData.rois.add(roi);
                    }
                }
            }

            return xmlImageData;

        } catch (Exception e) {
            throw new IOException("Error parsing XML file: " + e.getMessage(), e);
        }
    }



    public static class Meta
    {
        private double positionX;
        private double positionY;
        private double positionZ;
        private double positionT;
        private double pixelSizeX;
        private double pixelSizeY;
        private double pixelSizeZ;
        private double timeInterval;
        private String ChannelName0;
        private String ChannelName1;
        private String ChannelName2;
        private String userName;

        public double getPositionX() { return positionX; }
        public void setPositionX(double positionX) { this.positionX = positionX; }
        public double getPositionY() { return positionY; }
        public void setPositionY(double positionY) { this.positionY = positionY; }
        public double getPositionZ() { return positionZ; }
        public void setPositionZ(double positionZ) { this.positionZ = positionZ; }
        public double getPositionT() { return positionT; }
        public void setPositionT(double positionT) { this.positionT = positionT; }
        public double getPixelSizeX() { return pixelSizeX; }
        public void setPixelSizeX(double pixelSizeX) { this.pixelSizeX = pixelSizeX; }
        public double getPixelSizeY() { return pixelSizeY; }
        public void setPixelSizeY(double pixelSizeY) { this.pixelSizeY = pixelSizeY; }
        public double getPixelSizeZ() { return pixelSizeZ; }
        public void setPixelSizeZ(double pixelSizeZ) { this.pixelSizeZ = pixelSizeZ; }
        public double getTimeInterval() { return timeInterval; }
        public void setTimeInterval(double timeInterval) { this.timeInterval = timeInterval; }
        public String getChannelName0() { return ChannelName0; }
        public void setChannelName0(String channelName0) { ChannelName0 = channelName0; }
        public String getChannelName1() { return ChannelName1; }
        public void setChannelName1(String channelName1) { ChannelName1 = channelName1; }
        public String getChannelName2() { return ChannelName2; }
        public void setChannelName2(String channelName2) { ChannelName2 = channelName2; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public static Meta getMeta(Element metaNode) {
            Element metaElement = metaNode;
            Meta meta = new Meta();
            meta.setPositionX(Double.parseDouble(getElementTextContent(metaElement, "positionX")));
            meta.setPositionY(Double.parseDouble(getElementTextContent(metaElement, "positionY")));
            meta.setPositionZ(Double.parseDouble(getElementTextContent(metaElement, "positionZ")));
            meta.setPositionT(Double.parseDouble(getElementTextContent(metaElement, "positionT")));
            meta.setPixelSizeX(Double.parseDouble(getElementTextContent(metaElement, "pixelSizeX")));
            meta.setPixelSizeY(Double.parseDouble(getElementTextContent(metaElement, "pixelSizeY")));
            meta.setPixelSizeZ(Double.parseDouble(getElementTextContent(metaElement, "pixelSizeZ")));
            meta.setTimeInterval(Double.parseDouble(getElementTextContent(metaElement, "timeInterval")));
            meta.setChannelName0(getElementTextContent(metaElement, "channelName0"));
            meta.setChannelName1(getElementTextContent(metaElement, "channelName1"));
            meta.setChannelName2(getElementTextContent(metaElement, "channelName2"));
            meta.setUserName(getElementTextContent(metaElement, "userName"));
            return meta;
        }

        /**
         * Converts the Meta object to an XML Element.
         * @param doc The document to create the element in.
         * @return The XML Element representing this Meta object.
         */
        public Element toXmlElement(Document doc) {
            Element metaElement = doc.createElement("meta");
            createElementWithTextContent(doc, metaElement, "positionX", String.valueOf(this.positionX));
            createElementWithTextContent(doc, metaElement, "positionY", String.valueOf(this.positionY));
            createElementWithTextContent(doc, metaElement, "positionZ", String.valueOf(this.positionZ));
            createElementWithTextContent(doc, metaElement, "positionT", String.valueOf(this.positionT));
            createElementWithTextContent(doc, metaElement, "pixelSizeX", String.valueOf(this.pixelSizeX));
            createElementWithTextContent(doc, metaElement, "pixelSizeY", String.valueOf(this.pixelSizeY));
            createElementWithTextContent(doc, metaElement, "pixelSizeZ", String.valueOf(this.pixelSizeZ));
            createElementWithTextContent(doc, metaElement, "timeInterval", String.valueOf(this.timeInterval));
            if (this.ChannelName0 != null) createElementWithTextContent(doc, metaElement, "channelName0", this.ChannelName0);
            if (this.ChannelName1 != null) createElementWithTextContent(doc, metaElement, "channelName1", this.ChannelName1);
            if (this.ChannelName2 != null) createElementWithTextContent(doc, metaElement, "channelName2", this.ChannelName2);
            if (this.userName != null) createElementWithTextContent(doc, metaElement, "userName", this.userName);
            return metaElement;
        }

        @Override
        public String toString() {
            return "Meta : \n" + indent(1) + "Position X: " + positionX + "\n" +
                    indent(1) + "Position Y: " + positionY + "\n" +
                    indent(1) + "Position Z: " + positionZ + "\n" +
                    indent(1) + "Position T: " + positionT + "\n" +
                    indent(1) + "Pixel Size X: " + pixelSizeX + "\n" +
                    indent(1) + "Pixel Size Y: " + pixelSizeY + "\n" +
                    indent(1) + "Pixel Size Z: " + pixelSizeZ + "\n" +
                    indent(1) + "Time Interval: " + timeInterval + "\n" +
                    indent(1) + "Channel Name 0: " + ChannelName0 + "\n" +
                    indent(1) + "Channel Name 1: " + ChannelName1 + "\n" +
                    indent(1) + "Channel Name 2: " + ChannelName2 + "\n" +
                    indent(1) + "User Name: " + userName + "\n";
        }

    }

    public static class Roi
    {
        private String classname;
        private String id;
        private String name;
        private boolean selected;
        private boolean readOnly;
        private int color;
        private double stroke;
        private double opacity;
        private boolean showName;
        private double z;
        private double t;
        private double c;
        private List<Point> points;

        public String getClassname() { return classname; }
        public void setClassname(String classname) { this.classname = classname; }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
        public boolean isReadOnly() { return readOnly; }
        public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }
        public int getColor() { return color; }
        public void setColor(int color) { this.color = color; }
        public double getStroke() { return stroke; }
        public void setStroke(double stroke) { this.stroke = stroke; }
        public double getOpacity() { return opacity; }
        public void setOpacity(double opacity) { this.opacity = opacity; }
        public boolean isShowName() { return showName; }
        public void setShowName(boolean showName) { this.showName = showName; }
        public double getZ() { return z; }
        public void setZ(double z) { this.z = z; }
        public double getT() { return t; }
        public void setT(double t) { this.t = t; }
        public double getC() { return c; }
        public void setC(double c) { this.c = c; }
        public List<Point> getPoints() { return points; }
        public void setPoints(List<Point> points) { this.points = points; }

        public static Roi getRoi(Element roiElement) {
            Roi roi = new Roi();
            roi.setClassname(getElementTextContent(roiElement, "classname"));
            roi.setId(getElementTextContent(roiElement, "id"));
            roi.setName(getElementTextContent(roiElement, "name"));
            roi.setSelected(Boolean.parseBoolean(getElementTextContent(roiElement, "selected")));
            roi.setReadOnly(Boolean.parseBoolean(getElementTextContent(roiElement, "read_only")));
            roi.setColor(Integer.parseInt(getElementTextContent(roiElement, "color")));
            roi.setStroke(Double.parseDouble(getElementTextContent(roiElement, "stroke")));
            roi.setOpacity(Double.parseDouble(getElementTextContent(roiElement, "opacity")));
            roi.setShowName(Boolean.parseBoolean(getElementTextContent(roiElement, "show_name")));
            roi.setZ(Double.parseDouble(getElementTextContent(roiElement, "z")));
            roi.setT(Double.parseDouble(getElementTextContent(roiElement, "t")));
            roi.setC(Double.parseDouble(getElementTextContent(roiElement, "c")));

            // Parse points
            List<Point> points = new ArrayList<>();
            NodeList pointNodes = roiElement.getElementsByTagName("point");
            for (int i = 0; i < pointNodes.getLength(); i++) {
                Element pointElem = (Element) pointNodes.item(i);
                double posX = Double.parseDouble(getElementTextContent(pointElem, "pos_x"));
                double posY = Double.parseDouble(getElementTextContent(pointElem, "pos_y"));
                points.add(new Point(posX, posY));
            }
            roi.setPoints(points);

            return roi;
        }

        /**
         * Converts the Roi object to an XML Element.
         * @param doc The document to create the element in.
         * @return The XML Element representing this Roi object.
         */
        public Element toXmlElement(Document doc) {
            Element roiElement = doc.createElement("roi");

            if (this.classname != null) createElementWithTextContent(doc, roiElement, "classname", this.classname);
            if (this.id != null) createElementWithTextContent(doc, roiElement, "id", this.id);
            if (this.name != null) createElementWithTextContent(doc, roiElement, "name", this.name);
            createElementWithTextContent(doc, roiElement, "selected", String.valueOf(this.selected));
            createElementWithTextContent(doc, roiElement, "read_only", String.valueOf(this.readOnly)); // Note: XML uses read_only
            createElementWithTextContent(doc, roiElement, "color", String.valueOf(this.color));
            createElementWithTextContent(doc, roiElement, "stroke", String.valueOf(this.stroke));
            createElementWithTextContent(doc, roiElement, "opacity", String.valueOf(this.opacity));
            createElementWithTextContent(doc, roiElement, "show_name", String.valueOf(this.showName)); // Note: XML uses show_name
            createElementWithTextContent(doc, roiElement, "z", String.valueOf(this.z));
            createElementWithTextContent(doc, roiElement, "t", String.valueOf(this.t));
            createElementWithTextContent(doc, roiElement, "c", String.valueOf(this.c));

            if (this.points != null && !this.points.isEmpty()) {
                Element pointsElement = doc.createElement("points");
                roiElement.appendChild(pointsElement);
                for (Point point : this.points) {
                    pointsElement.appendChild(point.toXmlElement(doc));
                }
            }
            return roiElement;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(indent(1)).append("Classname: ").append(classname).append("\n");
            sb.append(indent(1)).append("ID: ").append(id).append("\n");
            sb.append(indent(1)).append("Name: ").append(name).append("\n");
            sb.append(indent(1)).append("Selected: ").append(selected).append("\n");
            sb.append(indent(1)).append("Read Only: ").append(readOnly).append("\n");
            sb.append(indent(1)).append("Color: ").append(color).append("\n");
            sb.append(indent(1)).append("Stroke: ").append(stroke).append("\n");
            sb.append(indent(1)).append("Opacity: ").append(opacity).append("\n");
            sb.append(indent(1)).append("Show Name: ").append(showName).append("\n");
            sb.append(indent(1)).append("Z: ").append(z).append("\n");
            sb.append(indent(1)).append("T: ").append(t).append("\n");
            sb.append(indent(1)).append("C: ").append(c).append("\n");
            sb.append(indent(1)).append("Points: \n");
            sb.append(indent(1));
            for (Point point : points) {
                sb.append(point).append(", ");
            }
            return sb.toString();
        }


        public static class Point
        {
            private double pos_x;
            private double pos_y;

            public double getPos_x() { return pos_x; }
            public void setPos_x(double pos_x) { this.pos_x = pos_x; }
            public double getPos_y() { return pos_y; }
            public void setPos_y(double pos_y) { this.pos_y = pos_y; }

            public Point(double pos_x, double pos_y) {
                this.pos_x = pos_x;
                this.pos_y = pos_y;
            }

            /**
             * Converts the Point object to an XML Element.
             * @param doc The document to create the element in.
             * @return The XML Element representing this Point object.
             */
            public Element toXmlElement(Document doc) {
                Element pointElement = doc.createElement("point");
                createElementWithTextContent(doc, pointElement, "pos_x", String.valueOf(this.pos_x));
                createElementWithTextContent(doc, pointElement, "pos_y", String.valueOf(this.pos_y));
                return pointElement;
            }

            @Override
            public String toString() {
                return "(" + pos_x + ", " + pos_y + ')';
            }

        }

    }

}
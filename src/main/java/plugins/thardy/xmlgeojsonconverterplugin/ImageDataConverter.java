package plugins.thardy.xmlgeojsonconverterplugin;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for converting image annotation data between XML and GeoJSON formats.
 * <p>
 * This class provides a set of static methods to handle the bidirectional conversion
 * of image metadata and Regions of Interest (ROIs). It supports both in-memory
 * object conversion and direct file-to-file conversion.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><b>Object Conversion:</b>
 *     <ul>
 *       <li>{@link #xmlToGeoJson(XmlImageData)}: Converts an {@code XmlImageData} object to a {@code GeoJsonImageData} object.</li>
 *       <li>{@link #geoJsonToXml(GeoJsonImageData)}: Converts a {@code GeoJsonImageData} object to an {@code XmlImageData} object.</li>
 *     </ul>
 *   </li>
 *   <li><b>File Conversion:</b>
 *     <ul>
 *       <li>{@link #xmlToGeoJsonFile(String, String)}: Reads an XML file and writes the converted data to a GeoJSON file.</li>
 *       <li>{@link #geoJsonToXmlFile(String, String)}: Reads a GeoJSON file and writes the converted data to an XML file.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @see plugins.thardy.xmlgeojsonconverterplugin.XmlImageData
 * @see plugins.thardy.xmlgeojsonconverterplugin.GeoJsonImageData
 * @author thardy
 */


public class ImageDataConverter {

    /**
     * Converts an XML file containing image data to a GeoJSON file.
     *
     * @param inputXmlFilePath      the path to the input XML file
     * @param outputGeoJsonFilePath the path to the output GeoJSON file
     * @param includeMetadata       whether to include metadata in the conversion
     * @throws IOException          if an I/O error occurs during file operations
     */
    public static void xmlToGeoJsonFile(String inputXmlFilePath, String outputGeoJsonFilePath, boolean includeMetadata) throws IOException {
        // Load the XML image data from the file
        XmlImageData xmlImageData = XmlImageData.fromXml(inputXmlFilePath);

        // Convert the XML image data to GeoJSON format
        GeoJsonImageData geoJsonImageData = xmlToGeoJson(xmlImageData, includeMetadata);

        // Save the converted GeoJSON data to a file
        geoJsonImageData.toJsonFile(outputGeoJsonFilePath);
    }

    /**
     * Converts an XML file containing image data to a GeoJSON file with metadata included.
     *
     * @param inputXmlFilePath      the path to the input XML file
     * @param outputGeoJsonFilePath the path to the output GeoJSON file
     * @throws IOException          if an I/O error occurs during file operations
     */
    public static void xmlToGeoJsonFile(String inputXmlFilePath, String outputGeoJsonFilePath) throws IOException {
        // Default to include metadata
        xmlToGeoJsonFile(inputXmlFilePath, outputGeoJsonFilePath, true);
    }

    /**
     * Converts a GeoJSON file containing image data to an XML file.
     *
     * @param inputGeoJsonFilePath  the path to the input GeoJSON file
     * @param outputXmlFilePath     the path to the output XML file
     * @param includeMetadata       whether to include metadata in the conversion
     * @throws IOException                  if an I/O error occurs during file operations
     * @throws ParserConfigurationException if a parser configuration error occurs
     * @throws TransformerException         if a transformation error occurs
     */
    public static void geoJsonToXmlFile(String inputGeoJsonFilePath, String outputXmlFilePath, boolean includeMetadata) throws IOException, ParserConfigurationException, TransformerException {
        // Load the GeoJSON image data from the file
        GeoJsonImageData geoJsonImageData = GeoJsonImageData.fromJsonFile(inputGeoJsonFilePath);

        // Convert the GeoJSON image data to XML format
        XmlImageData xmlImageData = geoJsonToXml(geoJsonImageData, includeMetadata);

        // Save the converted XML data to a file
        xmlImageData.toXmlFile(outputXmlFilePath);
    }

    /**
     * Converts a GeoJSON file containing image data to an XML file with metadata included.
     *
     * @param inputGeoJsonFilePath  the path to the input GeoJSON file
     * @param outputXmlFilePath     the path to the output XML file
     * @throws IOException                  if an I/O error occurs during file operations
     * @throws ParserConfigurationException if a parser configuration error occurs
     * @throws TransformerException         if a transformation error occurs
     */
    public static void geoJsonToXmlFile(String inputGeoJsonFilePath, String outputXmlFilePath) throws IOException, ParserConfigurationException, TransformerException {
        // Default to include metadata
        geoJsonToXmlFile(inputGeoJsonFilePath, outputXmlFilePath, true);
    }


    /**
     * Converts an XmlImageData object to a GeoJsonImageData object.
     *
     * @param xmlImageData the XmlImageData object to convert
     * @return a GeoJsonImageData object representing the same data
     */
    public static GeoJsonImageData xmlToGeoJson(XmlImageData xmlImageData, boolean includeMetadata) {
        GeoJsonImageData geoJsonImageData = new GeoJsonImageData();

        // Type is always FeatureCollection
        geoJsonImageData.setType("FeatureCollection");

        // METADATA
        if (includeMetadata) {
            // Convert the metadata from XmlImageData to GeoJsonImageData
            GeoJsonImageData.Metadata metadata = xmlToGeoJsonMetadata(xmlImageData);
            geoJsonImageData.setMetadata(metadata);
        } else {
            // If metadata is not included, set it to null
            geoJsonImageData.setMetadata(null);
        }

        // Features
        List<GeoJsonImageData.Feature> features = new ArrayList<>();
        for (XmlImageData.Roi roi : xmlImageData.getRois()) {
            GeoJsonImageData.Feature feature = xmlRoiToGeoJsonFeature(roi);
            features.add(feature);
        }
        // Set the features back to the GeoJsonImageData
        geoJsonImageData.setFeatures(features);

        return geoJsonImageData;
    }

    public static GeoJsonImageData xmlToGeoJson(XmlImageData xmlImageData) {
        // Default to include metadata
        return xmlToGeoJson(xmlImageData, true);
    }

    private static GeoJsonImageData.Metadata xmlToGeoJsonMetadata(XmlImageData xmlImageData) {

        // Create a new Metadata object for GeoJsonImageData
        GeoJsonImageData.Metadata metadata = new GeoJsonImageData.Metadata();

        // Get the meta data from the XML image data
        XmlImageData.Meta meta = xmlImageData.getMeta();

        // Set the name if available
        if (xmlImageData.getName() != null) {
            metadata.setFilename(xmlImageData.getName());
        }

        // MPP
        // Initialize the Mpp object
        GeoJsonImageData.Mpp mpp = new GeoJsonImageData.Mpp();
        metadata.setMpp(mpp);
        metadata.getMpp().setX(meta.getPixelSizeX());
        metadata.getMpp().setY(meta.getPixelSizeY());

        // set the dimensions to null
        metadata.setDimensions(null);

        return metadata;
    }

    private static GeoJsonImageData.Feature xmlRoiToGeoJsonFeature(XmlImageData.Roi roi) {
        GeoJsonImageData.Feature feature = new GeoJsonImageData.Feature();

        // Set the type to Feature
        // always Feature
        feature.setType("Feature");

        // set the id
        if (roi.getName() != null) {
            feature.setId(roi.getId());  // TO CHECK
        }

        // Set the geometry
        GeoJsonImageData.Geometry geometry = new GeoJsonImageData.Geometry();
        if (Objects.equals(roi.getClassname(), "plugins.kernel.roi.roi2d.ROI2DPolygon")) {
            geometry.setType("Polygon");
        } else {
            geometry.setType("Unknown");
        }

        // Set the coordinates
        List<List<GeoJsonImageData.Coordinate>> coordinates = new ArrayList<>();
        List<GeoJsonImageData.Coordinate> coordinateList = new ArrayList<>();
        for (XmlImageData.Roi.Point point : roi.getPoints()) {
            GeoJsonImageData.Coordinate coordinate = new GeoJsonImageData.Coordinate(point.getPos_x(), point.getPos_y());
            coordinateList.add(coordinate);
        }
        // For the GeoJSON Polygon, the first and last coordinates must be the same
        if (!coordinateList.isEmpty()) {
            // Ensure the first and last coordinates are the same
            GeoJsonImageData.Coordinate firstCoordinate = coordinateList.get(0);
            GeoJsonImageData.Coordinate lastCoordinate = coordinateList.get(coordinateList.size() - 1);
            if (!firstCoordinate.equals(lastCoordinate)) {
                coordinateList.add(firstCoordinate); // Close the polygon
            }
        }

        coordinates.add(coordinateList);
        geometry.setCoordinates(coordinates); // Assign coordinates to geometry

        // Set the geometry to the feature
        feature.setGeometry(geometry); // Assign geometry to feature

        // set the properties
        GeoJsonImageData.Properties properties = new GeoJsonImageData.Properties();
        // properties.setColor(intToColor(roi.getColor()));
        properties.setObjectType("annotation");
        // Set the classification
        GeoJsonImageData.Classification classification = new GeoJsonImageData.Classification(
                roi.getName(),
                intToColor(roi.getColor())
        );
        // Set the classification to properties
        properties.setClassification(classification);
        // Set the properties to the feature
        feature.setProperties(properties);

        return feature;
    }

    private static GeoJsonImageData.Color intToColor(int color) {
        // Convert an integer color value to a GeoJsonImageData.Color object

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return new GeoJsonImageData.Color(r, g, b);
    }

    /**
     * Converts a GeoJsonImageData object to an XmlImageData object.
     *
     * @param geoJsonImageData the GeoJsonImageData object to convert
     * @param includeMetadata whether to include metadata in the conversion
     * @return an XmlImageData object representing the same data
     */
    public static XmlImageData geoJsonToXml(GeoJsonImageData geoJsonImageData, boolean includeMetadata) {
        XmlImageData xmlImageData = new XmlImageData();

        // NAME if available
        if (geoJsonImageData.getMetadata() != null && geoJsonImageData.getMetadata().getFilename() != null) {
            xmlImageData.setName(geoJsonImageData.getMetadata().getFilename());
        }

        // Set the METADATA
        if (includeMetadata) {
            XmlImageData.Meta meta = getMeta(geoJsonImageData);
            xmlImageData.setMeta(meta);
        } else {
            // If metadata is not included, set it to null
            xmlImageData.setMeta(null);
        }

        // Convert features to ROIs
        List<XmlImageData.Roi> rois = new ArrayList<>();
        for (GeoJsonImageData.Feature feature : geoJsonImageData.getFeatures()) {
            XmlImageData.Roi roi = geoJsonFeatureToXmlRoi(feature);
            rois.add(roi);
        }
        xmlImageData.setRois(rois);

        return xmlImageData;
    }

    /**
     * Converts a GeoJsonImageData object to an XmlImageData object with metadata included.
     *
     * @param geoJsonImageData the GeoJsonImageData object to convert
     * @return an XmlImageData object representing the same data with metadata included
     */
    public static XmlImageData geoJsonToXml(GeoJsonImageData geoJsonImageData) {
        // Default to include metadata
        return geoJsonToXml(geoJsonImageData, true);
    }

    private static XmlImageData.Meta getMeta(GeoJsonImageData geoJsonImageData) {
        XmlImageData.Meta meta = new XmlImageData.Meta();

        // Initialize the metadata to default values
        meta.setPositionX(0);
        meta.setPositionY(0);
        meta.setPositionZ(0);
        meta.setPositionT(0);
        // Set the dimensions if available
        if (geoJsonImageData.getMetadata() != null && geoJsonImageData.getMetadata().getMpp() != null) {
            meta.setPixelSizeX(geoJsonImageData.getMetadata().getMpp().getX());
            meta.setPixelSizeY(geoJsonImageData.getMetadata().getMpp().getY());
        }
        else
        {
            meta.setPixelSizeX(1.0f); // Default value if not available
            meta.setPixelSizeY(1.0f); // Default value if not available
        }
        meta.setPixelSizeZ(1.0f); // Default value for Z
        meta.setTimeInterval(1.0f); // Default value for time interval
        meta.setChannelName0("ch 0");
        meta.setChannelName1("ch 1");
        meta.setChannelName2("ch 2");
        meta.setUserName("user");
        return meta;
    }

    private static XmlImageData.Roi geoJsonFeatureToXmlRoi(GeoJsonImageData.Feature feature) {
        XmlImageData.Roi roi = new XmlImageData.Roi();

        // Set the classname
        if (feature.getGeometry() != null && feature.getGeometry().getType() != null) {
            if (feature.getGeometry().getType().equals("Polygon")) {
                roi.setClassname("plugins.kernel.roi.roi2d.ROI2DPolygon");
            }
        } else {
            // We do not handle other types, so we set a default value
            roi.setClassname("Unknown");
        }

        // Set the id
        if (feature.getId() != null) {
            roi.setId(feature.getId());
        }

        // Set the name
        if (feature.getProperties() != null && feature.getProperties().getClassification() != null) {
            roi.setName(feature.getProperties().getClassification().getName());
        }

        // Set default values for selected and readonly
        roi.setSelected(false);
        roi.setReadOnly(false);

        // Set the color
        if (feature.getProperties() != null && feature.getProperties().getClassification() != null) {
            roi.setColor(colorToInt(feature.getProperties().getClassification().getColor()));
        }

        // Set the color to a default value for strokes and opacity
        roi.setStroke(2);
        roi.setOpacity(0.3f);

        roi.setShowName(false);

        roi.setZ(-1.f); // Default Z value
        roi.setT(-1.f); // Default T value
        roi.setC(-1.f); // Default C value

        // Set the points
        List<XmlImageData.Roi.Point> points = new ArrayList<>();
        for (GeoJsonImageData.Coordinate coordinate : feature.getGeometry().getCoordinates().get(0)) {
            XmlImageData.Roi.Point point = new XmlImageData.Roi.Point(coordinate.getX(), coordinate.getY());
            points.add(point);
        }
        // For xml, we do not need to close the polygon, so we do not add the first point again
        if (!points.isEmpty()) {
            // Ensure the first and last points are not the same
            XmlImageData.Roi.Point firstPoint = points.get(0);
            XmlImageData.Roi.Point lastPoint = points.get(points.size() - 1);
            if (firstPoint.getPos_x() == lastPoint.getPos_x() && firstPoint.getPos_y() == lastPoint.getPos_y()) {
                points.remove(points.size() - 1); // Remove the last point if it is the same as the first
            }
        }


        roi.setPoints(points);


        return roi;
    }

    private static int colorToInt(GeoJsonImageData.Color color) {
        // Convert a GeoJsonImageData.Color object to an integer color value
        return (0xFF << 24) | (color.getR() << 16) | (color.getG() << 8) | color.getB();
    }
}
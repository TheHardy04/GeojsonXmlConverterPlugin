package plugins.thardy.xmlgeojsonconverterplugin;

import com.google.api.client.json.Json;
import com.google.gson.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a GeoJSON object containing metadata and features.
 * It provides methods to read from and write to GeoJSON files,
 * as well as methods to convert the object to and from JSON format.
 *
 * @author thardy
 */

public class GeoJsonImageData {
    private String type;
    private Metadata metadata;
    private List<Feature> features;

    public String getType() {
        return type;
    }

    public void setType(String type) { this.type = type; }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    //region utility methods
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
        sb.append("GeoJson: \n");
        sb.append("Type: ").append(type).append("\n");
        if (metadata != null) {
            sb.append(metadata);
        } else {
            sb.append("Metadata: not set\n");
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of the features in the GeoJson object.
     * If no features are available, it returns a message indicating that.
     *
     * @return String representation of the features
     */
    public String featuresString(){
        return featuresString(features.size());
    }

    /**
     * Returns a string representation of the first n features in the GeoJson object.
     * If there are fewer than n features, it will return all available features.
     * If no features are available, it returns a message indicating that.
     *
     * @param n the number of features to include in the string representation
     * @return String representation of the first n features
     */
    public String featuresString(int n){
        StringBuilder sb = new StringBuilder();
        if (features != null && !features.isEmpty()) {
            for(int i=0; i<n; i++){
                if (i < features.size()) {
                    sb.append("\nFeature ").append(i+1).append(": ");
                    sb.append(features.get(i).toString());
                }
                if (i == n-1 && i < features.size() - 1) {
                    sb.append("\n... (").append(features.size() - n).append(" more features)\n");
                    break; // Stop if we've reached the requested number of features
                }
            }
        } else {
            sb.append("No features available.\n");
        }
        return sb.toString();
    }
    //endregion

    //region Geojson components
    public static class Metadata {
        private String filename;
        private Mpp mpp;
        private List<Integer> dimensions;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public Mpp getMpp() {
            return mpp;
        }

        public void setMpp(Mpp mpp) {
            this.mpp = mpp;
        }

        public List<Integer> getDimensions() {
            return dimensions;
        }

        public void setDimensions(List<Integer> dimensions) {
            this.dimensions = dimensions;
        }

        public Metadata() {}

        public Metadata(JsonObject metadataJson) {
            // Set filename if it exists
            if (metadataJson.has("filename")) {
                setFilename(metadataJson.get("filename").getAsString());
            }

            // Set mpp if it exists
            if (metadataJson.has("mpp")) {
                JsonObject mppJson = metadataJson.getAsJsonObject("mpp");
                Mpp mpp = new Mpp(mppJson);
                setMpp(mpp);
            }

            // Set dimensions if they exist
            if (metadataJson.has("dimensions")) {
                JsonArray dimensionsArray = metadataJson.getAsJsonArray("dimensions");
                List<Integer> dimensions = new ArrayList<>();
                for (JsonElement element : dimensionsArray) {
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                        dimensions.add(element.getAsInt());
                    } else {
                        System.err.println("Invalid dimension value: " + element);
                    }
                }
                setDimensions(dimensions);
            }
        }

        @Override
        public String toString() {
            StringBuilder metadataString = new StringBuilder("Metadata : \n");
            if( filename != null) {
                metadataString.append(indent(1)).append(filename).append("\n");
            } else {
                metadataString.append(indent(1)).append("Filename: not set,\n");
            }
            if (mpp != null) {
                metadataString.append(indent(1)).append(mpp).append("\n");
            } else {
                metadataString.append(indent(1)).append("Mpp: not set,\n");
            }
            if (dimensions != null && !dimensions.isEmpty()) {
                metadataString.append(indent(1)).append("Dimensions: ").append(dimensions).append("\n");
            } else {
                metadataString.append(indent(1)).append("Dimensions: not set\n");
            }
            return metadataString.toString();
        }



        /**
         * Converts the Metadata object to a JsonObject.
         * @return a JsonObject representation of this Metadata.
         */
        public JsonObject toJsonObject() {
            JsonObject metadataJson = new JsonObject();
            if (this.filename != null) {
                metadataJson.addProperty("filename", this.filename);
            }
            if (this.mpp != null) {
                metadataJson.add("mpp", this.mpp.toJsonObject());
            }
            if (this.dimensions != null && !this.dimensions.isEmpty()) {
                JsonArray dimensionsArray = new JsonArray();
                for (Integer dim : this.dimensions) {
                    dimensionsArray.add(dim);
                }
                metadataJson.add("dimensions", dimensionsArray);
            }
            return metadataJson;
        }
    }

    public static class Mpp {
        private double x;
        private double y;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public Mpp() {
            this.x = 0;
            this.y = 0;
        }

        public Mpp(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Mpp(JsonObject mppJson)
        {
            if (mppJson.has("x")) {
                setX(mppJson.get("x").getAsDouble());
            }
            if (mppJson.has("y")) {
                setY(mppJson.get("y").getAsDouble());
            }
        }

        @Override
        public String toString() {
            String mppString;
            if (x != 0 || y != 0) {
                mppString = "Mpp : " + x + ", " + y;
            } else {
                mppString = "Mpp{not set}";
            }
            return mppString;
        }

        /**
         * Converts the Mpp object to a JsonObject.
         * @return a JsonObject representation of this Mpp.
         */
        public JsonObject toJsonObject() {
            JsonObject mppJson = new JsonObject();
            // GeoJSON typically includes values even if 0, unlike some XML.
            mppJson.addProperty("x", this.x);
            mppJson.addProperty("y", this.y);
            return mppJson;
        }
    }

    public static class Feature {
        private String type;
        private String id;
        private Geometry geometry;
        private Properties properties;

        Feature() {
            this.type = null;
            this.id = null;
            this.geometry = null;
            this.properties = null;
        }

        Feature(JsonObject featureJson) {
            if (featureJson.has("type")) {
                setType(featureJson.get("type").getAsString());
            }
            if (featureJson.has("id")) {
                setId(featureJson.get("id").getAsString());
            }
            if (featureJson.has("geometry")) {
                JsonObject geometryJson = featureJson.getAsJsonObject("geometry");
                Geometry geometry = new Geometry(geometryJson);
                setGeometry(geometry);
            }
            if (featureJson.has("properties")) {
                JsonObject propertiesJson = featureJson.getAsJsonObject("properties");
                Properties properties = new Properties(propertiesJson);
                setProperties(properties);
            }
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {this.geometry = geometry;}

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        @Override
        public String toString() {
            StringBuilder featureString = new StringBuilder("\n");
            featureString.append(indent(1)).append("Type: ").append(type).append("\n");
            featureString.append(indent(1)).append("Id: ").append(id).append("\n");
            if (geometry != null) {
                featureString.append(indent(1)).append(geometry);
            } else {
                featureString.append(indent(1)).append("Geometry: not set\n");
            }
            if (properties != null) {
                featureString.append(indent(1)).append(properties);
            } else {
                featureString.append(indent(1)).append("Properties: not set\n");
            }
            return featureString.toString();
        }

        /**
         * Converts the Feature object to a JsonObject.
         * @return a JsonObject representation of this Feature.
         */
        public JsonObject toJsonObject() {
            JsonObject featureJson = new JsonObject();
            if (this.type != null) {
                featureJson.addProperty("type", this.type);
            }
            if (this.id != null) {
                featureJson.addProperty("id", this.id);
            }
            if (this.geometry != null) {
                featureJson.add("geometry", this.geometry.toJsonObject());
            }
            if (this.properties != null) {
                featureJson.add("properties", this.properties.toJsonObject());
            }
            return featureJson;
        }
    }

    public static class Geometry {
        private GeoJsonGeometryType type;
        private List<Coordinate> coordinates;
        private boolean isEllipse;

        Geometry() {
            this.type = null;
            this.coordinates = new ArrayList<>();
            this.isEllipse = false;

        }

        Geometry(JsonObject geometryJson) {
            if (geometryJson.has("type")) {
                setType(GeoJsonGeometryType.valueOf(geometryJson.get("type").getAsString().toUpperCase()));
            }
            if (geometryJson.has("coordinates")) {
                getCoordinatesArraysFromJsonArray(geometryJson.get("coordinates").getAsJsonArray());
            }
            if (geometryJson.has("isEllipse")) {
                setEllipse(geometryJson.get("isEllipse").getAsBoolean());
            }
        }


        // Getters and setters for Geometry
        public GeoJsonGeometryType getType() { return type; }
        public void setType(GeoJsonGeometryType type) { this.type = type; }
        public List<Coordinate> getCoordinates() { return coordinates; }
        public void setCoordinates(List<Coordinate> coordinates) { this.coordinates = coordinates; }
        public boolean isEllipse() { return isEllipse; }
        public void setEllipse(boolean isEllipse) { this.isEllipse = isEllipse; }

        /**
         * Extracts coordinates from a JsonArray and returns a list of Coordinate objects.
         * This method assumes that the coordinates are in the format [[x1, y1], [x2, y2], ...].
         *
         * @param coordinatesArray the JsonArray containing coordinates
         * @return a list of Coordinate objects
         */
        private static List<Coordinate> getCoordinatesFromJsonArray(JsonArray coordinatesArray) {
            // Initialize the coordinates list
            List<Coordinate> coordinateList = new ArrayList<>();
            if(coordinatesArray != null && !coordinatesArray.isEmpty()) {

                // there can be 3 lists nested
                if(coordinatesArray.size() == 1 && coordinatesArray.get(0).isJsonArray()) {
                    // If the first element is an array, we assume it's a Polygon or MultiPolygon
                    coordinatesArray = coordinatesArray.get(0).getAsJsonArray();
                }

                for (JsonElement element : coordinatesArray) {
                    if (element.isJsonArray()) {
                        JsonArray coordArray = element.getAsJsonArray();
                        if (coordArray.size() == 2) {
                            double x = coordArray.get(0).getAsDouble();
                            double y = coordArray.get(1).getAsDouble();
                            coordinateList.add(new Coordinate(x, y));
                        }
                    }
                }
            }
            return coordinateList;
        }

        /**
         * Processes a JsonArray of coordinates and populates the coordinates list.
         * This method handles both Polygon and MultiPolygon types.
         *
         * @param coordinatesArray the JsonArray containing coordinates
         */
        public void getCoordinatesArraysFromJsonArray(JsonArray coordinatesArray) {
            // Initialize the coordinates list
            this.coordinates = new ArrayList<>();
            if(coordinatesArray != null && !coordinatesArray.isEmpty()) {
                if (this.type == GeoJsonGeometryType.POLYGON)
                {
                    // For Polygon, we expect a single array of coordinates
                    this.coordinates = getCoordinatesFromJsonArray(coordinatesArray);
                    if (this.coordinates.isEmpty()) {
                        System.err.println("No coordinates found in the Polygon element.");
                    }
                }

                if (this.type == GeoJsonGeometryType.LINE_STRING)
                {
                    // For LineString, we expect a single array of coordinates
                    this.coordinates = getCoordinatesFromJsonArray(coordinatesArray);
                    if (this.coordinates.isEmpty()) {
                        System.err.println("No coordinates found in the LineString element.");
                    }
                }
                if (this.type == GeoJsonGeometryType.POINT)
                {
                    // For Point, we expect a single coordinate
                    if (coordinatesArray.size() == 2 && coordinatesArray.get(0).isJsonPrimitive()) {
                        double x = coordinatesArray.get(0).getAsDouble();
                        double y = coordinatesArray.get(1).getAsDouble();
                        this.coordinates.add(new Coordinate(x, y));
                    } else {
                        System.err.println("Invalid Point format: " + coordinatesArray);
                    }
                }
            }
            else {
                System.err.println("No coordinates found.");
            }
        }

        @Override
        public String toString() {
            StringBuilder geometryString = new StringBuilder("Geometry: \n");
            geometryString.append(indent(2)).append("Type : ").append(type).append("\n");
            if (coordinates != null && !coordinates.isEmpty()) {
                geometryString.append(indent(2)).append("Coordinates:\n");
                geometryString.append(indent(2));
                for(Coordinate coord : coordinates) {
                    geometryString.append(coord.toString()).append(", ");
                }
                geometryString.append("\n");
            } else {
                geometryString.append(indent(2)).append("Coordinates: not set\n");
            }
            return geometryString.toString();
        }

        /**
         * Converts the Geometry object to a JsonObject.
         * @return a JsonObject representation of this Geometry.
         */
        public JsonObject toJsonObject() {
            JsonObject geometryJson = new JsonObject();
            if (this.type != null) {
                geometryJson.addProperty("type", this.type.getTypeName());
            }

            if (this.coordinates != null && !this.coordinates.isEmpty()) {
                JsonElement jsonCoordinates;

                if (this.type == GeoJsonGeometryType.POLYGON) {
                    // For Polygon: coordinates is an array of rings. We'll create one ring.
                    JsonArray jsonRing = new JsonArray();
                    for (Coordinate coord : this.coordinates) {
                        jsonRing.add(coord.toJsonArray());
                    }
                    JsonArray polygonCoordinates = new JsonArray();
                    polygonCoordinates.add(jsonRing);
                    jsonCoordinates = polygonCoordinates;
                } else if (this.type == GeoJsonGeometryType.LINE_STRING) {
                    // For LineString: coordinates is a single array of coordinates
                    JsonArray jsonLine = new JsonArray();
                    for (Coordinate coord : this.coordinates) {
                        jsonLine.add(coord.toJsonArray());
                    }
                    jsonCoordinates = jsonLine;
                } else if (this.type == GeoJsonGeometryType.POINT) {
                    // For Point: coordinates is a single coordinate
                    jsonCoordinates = this.coordinates.get(0).toJsonArray();
                } else {
                    // Default or unsupported types
                    jsonCoordinates = new JsonArray();
                }

                geometryJson.add("coordinates", jsonCoordinates);
            }

            // Add isEllipse
            if (this.isEllipse) {
                geometryJson.addProperty("isEllipse", true);
            }



            return geometryJson;
        }
    }

    public static class Coordinate {
        private double x;
        private double y;

        public Coordinate(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }

        /**
         * Converts the Coordinate object to a JsonArray [x, y].
         * @return a JsonArray representation of this Coordinate.
         */
        public JsonArray toJsonArray() {
            JsonArray coordArray = new JsonArray();
            coordArray.add(this.x);
            coordArray.add(this.y);
            return coordArray;
        }
    }


    public static class Properties {
        private Color color;
        private boolean isLocked;
        private String objectType;
        private Classification classification;

        public Properties() {
            this.color = null;
            this.isLocked = false;
            this.objectType = null;
            this.classification = null;
        }

        public Properties(JsonObject propertiesJson) {
            // Set color if it exists
            if (propertiesJson.has("color")) {
                JsonArray colorArray = propertiesJson.getAsJsonArray("color");
                if (colorArray.size() == 3) {
                    setColor(new Color(
                            colorArray.get(0).getAsInt(),
                            colorArray.get(1).getAsInt(),
                            colorArray.get(2).getAsInt()
                    ));
                } else {
                    System.err.println("Invalid color array size: " + colorArray.size());
                }
            }

            // Set isLocked if it exists
            if (propertiesJson.has("isLocked")) {
                setLocked(propertiesJson.get("isLocked").getAsBoolean());
            }

            // Set objectType if it exists
            if (propertiesJson.has("objectType")) {
                setObjectType(propertiesJson.get("objectType").getAsString());
            }

            // Set classification if it exists
            if (propertiesJson.has("classification")) {
                JsonObject classificationJson = propertiesJson.getAsJsonObject("classification");
                Classification classification = new Classification(classificationJson);
                setClassification(classification);
            }
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public boolean isLocked() {
            return isLocked;
        }

        public void setLocked(boolean locked) {
            isLocked = locked;
        }

        public String getObjectType() { return objectType; }

        public void setObjectType(String objectType) { this.objectType = objectType; }

        public Classification getClassification() {
            return classification;
        }

        public void setClassification(Classification classification) {
            this.classification = classification;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Properties: \n");
            if (color != null) {
                sb.append(indent(2)).append("Color: ").append(color.toString()).append("\n");
            } else {
                sb.append(indent(2)).append("Color: not set\n");
            }
            sb.append(indent(2)).append("Is Locked: ").append(isLocked).append("\n");
            if (classification != null) {
                sb.append(indent(2)).append(classification).append("\n");
            } else {
                sb.append(indent(2)).append("Classification: not set\n");
            }
            return sb.toString();
        }

        /**
         * Converts the Properties object to a JsonObject.
         * @return a JsonObject representation of this Properties.
         */
        public JsonObject toJsonObject() {
            JsonObject propertiesJson = new JsonObject();
            if (this.color != null) {
                propertiesJson.add("color", this.color.toJsonArray());
            }
            propertiesJson.addProperty("isLocked", this.isLocked); // boolean is fine as is
            if (this.objectType != null) {
                propertiesJson.addProperty("objectType", this.objectType);
            }
            if (this.classification != null) {
                propertiesJson.add("classification", this.classification.toJsonObject());
            }
            return propertiesJson;
        }
    }

    public static class Color {
        private int r;
        private int g;
        private int b;


        public Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public int getR() {
            return r;
        }

        public void setR(int r) {
            this.r = r;
        }

        public int getG() {
            return g;
        }

        public void setG(int g) {
            this.g = g;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }

        @Override
        public String toString() {
            return  "Color : [ " + r + ", " + g + ", " + b + "]";
        }

        /**
         * Converts the Color object to a JsonArray [r, g, b].
         * @return a JsonArray representation of this Color.
         */
        public JsonArray toJsonArray() {
            JsonArray colorArray = new JsonArray();
            colorArray.add(this.r);
            colorArray.add(this.g);
            colorArray.add(this.b);
            return colorArray;
        }
    }

    public static class Classification {
        private String name;
        private Color color;

        public Classification() {
            this.name = null;
            this.color = null;
        }

        public Classification(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        public Classification(JsonObject classificationJson) {
            // Set name if it exists
            if (classificationJson.has("name")) {
                setName(classificationJson.get("name").getAsString());
            }

            // Set color if it exists
            if (classificationJson.has("color")) {
                JsonArray colorArray = classificationJson.getAsJsonArray("color");
                if (colorArray.size() == 3) {
                    setColor(new Color(
                            colorArray.get(0).getAsInt(),
                            colorArray.get(1).getAsInt(),
                            colorArray.get(2).getAsInt()
                    ));
                } else {
                    System.err.println("Invalid color array size: " + colorArray.size());
                }
            }
        }


        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Color getColor() {
            return color;
        }
        public void setColor(Color color) {
            this.color = color;
        }

        @Override
        public String toString() {
            return "Classification :\n" + indent(3) + "name : '" + name + "',\n" + indent(3) + color + "}";
        }

        /**
         * Converts the Classification object to a JsonObject.
         * @return a JsonObject representation of this Classification.
         */
        public JsonObject toJsonObject() {
            JsonObject classificationJson = new JsonObject();
            if (this.name != null) {
                classificationJson.addProperty("name", this.name);
            }
            if (this.color != null) {
                classificationJson.add("color", this.color.toJsonArray());
            }
            return classificationJson;
        }
    }
    //endregion

    //region From File
    /**
     * Reads a GeoJSON file and returns a GeoJson object.
     *
     * @param filePath the path to the GeoJSON file
     * @return a GeoJson object containing the parsed data
     * @throws IOException if an error occurs while reading the file
     */
    public static GeoJsonImageData fromJsonFile(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            // Parse the JSON file
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            // Create our GeoJson object
            GeoJsonImageData geoJsonImageData = new GeoJsonImageData();

            // Extract and set the type
            if (jsonObject.has("type")) {
                geoJsonImageData.setType(jsonObject.get("type").getAsString());
            }

            // Extract and set the metadata if it exists
            if (jsonObject.has("metadata")) {
                JsonObject metadataJson = jsonObject.getAsJsonObject("metadata");
                Metadata metadata = new Metadata(metadataJson);

                geoJsonImageData.setMetadata(metadata);
            }

            // Extract and set the features if they exist
            if(jsonObject.has("features")) {
                // The features are stored in a JsonArray
                JsonArray featuresArray = jsonObject.getAsJsonArray("features");
                List<Feature> featuresList = new ArrayList<>();

                // We loop through the all the features in the array
                for(int i = 0; i < featuresArray.size(); i++){

                    JsonObject featureJson = featuresArray.get(i).getAsJsonObject();
                    Feature feature = new Feature(featureJson);

                    // Add the feature to the list
                    featuresList.add(feature);
                }
                // Set the features to the GeoJson object
                geoJsonImageData.setFeatures(featuresList);

            }

            return geoJsonImageData;
        }
    }
    //endregion

    //region To File
    /**
     * Writes the GeoJsonImageData object to a GeoJSON file.
     *
     * @param filePath the path to the GeoJSON file to be created/overwritten.
     * @throws IOException if an error occurs while writing the file.
     */
    public void toJsonFile(String filePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(this.toJsonObject(), writer);
        }
    }

    /**
     * Converts the GeoJsonImageData object to a JsonObject.
     *
     * @return a JsonObject representation of this GeoJsonImageData.
     */
    public JsonObject toJsonObject() {
        JsonObject geoJsonRoot = new JsonObject();
        if (this.type != null) {
            geoJsonRoot.addProperty("type", this.type);
        }

        if (this.metadata != null) {
            geoJsonRoot.add("metadata", this.metadata.toJsonObject());
        }

        if (this.features != null && !this.features.isEmpty()) {
            JsonArray featuresArray = new JsonArray();
            for (Feature feature : this.features) {
                featuresArray.add(feature.toJsonObject());
            }
            geoJsonRoot.add("features", featuresArray);
        }
        return geoJsonRoot;
    }

    //endregion


}

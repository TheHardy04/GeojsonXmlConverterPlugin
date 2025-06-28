package plugins.thardy.xmlgeojsonconverterplugin;

public enum GeoJsonGeometryType {
    POLYGON("Polygon"),
    LINE_STRING("LineString"),
    POINT("Point");

    private final String typeName;

    GeoJsonGeometryType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public static GeoJsonGeometryType fromTypeName(String typeName) {
        for (GeoJsonGeometryType type : GeoJsonGeometryType.values()) {
            if (type.typeName.equalsIgnoreCase(typeName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported GeoJSON geometry type: " + typeName);
    }
}

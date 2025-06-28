package plugins.thardy.xmlgeojsonconverterplugin;

public enum XmlRoiType {
    POLYGON("plugins.kernel.roi.roi2d.ROI2DPolygon"),
    LINE("plugins.kernel.roi.roi2d.ROI2DLine"),
    POINT("plugins.kernel.roi.roi2d.ROI2DPoint"),
    POLYLINE("plugins.kernel.roi.roi2d.ROI2DPolyLine"),
    RECTANGLE("plugins.kernel.roi.roi2d.ROI2DRectangle"),
    ELLIPSE("plugins.kernel.roi.roi2d.ROI2DEllipse");

    private final String className;

    XmlRoiType(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public static XmlRoiType fromClassName(String className) {
        for (XmlRoiType type : XmlRoiType.values()) {
            if (type.className.equals(className)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported ROI classname: " + className);
    }
}

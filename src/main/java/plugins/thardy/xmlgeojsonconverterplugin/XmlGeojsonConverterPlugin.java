package plugins.thardy.xmlgeojsonconverterplugin;

import icy.main.Icy;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import plugins.adufour.ezplug.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * This plugin provides functionality to convert XML files to GeoJSON format and vice versa.
 * It uses the EzPlug framework for easy integration with Icy.
 *
 * @author thardy
 */

public class XmlGeojsonConverterPlugin extends EzPlug {

    private enum Mode {
        XML_TO_GEOJSON,
        GEOJSON_TO_XML
    }

    private final EzVarEnum<Mode> mode = new EzVarEnum<>("Conversion Mode", Mode.values(), Mode.XML_TO_GEOJSON);

    private final EzVarFile inputFile = new EzVarFile("Input File", System.getProperty("user.home") + File.separator + "Documents");

    private final EzVarText outputName = new EzVarText("Output File Name");

    private final EzVarBoolean includeMetadata = new EzVarBoolean("Include Metadata", true);

    final EzLabel text = new EzLabel("Choose a file to convert", Color.BLACK);



    @Override
    protected void initialize() {
        addEzComponent(mode);
        addEzComponent(inputFile);
        addEzComponent(outputName);
        addEzComponent(includeMetadata);
        addEzComponent(text);
    }

    @Override
    protected void execute() {
        // Check input file presence
        File inFile = inputFile.getValue();
        String outName = outputName.getValue();
        Mode selectedMode = mode.getValue();
        boolean includeMeta = includeMetadata.getValue();

        if (inFile == null || !inFile.exists()) {
            text.setText("Input file is missing or does not exist.");
            text.setColor(Color.RED);
            return;
        }

        // Check input file extension according to mode
        if (selectedMode == Mode.XML_TO_GEOJSON && !checkFileExtension(inFile.getName(), ".xml")) {
            text.setText("Input file must have .xml extension for XML to GeoJSON conversion.");
            text.setColor(Color.RED);
            return;
        }
        if (selectedMode == Mode.GEOJSON_TO_XML && !checkFileExtension(inFile.getName(), ".geojson")) {
            text.setText("Input file must have .geojson extension for GeoJSON to XML conversion.");
            text.setColor(Color.RED);
            return;
        }

        // Determine correct output extension
        String requiredExtension = selectedMode == Mode.XML_TO_GEOJSON ? ".geojson" : ".xml";

        // If output name is empty, use input file name (without extension) + required extension
        if (outName == null || outName.trim().isEmpty()) {
            String baseName = inFile.getAbsolutePath();
            // Remove the file extension from the input file name
            int dotIdx = baseName.lastIndexOf('.');
            if (dotIdx > 0) baseName = baseName.substring(0, dotIdx);
            // Append the required extension
            outName = baseName + requiredExtension;
        } else {
            // If output name does not have an extension, add the required extension
            if (!outName.toLowerCase().endsWith(requiredExtension)) {
                // Check if the output name already has an extension
                outName = inFile.getParent() + File.separator + outName;
                outName = outName + requiredExtension;
            }
            else
            {
                // Ensure the output file is in the same directory as the input file
                if (!outName.startsWith(inFile.getParent())) {
                    outName = inFile.getParent() + File.separator + outName;
                }
            }
        }


        boolean success = false;
        String message = "";
        Color messageColor = Color.BLACK;

        try {
            if (selectedMode == Mode.XML_TO_GEOJSON) {
                text.setText("Converting XML to GeoJSON...");
                ImageDataConverter.xmlToGeoJsonFile(inFile.getPath(), outName, includeMeta);
                success = true;
                message = "XML to GeoJSON conversion successful.\n Output: " + outName;
            } else if (selectedMode == Mode.GEOJSON_TO_XML) {
                text.setText("Converting GeoJSON to XML...");
                ImageDataConverter.geoJsonToXmlFile(inFile.getPath(), outName, includeMeta);
                success = true;
                message = "GeoJSON to XML conversion successful.\n Output: " + outName;
            } else {
                message = "Unknown conversion mode.";
                messageColor = Color.RED;
            }
        } catch (Exception ex) {
            message = "Error during conversion: " + ex.getMessage();
            messageColor = Color.RED;
        }

        // Display result
        text.setText(message);
        text.setColor(success ? Color.BLACK : messageColor);
    }

    @Override
    public void clean() {
        // This method is called when the plugin is closed
    }

    /**
     * Checks if the file has the expected extension.
     *
     * @param fileName The filename to check.
     * @param expectedExtension The expected file extension (e.g., ".xml" or ".geojson").
     * @return true if the file has the expected extension, false otherwise.
     */
    private static boolean checkFileExtension(String fileName, String expectedExtension) {
        return fileName.toLowerCase().endsWith(expectedExtension.toLowerCase());
    }

    /**
     * Only for test purpose.
     */
    public static void main(final String[] args) {
        // Launch the application.
        Icy.main(args);

        /*
         * Programmatically launch a plugin, as if the user had clicked its
         * button.
         */
        PluginLauncher.start(PluginLoader.getPlugin(XmlGeojsonConverterPlugin.class.getName()));
    }
}

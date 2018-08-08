package net.adamcin.bnd.vendor;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Processor;
import aQute.bnd.osgi.Resource;
import aQute.bnd.service.AnalyzerPlugin;
import aQute.bnd.service.Plugin;
import aQute.service.reporter.Reporter;

/**
 * Uses the value of the Bundle-Vendor parameter to set the values of service.vendor service properties for all processed scr files.
 */
@SuppressWarnings("unused")
public class VendorPlugin implements AnalyzerPlugin, Plugin {

	/**
	 * Object used to report logs to bnd.
	 */
	private Reporter reporter;

	/**
	 * Bnd plugin properties.
	 */
	private Map<String, String> properties;

	/**
	 * Sets the reporter for logging into the bnd logger.
	 *
	 * @param processor the reporter
	 */
	public void setReporter(final Reporter processor) {
		this.reporter = processor;
	}

	/**
	 * Sets properties which can be specified in the "-plugin" directive. For
	 * example: -plugin
	 * net.adamcin.bnd.vendor.VendorPlugin;service.vendor
	 * ="Acme Bundles, Inc."
	 */
	public void setProperties(final Map<String, String> map) {
		this.properties = map;
	}

	/**
	 * Get the best vendor name if available.
	 *
	 * @param analyzer the analyzer
	 * @return optional string value
	 */
	private Optional<String> getVendorName(final Analyzer analyzer) {
		return Stream.of(
				Optional.ofNullable(this.properties.get("service.vendor")),
				Optional.ofNullable(analyzer.getProperty(Constants.BUNDLE_VENDOR)))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	/**
	 * Modifies SCR xml files to set the service.vendor property.
	 *
	 * @param analyzer the bnd analyzer
	 * @return false to not reanalyze bundle classpath because our plugin will not change it.
	 * @throws Exception unexpectedly
	 */
	public boolean analyzeJar(final Analyzer analyzer) throws Exception {
		final String vendorName = getVendorName(analyzer).orElse(null);

		if (vendorName != null) {
			final Transformer transformer = getTransformer(vendorName);

			final String bpHeader = analyzer.getProperty(Constants.SERVICE_COMPONENT);

			Map<String, ? extends Map<String, String>> map = Processor.parseHeader(bpHeader, null);
			for (String root : map.keySet()) {
				Resource resource = analyzer.getJar().getResource(root);
				process(analyzer, transformer, root, resource);
			}
		}

		// do not reanalyze bundle classpath because our plugin has not changed it.
		return false;
	}

	/**
	 * Wrap the resource and put it back on the shelf.
	 *
	 * @param analyzer    the analyzer
	 * @param transformer the singleton transformer
	 * @param path        the resource path
	 * @param resource    the source resource
	 */
	private void process(final Analyzer analyzer, final Transformer transformer, final String path, final Resource resource) {
		analyzer.getJar().putResource(path, new TransformResource(transformer, resource));
	}

	/**
	 * Construct a transformer for the particular vendorName.
	 *
	 * @param vendorName the vendor name to inject
	 * @return a new singleton transformer
	 * @throws Exception for IO errors and the like
	 */
	private Transformer getTransformer(final String vendorName) throws Exception {
		final URL xslUrl = getClass().getResource("scrvendor.xsl");
		final Transformer tx = TransformerFactory.newInstance()
				.newTransformer(new StreamSource(xslUrl.openStream(),
						xslUrl.toExternalForm()));

		tx.setParameter("vendor_name", vendorName);
		return tx;
	}

}

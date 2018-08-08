package net.adamcin.bnd.vendor;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import aQute.bnd.osgi.Resource;
import aQute.bnd.osgi.WriteResource;

/**
 * Wraps a bnd Resource with an XSL transformation.
 */
final class TransformResource extends WriteResource {
	private final Transformer tx;

	private final Resource source;

	/**
	 * New instance.
	 * @param tx the singleton transformer
	 * @param source the source SCR xml resource.
	 */
	TransformResource(final Transformer tx, final Resource source) {
		this.tx = tx;
		this.source = source;
	}

	@Override
	public void write(final OutputStream out) throws Exception {
		InputStream in = null;
		try {
			in = new BufferedInputStream(this.source.openInputStream());
			final Source inSource = new StreamSource(in);
			final Result result = new StreamResult(out);
			tx.transform(inSource, result);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * Pass through for lastModified.
	 * @return source.lastModified()
	 */
	@Override
	public long lastModified() {
		return source.lastModified();
	}
}

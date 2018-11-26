package cz.metacentrum.perun.rpc.serializer;

import com.lowagie.text.DocumentException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Implementation of {@link Serializer} that returns PDF files.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class PdfSerializer implements Serializer {

	private OutputStream outputStream;

	public PdfSerializer(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public String getContentType() {
		return "application/pdf";
	}

	@Override
	public void write(Object object) throws IllegalArgumentException, IOException, RpcException {

		if (object instanceof String) {

			String htmlText = (String) object;

			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(htmlText);
			renderer.layout();
			try {
				renderer.createPDF(outputStream);
			} catch (DocumentException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public void writePerunException(PerunException pex) throws IOException {
		try {
			write("Operation failed. ErrorId: " +  pex.getErrorId() + " Reason: " + pex.getMessage());
		} catch (RpcException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void writePerunRuntimeException(PerunRuntimeException prex) throws IOException {
		try {
			write("Operation failed. ErrorId: " +  prex.getErrorId() + " Reason: " + prex.getMessage());
		} catch (RpcException e) {
			throw new IOException(e);
		}
	}
}

package cz.metacentrum.perun.rpc.serializer;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class SvgGraphvizSerializer implements Serializer {

	private PrintStream outputStream;

	public SvgGraphvizSerializer(OutputStream outputStream) {
		this.outputStream = new PrintStream(outputStream);
	}

	@Override
	public void write(Object object) throws IllegalArgumentException, IOException, RpcException {
		if (object instanceof Graphviz) {
			((Graphviz) object).render(Format.SVG).toOutputStream(outputStream);
		} else {
			writePerunException(new InternalErrorException("Received object was not Graphviz object."));
		}
	}

	@Override
	public void writePerunException(PerunException pex) throws IOException {
		outputStream.print(pex);
	}

	@Override
	public void writePerunRuntimeException(PerunRuntimeException prex) throws IOException {
		outputStream.print(prex);
	}

	@Override
	public String getContentType() {
		return "image/svg+xml";
	}
}

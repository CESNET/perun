package cz.metacentrum.perun.rpc.serializer;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class TextFileSerializer implements Serializer {
	private PrintStream outputStream;

	public TextFileSerializer(OutputStream outputStream) {
		this.outputStream = new PrintStream(outputStream);
	}

	@Override
	public String getContentType() {
		return "text/plain";
	}

	@Override
	public void write(Object object) throws IllegalArgumentException, IOException, RpcException {
		outputStream.print(object);
	}

	@Override
	public void writePerunException(PerunException pex) throws IOException {
		outputStream.print(pex);
	}

	@Override
	public void writePerunRuntimeException(PerunRuntimeException prex) throws IOException {
		outputStream.print(prex);
	}
}

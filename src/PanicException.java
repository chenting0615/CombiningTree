
public class PanicException extends Exception {

	public PanicException(String exception) {
		System.err.println(exception);
		System.exit(1);
	}
}

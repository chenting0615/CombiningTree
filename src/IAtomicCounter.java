
public interface IAtomicCounter {
	public int getAndIncrement() throws PanicException;
}

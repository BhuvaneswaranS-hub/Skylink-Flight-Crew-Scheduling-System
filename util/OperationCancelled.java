package util;

/** Signals that the user entered 0 at an input prompt. */
public final class OperationCancelled extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public OperationCancelled() { super("Operation cancelled successfully."); }
}

package bighead.exception;

/**不合法sql异常*/
public class UnqualifiedSqlException extends Exception {
	public UnqualifiedSqlException(String message){
		super(message);
	}
}

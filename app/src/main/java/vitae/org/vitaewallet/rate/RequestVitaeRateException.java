package vitae.org.vitaewallet.rate;

/**
 * Created by kaali on 7/5/17.
 */
public class RequestVitaeRateException extends Exception {
    public RequestVitaeRateException(String message) {
        super(message);
    }

    public RequestVitaeRateException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestVitaeRateException(Exception e) {
        super(e);
    }
}

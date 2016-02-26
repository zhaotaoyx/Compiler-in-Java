import org.antlr.v4.runtime.*;

public class NewErrorStrategy extends DefaultErrorStrategy {
    public static boolean acceptornot = true;

    @Override
    public void reportError(Parser recognizer, RecognitionException e) {
	//Do not report error.
    }

    @Override
    public void reportMissingToken(Parser recognizer) {
	//Do not report missing token.
    }
    
    @Override
    public void reportUnwantedToken(Parser recognizer) {
	//Do not report unwanted token.
    }
    
    @Override
    public void reportMatch(Parser recognizer) {
	//Do not report match.
    }

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
	//Do not recover, but tell the main function that the input code is not a valid program to compile.
	acceptornot = false;
    }
    
    //If no error is found, return true, which means the input code is a valid program to compile.

    boolean getAcceptornot() {
	return acceptornot;
    }

}

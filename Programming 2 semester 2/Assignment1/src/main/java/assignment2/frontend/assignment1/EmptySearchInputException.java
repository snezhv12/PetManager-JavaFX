package assignment2.frontend.assignment1;

public class EmptySearchInputException extends Exception {
    public EmptySearchInputException() {
        super("Oops! Search field is empty.");
    }
}

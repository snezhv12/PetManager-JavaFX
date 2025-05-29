package assignment2.frontend.assignment1;

public class PetNotFoundException extends Exception {
    public PetNotFoundException(String name) {
        super("No pet found with name: " + name);
    }
}


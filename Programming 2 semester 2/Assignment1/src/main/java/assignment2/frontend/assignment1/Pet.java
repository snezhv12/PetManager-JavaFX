package assignment2.frontend.assignment1;

public class Pet {
    public static final int MAX_VALUE = 100;
    public static final int MIN_VALUE = 0;

    private static int nextId = 1;

    private final int id;
    private String name;
    private int happiness;
    private int hunger;
    private int energy;

    // Constructor for new pets (auto ID)
    public Pet(String name, int happiness, int hunger, int energy)
            throws InvalidNameException, InvalidStatException {
        this.id = getNextId();
        setName(name);
        setHappiness(happiness);
        setHunger(hunger);
        setEnergy(energy);
    }

    // Constructor for imported pets (manual ID)
    public Pet(int id, String name, int happiness, int hunger, int energy)
            throws InvalidNameException, InvalidStatException {
        this.id = id;
        setName(name);
        setHappiness(happiness);
        setHunger(hunger);
        setEnergy(energy);
    }

    public static int getNextId() {
        return nextId++;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidNameException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidNameException("Name cannot be empty.");
        }
        this.name = name.trim();
    }

    public int getHappiness() {
        return happiness;
    }

    public void setHappiness(int happiness) throws InvalidStatException {
        validateStat("Happiness", happiness);
        this.happiness = happiness;
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) throws InvalidStatException {
        validateStat("Hunger", hunger);
        this.hunger = hunger;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) throws InvalidStatException {
        validateStat("Energy", energy);
        this.energy = energy;
    }

    private void validateStat(String field, int value) throws InvalidStatException {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new InvalidStatException(field + " must be between " + MIN_VALUE + " and " + MAX_VALUE);
        }
    }
}

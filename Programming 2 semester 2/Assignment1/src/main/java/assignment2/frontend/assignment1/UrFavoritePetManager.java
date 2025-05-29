package assignment2.frontend.assignment1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.URL;
import java.util.*;

public class UrFavoritePetManager implements Initializable {
    @FXML
    private TextField searchField;
    @FXML
    private TextField nameField;
    @FXML
    private Slider happinessSlider, hungerSlider, energySlider;
    @FXML
    private TableView<Pet> petTable;
    @FXML
    private TableColumn<Pet, Integer> idColumn, happinessColumn, hungerColumn, energyColumn;
    @FXML
    private TableColumn<Pet, String> nameColumn;

    private final Map<String, Pet> allPets = new HashMap<>();
    private final ObservableList<Pet> shownPets = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        happinessColumn.setCellValueFactory(new PropertyValueFactory<>("happiness"));
        hungerColumn.setCellValueFactory(new PropertyValueFactory<>("hunger"));
        energyColumn.setCellValueFactory(new PropertyValueFactory<>("energy"));

        petTable.setItems(shownPets);
        petTable.setEditable(true);
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        setupTableEditing();
        petTable.setPlaceholder(new Label("Your pets will appear here!"));
    }

    private void setupTableEditing() {
        nameColumn.setOnEditCommit(event -> {
            String newName = event.getNewValue().trim();
            Pet pet = event.getRowValue();

            if (newName.isEmpty() || allPets.containsKey(newName)) {
                showAlert(Alert.AlertType.ERROR, "Invalid name", "Name cannot be empty or duplicate.");
                petTable.refresh();
            } else {
                try {
                    allPets.remove(pet.getName());
                    pet.setName(newName);
                    allPets.put(newName, pet);
                } catch (InvalidNameException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Name", e.getMessage());
                    petTable.refresh();
                }
            }
        });

        happinessColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        happinessColumn.setOnEditCommit(event -> {
            try {
                event.getRowValue().setHappiness(event.getNewValue());
            } catch (InvalidStatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", e.getMessage());
                petTable.refresh();
            }
        });

        hungerColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        hungerColumn.setOnEditCommit(event -> {
            try {
                event.getRowValue().setHunger(event.getNewValue());
            } catch (InvalidStatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", e.getMessage());
                petTable.refresh();
            }
        });

        energyColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        energyColumn.setOnEditCommit(event -> {
            try {
                event.getRowValue().setEnergy(event.getNewValue());
            } catch (InvalidStatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", e.getMessage());
                petTable.refresh();
            }
        });
    }

    @FXML
    private void savePet() {
        String name = nameField.getText().trim();
        int happiness = (int) happinessSlider.getValue();
        int hunger = (int) hungerSlider.getValue();
        int energy = (int) energySlider.getValue();

        if (allPets.containsKey(name)) {
            showAlert(Alert.AlertType.ERROR, "Duplicate", "Pet already exists.");
            return;
        }

        try {
            Pet newPet = new Pet(name, happiness, hunger, energy);
            allPets.put(name, newPet);
            showAlert(Alert.AlertType.INFORMATION, "Pet Saved", "Pet added successfully.");
            clearForm();
        } catch (InvalidNameException | InvalidStatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", e.getMessage());
        }
    }

    @FXML
    private void showPets() {
        shownPets.setAll(allPets.values());
        shownPets.sort(Comparator.comparingInt(Pet::getId));
    }

    @FXML
    private void searchPet() {
        try {
            String input = searchField.getText();
            if (input == null || input.trim().isEmpty()) {
                throw new EmptySearchInputException();
            }

            // searching directly o(1)
            Pet pet = allPets.get(input.trim());

            if (pet == null) {
                throw new PetNotFoundException(input);
            }

            // pet data
            showAlert(Alert.AlertType.INFORMATION, "Pet Found",
                    String.format("ID: %d\nName: %s\nHappiness: %d\nHunger: %d\nEnergy: %d",
                            pet.getId(), pet.getName(), pet.getHappiness(), pet.getHunger(), pet.getEnergy()));

            //  select the pet in the TableView
            if (shownPets.contains(pet)) {
                petTable.getSelectionModel().select(pet);
                petTable.scrollTo(pet);
            }

        } catch (EmptySearchInputException | PetNotFoundException e) {
            showAlert(Alert.AlertType.WARNING, "Search Error", e.getMessage());
        }
    }

  // i know searchPet already handles this functionality but i wanna keep this method for future use
    private Pet findPetByName(String name) throws EmptySearchInputException, PetNotFoundException {
        if (name == null || name.trim().isEmpty()) {
            throw new EmptySearchInputException();
        }

        Pet pet = allPets.get(name.trim().toLowerCase());
        if (pet == null) {
            throw new PetNotFoundException(name);
        }// throws if pet not found
            return pet;
        }

    @FXML
    private void removePet() {
        Pet selectedPet = petTable.getSelectionModel().getSelectedItem();
        if (selectedPet == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a pet to remove.");
            return;
        }

        allPets.remove(selectedPet.getName());
        shownPets.remove(selectedPet);
        showAlert(Alert.AlertType.INFORMATION, "Removed", selectedPet.getName() + " has been removed.");
    }

    @FXML
    private void exportPetsToExcel() {
        String filePath = System.getProperty("user.home") + "/Desktop/pets_export.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Pets");

            int rowIndex = 0;
            Row header = sheet.createRow(rowIndex++);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Happiness");
            header.createCell(3).setCellValue("Hunger");
            header.createCell(4).setCellValue("Energy");

            List<Pet> petsSorted = new ArrayList<>(allPets.values());
            petsSorted.sort(Comparator.comparingInt(Pet::getId));

            for (Pet pet : petsSorted) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(pet.getId());
                row.createCell(1).setCellValue(pet.getName());
                row.createCell(2).setCellValue(pet.getHappiness());
                row.createCell(3).setCellValue(pet.getHunger());
                row.createCell(4).setCellValue(pet.getEnergy());
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Pets exported to:\n" + filePath);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Error", "An error occurred:\n" + e.getMessage());
        }
    }

    @FXML
    private void importPetsFromExcel() {
        String filePath = System.getProperty("user.home") + "/Desktop/pets_export.xlsx";

        try (FileInputStream fis = new FileInputStream(filePath); // opens ex file in binary mode
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() < 1) {
                showAlert(Alert.AlertType.WARNING, "Import Error", "File is empty. Nothing to import.");
                return;
            }

            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next(); // Skip header

            int validCount = 0;
            int invalidCount = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row == null || row.getCell(0) == null) continue;

                try {
                    int id = (int) row.getCell(0).getNumericCellValue();
                    String name = row.getCell(1).getStringCellValue();
                    int happiness = (int) row.getCell(2).getNumericCellValue();
                    int hunger = (int) row.getCell(3).getNumericCellValue();
                    int energy = (int) row.getCell(4).getNumericCellValue();

                    if (!allPets.containsKey(name)) {
                        Pet pet = new Pet(id, name, happiness, hunger, energy);
                        allPets.put(name, pet);
                        validCount++;
                    } else {
                        invalidCount++;
                    }
                } catch (Exception e) {
                    invalidCount++;
                }
            }

            showPets();

            if (validCount == 0) {
                showAlert(Alert.AlertType.WARNING, "Import Result", "No pets were imported. All were invalid or duplicates.");
            } else {
                String msg = "Imported: " + validCount + " pet(s)";
                if (invalidCount > 0) msg += "\nInvalid/Duplicate rows: " + invalidCount;
                showAlert(Alert.AlertType.INFORMATION, "Import Result", msg);
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Import Error", "Could not open the file:\n" + e.getMessage());
        }
    }

    @FXML
    private void cancelInput() {
        clearForm();
    }

    @FXML
    private void quitApplication() {
        System.exit(0);
    }

    private void clearForm() {
        nameField.clear();
        happinessSlider.setValue(50);
        hungerSlider.setValue(50);
        energySlider.setValue(50);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

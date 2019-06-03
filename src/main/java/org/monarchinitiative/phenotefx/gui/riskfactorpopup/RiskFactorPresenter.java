package org.monarchinitiative.phenotefx.gui.riskfactorpopup;

/*
 * #%L
 * PhenoteFX
 * %%
 * Copyright (C) 2017 - 2019 Peter Robinson
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import base.OntoTerm;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Riskfactor;
import model.TimeAwareEffectSize;
import ontology_term.*;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.monarchinitiative.phenotefx.gui.Signal;
import org.monarchinitiative.phenotefx.gui.WidthAwareTextFields;
import org.monarchinitiative.phenotefx.service.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

//@TODO: inject some resources
public class RiskFactorPresenter implements Initializable {

    private static Logger logger = LoggerFactory.getLogger(RiskFactorPresenter.class);

    private Stage stage;

    private Resources resources;

    @FXML
    private ComboBox<TimeAwareEffectSize.EffectSizeType> effectSizeTypeComboBox;

    @FXML
    private ComboBox<Riskfactor.RiskFactorType> riskFactorCombo;

    @FXML
    private TextField riskFactorTextField;

    @FXML
    private TextField oddsField;

    @FXML
    private TextField timeMeanField;

    @FXML
    private TextField timeSDfield;

    @FXML
    private ComboBox<SimpleTimeUnit> timeUnitCombo;

    @FXML
    private TableView<RiskFactorRow> riskFactorsTable;

    @FXML
    private TableColumn<RiskFactorRow, String> modifierColumn;

    @FXML
    private TableColumn<RiskFactorRow, String> oddsColumn;

    @FXML
    private TableColumn<RiskFactorRow, String> riskFactorTypeColumn;

    @FXML
    private TableColumn<RiskFactorRow, String> riskFactorColumn;

    @FXML
    private TableColumn<RiskFactorRow, String> timeMeanColumn;

    @FXML
    private TableColumn<RiskFactorRow, String> timeSDcolumn;

    @FXML
    private TableColumn<RiskFactorRow, String> timeUnitColumn;

    //This is a pointer to different maps, depending on the value of riskFactorCombo
    private Map<String, String> riskFactorMap = new HashMap<>();
    private AutoCompletionBinding autoCompletionBinding;

    private Consumer<Signal> confirm;

    private ObservableList<RiskFactorRow> riskFactorRows = FXCollections.observableArrayList();

    public void setDialogStage(Stage stage) {
        this.stage = stage;

    }

    public void setResource(Resources injected) {
        resources = injected;
    }

    public void setCurrentAnnotation(List<RiskFactorRow> current) {
        if (current != null && !current.isEmpty()) {
            riskFactorRows.addAll(current);
        }
    }

    public void setSignal(Consumer<Signal> signal) {
        this.confirm = signal;
    }

    public List<RiskFactorRow> getConfirmed() {
        return new ArrayList<>(riskFactorRows);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        effectSizeTypeComboBox.getItems().addAll(TimeAwareEffectSize.EffectSizeType.values());
        riskFactorCombo.getItems().addAll(Riskfactor.RiskFactorType.values());
        timeUnitCombo.getItems().addAll(SimpleTimeUnit.values());

        riskFactorCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if( observable != null) {
                riskFactorTextField.clear();
                if (autoCompletionBinding != null) {
                    autoCompletionBinding.dispose();
                }
                bindName2IdMap(newValue);
                autoCompletionBinding =
                        WidthAwareTextFields.bindWidthAwareAutoCompletion(riskFactorTextField,
                        riskFactorMap.keySet());
                autoCompletionBinding.setVisibleRowCount(10);
            }
        });
        initRiskFactorTable();
    }

    private void bindName2IdMap(Riskfactor.RiskFactorType riskType) {

        switch (riskType) {
            case SEX:
                riskFactorMap.clear();
                riskFactorMap.putAll(BiologySex.values().stream()
                        .collect(Collectors.toMap(OntoTerm::getLabel, OntoTerm::getId)));
                break;
            case AGE:
                riskFactorMap.clear();
                riskFactorMap.putAll(LifeStage.values.stream()
                        .collect(Collectors.toMap(OntoTerm::getLabel, OntoTerm::getId)));
                break;
            case ETHNICITY:
                riskFactorMap.clear();
                riskFactorMap.putAll(Ethnicity.values().stream()
                        .collect(Collectors.toMap(OntoTerm::getLabel, OntoTerm::getId)));
                break;
            case LIFESTYLE:
                riskFactorMap.clear();
                riskFactorMap.putAll(LifeStyle.values().stream()
                        .collect(Collectors.toMap(OntoTerm::getLabel, OntoTerm::getId)));
                break;
            case ENVIRONMENT: //bind to environmental exposure terms
                riskFactorMap.clear();
                riskFactorMap.putAll(resources.getEctoName2Id());
                break;
            case DISEASE: //bind to mondo terms
                riskFactorMap.clear();
                riskFactorMap.putAll(resources.getMondoDiseaseName2IdMap());
                break;
            case PHENOTYPE: //bind to hpo terms
                riskFactorMap.clear();
                riskFactorMap.putAll(resources.getHpoSynonym2PreferredLabelMap());
                break;
            case FAMILYHISTORY:
                riskFactorMap.clear();
                riskFactorMap.putAll(FamilyHistory.values().stream()
                        .collect(Collectors.toMap(OntoTerm::getLabel, OntoTerm::getId)));
                break;
            case PRS:
                riskFactorMap.clear();
                riskFactorMap.putAll(PolygenicRiskScore.values().stream()
                        .collect(Collectors.toMap(OntoTerm::getLabel, OntoTerm::getId)));
                break;
            default:
                //do nothing
                return;
        }
    }

    private void initRiskFactorTable() {

        riskFactorsTable.setItems(riskFactorRows);

        modifierColumn = new TableColumn<>("effectSizeType");
        oddsColumn = new TableColumn<>("odds");
        riskFactorTypeColumn = new TableColumn<>("risk type");
        riskFactorColumn = new TableColumn<>("risk term");
        timeMeanColumn = new TableColumn<>("mean");
        timeSDcolumn = new TableColumn<>("sd");
        timeUnitColumn = new TableColumn<>("unit");

        modifierColumn.setCellValueFactory(new PropertyValueFactory<>("effectSizeType"));
        oddsColumn.setCellValueFactory(new PropertyValueFactory<>("odds"));
        riskFactorTypeColumn.setCellValueFactory(new PropertyValueFactory<>("riskFactorType"));
        riskFactorColumn.setCellValueFactory(new PropertyValueFactory<>("riskFactor"));
        timeMeanColumn.setCellValueFactory(new PropertyValueFactory<>("mean"));
        timeSDcolumn.setCellValueFactory(new PropertyValueFactory<>("sd"));
        timeUnitColumn.setCellValueFactory(new PropertyValueFactory<>("timeUnit"));

        riskFactorsTable.getColumns().addAll(modifierColumn, oddsColumn, riskFactorTypeColumn, riskFactorColumn, timeMeanColumn, timeSDcolumn, timeUnitColumn);

    }

    @FXML
    void modifierComboClicked(ActionEvent event) {

    }

    @FXML
    void riskFactorClicked(ActionEvent event) {

    }

    @FXML
    void timeUnitComboClicked(ActionEvent event) {

    }

    @FXML
    void clearClicked(ActionEvent event) {
        event.consume();
        effectSizeTypeComboBox.getSelectionModel().clearSelection();
        oddsField.clear();
        riskFactorCombo.getSelectionModel().clearSelection();
        riskFactorTextField.clear();
        timeMeanField.clear();
        timeUnitCombo.getSelectionModel().clearSelection();
        timeSDfield.clear();
    }

    @FXML
    void addClicked(ActionEvent event) {

        RiskFactorRow newRow = new RiskFactorRow();
        newRow.setEffectSizeType(effectSizeTypeComboBox.getSelectionModel().getSelectedItem());
        newRow.setOdds(Float.parseFloat(oddsField.getText()));
        newRow.setRiskFactorType(riskFactorCombo.getSelectionModel().getSelectedItem());
        newRow.setRiskfactor(riskFactorTextField.getText());
        newRow.setMean(Float.parseFloat(timeMeanField.getText()));
        newRow.setSd(Float.parseFloat(timeSDfield.getText()));
        newRow.setTimeUnit(timeUnitCombo.getSelectionModel().getSelectedItem());

        riskFactorRows.add(newRow);
        clearClicked(event);

    }

    @FXML
    void deleteClicked(ActionEvent event) {
        event.consume();
        riskFactorRows.remove(riskFactorsTable.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void confirmClicked(ActionEvent e) {
        e.consume();
        confirm.accept(Signal.DONE);
        riskFactorRows.clear();
    }

    @FXML
    private void cancelClicked(ActionEvent e) {
        e.consume();
        confirm.accept(Signal.CANCEL);
        riskFactorRows.clear();
    }

    public enum SimpleTimeUnit{
        Year,
        Month,
        Day
    }

    public class RiskFactorRow {
        private SimpleStringProperty diseaseName;
        private Riskfactor.RiskFactorType riskFactorType;
        private SimpleStringProperty riskfactor;
        private TimeAwareEffectSize.EffectSizeType effectSizeType;
        private SimpleFloatProperty mean;
        private SimpleFloatProperty sd;
        private SimpleTimeUnit timeUnit;
        private SimpleFloatProperty odds;

        public RiskFactorRow() {
            this.diseaseName = new SimpleStringProperty();
            this.riskfactor = new SimpleStringProperty();
            this.mean = new SimpleFloatProperty();
            this.sd = new SimpleFloatProperty();
            this.odds = new SimpleFloatProperty();
        }

        public String getDiseaseName() {
            return diseaseName.get();
        }

        public SimpleStringProperty diseaseNameProperty() {
            return diseaseName;
        }

        public void setDiseaseName(String diseaseName) {
            this.diseaseName.set(diseaseName);
        }

        public String getRiskfactor() {
            return riskfactor.get();
        }

        public SimpleStringProperty riskfactorProperty() {
            return riskfactor;
        }

        public void setRiskfactor(String riskfactor) {
            this.riskfactor.set(riskfactor);
        }

        public TimeAwareEffectSize.EffectSizeType getEffectSizeType() {
            return effectSizeType;
        }

        public void setEffectSizeType(TimeAwareEffectSize.EffectSizeType effectSizeType) {
            this.effectSizeType = effectSizeType;
        }

        public float getMean() {
            return mean.get();
        }

        public SimpleFloatProperty meanProperty() {
            return mean;
        }

        public void setMean(float mean) {
            this.mean.set(mean);
        }

        public float getSd() {
            return sd.get();
        }

        public SimpleFloatProperty sdProperty() {
            return sd;
        }

        public void setSd(float sd) {
            this.sd.set(sd);
        }

        public SimpleTimeUnit getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(SimpleTimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }

        public float getOdds() {
            return odds.get();
        }

        public SimpleFloatProperty oddsProperty() {
            return odds;
        }

        public void setOdds(float odds) {
            this.odds.set(odds);
        }

        public Riskfactor.RiskFactorType getRiskFactorType() {
            return riskFactorType;
        }

        public void setRiskFactorType(Riskfactor.RiskFactorType riskFactorType) {
            this.riskFactorType = riskFactorType;
        }
    }

}

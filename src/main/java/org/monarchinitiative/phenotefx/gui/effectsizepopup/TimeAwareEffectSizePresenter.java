package org.monarchinitiative.phenotefx.gui.effectsizepopup;

import base.PointValueEstimate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import model.CurationMeta;
import model.TimeAwareEffectSize;
import org.monarchinitiative.phenotefx.gui.Signal;
import org.monarchinitiative.phenotefx.gui.evidencepopup.EvidenceFactory;
import org.monarchinitiative.phenotefx.gui.pointvalueestimate.PointValueEstimateFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class TimeAwareEffectSizePresenter {

    @FXML
    private Label titleLabel;

    @FXML
    private ComboBox<TimeAwareEffectSize.EffectSizeType> effectSizeTypeCombo;

    @FXML
    private TextField sizeTextField;

    @FXML
    private ComboBox<TimeAwareEffectSize.TrendType> trendTypeCombo;

    @FXML
    private TextField onsetField;

    @FXML
    private TextField plateauField;

    @FXML
    private TextField evidenceField;

    @FXML
    private TextField curationField;

    @FXML
    private ListView<TimeAwareEffectSize> listView;


    private TimeAwareEffectSize beingEditted = new TimeAwareEffectSize.Builder().build();

    private ObservableList<TimeAwareEffectSize> ezObservableList = FXCollections.observableArrayList();

    private Consumer<Signal> signalConsumer;

    private String curator;

    private boolean isUpdated;

    private ObjectMapper mapper = new ObjectMapper();


    public void setWindowTitle(String title){
        titleLabel.setText(title);
    }

    public void setCurrent(Collection<TimeAwareEffectSize> current){
        if (current != null){
            ezObservableList.addAll(current);
        }
        refresh();
    }

    public void setSignal(Consumer<Signal> signalConsumer){
        this.signalConsumer = signalConsumer;
    }

    public void setCurator(String curator){
        this.curator = curator;
    }

    @FXML
    void initialize(){
        effectSizeTypeCombo.getItems().addAll(TimeAwareEffectSize.EffectSizeType.values());
        trendTypeCombo.getItems().addAll(TimeAwareEffectSize.TrendType.values());

        onsetField.setDisable(true);
        plateauField.setDisable(true);
        trendTypeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (observable != null){
                if (newValue == TimeAwareEffectSize.TrendType.FLAT){
                    onsetField.setDisable(true);
                    plateauField.setDisable(true);
                } else {
                    onsetField.setDisable(false);
                    plateauField.setDisable(false);
                }
            }
        });
        listView.setItems(ezObservableList);
        listView.setCellFactory(new Callback<ListView<TimeAwareEffectSize>, ListCell<TimeAwareEffectSize>>() {
            @Override
            public ListCell<TimeAwareEffectSize> call(ListView<TimeAwareEffectSize> param) {
                return new ListCell<TimeAwareEffectSize>(){
                    @Override
                    protected void updateItem(TimeAwareEffectSize ez, boolean bl){
                        super.updateItem(ez, bl);
                        if (ez != null) {
                            String text;
                            try {
                                text = mapper.writeValueAsString(ez);
                            } catch (JsonProcessingException e){
                                text = "JsonProcessingException";
                            }
                            setText(text);
                        }
                    }
                };
            }
        });
    }


    @FXML
    void changeEZClicked(ActionEvent event) {
        event.consume();
        PointValueEstimate ez = beingEditted.getSize();
        PointValueEstimateFactory factory = new PointValueEstimateFactory(ez);
        boolean updated = factory.openDiag();
        if (updated){
            beingEditted.setSize(factory.updated());
            refresh();
        }
    }

    @FXML
    void evidenceClicked(ActionEvent event) {
        event.consume();
        model.Evidence evidence = beingEditted.getEvidence();
        EvidenceFactory factory = new EvidenceFactory(evidence);
        boolean updated = factory.openDiag();
        if (updated){
            beingEditted.setEvidence(factory.getEvidence());
            refresh();
        }
    }

    @FXML
    void addClicked(ActionEvent event){
        event.consume();

        beingEditted.setCurationMeta(new CurationMeta.Builder()
                .curator(this.curator)
                .timestamp(LocalDate.now())
                .build());
        ezObservableList.add(beingEditted);
        beingEditted = new TimeAwareEffectSize.Builder().build();
        clear();

    }

    @FXML
    void clearClicked(ActionEvent event){
        event.consume();
        clear();
    }

    private void clear(){
        beingEditted = new TimeAwareEffectSize.Builder().build();
        refresh();
    }

    private void refresh(){
        effectSizeTypeCombo.getSelectionModel().select(beingEditted.getType());
        try {
            sizeTextField.setText(mapper.writeValueAsString(beingEditted.getSize()));
        } catch (Exception e) {
            //eat exception
            sizeTextField.clear();
        }

        try {
            onsetField.setText(Double.toString(beingEditted.getYearsToOnset()));
            plateauField.setText(Double.toString(beingEditted.getYearsToPlateau()));
        } catch (NullPointerException e){
            onsetField.clear();
            plateauField.clear();
        }

        try {
            evidenceField.setText(mapper.writeValueAsString(beingEditted.getEvidence()));
        } catch (Exception e) {
            //eat exception
            evidenceField.clear();
        }

        try {
            curationField.setText(mapper.writeValueAsString(beingEditted.getCurationMeta()));
        } catch (Exception e) {
            //eat exception
            curationField.clear();
        }
    }


    @FXML
    void confirmClicked(ActionEvent event){
        event.consume();

        isUpdated = true;
        signalConsumer.accept(Signal.DONE);
    }

    @FXML
    void cancelClicked(ActionEvent event){
        event.consume();

        signalConsumer.accept(Signal.CANCEL);
    }

    @FXML
    void deleteClicked(ActionEvent event){
        event.consume();
        TimeAwareEffectSize toRemove = listView.getSelectionModel().getSelectedItem();
        ezObservableList.remove(toRemove);
    }

    @FXML
    void editClicked(ActionEvent event){
        event.consume();
        beingEditted = listView.getSelectionModel().getSelectedItem();
        ezObservableList.remove(beingEditted);
        refresh();

    }

    public boolean isUpdated() {
        return this.isUpdated;
    }

    public List<TimeAwareEffectSize> updated(){
        return new ArrayList<>(ezObservableList);
    }

}

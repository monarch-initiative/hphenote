package org.monarchinitiative.phenotefx.gui.infoviewer;

/*
 * #%L
 * PhenoteFX
 * %%
 * Copyright (C) 2017 Peter Robinson
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

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import org.monarchinitiative.phenotefx.gui.Signal;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Created by peterrobinson on 7/3/17.
 */
public class InfoViewerPresenter implements Initializable {
    @FXML private WebView wview;
    @FXML private Button closeButton;

    private Consumer<Signal> signal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> this.closeButton.requestFocus());
    }

    public void setData(String html) {
        wview.getEngine().loadContent(html,"text/html");
    }



    public void setSignal(Consumer<Signal> signal) {
        this.signal = signal;
    }


    @FXML
    public void closeWindow(ActionEvent e) {
        e.consume();
        signal.accept(Signal.DONE);
    }
}
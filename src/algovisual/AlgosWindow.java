/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algovisual;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author meng
 */
public class AlgosWindow extends Stage {
    private final ObservableList<Data> dataSets = FXCollections.checkedObservableList(FXCollections.observableArrayList(), Data.class);
    private final ArrayList<Thread> threads = new ArrayList<>();
    private int row, col;
    private final double PRI_COMPONENT_HEIGHT_RATIO = 1.0, 
                         PRI_COMPONENT_WIDTH_RATIO  = 1.0, 
                         SEC_COMPONENT_HEIGHT_RATIO = 0.2, 
                         SEC_COMPONENT_WIDTH_RATIO  = 1.0;
    
    AlgosWindow(Window parent) {
        GridPane mainPane = new GridPane();
        
        dataSets.addListener( (ListChangeListener.Change<? extends Data> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Data d : change.getAddedSubList()) {
                        AlgoBarCanvas primary = new AlgoBarCanvas();
                        primary.setData(d);
                        primary.setHeight(PRI_COMPONENT_HEIGHT_RATIO * AlgoBarCanvas.HEIGHT);
                        primary.setWidth(PRI_COMPONENT_WIDTH_RATIO * AlgoBarCanvas.WIDTH);
                        d.registerObserver(primary);
                        AlgoBarCanvas secondary = new AlgoBarCanvas();
                        secondary.setData(d.getTemp());
                        secondary.setHeight(SEC_COMPONENT_HEIGHT_RATIO * AlgoBarCanvas.HEIGHT);
                        secondary.setWidth(SEC_COMPONENT_WIDTH_RATIO * AlgoBarCanvas.WIDTH);
                        d.getTemp().registerObserver(secondary);
                        
                        threads.add(new Thread(d.getAlgo()));

                        mainPane.add(secondary, col, row++);
                        mainPane.add(primary, col, row++);
                    }
                }
            }
        } );
        
        this.setScene(new Scene(mainPane));
        
        this.initModality(Modality.NONE);
//        this.initOwner(parent);
    }
    
    void add(Data d) {
        if (d.getAlgo() == null) throw new IllegalStateException("Data objects must have attached SortingAlgorithm before being added to AlgosWindow.");
        dataSets.add(d);
    }
    
    void startAll() {
        threads.forEach( thread -> thread.start() );
    }
    
    void endAll() {
        threads.forEach( t -> { 
            System.out.println("Stopping Thread");
            if (t.isAlive()) t.interrupt(); 
        } );
    }
}

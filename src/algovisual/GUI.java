/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algovisual;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 *
 * @author meng
 */
public class GUI extends Application {
    private AlgosWindow algosWindow;
    private ControlPanel cp;
    
    @Override
    public void start(Stage primaryStage) {
        
        cp = new ControlPanel(this);
        Scene scene = new Scene(cp);
        
        loadDataSets(cp);
        
        scene.getStylesheets().add(GUI.class.getResource("GUI.css").toExternalForm());
        
        primaryStage.setOnCloseRequest( windowEvent -> {
            if (algosWindow != null) algosWindow.close();
            try {
                saveDataSets(cp);
            } catch (Exception e) {
                popupNotice("Sorry, I attempted to save existing datasets but failed.\n" + e);
            }
        } );
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    void createSimulation(Data d, SortingAlgorithm.Catalog algo, Window parent) {
        if (algosWindow == null) {
            cp.enableAlgoControls();
            algosWindow = new AlgosWindow(parent);
        }
        algosWindow.setOnCloseRequest( event -> {
            cp.disableAlgoControls();
            algosWindow = null;
        } );
        algosWindow.add(Data.makeDataFrom(d, algo));
        algosWindow.sizeToScene();
        algosWindow.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    static void saveDataSets(ControlPanel cp) throws FileNotFoundException, IOException {
        try (   FileOutputStream fos = new FileOutputStream("datasets.dat");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
            )
        {
            ArrayList<Data> datasets = new ArrayList<>(cp.getDataSets());
            oos.writeObject(datasets);
        }
    }
    
    static void loadDataSets(ControlPanel cp) {
        File savefile = new File("datasets.dat");
        if (savefile.exists()) {
            try (   FileInputStream fis = new FileInputStream(savefile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                )
            {
                @SuppressWarnings("unchecked")
                ArrayList<Data> datasets = (ArrayList<Data>) ois.readObject();
                for (Data d : datasets) { cp.addDataSet(d); }
            }
            catch (ClassNotFoundException | IOException cnfe) {
                System.out.println(cnfe);
            }
        }
    }
    
    void popupNotice(String msg) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        HBox box = new HBox(5);
        Text msgText = new Text(msg);
        Button ok = new Button("OK");
        box.getChildren().addAll(msgText, ok);
        ok.setOnAction( event -> stage.close() );
        stage.setScene(new Scene(box));
        stage.show();
    }
    
    void startAlgos() {
        algosWindow.startAll();
    }
}

class ControlPanel extends VBox {
    private final ObservableList<Data> dataSets = FXCollections.observableArrayList();
    private final Button startBtn;
    
    ControlPanel(GUI parent) {
        GridPane dragdropPane = new GridPane();
        dragdropPane.getColumnConstraints().add(new ColumnConstraints(DataListItem.WIDTH));
        dragdropPane.getColumnConstraints().add(new ColumnConstraints(AlgoListItem.WIDTH));
        
        // algorithm icons
        int row = 0;
        for (SortingAlgorithm.Catalog algo : SortingAlgorithm.Catalog.values()) {
            AlgoListItem item = new AlgoListItem(algo, parent);
            dragdropPane.add(item, 1, row++);
        }
        
        // dataset icons
        dataSets.addListener((ListChangeListener.Change<? extends Data> change) -> {
            while (change.next()) {
                dragdropPane.getChildren().removeAll(
                        dragdropPane.getChildren().filtered( node -> GridPane.getColumnIndex(node) == 0 )
                );
                Iterator<Data> iter = dataSets.iterator();
                int dataItemRow = 0;
                while (iter.hasNext()) {
                    Data d = iter.next();
                    if (d == null) {
                        iter.remove();
                    }
                    else {
                        DataListItem item = new DataListItem(d, parent);
                        applyDeleteContext(item);
                        dragdropPane.add(item, 0, dataItemRow++);
                    }
                }
            }
        } );
        
        super.getChildren().add(dragdropPane);
        
        // CONTROL BUTTONS
        
        // New Dataset button
        Button addDataBtn = new Button("New Dataset");
        addDataBtn.setOnAction(event -> {
            dataSets.add(DataConfigPane.requestDataConfig());
        } );
        addDataBtn.setMaxWidth(Double.MAX_VALUE);

        // Start Sorting button
        startBtn = new Button("Start Sorting");
        startBtn.setOnAction(event -> {
            parent.startAlgos();
            ((Button)event.getSource()).setDisable(true);
        } );
        startBtn.setMaxWidth(Double.MAX_VALUE);
        
        disableAlgoControls();
        
        super.getChildren().addAll(addDataBtn, startBtn);
    }
    
    ObservableList<Data> getDataSets() { return dataSets; }
    void addDataSet(Data d) { dataSets.add(d); }
    void removeDataSet(Data d) { dataSets.remove(d); }
    
    void enableAlgoControls() {
        if (startBtn != null) startBtn.setDisable(false);
    }
    
    void disableAlgoControls() {
        if (startBtn != null) startBtn.setDisable(true);
    }
    
    private void applyDeleteContext(DataListItem item) {
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction( event -> dataSets.remove(item.getData()) );
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(delete);
        item.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            menu.show(item, event.getScreenX(), event.getScreenY());
        });
    }
}

final class AlgoListItem extends Canvas {
    static final int WIDTH = 100, HEIGHT = 50;
    private final SortingAlgorithm.Catalog algoType;
    private final GUI mainGui;
    
    AlgoListItem(SortingAlgorithm.Catalog algoType, GUI parent) {
        this.algoType = algoType;
        this.mainGui = parent;
        
        this.setWidth(WIDTH);
        this.setHeight(HEIGHT);
        this.setOnMouseDragReleased(dragEvent -> {
            if (dragEvent.getGestureSource() instanceof DataListItem) {
                DataListItem dli = (DataListItem)dragEvent.getGestureSource();
                mainGui.createSimulation(dli.getData(), this.algoType, this.getScene().getWindow());
            }
        } );
        
        drawIcon();
    }
    
    void drawIcon() {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, this.getWidth(), this.getHeight());
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(algoType.toString(), this.getWidth()/2, this.getHeight()/2, this.getWidth()-20);
    }
}

class DataListItem extends Canvas {
    static final int WIDTH = 100, HEIGHT = 50,
                             POPUP_WIDTH = 150, POPUP_HEIGHT = 50;
    private final Data data;
    private Stage algoStage;
    
    DataListItem(Data d, GUI parent) {
        this.data = d;
        
        this.setWidth(WIDTH);
        this.setHeight(HEIGHT);
        // hover behaviour
        this.setOnMouseEntered(mouseEvent -> {
            trackStage(mouseEvent);
            getStage().show();
        } );
        this.setOnMouseMoved(mouseEvent -> {
            trackStage(mouseEvent);
        } );
        this.setOnMouseExited(mouseEvent -> {
            getStage().hide();
        } );
        // drag behaviour
        this.setOnDragDetected(mouseDragEvent -> {
            System.out.println("starting full drag");
            getStage().hide();
            startFullDrag();
        } );
        
        drawIcon();
    }
    
    private void drawIcon() {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, this.getWidth(), this.getHeight());
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("DataSet", this.getWidth()/2, this.getHeight()/2, this.getWidth()-20);
    }
    
    private void trackStage(MouseEvent evt) {
        Stage s = getStage();
        s.setX(evt.getScreenX() + 10);
        s.setY(evt.getScreenY());
    }
    
    private Stage getStage() {
        if (algoStage == null) {
            System.out.println("Creating new algoStage");
            AlgoBarCanvas c = new AlgoBarCanvas(new Color(0, 0, 0, 0.5));
            c.setData(data);
            c.setWidth(POPUP_WIDTH);
            c.setHeight(POPUP_HEIGHT);
            c.draw();
            Scene scene = new Scene(new Group(c), POPUP_WIDTH, POPUP_HEIGHT, Color.TRANSPARENT);
            algoStage = new Stage(StageStyle.TRANSPARENT);
            algoStage.setScene(scene);
        }
        return algoStage;
    }
    
    Data getData() { return data; }
}


class DataConfigPane extends BorderPane {
    private static final int DATA_MAX_SIZE = 1000, DATA_MIN_SIZE = 10, DATA_STEP_SIZE = 10, DATA_DEFAULT_SIZE = 100,
                             DATA_MIN = 0, DATA_MAX = 1000, DATA_STEP = 10, DATA_DEFAULT_MIN = 10, DATA_DEFAULT_MAX = 100;
    
    private Data data;
    private final AlgoBarCanvas canvas;
    private final IntegerProperty length, lowerBound, upperBound;
    private Distribution distr;
    private Order order;
    
    private enum Distribution {
        RANDOM_NORMAL("Random Normal Dist."),
        EVEN("Even Dist.");
        
        private final String name;
        
        Distribution(String name) {
            this.name = name;
        }
        
        @Override public String toString() { return name; }
        
        public static Distribution get(String name) {
            for (Distribution d : Distribution.values())
                if (d.toString().equalsIgnoreCase(name)) return d;
            return null;
        }
    }
    
    private enum Order {
        ASCENDING("Ascending"),
        DESCENDING("Descending"),
        SHUFFLED("Shuffled"),
        ALMOST_SORTED("Almost Sorted");
        
        private final String name;
        
        Order(String name) {
            this.name = name;
        }
        
        @Override public String toString() { return name; }
        
        public static Order get(String name) {
            for (Order o : Order.values())
                if (o.toString().equalsIgnoreCase(name)) return o;
            return null;
        }
    }
    
    private DataConfigPane(Stage parentStage) {
        length = new SimpleIntegerProperty(DATA_DEFAULT_SIZE);
        lowerBound = new SimpleIntegerProperty(DATA_DEFAULT_MIN);
        upperBound = new SimpleIntegerProperty(DATA_DEFAULT_MAX);
        
        VBox left = new VBox();
        
        // dataset configuration options
        left.getChildren().addAll(makeDataBasicConfigPane(), makeDistributionSelector(), makeOrderSelector());
        
        left.getStyleClass().add("box");
        
        // done button
        Button doneBtn = new Button("Done");
        doneBtn.setOnAction( event -> parentStage.close() );
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction( event -> { data = null; parentStage.close(); } );
        HBox doneBtnBox = new HBox(10);
        doneBtnBox.setAlignment(Pos.CENTER_RIGHT);
        doneBtnBox.getChildren().addAll(cancelBtn, doneBtn);
        
        left.getChildren().add(doneBtnBox);
        
        this.setLeft(left);
        
        canvas = new AlgoBarCanvas(Color.BLACK);
        canvas.setData(Data.makeEmptyData(DATA_MIN));
        canvas.setWidth(500);
        canvas.setHeight(200);
        canvas.draw();
        this.setCenter(canvas);
    }
    
    Data getData() { return data; }
    
    static Data requestDataConfig() {
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        
        DataConfigPane dcp = new DataConfigPane(stage);
        Scene scene = new Scene(dcp);
        scene.getStylesheets().add(DataConfigPane.class.getResource("GUI.css").toExternalForm());
        
        stage.setScene(scene);
        stage.showAndWait();
        
        return dcp.getData();
    }
    
    private GridPane makeDataBasicConfigPane() {
        GridPane pane = new GridPane();

        Label l;
        Spinner<Integer> sp;
        
        l = new Label("Data Length:");
        sp = new Spinner<>(DATA_MIN_SIZE, DATA_MAX_SIZE, DATA_DEFAULT_SIZE, DATA_STEP_SIZE);
        sp.setMaxWidth(80);
        sp.setEditable(true);
        length.bind(sp.valueProperty());
        pane.add( l, 0, 0);
        pane.add(sp, 1, 0);
        
        l = new Label("Lower Limit:");
        Spinner<Integer> lowerBoundSpinner = new Spinner<>(DATA_MIN, DATA_MAX, DATA_DEFAULT_MIN);
        lowerBoundSpinner.setMaxWidth(80);
        lowerBoundSpinner.setEditable(true);
        lowerBound.bind(lowerBoundSpinner.valueProperty());
        pane.add(                l, 0, 1);
        pane.add(lowerBoundSpinner, 1, 1);
        
        l = new Label("Upper Limit:");
        Spinner<Integer> upperBoundSpinner = new Spinner<>(DATA_MIN, DATA_MAX, DATA_DEFAULT_MAX);
        upperBoundSpinner.setMaxWidth(80);
        upperBoundSpinner.setEditable(true);
        upperBound.bind(upperBoundSpinner.valueProperty());
        pane.add(                l, 0, 2);
        pane.add(upperBoundSpinner, 1, 2);
        
        IntegerSpinnerValueFactory lowerBoundValueFactory = (IntegerSpinnerValueFactory)lowerBoundSpinner.valueFactoryProperty().get();
        IntegerSpinnerValueFactory upperBoundValueFactory = (IntegerSpinnerValueFactory)upperBoundSpinner.valueFactoryProperty().get();
        
        lowerBoundValueFactory.maxProperty().bind(upperBoundValueFactory.valueProperty());
        upperBoundValueFactory.minProperty().bind(lowerBoundValueFactory.valueProperty());
        
        pane.getStyleClass().addAll("box", "outerBox", "grid");
        
        return pane;
    }
    
    private VBox makeDistributionSelector() {
        VBox box = new VBox();
        ToggleGroup group = new ToggleGroup();
        
        RadioButton rb;
        
        for (Distribution d : Distribution.values()) {
            rb = new RadioButton(d.toString());
            rb.setToggleGroup(group);
            box.getChildren().add(rb);
        }
        
        group.selectedToggleProperty().addListener( obs -> {
            String label = ( (RadioButton)(group.getSelectedToggle()) ).getText();
            distr = Distribution.get(label);
            updateData();
        } );
        
        box.getStyleClass().addAll("box", "outerBox");
        
        return box;
    }
    
    private VBox makeOrderSelector() {
        VBox box = new VBox();
        
        ToggleGroup group = new ToggleGroup();
        
        RadioButton rb;
        
        for (Order o : Order.values()) {
            rb = new RadioButton(o.toString());
            rb.setToggleGroup(group);
            box.getChildren().add(rb);
        }
        
        group.selectedToggleProperty().addListener( obs -> {
            String label = ( (RadioButton)(group.getSelectedToggle()) ).getText();
            order = Order.get(label);
            updateData();
        } );
        
        box.getStyleClass().addAll("box", "outerBox");
        
        return box;
    }
    
    private void updateData() {
        //System.out.println("Updating Data Object");
        if (distr == null || order == null) return;
        
        int[] arr;
        switch (distr) {
            case RANDOM_NORMAL:
                arr = IntGenerator.randomIntsArray(length.get(), lowerBound.get(), upperBound.get());
                break;
            case EVEN:
                arr = IntGenerator.evenDistributionIntsArray(length.get(), lowerBound.get(), upperBound.get());
                break;
            default:
                throw new AssertionError(distr.name());
        }
        
        if (arr == null) return;
        switch (order) {
            case ASCENDING:
                Arrays.sort(arr);
                break;
            case DESCENDING:
                Arrays.sort(arr);
                arr = IntGenerator.reverse(arr);
                break;
            case SHUFFLED:
                IntGenerator.shuffle(arr);
                break;
            case ALMOST_SORTED:
                int freq = Math.min(length.get() / 5, 5);
                arr = IntGenerator.almostSortedIntsArray(length.get(), lowerBound.get(), upperBound.get(), freq, 2);
                break;
            default:
                throw new AssertionError(order.name());
        }
        
        data = Data.makeDataFrom(arr, null);
        canvas.setData(data);
        //System.out.println(data);
    }
}
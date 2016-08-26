/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algovisual;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *
 * @author meng
 */
public class AlgoBarCanvas extends Canvas {
    private static final Color DEF_BAR_COLOUR = Color.CORNFLOWERBLUE, DEF_BG = Color.BLACK;
    public static final int WIDTH = 1200, HEIGHT = 100;
    private Data data;
    private GraphicsContext gc;
    private double barWidth;
    private Color bg;
    
    AlgoBarCanvas(Color bg) {
        super();
        init();
        this.bg = bg;
    }
    
    AlgoBarCanvas() {
        this(DEF_BG);
    }
    
    void setData(Data d) {
        data = d;
//        data.registerObserver(this);
        draw();
    }
    
    private void init() {
//        setWidth(WIDTH);
//        setHeight(HEIGHT);
//        draw();
        widthProperty().addListener(observerable -> draw() );
        addEventHandler(AlgoEvent.COMPARE_EVENT, event -> {
            Platform.runLater(() -> {
                draw();
                highlight(event.getIndex1(), Color.DARKBLUE);
                highlight(event.getIndex2(), Color.CORNFLOWERBLUE);
//                System.out.println(event);
            });
        });
        addEventHandler(AlgoEvent.SWAP_EVENT, event -> {
            Platform.runLater(() -> {
                draw();
                highlight(event.getIndex1(), Color.CORNFLOWERBLUE);
                highlight(event.getIndex2(), Color.DARKBLUE);
//                System.out.println(event);
            });
        });
        addEventHandler(AlgoEvent.COPY_EVENT, event -> {
            Platform.runLater(() -> {
                draw();
                for (int i : event.getIndices()) highlight(i, Color.CHOCOLATE);
//                System.out.println(event);
            });
        });
        addEventHandler(AlgoEvent.FINISHED_EVENT, event -> {
            Platform.runLater(() -> {
                draw();
                StringBuilder sb = new StringBuilder();
                sb
                        .append(data.getAlgo().getName())
                        .append(" finished in ")
                        .append(event.getIndex1()).append(" compares and ")
                        .append(event.getIndex2()).append("(").append(event.getIndices()[2]).append(") ")
                        .append("copies(swaps)")
                        ;
                gc.setFill(Color.RED);
                gc.setFont(Font.font("Calibri", FontWeight.LIGHT, 16));
                gc.fillText(sb.toString(), 20, 20);
            } );
        } );
    }
    
    private void highlight(int index, Color c) {
        gc.setFill(c);
        drawBar(index);
    }

    private void drawBar(int index) {
//        System.out.println(data.normalized(index));
        double x = index * (barWidth + 1);
        double height = (1 - data.normalized(index)) * this.heightProperty().doubleValue();
        gc.fillRect(x, height, barWidth, this.heightProperty().doubleValue());
    }
    
    private void calcBarWidth() {
        barWidth = this.getWidth() / data.length() - 1;
    }
    
    
    public void draw() {
        if (gc == null) {
            gc = this.getGraphicsContext2D();
        }
        calcBarWidth();
        gc.setFill(bg);
        gc.clearRect(0, 0, this.getWidth(), this.getHeight());
        gc.fillRect(0, 0, this.getWidth(), this.getHeight());
        gc.setFill(Color.GREY);
        
        for (int i = 0; i < data.length(); i++) {
            if (i >= data.getFocusFirst() && i <= data.getFocusLast()) 
                gc.setFill(Color.WHITESMOKE);
            else 
                gc.setFill(Color.GREY);
            drawBar(i);
        }
    }
}



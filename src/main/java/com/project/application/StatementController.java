package com.project.application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.sql.Connection;

public class StatementController {
    private VBox main;
    private HBox h1,h2,searchPane;
    private Label title,from,to;
    private DatePicker fromDate,toDate;
    private TextField searchText;
    private Button searchBtn,processBtn,clearBtn,printBtn;
    private Region spacer;
    private TableView<StatementEntity> stmtTable;
    private int colSrno;
    private String colType,colInvoiceNo,colSCName,colSCId;
    private int colTotalQty;
    private double colSubTotal;
    private String colPaymentStatus;

    public VBox loadContent(Connection conn){
        title = new Label("Statements");

        searchPane = new HBox();
        searchText = new TextField();
        searchText.setPrefWidth(200);
        searchBtn = new Button();
        searchBtn.setPrefWidth(25);
        searchPane.setAlignment(Pos.CENTER);
        searchPane.getChildren().addAll(searchText,searchBtn);

        h1 = new HBox();
        h1.getChildren().addAll(title,searchPane);

        from = new Label("From: ");
        fromDate = new DatePicker();
        to = new Label("To: ");
        toDate = new DatePicker();
        processBtn = new Button("Process");
        clearBtn = new Button("Clear");
        spacer = new Region();
        HBox.setHgrow(spacer,Priority.ALWAYS);
        printBtn = new Button("Print");
//        printBtn.setAlignment(Pos.CENTER_RIGHT);
        h2 = new HBox();
        h2.getChildren().addAll(from,fromDate,to,toDate,processBtn,clearBtn,spacer,printBtn);



        main = new VBox();
        main.getChildren().addAll(h1,h2);

        addStyles();
        return main;
    }

    public void addStyles(){
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;");
        HBox.setMargin(searchPane,new Insets(0,0,0,450));
        HBox.setMargin(to,new Insets(0,0,0,20));
        HBox.setMargin(processBtn,new Insets(0,0,0,20));
        HBox.setMargin(clearBtn,new Insets(0,0,0,10));
        HBox.setMargin(printBtn,new Insets(0,10,0,0));
        searchBtn.setStyle("-fx-background-image: url('/search-icon.png'); " +
                "-fx-background-size: 15px; " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-position: center;");
        processBtn.setPrefWidth(150);
        clearBtn.setPrefWidth(150);
        printBtn.setPrefWidth(150);
        h1.setPadding(new Insets(5,0,5,10));
        h1.setStyle("-fx-border-color:black;-fx-border-width: 0 0 1px 0;");
        h2.setPadding(new Insets(5,0,0,10));
        h2.setStyle("-fx-border-color:black;-fx-border-width: 0 0 1px 0;");
    }
}

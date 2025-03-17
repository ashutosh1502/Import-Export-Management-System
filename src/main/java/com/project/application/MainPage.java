package com.project.application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainPage{
    //Declarations.
    protected Stage primaryStage;
    private static final String UNITECH_INDUSTRIES = "Unitech Industries";
    private static final String SURYA_INDUSTRIES = "Surya Industries";
    private String sectionName;
    protected Scene mainScene;
    private VBox mainPane=new VBox();
    private HBox headingPane=new HBox(10);
    private HBox leftHeadingPane=new HBox();
    private HBox rightHeadingPane=new HBox();
    private Button switchBtn=new Button();
    protected Label heading;
    private SplitPane bodyPane;
    private HBox footerPane;
    private HBox printOptionPane;
    private HBox operationsPane;
    private Button printBtn;
    private List<Button>[] operations;
    private ScrollPane scrollPane;
    private ImportController importController=new ImportController();
    private ExportController exportController=new ExportController();
    private StockController stockController=new StockController();
    private StatementController statementController = new StatementController();
    private VBox optionsPane;
    private Button[] options;
    private Font currFont;
    private String[] btnTexts=new String[]{"Imports","Exports","Stocks","Statements","Report"};

    //database
    String url="jdbc:oracle:thin:@localhost:1521:xe";
    String username1="unitech_admin";
    String password1="unitech@1234";
    String username2="surya_ind_admin";
    String password2="surya@1234";
    Connection conn;

    @SuppressWarnings("unchecked")
    public MainPage(String sectionName){
        this.sectionName=sectionName;
        primaryStage=new Stage();
        connectDatabase();

    //HEADING BAR PART ----------------------------------------------------------------------
        heading=new Label();
        heading.setStyle("-fx-font-size: 20;-fx-text-fill:white;");
        switchBtn.setText("Switch Section");
        switchBtn.setStyle("-fx-background-color:#1a5276;-fx-border-color:#2980b9;-fx-border-width:2px;-fx-text-fill:white;-fx-font-weight:bold;");
        switchBtn.setPrefHeight(35);
        switchBtn.setFocusTraversable(false);
        switchBtn.setOnAction(e ->{
            String newSectionName = sectionName.equals(UNITECH_INDUSTRIES) ? SURYA_INDUSTRIES : UNITECH_INDUSTRIES;
            new MainPage(newSectionName);
            primaryStage.close();
        });
        headingPane.setStyle("-fx-border-color: grey;-fx-border-width: 0 0 1px 0;-fx-border-style: solid;");
        leftHeadingPane.setPadding(new Insets(5,0,5,7));
        leftHeadingPane.setAlignment(Pos.CENTER_LEFT);
        leftHeadingPane.getChildren().add(heading);
        HBox.setHgrow(leftHeadingPane, Priority.ALWAYS);
        rightHeadingPane.setPadding(new Insets(2,2,2,0));
        rightHeadingPane.setAlignment(Pos.CENTER_RIGHT);
        rightHeadingPane.getChildren().add(switchBtn);
        headingPane.setAlignment(Pos.CENTER_LEFT);
        headingPane.getChildren().addAll(leftHeadingPane,rightHeadingPane);
        headingPane.setStyle("-fx-background-color:#1a5276;");
        //--------------------------------------------------------------------------------------------------


        //OPTIONS SIDE BAR PART ---------------------------------------------------------------------------
        bodyPane=new SplitPane();
        optionsPane=new VBox();
        options=new Button[5];

        options[0]=new Button(btnTexts[0]);
        options[1]=new Button(btnTexts[1]);
        options[2]=new Button(btnTexts[2]);
        options[3]=new Button(btnTexts[3]);
        options[4]=new Button(btnTexts[4]);
//        #1a5276
        optionsPane.setStyle("-fx-background-color:#1f618d;-fx-border-color:#2980b9;");
        optionsPane.setPrefWidth(140);
        optionsPane.setMaxWidth(140);
        VBox.setVgrow(optionsPane,Priority.ALWAYS);
        VBox.setVgrow(bodyPane,Priority.ALWAYS);
        HBox.setHgrow(bodyPane,Priority.ALWAYS);
        for(int i=0;i<5;i++){
            final int temp=i;
            options[i].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;");
            options[i].setOnMouseEntered(m -> options[temp].setStyle("-fx-background-color:#1a5276; -fx-text-fill: white;"));
            options[i].setOnMouseExited(m -> options[temp].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;"));
            options[i].focusedProperty().addListener((ob, b, isFocused) -> {
                if (isFocused) {
                    options[temp].setStyle("-fx-background-color: #1a5276; -fx-text-fill: white;");
                } else {
                    options[temp].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;");
                }
            });
            options[i].setPrefSize(150,40);
            addActionListener(i);
            optionsPane.getChildren().add(options[i]);
        }
        options[4].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;");
        options[4].setOnMouseEntered(m -> options[4].setStyle("-fx-background-color: #1a5276; -fx-text-fill: white;"));
        options[4].setOnMouseExited(m -> options[4].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;"));
        options[4].focusedProperty().addListener((ob, b, isFocused) -> {
            if (isFocused) {
                options[4].setStyle("-fx-background-color: #1a5276; -fx-text-fill: white;");
            } else {
                options[4].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;");
            }
        });
        currFont=options[0].getFont();
        options[0].setFont(Font.font(currFont.getFamily(), FontWeight.BOLD,16));
        selectOption(0);
        bodyPane.getItems().add(optionsPane);
        //-------------------------------------------------------------------------------------------------

        //BODY (DISPLAY) PART------------------------------------------------------------------------------
        ImportController importController=new ImportController();
        scrollPane=new ScrollPane(importController.initializeImportsTable(conn));
        scrollPane.setId("scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        bodyPane.getItems().add(scrollPane);
        //-------------------------------------------------------------------------------------------------

        //FOOTER PART--------------------------------------------------------------------------------------
        footerPane=new HBox();
        footerPane.setStyle("-fx-background-color:#1a5276;");
        footerPane.setMinHeight(50);

        printBtn=new Button();
        printBtn.setStyle("-fx-font-weight:bold;-fx-background-color:#d4e6f1;");
        printBtn.setOnMouseEntered(m -> printBtn.setStyle("-fx-background-color:#a9cce3;"));
        printBtn.setOnMouseExited(m -> printBtn.setStyle("-fx-font-weight:bold;-fx-background-color:#d4e6f1;"));
        printBtn.setText("Print");
        printBtn.setPrefSize(120,30);
        HBox.setMargin(printBtn,new Insets(0,0,0,10));
        printOptionPane=new HBox();
        printOptionPane.getChildren().add(printBtn);
        printOptionPane.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(printOptionPane,Priority.ALWAYS);

        operationsPane=new HBox();
        operations=new ArrayList[options.length];
        initializeOperations();
        operationsPane.getChildren().clear();
        for(Button btn:operations[0])
            operationsPane.getChildren().add(btn);
        operationsPane.setAlignment(Pos.CENTER_RIGHT);
        footerPane.getChildren().addAll(printOptionPane,operationsPane);
        //-------------------------------------------------------------------------------------------------

        mainPane.getChildren().addAll(headingPane,bodyPane,footerPane);
        VBox.setVgrow(mainPane,Priority.ALWAYS);
        mainScene=new Scene(mainPane);

//        if(!sectionName.isEmpty()){
            launchPrimaryStage(primaryStage,mainScene,heading);
//        }
    }

    public void launchPrimaryStage(Stage primaryStage,Scene mainScene,Label heading){
        primaryStage.setTitle(sectionName +" section");
        heading.setText((sectionName.equals(UNITECH_INDUSTRIES) ? UNITECH_INDUSTRIES+": Cold Storage" : SURYA_INDUSTRIES+": Agriculture Equipments"));
        primaryStage.setScene(mainScene);
        primaryStage.setMaximized(true);
        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest(w -> {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
//                LOGGER.error("Error closing database connection", e);
            }
        });
        primaryStage.show();
    }

    public void selectOption(int index){
        for(int i=0;i<options.length;i++){
            options[i].setText(btnTexts[i]);
            options[i].setFont(Font.font(currFont.getFamily(), FontWeight.NORMAL,14));
        }
        options[index].setFont(Font.font(currFont.getFamily(), FontWeight.BOLD,16));
    }

    public void addActionListener(int index){
        options[index].setOnAction(e ->{
            selectOption(index);
            if(index==0){
                scrollPane.setContent(importController.initializeImportsTable(conn));
                operationsPane.getChildren().clear();
                for(Button btn:operations[index])
                    operationsPane.getChildren().add(btn);
            }else if(index==1){
                scrollPane.setContent(exportController.loadHistory(conn));
                operationsPane.getChildren().clear();
                for(Button btn:operations[index])
                    operationsPane.getChildren().add(btn);
            }else if(index==2){
                scrollPane.setContent(stockController.loadStocks(conn));
                operationsPane.getChildren().clear();
                for(Button btn:operations[index])
                    operationsPane.getChildren().add(btn);
            }else if(index==3){
                scrollPane.setContent(statementController.loadComponents(conn));
                operationsPane.getChildren().clear();
                for(Button btn:operations[index])
                    operationsPane.getChildren().add(btn);
            }else if(index==4){
                try{
                    new ReportController(sectionName);
                } catch (Exception ex) {
//                    LOGGER.info("Failed to start Report Generation");
//                    LOGGER.error(String.valueOf(ex.getCause()));
                }
            }
        });
    }

    public void initializeOperations(){
        for(int i=0;i<options.length;i++){
            operations[i]=new ArrayList<>();
        }

        operations[0].add(new Button("Add Entry"));
        operations[0].getFirst().setOnAction(e -> importController.addEntry(scrollPane));
        operations[0].add(new Button("View/Update Entry"));
        operations[0].get(1).setOnAction(e -> importController.viewUpdateEntry(scrollPane));
        operations[0].add(new Button("Delete Entry"));
        operations[0].get(2).setOnAction(e -> importController.deleteEntry());

        operations[1].add(new Button("Add Entry"));
        operations[1].getFirst().setOnAction(e -> exportController.addEntry(scrollPane));
        operations[1].add(new Button("View/Update Entry"));
        operations[1].get(1).setOnAction(e -> exportController.viewUpdateEntry(scrollPane));
        operations[1].add(new Button("Delete Entry"));
        operations[1].get(2).setOnAction(e -> exportController.deleteEntry());

        operations[2].add(new Button("Add New Stock"));
        operations[2].getFirst().setOnAction(e ->{stockController.addStock();scrollPane.setContent(stockController.loadStocks(conn));});
        operations[2].add(new Button("Update Stock"));
        operations[2].get(1).setOnAction(e ->{stockController.updateStock();scrollPane.setContent(stockController.loadStocks(conn));});
        operations[2].add(new Button("Delete Stock"));
        operations[2].get(2).setOnAction(e ->{stockController.deleteStock();scrollPane.setContent(stockController.loadStocks(conn));});

        for(List<Button> btnList:operations){
            for(Button btn:btnList){
                btn.setStyle("-fx-background-color:#d4e6f1;");
                btn.setOnMouseEntered(m -> btn.setStyle("-fx-background-color:#a9cce3;"));
                btn.setOnMouseExited(m -> btn.setStyle("-fx-background-color:#d4e6f1;"));
                HBox.setMargin(btn,new Insets(0,10,0,0));
                btn.setMinWidth(100);
                btn.setMinHeight(30);
            }
        }
    }

    public void connectDatabase(){
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");
            if(sectionName.equals(UNITECH_INDUSTRIES)){
                conn= DriverManager.getConnection(url,username1,password1);
            }
            if(sectionName.equals(SURYA_INDUSTRIES)){
                conn= DriverManager.getConnection(url,username2,password2);
            }
            System.out.println("Connected to "+sectionName+" database...");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected Error: " + e.getMessage());
        }
    }
}
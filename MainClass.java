/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expenses.expenses;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.MaskFormatter;

/**
 * Программа учета активов, доходов и расходов
 * @author Ruslan Orlov
 **/
public class MainClass {
    public static final int VIEW_CARD = 0;
    public static final int EDIT_CARD = 1;
    
    JFrame frame;
    JPanel background;
    JMenuBar menuBar;
    
    File fileDB = new File("../BASE/database.ser");
    
    // ------------------------------------------------------
    // Массивы с шапками основной отображаемой таблицы данных
    // ------------------------------------------------------
    String[] columnNames;               // Массив текущей шапки таблицы, которому 
                                        // присваивается один из ниже следующих:
    String[] columnNamesExpenses = {"№ п/п", "Дата", "Сумма", "Вид расходов", 
                                    "Где затрачено", "Описание"};
    String[] columnNamesIncomes  = {"№ п/п", "Дата", "Сумма", "Вид дохода", 
                                    "Источник дохода", "Описание"};
    String[] columnNamesAssets   = {"№ п/п", "Дата", "Сумма", "Вид актива", 
                                    "Где приобретено", "Описание"};
    
    // -----------------------------------------------
    // Списки данных для основной отображаемой таблицы
    // -----------------------------------------------
    ArrayList<Object>   data;           // Текущий обрабатываемый список,которому
                                        // присваивается один из ниже следующих:
    ArrayList<Expenses> expenses;       // Список расходов
    ArrayList<Income>   incomes;        // Список доходов
    ArrayList<Assets>   assets;         // Список активов
    
    JTable              table;          // Таблица для отображения вышеуказанных спиков данных
    MyTableModel        myModel;        // Модель данных для таблицы
    
    JPanel              backgroundOut;  // Панель для отображения таблицы с вышеуказанными спиками данных
    
    int[] sequences;                    // Последовательности для нумерации записей в списках
    String kind;                        // Текущая выполняемая операция в программе
    
    // --------------------------------------------------
    // Поля карточной формы для ввода данных по операциям
    // --------------------------------------------------
    JTextField          numField;
    JFormattedTextField dateField; MaskFormatter formatter;
    JTextField          sumField;
    ArrayList<Details>  detailsList;
    JTextField          kindField;
    JTextField          orgOrPersonField;
    JTextArea           descriptionField;
    
    public MainClass() {
        this.data       = new ArrayList<>();
        this.incomes    = new ArrayList<>();
        this.expenses   = new ArrayList<>();
        this.assets     = new ArrayList<>();
        this.sequences  = new int[3];
    }
    
    public static void main(String[] args) {
        MainClass mс = new MainClass();
        mс.go();
    }
    
    public void go() {
        checkPath();
        loadDatabase();
        setSequences();
        buildApplicationGUI();
    }
    
    public void checkPath() {
        // Проверить наличие пути к БД, если нет - создать путь
        File path = new File("../BASE/");
        if (!path.exists()) {
            path.mkdir();
        }
    }
    
    public void loadDatabase() {
        // Если файл с БД присутствует, то загрузить БД из файла в приложение
        if (fileDB.exists()) {
            try (ObjectInputStream input = 
                    new ObjectInputStream(new FileInputStream(fileDB))){
                expenses = (ArrayList<Expenses>) input.readObject();
                incomes  = (ArrayList<Income>) input.readObject();
                assets   = (ArrayList<Assets>) input.readObject();
            } catch (ClassNotFoundException | IOException ex) {
                Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void setSequences() {
        // Инициализация последовательностей для нумерации записей в БД
        sequences[0] = expenses.size();
        sequences[1] = incomes.size();
        sequences[2] = assets.size();
    }
    
    public void saveDatabase() {
        try (ObjectOutputStream output = 
                new ObjectOutputStream(new FileOutputStream(fileDB))) {
            output.writeObject(expenses);
            output.writeObject(incomes);
            output.writeObject(assets);
        } catch (IOException ex) {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void buildApplicationGUI() {
        frame = new JFrame("Расходы и Доходы");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        background = new JPanel(new BorderLayout());
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        MyInputListener inputListener = new MyInputListener();
        MyOutputListener outputListener = new MyOutputListener();
        MyViewBalanceListener viewBalanceListener = new MyViewBalanceListener();
        
        menuBar = new JMenuBar();
        
        JMenu addMenu = new JMenu("Добавить");
        JMenu viewMenu = new JMenu("Просмотреть");
        
        JMenuItem addExpenseItem = new JMenuItem("Запись о расходах");
        JMenuItem addIncomeItem = new JMenuItem("Запись о доходах");
        JMenuItem addAssetItem = new JMenuItem("Запись о активах");
        JMenuItem viewExpensesItem = new JMenuItem("Список расходов");
        JMenuItem viewIncomesItem = new JMenuItem("Список доходов");
        JMenuItem viewAssetsItem = new JMenuItem("Список активов");
        JMenuItem viewBalanceItem = new JMenuItem("Баланс");
        
        addExpenseItem.addActionListener(inputListener);
        addIncomeItem.addActionListener(inputListener);
        addAssetItem.addActionListener(inputListener);
        viewExpensesItem.addActionListener(outputListener);
        viewIncomesItem.addActionListener(outputListener);
        viewAssetsItem.addActionListener(outputListener);
        viewBalanceItem.addActionListener(viewBalanceListener);
        
        addExpenseItem.setActionCommand("INExpenses");
        addIncomeItem.setActionCommand("INIncome");
        addAssetItem.setActionCommand("INAssets");
        viewExpensesItem.setActionCommand("OUTExpenses");
        viewIncomesItem.setActionCommand("OUTIncome");
        viewAssetsItem.setActionCommand("OUTAssets");
        viewBalanceItem.setActionCommand("ViewBalance");
        
        addMenu.add(addExpenseItem);
        addMenu.add(addIncomeItem);
        addMenu.add(addAssetItem);
        viewMenu.add(viewExpensesItem);
        viewMenu.add(viewIncomesItem);
        viewMenu.add(viewAssetsItem);
        viewMenu.addSeparator();
        viewMenu.add(viewBalanceItem);
        menuBar.add(addMenu);
        menuBar.add(viewMenu);
        
        JPanel panelIn = new JPanel();
        panelIn.setLayout(new BoxLayout(panelIn, BoxLayout.PAGE_AXIS));
        panelIn.setBorder(BorderFactory.createTitledBorder(
                                    BorderFactory.createLineBorder(Color.GRAY), 
                                    "Новые операции"));
        
        JPanel panelOut = new JPanel();
        panelOut.setLayout(new BoxLayout(panelOut, BoxLayout.PAGE_AXIS));
        panelOut.setBorder(BorderFactory.createTitledBorder(
                                    BorderFactory.createLineBorder(Color.GRAY),
                                    "Сводные данные", TitledBorder.RIGHT, TitledBorder.TOP));
        
        JPanel panelBalance = new JPanel();
        panelBalance.setLayout(new BoxLayout(panelBalance, BoxLayout.PAGE_AXIS));
        panelBalance.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JButton buttonINExpenses    = new JButton("Записать расходы");
        JButton buttonINIncome      = new JButton("Записать доходы");
        JButton buttonINAssets      = new JButton("Записать активы");
        JButton buttonOUTExpenses   = new JButton("Просмотреть расходы");
        JButton buttonOUTIncome     = new JButton("Просмотреть доходы");
        JButton buttonOUTAssets     = new JButton("Просмотреть активы");
        JButton buttonViewBalance   = new JButton("Просмотреть баланс");
        
        buttonINExpenses.addActionListener(inputListener);
        buttonINIncome.addActionListener(inputListener);
        buttonINAssets.addActionListener(inputListener);
        buttonOUTExpenses.addActionListener(outputListener);
        buttonOUTIncome.addActionListener(outputListener);
        buttonOUTAssets.addActionListener(outputListener);
        buttonViewBalance.addActionListener(viewBalanceListener);
        
        buttonINExpenses.setActionCommand("INExpenses");
        buttonINIncome.setActionCommand("INIncome");
        buttonINAssets.setActionCommand("INAssets");
        buttonOUTExpenses.setActionCommand("OUTExpenses");
        buttonOUTIncome.setActionCommand("OUTIncome");
        buttonOUTAssets.setActionCommand("OUTAssets");
        buttonViewBalance.setActionCommand("ViewBalance");
        
        buttonOUTExpenses.setAlignmentX(Component.RIGHT_ALIGNMENT);
        buttonOUTIncome.setAlignmentX(Component.RIGHT_ALIGNMENT);
        buttonOUTAssets.setAlignmentX(Component.RIGHT_ALIGNMENT);
        buttonViewBalance.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panelIn.add(Box.createRigidArea(new Dimension(0, 7)));
        panelIn.add(buttonINExpenses);
        panelIn.add(Box.createRigidArea(new Dimension(0, 7)));
        panelIn.add(buttonINIncome);
        panelIn.add(Box.createRigidArea(new Dimension(0, 7)));
        panelIn.add(buttonINAssets);
        panelIn.add(Box.createRigidArea(new Dimension(0, 7)));
        
        panelOut.add(Box.createRigidArea(new Dimension(0, 7)));
        panelOut.add(buttonOUTExpenses);
        panelOut.add(Box.createRigidArea(new Dimension(0, 7)));
        panelOut.add(buttonOUTIncome);
        panelOut.add(Box.createRigidArea(new Dimension(0, 7)));
        panelOut.add(buttonOUTAssets);
        panelOut.add(Box.createRigidArea(new Dimension(0, 7)));
        
        panelBalance.add(Box.createRigidArea(new Dimension(0, 10)));
        panelBalance.add(buttonViewBalance);
        panelBalance.add(Box.createRigidArea(new Dimension(0, 10)));
        
        background.add(BorderLayout.WEST, panelIn);
        background.add(BorderLayout.EAST, panelOut);
        background.add(BorderLayout.SOUTH, panelBalance);
        
        frame.setSize(300, 300);
        drawMainGui();
    }
    
    void drawMainGui() {
        frame.setContentPane(background);
        frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setLocationRelativeTo(null);  // Центрирование окна на экране
        frame.setVisible(true);
    }
    
    class MyInputListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            switch (a.getActionCommand()) {
                case ("INExpenses"):  kind = "Записать расходы";    break;
                case ("INIncome"):    kind = "Записать доходы";     break;
                case ("INAssets"):    kind = "Записать активы";     break;
            }
            frame.setJMenuBar(null);
            buildGuiIn();
        }
    }
    
    void buildGuiIn() {
        int numRows, numCols, width, height, columnsForButton;
        if (kind.equals("Записать расходы") || 
                kind.equals("Записать активы")) 
             columnsForButton = 3;
        else columnsForButton = 2;
        
        numRows = 6;    width   = 500;
        numCols = 2;    height  = 250;
        
        JPanel backgroundMainIn = new JPanel(new BorderLayout());
        Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 5, 10);
        backgroundMainIn.setBorder(BorderFactory.createTitledBorder(emptyBorder, kind));
        
        JPanel backgroundIn = new JPanel(new GridLayout(numRows, numCols, 5, 3));
        JPanel buttonsPanel = new JPanel(new GridLayout(1, columnsForButton, 20, 0));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 9, 5, 9));
        
        JLabel numLabel             = new JLabel("№ п/п:");
        JLabel dateLabel            = new JLabel("Дата:");
        JLabel sumLabel             = new JLabel("Сумма:");
        JLabel kindLabel            = new JLabel("Вид операции:");
        JLabel orgsOrPersonLabel    = new JLabel("Организация/лицо:");
        JLabel descriptionLabel     = new JLabel("Описание:");
        
        int curSequence = 1; // Вычислить сиквенс для поля numField
        switch (kind) {
            case ("Записать расходы"):
                curSequence = sequences[0] + 1; break;
            case ("Записать доходы"):
                curSequence = sequences[1] + 1; break;
            case ("Записать активы"):
                curSequence = sequences[2] + 1; break;
        }
                
        numField          = new JTextField(Integer.toString(curSequence), 5);
        numField.setEditable(false);
        
        // Установить формат даты и инициализировать поле даты текущей датой
        try {
            formatter         = new MaskFormatter("##-##-####");
        } catch (ParseException ex) {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        formatter.setPlaceholderCharacter(' ');
        dateField         = new JFormattedTextField(formatter);
        dateField.setText(String.format("%td-%<tm-%<tY", new Date()));
        
        sumField          = new JTextField("", 10);
        detailsList       = new ArrayList<>();
        kindField         = new JTextField(kind, 10);
        orgOrPersonField  = new JTextField("", 10);
        descriptionField  = new JTextArea("", 3, 10); 
        descriptionField.setLineWrap(true);
        
        JScrollPane scroller = new JScrollPane(descriptionField, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        backgroundIn.add(numLabel);         backgroundIn.add(numField);
        backgroundIn.add(dateLabel);        backgroundIn.add(dateField);
        backgroundIn.add(sumLabel);         backgroundIn.add(sumField);
        backgroundIn.add(kindLabel);        backgroundIn.add(kindField);
        backgroundIn.add(orgsOrPersonLabel);backgroundIn.add(orgOrPersonField);
        backgroundIn.add(descriptionLabel); backgroundIn.add(scroller);
        
        JButton saveButton      = new JButton("Сохранить");
        JButton detailButton    = new JButton("Детали");
        JButton cancelButton    = new JButton("Отменить");
        
        saveButton.addActionListener(new MySaveGuiInListener());
        detailButton.addActionListener(new MyDetailsListener(backgroundMainIn, 
                                           width, height, EDIT_CARD));
        cancelButton.addActionListener(new MyComeBackMainGuiListener());
        
        buttonsPanel.add(saveButton);
        if (kind.equals("Записать расходы") || kind.equals("Записать активы")) 
            buttonsPanel.add(detailButton);
        buttonsPanel.add(cancelButton);
        
        backgroundMainIn.add(BorderLayout.CENTER, backgroundIn);
        backgroundMainIn.add(BorderLayout.SOUTH, buttonsPanel);
        
        drawGui(backgroundMainIn, width, height);
    }
    
    void drawGui(JPanel panel, int width, int height) {
        frame.setContentPane(panel);
        frame.setSize(width, height);
        frame.setVisible(true);
    }
    
    class MyDetailsListener implements ActionListener {
        JPanel parentPanel; 
        int width, height, mode;
        
        public MyDetailsListener(JPanel parentPanel, int width, int height, int mode) {
            this.parentPanel  = parentPanel;
            this.width  = width;
            this.height = height;
            this.mode   = mode;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            buildDetailsGui(parentPanel, width, height, mode);
        }
    }
    
    public void buildDetailsGui(JPanel parentPanel, 
                                int width, int height, int mode) {
        JPanel canvasPanel = new JPanel(new BorderLayout());
        canvasPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        String title = "";
        switch (kind) {
            case("Записать расходы"):
            case("Просмотреть расходы"):
                title = "Детали по расходам";
                break;
            case("Записать активы"):
            case("Просмотреть активы"):
                title = "Детали по активам";
                break;
        }
        JPanel backgroundDetails = new JPanel(new BorderLayout());
        Border border = BorderFactory.createLineBorder(Color.GRAY);
        backgroundDetails.setBorder(BorderFactory.createTitledBorder(border, title));
        
        MyTableDetailsModel detailsModel = new MyTableDetailsModel();
        JTable detailsTable = new JTable(detailsModel);
        detailsTable.setFillsViewportHeight(true);
        // detailsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        detailsTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        detailsTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        JScrollPane scroller = new JScrollPane(detailsTable);
        
        JButton addButton = new JButton("Добавить");
        JButton editButton = new JButton("Редактировать");
        JButton deleteButton = new JButton("Удалить");
        JButton returnButton = new JButton("Вернуться");
        addButton.addActionListener(new MyAddDetailListener(detailsModel));
        editButton.addActionListener(new MyEditDetailListener(detailsTable, detailsModel));
        deleteButton.addActionListener(new MyDeleteDetailListener(detailsTable, detailsModel));
        returnButton.addActionListener(new MyReturnDetailListener(parentPanel, width, height));
        
        int columnsForButtons = 4;                      // режим редактирования
        if (mode == VIEW_CARD) columnsForButtons = 3;   // режим просмотра
        JPanel buttonsPanel = new JPanel(new GridLayout(1, columnsForButtons));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        switch (mode) {
            case(EDIT_CARD):
                buttonsPanel.add(addButton);
                buttonsPanel.add(editButton);
                buttonsPanel.add(deleteButton);
                buttonsPanel.add(returnButton);
                break;
            case(VIEW_CARD):
                buttonsPanel.add(new JLabel());
                buttonsPanel.add(returnButton);
                buttonsPanel.add(new JLabel());
                break;
        }
        
        backgroundDetails.add(BorderLayout.CENTER, scroller);
        backgroundDetails.add(BorderLayout.SOUTH, buttonsPanel);
        
        canvasPanel.add(backgroundDetails);
        
        frame.setContentPane(canvasPanel);
        frame.setVisible(true);
    }
    
    class MyAddDetailListener implements ActionListener {
        MyTableDetailsModel detailsModel;
        
        public MyAddDetailListener(MyTableDetailsModel detailsModel) {
            this.detailsModel = detailsModel;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            JPanel inputDetailPanel = new JPanel(new GridLayout(3,2));
            
            JTextField name = new JTextField(10);
            try {
                formatter     = new MaskFormatter("###");
            } catch (ParseException ex) {
                Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
            }
            formatter.setPlaceholderCharacter('0');
            JFormattedTextField quantity = new JFormattedTextField(formatter);
            JTextField measure = new JTextField(10);
            
            inputDetailPanel.add(new JLabel("Наименование:"));
            inputDetailPanel.add(name);
            inputDetailPanel.add(new JLabel("Количество:"));
            inputDetailPanel.add(quantity);
            inputDetailPanel.add(new JLabel("Ед.измерения:"));
            inputDetailPanel.add(measure);
            
            int result = JOptionPane.showConfirmDialog(frame, 
                    inputDetailPanel, "Укажите детали", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String n    = name.getText().trim();
                int    q    = Integer.parseInt(quantity.getText().trim());
                String m    = measure.getText().trim();
                detailsList.add(new Details(n, q, m));
                detailsModel.fireTableDataChanged();
            }
        }
    }
    
    class MyEditDetailListener implements ActionListener {
        JTable detailsTable;
        MyTableDetailsModel detailsModel;
        
        public MyEditDetailListener(JTable detailsTable, 
                                    MyTableDetailsModel detailsModel) {
            this.detailsTable = detailsTable;
            this.detailsModel = detailsModel;
        }
        
        @Override
        public void actionPerformed(ActionEvent a) {
            int rowIndex = detailsTable.getSelectedRow();
            rowIndex = detailsTable.convertRowIndexToModel(rowIndex);
            if (rowIndex > -1) {
                
                JPanel inputDetailPanel = new JPanel(new GridLayout(3,2));
                JTextField name = new JTextField(10);
                try {
                    formatter     = new MaskFormatter("###");
                } catch (ParseException ex) {
                    Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
                }
                formatter.setPlaceholderCharacter('0');
                JFormattedTextField quantity = new JFormattedTextField(formatter);
                JTextField measure = new JTextField(10);
                
                // !!! Следующие три строки, вероятно, содержат алгоритмическую ошибку. Разобраться !!!
                name.       setText((String) 
                            detailsTable.getValueAt(rowIndex, detailsTable.convertColumnIndexToModel(0)));
                quantity.   setText(Integer.toString((int) 
                            detailsTable.getValueAt(rowIndex, detailsTable.convertColumnIndexToModel(1))));
                measure.    setText((String) 
                            detailsTable.getValueAt(rowIndex, detailsTable.convertColumnIndexToModel(2)));
                name.requestFocus();
                
                inputDetailPanel.add(new JLabel("Наименование:"));
                inputDetailPanel.add(name);
                inputDetailPanel.add(new JLabel("Количество:"));
                inputDetailPanel.add(quantity);
                inputDetailPanel.add(new JLabel("Ед.измерения:"));
                inputDetailPanel.add(measure);
            
                int result = JOptionPane.showConfirmDialog(frame, 
                    inputDetailPanel, "Укажите детали", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    String n    = name.getText().trim();
                    System.out.println("quantity.getText().trim() - " + quantity.getText().trim());
                    int    q    = Integer.parseInt(quantity.getText().trim());
                    String m    = measure.getText().trim();
                    detailsList.get(rowIndex).setName(n);
                    detailsList.get(rowIndex).setQuantity(q);
                    detailsList.get(rowIndex).setMeasure(m);
                    detailsModel.fireTableDataChanged();
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Чтобы отредактировать "
                        + "запись, выберите строку в списке !!!",
                        "Предупреждение", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    class MyDeleteDetailListener implements ActionListener {
        JTable detailsTable;
        MyTableDetailsModel detailsModel;
        
        public MyDeleteDetailListener(JTable detailsTable, 
                                    MyTableDetailsModel detailsModel) {
            this.detailsTable = detailsTable;
            this.detailsModel = detailsModel;
        }
        
        @Override
        public void actionPerformed(ActionEvent a) {
            int[] selection = detailsTable.getSelectedRows();
            for (int i = 0; i < selection.length; i++) {
                selection[i] = detailsTable.convertRowIndexToModel(selection[i]);
                System.out.println(i + " index - " + selection[i]);
            }
            if (selection.length > 0) {
                int confirm = JOptionPane.showConfirmDialog(frame, 
                        "Вы действительно хотите удалить запись из списка?" +
                        "\n    Данная операция является необратимой !!!", 
                        "Подтверждение", 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    for (int i = selection.length - 1; i >= 0; i--)
                        detailsList.remove(selection[i]);
                    detailsModel.fireTableDataChanged();
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Выберите запись в списке"
                        + " чтобы выполнить ее удаление !!!",
                        "Предупреждение", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    class MyReturnDetailListener implements ActionListener {
        JPanel parentPanel; 
        int width, height;
        
        public MyReturnDetailListener(JPanel parentPanel, int width, int height) {
            this.parentPanel    = parentPanel;
            this.width          = width;
            this.height         = height;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            drawGui(parentPanel, width, height);
        }
    }
    
    class MyTableDetailsModel extends AbstractTableModel {
        String[] columnNames = {"Наименование", "Количество", "Ед.изм."};
        
        @Override
        public int getRowCount() {
            return detailsList.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName​(int column) {
            return columnNames[column];
        }
        
        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object value = null;
            switch (columnIndex) {
                case (0):
                    value = detailsList.get(rowIndex).getName();
                    break;
                case (1):
                    value = detailsList.get(rowIndex).getQuantity();
                    break;
                case (2):
                    value = detailsList.get(rowIndex).getMeasure();
                    break;
            }
            return value;
        }
    }
    
    public boolean checkDate(String date) {
        // Если дата меньше 10 символов, то вернуть false.
        if (date.length() < 10) return false;
        
        // Иначе "разобрать" дату на части и подготовить списки 
        // для проверки корректности количества дней в месяцах.
        String[] dayMonth = date.split("-");
        ArrayList<Integer> monthOf31Days = new ArrayList<>();
            monthOf31Days.add(1);   monthOf31Days.add(3);
            monthOf31Days.add(5);   monthOf31Days.add(7);
            monthOf31Days.add(8);   monthOf31Days.add(10);
            monthOf31Days.add(12);
        ArrayList<Integer> monthOf30Days = new ArrayList<>();
            monthOf30Days.add(4);   monthOf30Days.add(6);
            monthOf30Days.add(9);   monthOf30Days.add(11);
        
        // Проверить день и месяц даты на "пустое" значение.
        // Если проверки прошли неуспешно, то вернуть false.
        // Данная проверка является избыточной.
        if (dayMonth.length >= 2 && ( dayMonth[0].equals("") || 
                                      dayMonth[0].equals("  ") ||
                                      dayMonth[1].equals("") || 
                                      dayMonth[1].equals("  ") 
                                    ) 
            ) return false;
        
        // Проверить каждый элемент даты (день, месяц, год).
        // Если проверки прошли неуспешно, то вернуть false.
        if (dayMonth.length == 3) {
            int day     = Integer.parseInt(dayMonth[0]);
            int month   = Integer.parseInt(dayMonth[1]);
            int year    = Integer.parseInt(dayMonth[2]);
            if ( monthOf31Days.contains(month) && (day < 1 || day > 31) || 
                 monthOf30Days.contains(month) && (day < 1 || day > 30) || 
                 ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 
                                 && month == 2 && (day < 1 || day > 29) ||
                 (year % 4 != 0) && month == 2 && (day < 1 || day > 28) ||
                 (month < 1 || month > 12)  || 
                 year < 2000 || 
                 year > Integer.parseInt(String.format("%tY", new Date())) 
               ) return false;
        }
        
        // Если мы здесь, значит все проверки успешны, и нужно вернуть true.
        return true;
    }
    
    public String checkCorrectInput() {
        String message = "";
        switch(kind) {
                case("Записать доходы"):
                case("Просмотреть доходы"):
                    if (numField.getText().trim().equals("")) message += "\n  - '№ п/п'";
                    if (!checkDate(dateField.getText().trim())) message += "\n  - 'Дата'";
                    if (sumField.getText().trim().equals("")) message += "\n  - 'Сумма'";
                    if (kindField.getText().trim().equals("")) message += "\n  - 'Вид операции'";
                    if (orgOrPersonField.getText().trim().equals("")) message += "\n  - 'Организация/лицо'";
                    if (descriptionField.getText().trim().equals("")) message += "\n  - 'Описание'";
                    break;
                case("Записать расходы"):
                case("Записать активы"):
                case("Просмотреть расходы"):
                case("Просмотреть активы"):
                    if (numField.getText().trim().equals("")) message += "\n  - '№ п/п'";
                    if (!checkDate(dateField.getText().trim())) message += "\n  - 'Дата'";
                    if (sumField.getText().trim().equals("")) message += "\n  - 'Сумма'";
                    //if (quantityField.getText().trim().equals("")) message += "\n  - 'Количество'";
                    //if (measureField.getText().trim().equals("")) message += "\n  - 'Единица изм'";
                    if (kindField.getText().trim().equals("")) message += "\n  - 'Вид операции'";
                    if (orgOrPersonField.getText().trim().equals("")) message += "\n  - 'Организация/лицо'";
                    if (descriptionField.getText().trim().equals("")) message += "\n  - 'Описание'";
                    break;
        }
        if (!message.equals("")) message = 
                "Не определены значения ниже следующих полей !!!" + message
              + "\nОбязательно заполните значения перечисленных полей "
              + "\nдля продолжения работы или нажмите кнопку 'Отменить' "
              + "\nдля возврата в предыдущий интерфейс программы !!!";
        return message;
    }
    
    class MySaveGuiInListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            // Выполняем проверку на кооректность и полноту заполнения 
            // полей интерфейсной формы. Если проверка прошла неуспешно, 
            // выходим из метода, отобразив сообщение с предупреждением
            String message;
            if (!"".equals(message = checkCorrectInput())) {
                JOptionPane.showMessageDialog(frame, message,
                        "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Извлекаем данные из полей интерфейсной формы и преобразуем их 
            // к типу полей объекта одного из классов Expenses/Income/Assets 
            int num        = Integer.parseInt(numField.getText().trim());
            Date date = null;
            try {
                date = new SimpleDateFormat("dd-MM-yyyy", new Locale("ru")).parse(dateField.getText().trim());
            } catch (ParseException ex) {
                Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
            }
            float sum      = Float.parseFloat(sumField.getText().trim());
            String kindOperation = kindField.getText().trim();
            String orgOrPerson   = orgOrPersonField.getText().trim();
            String description   = descriptionField.getText().trim();
            
            // В зависимости от типа текущей операции сохраняем преобразованные 
            // данные в соответствующем списке: expenses, incomes, assets.
            switch(kind) {
                case("Записать расходы"):
                    Expenses curExpense = new Expenses(num, date, sum, 
                                            detailsList, kindOperation,
                                            orgOrPerson, description);
                    expenses.add(curExpense);
                    sequences[0]++;     // можно было так sequences[0] = num;
                    break;
                case("Записать доходы"):
                    Income curIncome = new Income(num, date, sum, 
                                            kindOperation, orgOrPerson, 
                                            description);
                    incomes.add(curIncome);
                    sequences[1]++;     // можно было так sequences[1] = num;
                    break;
                case("Записать активы"):
                    Assets curAsset = new Assets(num, date, sum, 
                                            detailsList, kindOperation,
                                            orgOrPerson, description);
                    assets.add(curAsset);
                    sequences[2]++;     // можно было так sequences[2] = num;
                    break;
            }
            drawMainGui();
            saveDatabase();
        }
    }
    
    class MyOutputListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            switch (a.getActionCommand()) {
                case ("OUTExpenses"): kind = "Просмотреть расходы"; break;
                case ("OUTIncome"):   kind = "Просмотреть доходы";  break;
                case ("OUTAssets"):   kind = "Просмотреть активы";  break;
            }
            frame.setJMenuBar(null);
            buildGuiOut();
        }
    }
    
    void buildGuiOut() {
        backgroundOut = new JPanel(new BorderLayout());
        Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        backgroundOut.setBorder(BorderFactory.createTitledBorder(emptyBorder, kind));
        
        myModel = new MyTableModel();
        table = new JTable(myModel);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < table.getColumnCount(); i++) {
            int widthColumn = 100;
            switch(table.getColumnName(i)) {
                case ("№ п/п"):
                    widthColumn = 30; break;
                case ("Дата"):
                    widthColumn = 70; break;
                case ("Сумма"):
                    widthColumn = 60; break;
            }
            table.getColumnModel().getColumn(i).setPreferredWidth(widthColumn);
        }
        JScrollPane scroller = new JScrollPane(table);
        
        JButton openCardButton = new JButton("Просмотреть");
        JButton editCardButton = new JButton("Редактировать");
        JButton deleteCardButton = new JButton("Удалить");
        JButton comeBackButton = new JButton("Вернуться");
        openCardButton.addActionListener(new MyOpenCardGuiOutListener());
        editCardButton.addActionListener(new MyEditCardGuiOutListener());
        deleteCardButton.addActionListener(new MyDeleteCardGuiOutListener());
        comeBackButton.addActionListener(new MyComeBackMainGuiListener());
        
        JPanel panelButton = new JPanel(new GridLayout(1, 4, 10, 0));
        panelButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        panelButton.add(openCardButton);
        panelButton.add(editCardButton);
        panelButton.add(deleteCardButton);
        panelButton.add(comeBackButton);
        
        backgroundOut.add(BorderLayout.CENTER, scroller);
        backgroundOut.add(BorderLayout.SOUTH, panelButton);
        
        drawBackgroundOutGui();
    }
    
    void drawBackgroundOutGui() {
        frame.setContentPane(backgroundOut);
        frame.setSize(590, 300);
        frame.setLocationRelativeTo(null);  // Центрирование окна на экране
        frame.setVisible(true);
    }
    
    class MyTableModel extends AbstractTableModel {
        public MyTableModel() {
            switch (kind) {
                case ("Просмотреть расходы"):
                    columnNames = columnNamesExpenses;
                    data.removeAll(data);
                    data.addAll(expenses);
                    break;
                case ("Просмотреть доходы"):
                    columnNames = columnNamesIncomes;
                    data.removeAll(data);
                    data.addAll(incomes);
                    break;
                case ("Просмотреть активы"):
                    columnNames = columnNamesAssets;
                    data.removeAll(data);
                    data.addAll(assets);
                    break;
            }
        }
        
        @Override
        public int getRowCount() {
            return data.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName​(int column) {
            return columnNames[column];
        }
        
        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object curRow = data.get(rowIndex);
            Object value = null;
            switch (kind) {
                case ("Просмотреть расходы"):
                    switch (columnIndex) {
                        case (0): value = ((Expenses) curRow).getNumber(); break;
                        case (1): value = ((Expenses) curRow).getDate(); break;
                        case (2): value = ((Expenses) curRow).getSum(); break;
                        case (3): value = ((Expenses) curRow).getKindOfExpenses(); break;
                        case (4): value = ((Expenses) curRow).getOrgsOrPerson(); break;
                        case (5): value = ((Expenses) curRow).getDescription(); break;
                    }
                    break;
                case ("Просмотреть доходы"):
                    switch (columnIndex) {
                        case (0): value = ((Income) curRow).getNumber(); break;
                        case (1): value = ((Income) curRow).getDate(); break;
                        case (2): value = ((Income) curRow).getSum(); break;
                        case (3): value = ((Income) curRow).getKindOfIncome(); break;
                        case (4): value = ((Income) curRow).getIncomeSource(); break;
                        case (5): value = ((Income) curRow).getDescription(); break;
                    }
                    break;
                case ("Просмотреть активы"):
                    switch (columnIndex) {
                        case (0): value = ((Assets) curRow).getNumber(); break;
                        case (1): value = ((Assets) curRow).getDate(); break;
                        case (2): value = ((Assets) curRow).getSum(); break;
                        case (3): value = ((Assets) curRow).getKindOfAssets(); break;
                        case (4): value = ((Assets) curRow).getOrgsOrPerson(); break;
                        case (5): value = ((Assets) curRow).getDescription(); break;
                    }
                    break;
            }
            return value;
        }
    }
    
    class MyOpenCardGuiOutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            int rowIndex = table.getSelectedRow();
            rowIndex = table.convertRowIndexToModel(rowIndex);
            if (rowIndex > -1) {
                buildGuiViewCard(rowIndex, VIEW_CARD);
            } else {
                JOptionPane.showMessageDialog(frame, "Чтобы перейти к просмотру"
                        + " записи выберите строку в списке !!!",
                        "Предупреждение", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    class MyEditCardGuiOutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            int rowIndex = table.getSelectedRow();
            rowIndex = table.convertRowIndexToModel(rowIndex);
            if (rowIndex > -1) {
                buildGuiViewCard(rowIndex, EDIT_CARD);
            } else {
                JOptionPane.showMessageDialog(frame, "Чтобы отредактировать "
                        + "запись, выберите строку в списке !!!",
                        "Предупреждение", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
void buildGuiViewCard(int rowIndex, int mode) {
        int numRows, numCols, width, height;
        
        numRows = 6; width  = 510;
        numCols = 2; height = 250;
        
        JPanel backgroundMainIn = new JPanel(new BorderLayout());
        Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 5, 10);
        backgroundMainIn.setBorder(BorderFactory.createTitledBorder(emptyBorder, kind));
        
        JPanel panelViewCard = new JPanel(new GridLayout(numRows, numCols, 5, 3));
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 9, 5, 9));
        
        // Создаются метки и поля карточной интерфейсной формы 
        JLabel numLabel             = new JLabel("№ п/п:");
        JLabel dateLabel            = new JLabel("Дата:");
        JLabel sumLabel             = new JLabel("Сумма:");
        JLabel kindLabel            = new JLabel("Вид операции:");
        JLabel orgsOrPersonLabel    = new JLabel("Организация/лицо:");
        JLabel descriptionLabel     = new JLabel("Описание:");
        
        numField          = new JTextField(5);
        try {
            formatter     = new MaskFormatter("##-##-####");
        } catch (ParseException ex) {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        formatter.setPlaceholderCharacter(' ');
        dateField         = new JFormattedTextField(formatter);
        sumField          = new JTextField(10);
        kindField         = new JTextField(10);
        orgOrPersonField  = new JTextField(10);
        descriptionField  = new JTextArea(3, 10); 
        descriptionField.setLineWrap(true);
        
        JScrollPane scroller = new JScrollPane(descriptionField, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // В зависимости от режима, поля карточной интерфейсной 
        // формы разрешаются или запрещаются для редактирования
        boolean isEditableCard = true;
        if (mode == VIEW_CARD) isEditableCard = false;
        numField.setEditable(false);
        dateField.setEditable(isEditableCard);
        sumField.setEditable(isEditableCard);
        kindField.setEditable(isEditableCard);
        orgOrPersonField.setEditable(isEditableCard);
        descriptionField.setEditable(isEditableCard);
        
        // Поля карточной интерфейсной формы инициализируются значениями из 
        // полей выделенной  записи таблицы в  списочной интерфейсной форме
        int columnIndex = 0;
        numField        .setText(Integer.toString((int)table
                        .getValueAt(rowIndex, table.convertColumnIndexToModel(columnIndex++))));
        dateField       .setText(String.format("%td-%<tm-%<tY", (Date)table
                        .getValueAt(rowIndex, table.convertColumnIndexToModel(columnIndex++)))); 
        sumField        .setText(Float.toString((float)table
                        .getValueAt(rowIndex, table.convertColumnIndexToModel(columnIndex++))));
        kindField       .setText((String)table
                        .getValueAt(rowIndex, table.convertColumnIndexToModel(columnIndex++)));
        orgOrPersonField.setText((String)table
                        .getValueAt(rowIndex, table.convertColumnIndexToModel(columnIndex++)));
        descriptionField.setText((String)table
                        .getValueAt(rowIndex, table.convertColumnIndexToModel(columnIndex++)));
        
        // Извлекается список деталей по расходам (или активам)
        switch (kind) {
            case("Записать расходы"):
            case("Просмотреть расходы"):
                detailsList = expenses.get(rowIndex).getDetails();
                break;
            case("Записать активы"):
            case("Просмотреть активы"):
                detailsList = assets.get(rowIndex).getDetails();
                break;
        }
        
        JButton detailsButton = new JButton("Детали");
        detailsButton.addActionListener(new MyDetailsListener(backgroundMainIn, 
                                                              width, height, mode));
        switch(mode) {
            case (VIEW_CARD): 
                JButton returnButton = new JButton("Обратно к списку");
                returnButton.addActionListener(new MyReturnGuiViewCardListener());
                if (kind.equals("Записать расходы") || 
                        kind.equals("Просмотреть расходы") || 
                        kind.equals("Записать активы") || 
                        kind.equals("Просмотреть активы")) {
                    buttonsPanel.setLayout((new GridLayout(1, 2, 20, 0)));
                    buttonsPanel.add(detailsButton);
                    buttonsPanel.add(returnButton);
                } else {
                    buttonsPanel.setLayout((new GridLayout(1, 3, 20, 0)));
                    buttonsPanel.add(new JLabel());
                    buttonsPanel.add(returnButton);
                    buttonsPanel.add(new JLabel());
                }
                break;
            case (EDIT_CARD): 
                JButton saveButton          = new JButton("Сохранить");
                JButton cancelButton        = new JButton("Отменить");
                saveButton.addActionListener(new MySaveGuiEditListener(rowIndex));
                cancelButton.addActionListener(new MyReturnGuiViewCardListener());
                if (kind.equals("Записать расходы") || 
                        kind.equals("Просмотреть расходы") || 
                        kind.equals("Записать активы") || 
                        kind.equals("Просмотреть активы")) {
                    buttonsPanel.setLayout((new GridLayout(1, 3, 20, 0)));
                    buttonsPanel.add(saveButton);
                    buttonsPanel.add(detailsButton);
                    buttonsPanel.add(cancelButton);
                } else {
                    buttonsPanel.setLayout((new GridLayout(1, 2, 20, 0)));
                    buttonsPanel.add(saveButton);
                    buttonsPanel.add(cancelButton);
                }
                break;
        }
        
        panelViewCard.add(numLabel);         panelViewCard.add(numField);
        panelViewCard.add(dateLabel);        panelViewCard.add(dateField);
        panelViewCard.add(sumLabel);         panelViewCard.add(sumField);
        panelViewCard.add(kindLabel);        panelViewCard.add(kindField);
        panelViewCard.add(orgsOrPersonLabel);panelViewCard.add(orgOrPersonField);
        panelViewCard.add(descriptionLabel); panelViewCard.add(scroller);
        
        backgroundMainIn.add(BorderLayout.CENTER, panelViewCard);
        backgroundMainIn.add(BorderLayout.SOUTH, buttonsPanel);
        
        drawGui(backgroundMainIn, width, height);
    }
    
    class MyReturnGuiViewCardListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            drawBackgroundOutGui();
        }
    }
    
    class MySaveGuiEditListener implements ActionListener {
        int rowIndex;
        public MySaveGuiEditListener(int rowIndex) {
            this.rowIndex = rowIndex;
        }
        
        @Override
        public void actionPerformed(ActionEvent a) {
            // Выполняем проверку на кооректность и полноту заполнения 
            // полей интерфейсной формы. Если проверка прошла неуспешно, 
            // выходим из метода, отобразив сообщение с предупреждением
            String message;
            if (!"".equals(message = checkCorrectInput())) {
                JOptionPane.showMessageDialog(frame, message,
                        "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Извлекаем данные из полей интерфейсной формы и преобразуем их 
            // к типу полей объекта одного из классов Expenses/Income/Assets 
            Date date = null;
            try {
                date = new SimpleDateFormat("dd-MM-yyyy", new Locale("ru")).parse(dateField.getText().trim());
            } catch (ParseException ex) {
                Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
            }
            float sum      = Float.parseFloat(sumField.getText().trim());
            String kindOperation = kindField.getText().trim();
            String orgOrPerson   = orgOrPersonField.getText().trim();
            String description   = descriptionField.getText().trim();
            
            // В зависимости от типа текущей операции сохраняем преобразованные 
            // данные в соответствующем списке: expenses, incomes, assets.
            Object curObject;
            switch(kind) {
                case("Просмотреть расходы"):
                    Expenses curExpense = expenses.get(rowIndex);
                    curExpense.setDate(date);
                    curExpense.setSum(sum);
                    // Здесь поле details в объекте Expenses не сохраняется, так 
                    // как  ссылка  на  него была  ранее сохранена  в глобальной 
                    // переменной  detailsList  в  методе buildGuiViewCard(),  в 
                    // связи  с  чем сохранение  в  явном виде с  использованием 
                    // метода-сеттера из объекта Expenses уже не требуется.
                    curExpense.setKindOfExpenses(kindOperation);
                    curExpense.setOrgsOrPerson(orgOrPerson);
                    curExpense.setDescription(description);
                    curObject = data.get(rowIndex);
                    ((Expenses)curObject).setDate(date);
                    ((Expenses)curObject).setSum(sum);
                    // Здесь поле details в объекте Expenses не сохраняется, так 
                    // как  ссылка  на  него была  ранее сохранена  в глобальной 
                    // переменной  detailsList  в  методе buildGuiViewCard(),  в 
                    // связи  с  чем сохранение  в  явном виде с  использованием 
                    // метода-сеттера из объекта Expenses уже не требуется.
                    ((Expenses)curObject).setKindOfExpenses(kindOperation);
                    ((Expenses)curObject).setOrgsOrPerson(orgOrPerson);
                    ((Expenses)curObject).setDescription(description);
                    break;
                case("Просмотреть доходы"):
                    Income curIncome = incomes.get(rowIndex);
                    curIncome.setDate(date);
                    curIncome.setSum(sum);
                    curIncome.setKindOfIncome(kindOperation);
                    curIncome.setIncomeSource(orgOrPerson);
                    curIncome.setDescription(description);
                    curObject = data.get(rowIndex);
                    ((Income)curObject).setDate(date);
                    ((Income)curObject).setSum(sum);
                    ((Income)curObject).setKindOfIncome(kindOperation);
                    ((Income)curObject).setIncomeSource(orgOrPerson);
                    ((Income)curObject).setDescription(description);
                    break;
                case("Просмотреть активы"):
                    Assets curAssets = assets.get(rowIndex);
                    curAssets.setDate(date);
                    curAssets.setSum(sum);
                    // Здесь поле details в объекте Assets не сохраняется, так 
                    // как  ссылка  на него была  ранее сохранена  в глобальной 
                    // переменной  detailsList в  методе buildGuiViewCard(),  в 
                    // связи  с  чем сохранение в  явном виде с  использованием 
                    // метода-сеттера из объекта Assets уже не требуется.
                    curAssets.setKindOfAssets(kindOperation);
                    curAssets.setOrgsOrPerson(orgOrPerson);
                    curAssets.setDescription(description);
                    curObject = data.get(rowIndex);
                    ((Assets)curObject).setDate(date);
                    ((Assets)curObject).setSum(sum);
                    // Здесь поле details в объекте Assets не сохраняется, так 
                    // как  ссылка  на него была  ранее сохранена  в глобальной 
                    // переменной  detailsList в  методе buildGuiViewCard(),  в 
                    // связи  с  чем сохранение в  явном виде с  использованием 
                    // метода-сеттера из объекта Assets уже не требуется.
                    ((Assets)curObject).setKindOfAssets(kindOperation);
                    ((Assets)curObject).setOrgsOrPerson(orgOrPerson);
                    ((Assets)curObject).setDescription(description);
                    break;
            }
            drawBackgroundOutGui();
            myModel.fireTableDataChanged();
            saveDatabase();
        }
    }
    
    class MyDeleteCardGuiOutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            // Здесь  на  самом  деле   можно   использовать  метод 
            // table.getSelectedRow(),  возвращающий индекс  только 
            // одной выделенной записи в таблице, так как в таблице 
            // установлен режим выбора одиночных строк
            
            int[] selection = table.getSelectedRows();
            for (int i = 0; i < selection.length; i++) {
                selection[i] = table.convertRowIndexToModel(selection[i]);
                System.out.println(i + " index - " + selection[i]);
            }
            if (selection.length > 0) {
                int confirm = JOptionPane.showConfirmDialog(frame, 
                        "Вы действительно хотите удалить запись из списка?" +
                        "\n    Данная операция является необратимой !!!", 
                        "Подтверждение", 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    for (int i = selection.length - 1; i >= 0; i--) {
                        data.remove(selection[i]);
                        switch (kind) {
                            case("Просмотреть расходы"): 
                                expenses.remove(selection[i]); break;
                            case("Просмотреть активы"): 
                                assets.remove(selection[i]); break;
                            case("Просмотреть доходы"): 
                                incomes.remove(selection[i]); break;
                        }
                    }
                    myModel.fireTableDataChanged();
                    saveDatabase();
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Выберите запись в списке"
                        + " чтобы выполнить ее удаление !!!",
                        "Предупреждение", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    class MyComeBackMainGuiListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            drawMainGui();
        }
    }
    
    class MyViewBalanceListener implements  ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            if (a.getActionCommand().equals("ViewBalance")) {
                kind = "Просмотреть баланс";
            }
            frame.setJMenuBar(null);
            buildGuiBalance();
        }
    }
    
    public void buildGuiBalance() {
        JPanel canvasPanel = new JPanel(new BorderLayout());
        canvasPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel backgroundBalance = new JPanel(new BorderLayout());
        Border border = BorderFactory.createLineBorder(Color.GRAY);
        backgroundBalance.setBorder(BorderFactory.createTitledBorder(border, kind));
        
        MyTableBalanceModel balanceModel = new MyTableBalanceModel();
        JTable balanceTable = new JTable(balanceModel);
        balanceTable.setFillsViewportHeight(true);
        JScrollPane scroller = new JScrollPane(balanceTable);
        
        JPanel msgPanel = new JPanel();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.PAGE_AXIS));
        msgPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        String[] message = {"ОТЛИЧНО !!!", 
                            "Ваши расходы НЕ ПРЕВЫШАЮТ ваши доходы !!!"};
        if ((Float)balanceModel.getValueAt(0, 2) < 0) { 
            message[0] = "ВНИМАНИЕ !!! ";
            message[1] = "Ваши расходы ПРЕВЫШАЮТ ваши доходы !!!";
        }
        JLabel lbl_1 = new JLabel(message[0]);
        JLabel lbl_2 = new JLabel(message[1]);
        lbl_1.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl_2.setAlignmentX(Component.CENTER_ALIGNMENT);
        msgPanel.add(lbl_1);
        msgPanel.add(lbl_2);
        
        JPanel buttonsPanel = new JPanel(new GridLayout(1,3));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton returnButton = new JButton("Вернуться");
        returnButton.addActionListener(new MyComeBackMainGuiListener());
        
        buttonsPanel.add(new JLabel());
        buttonsPanel.add(returnButton);
        buttonsPanel.add(new JLabel());
        
        backgroundBalance.add(BorderLayout.NORTH, msgPanel);
        backgroundBalance.add(BorderLayout.CENTER, scroller);
        backgroundBalance.add(BorderLayout.SOUTH, buttonsPanel);
        
        canvasPanel.add(backgroundBalance);
        
        frame.setContentPane(canvasPanel);
        frame.setVisible(true);
    }
    
    class MyTableBalanceModel extends AbstractTableModel {
        ArrayList<Float> balTable = new ArrayList<>();
        
        public MyTableBalanceModel() {
            balTable = new ArrayList<>();
            balTable.add(sumForBalance("INCOME"));
            balTable.add(sumForBalance("EXPENSE"));
            balTable.add(balTable.get(0) - balTable.get(1));
        }
        
        private float sumForBalance(String s) {
            float sum = 0;
            
            switch (s) {
                case ("INCOME"):
                    if (incomes.size() > 0) {
                        for (int i = 0; i < incomes.size(); i++)
                            sum += incomes.get(i).getSum();
                    }
                    break;
                case ("EXPENSE"):
                    if (expenses.size() > 0) {
                        for (int i = 0; i < expenses.size(); i++)
                            sum += expenses.get(i).getSum();
                    }
                    break;
            }
            return sum;
        }
        
        @Override
        public int getRowCount() {
            return 1;
        }
        
        @Override
        public int getColumnCount() {
            return 3;
        }
        
        @Override
        public String getColumnName​(int column) {
            String columnName = "";
            switch (column) {
                case (0):
                    columnName = "Доходы";
                    break;
                case (1):
                    columnName = "Расходы";
                    break;
                case (2):
                    columnName = "Сальдо";
                    break;
            }
            return columnName;
        }
        
        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Float value = 0f;
            switch (columnIndex) {
                case (0):
                    value = balTable.get(0);
                    break;
                case (1):
                    value = balTable.get(1);
                    break;
                case (2):
                    value = balTable.get(2);
                    break;
            }
            return value;
        }
    }
}
package org.example;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BurpExtender implements BurpExtension {
    private MontoyaApi api;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private List<String> paramsList;
    private JTextField urlField;
    JButton startButton;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("CaptchaDos");
        api.logging().logToOutput("插件版本：0.1.3");
        api.logging().logToOutput("作者：ther0ok1eboy");
        api.logging().logToOutput("项目地址：https://github.com/ther0ok1eboy/CaptchaDos");

        // 初始化参数列表
        paramsList = new ArrayList<>();
        urlField = new JTextField("https://example.com/", 50);
        startButton = new JButton("Run");

        // 添加自定义Tab到UI
        api.userInterface().registerSuiteTab("CaptchaDos" ,CustomTab());

        //右键发送到插件
        api.userInterface().registerContextMenuItemsProvider(new ContextMenuItemsProvider() {
            @Override
            public List<Component> provideMenuItems(ContextMenuEvent event) {
                if (event.isFromTool(ToolType.PROXY, ToolType.TARGET, ToolType.LOGGER))
                {
                    List<Component> menuItemList = new ArrayList<>();

                    JMenuItem retrieveRequestItem = new JMenuItem("Send to CaptchaDos");

                    HttpRequestResponse requestResponse = event.messageEditorRequestResponse().isPresent() ? event.messageEditorRequestResponse().get().requestResponse() : event.selectedRequestResponses().get(0);

                    retrieveRequestItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            urlField.setText(requestResponse.request().url());
                            startButton.doClick();
                        }
                    });
                    menuItemList.add(retrieveRequestItem);

                    return menuItemList;
                }
                return ContextMenuItemsProvider.super.provideMenuItems(event);
            }
        });

    }

    private Component CustomTab() {

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建顶部面板，包含URL输入框和按钮
        JPanel topPanel = new JPanel(new FlowLayout());

        //JTextField urlField = new JTextField("https://example.com/",50);
        topPanel.add(new JLabel("URL:"));
        topPanel.add(urlField);

        topPanel.add(startButton);

        // 创建参数输入面板，设置默认值
        JPanel paramPanel = new JPanel(new FlowLayout());

        JTextField heightField = new JTextField("2000", 5);
        JTextField hField = new JTextField("2000", 5);
        JTextField widthField = new JTextField("2000", 5);
        JTextField wField = new JTextField("2000", 5);
        JTextField marginField = new JTextField("2000", 5);
        JTextField mField = new JTextField("2000", 5);
        JTextField sizeField = new JTextField("2000", 5);
        JTextField sField = new JTextField("2000", 5);
        JTextField paddingField = new JTextField("2000", 5);
        JTextField pField = new JTextField("2000", 5);

        paramPanel.add(new JLabel("Height:"));
        paramPanel.add(heightField);
        paramPanel.add(new JLabel("Width:"));
        paramPanel.add(widthField);
        paramPanel.add(new JLabel("Margin:"));
        paramPanel.add(marginField);
        paramPanel.add(new JLabel("Size:"));
        paramPanel.add(paddingField);
        paramPanel.add(new JLabel("Padding:"));
        paramPanel.add(sizeField);
        paramPanel.add(new JLabel("h:"));
        paramPanel.add(hField);
        paramPanel.add(new JLabel("w:"));
        paramPanel.add(wField);
        paramPanel.add(new JLabel("m:"));
        paramPanel.add(mField);
        paramPanel.add(new JLabel("s:"));
        paramPanel.add(sField);
        paramPanel.add(new JLabel("p:"));
        paramPanel.add(pField);

        // 创建表格用于显示结果
        String[] columnNames = {"URL", "Status code", "Time(ms)", "Length(bytes)", "Y/N"};
        tableModel = new DefaultTableModel(columnNames, 0);
        resultTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(resultTable);

        // 添加组件到主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(paramPanel, BorderLayout.SOUTH); // 将参数输入面板放在上面
        mainPanel.add(scrollPane, BorderLayout.CENTER); // 文本区域在下方

        // 添加表格的右键菜单
        addTableRightClickMenu(resultTable);

        // 定义按钮点击事件
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.setRowCount(0); // 清空表格

                // 获取输入的URL
                String baseUrl = urlField.getText().trim();
                String height = heightField.getText().trim();
                String h = hField.getText().trim();
                String width = widthField.getText().trim();
                String w = wField.getText().trim();
                String margin = marginField.getText().trim();
                String m = mField.getText().trim();
                String size = sizeField.getText().trim();
                String s = sField.getText().trim();
                String padding = paddingField.getText().trim();
                String p = pField.getText().trim();

                if (!baseUrl.isEmpty()) {
                    paramsList.clear();
                    paramsList.add("height=" + height);
                    paramsList.add("h=" + h);
                    paramsList.add("width=" + width);
                    paramsList.add("w=" + w);
                    paramsList.add("m=" + m);
                    paramsList.add("margin=" + margin);
                    paramsList.add("size=" + size);
                    paramsList.add("s=" + s);
                    paramsList.add("padding=" + padding);
                    paramsList.add("p=" + p);
                    paramsList.add("height=" + height + "&width=" + width);
                    paramsList.add("h=" + h + "&w=" + w);

                    ExecutorService executorService = Executors.newFixedThreadPool(paramsList.size());

                    for (String param : paramsList) {
                        executorService.submit(() -> {
                            String urlWithParam;
                            if (baseUrl.contains("?")) {
                                urlWithParam = baseUrl + "&" + param;
                            } else {
                                urlWithParam = baseUrl + "?" + param;
                            }
                            try {
                                long startTime = System.currentTimeMillis();
                                // 创建连接并发起请求
                                URL url = new URL(urlWithParam);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");
                                connection.setConnectTimeout(5000); // 设置超时时间
                                connection.connect();

                                int responseCode = connection.getResponseCode();
                                int contentLength = connection.getContentLength();

                                long endTime = System.currentTimeMillis();
                                long duration = endTime - startTime;
                                String isOverFiveSeconds = duration > 3000 ? "存在" : "不存在";

                                // 在表格中显示结果
                                SwingUtilities.invokeLater(() -> {
                                    tableModel.addRow(new Object[]{
                                            urlWithParam,
                                            responseCode,
                                            duration,
                                            contentLength,
                                            isOverFiveSeconds
                                    });
                                });
                            } catch (Exception ex) {
                                SwingUtilities.invokeLater(() -> {
                                    tableModel.addRow(new Object[]{
                                            urlWithParam,
                                            "错误",
                                            "N/A",
                                            "N/A",
                                            "N/A"
                                    });
                                });
                            }



                        });
                    }

                    executorService.shutdown();
                    try {
                        executorService.awaitTermination(10, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "请输入有效的URL和参数", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return  mainPanel;
    }

    // 添加表格的右键菜单
    private void addTableRightClickMenu(JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem copyUrlItem = new JMenuItem("Copy URL");
        JMenuItem sendToRepeaterItem = new JMenuItem("发送到 Repeater");

        // 复制URL
        copyUrlItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String url = (String) table.getValueAt(selectedRow, 0);
                StringSelection selection = new StringSelection(url);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                JOptionPane.showMessageDialog(table, "URL已复制到剪贴板", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // 发送URL到Repeater
        sendToRepeaterItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String url = (String) table.getValueAt(selectedRow, 0);
                HttpRequest request = HttpRequest.httpRequestFromUrl(url);

                api.repeater().sendToRepeater(request);
                JOptionPane.showMessageDialog(table, "已发送到Repeater", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        popupMenu.add(copyUrlItem);
        popupMenu.add(sendToRepeaterItem);

        // 右键点击时显示菜单
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            // 在指定位置显示右键菜单
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger() && table.getSelectedRow() != -1) {
                    int row = table.rowAtPoint(e.getPoint());
                    table.setRowSelectionInterval(row, row);  // 选中行
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

}

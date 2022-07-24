/*Copyright (c) 2008 Nikos Siatras

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.*/
package Extasys.Examples.TCPServer;

import java.net.InetAddress;

/**
 *
 * @author Nikos Siatras
 */
public class frmTCPServer extends javax.swing.JFrame
{

    private TCPServer fTCPServer;
    private Thread fUpdateStatusThread;
    private boolean fUpdateStatusActive = true;

    private long fOldBytesIn = 0, fOldBytesOut = 0;

    /**
     * Creates new form frmTCPServer
     */
    public frmTCPServer()
    {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jTextFieldTCPServerIP = new javax.swing.JTextField();
        jTextFieldTCPServerConnectionsTimeOut = new javax.swing.JTextField();
        jTextFieldTCPServerMaxConnections = new javax.swing.JTextField();
        jTextFieldTCPServerCorePoolSize = new javax.swing.JTextField();
        jTextFieldTCPServerPort = new javax.swing.JTextField();
        jTextFieldTCPServerMaxPoolSize = new javax.swing.JTextField();
        jButtonStartServer = new javax.swing.JButton();
        jButtonStopServer = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabelBytesIn = new javax.swing.JLabel();
        jLabelBytesOut = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabelClientsConnected = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Extasys Example TCP Server");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("TCP Server");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel2.setText("Listener IP Address:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel4.setText("Core Pool Size:");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel3.setText("Listener Port:");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel5.setText("Max. Pool Size:");

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel11.setText("Max. Connections:");

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel12.setText("Connection Time-Out (ms):");

        jTextFieldTCPServerIP.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jTextFieldTCPServerIP.setText("127.0.0.1");

        jTextFieldTCPServerConnectionsTimeOut.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jTextFieldTCPServerConnectionsTimeOut.setText("10000");

        jTextFieldTCPServerMaxConnections.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jTextFieldTCPServerMaxConnections.setText("10000");

        jTextFieldTCPServerCorePoolSize.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jTextFieldTCPServerCorePoolSize.setText("2");

        jTextFieldTCPServerPort.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jTextFieldTCPServerPort.setText("5000");

        jTextFieldTCPServerMaxPoolSize.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jTextFieldTCPServerMaxPoolSize.setText("4");

        jButtonStartServer.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButtonStartServer.setText("Start TCP Server");
        jButtonStartServer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonStartServerActionPerformed(evt);
            }
        });

        jButtonStopServer.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButtonStopServer.setText("Stop TCP Server");
        jButtonStopServer.setEnabled(false);
        jButtonStopServer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonStopServerActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel6.setText("Bytes In:");

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel7.setText("Bytes Out:");

        jLabelBytesIn.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabelBytesIn.setText("0");

        jLabelBytesOut.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabelBytesOut.setText("0");

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel10.setText("Clients connected:");

        jLabelClientsConnected.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabelClientsConnected.setText("0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12))
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextFieldTCPServerIP)
                            .addComponent(jTextFieldTCPServerConnectionsTimeOut)
                            .addComponent(jTextFieldTCPServerMaxConnections)
                            .addComponent(jTextFieldTCPServerCorePoolSize, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextFieldTCPServerPort)
                            .addComponent(jTextFieldTCPServerMaxPoolSize, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jButtonStartServer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonStopServer)
                .addGap(98, 98, 98))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel10)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelBytesIn)
                    .addComponent(jLabelClientsConnected)
                    .addComponent(jLabelBytesOut))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldTCPServerIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldTCPServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldTCPServerCorePoolSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextFieldTCPServerMaxPoolSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jTextFieldTCPServerMaxConnections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jTextFieldTCPServerConnectionsTimeOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonStopServer)
                    .addComponent(jButtonStartServer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabelBytesIn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabelBytesOut))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabelClientsConnected))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jButtonStartServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartServerActionPerformed
    try
    {

        String serverName = "My TCP server";
        String serverDescription = "Example";
        InetAddress listenerIPAddress = InetAddress.getByName(jTextFieldTCPServerIP.getText());
        int listenerPort = Integer.parseInt(jTextFieldTCPServerPort.getText());
        int maxConnections = Integer.parseInt(jTextFieldTCPServerMaxConnections.getText());
        int connectionsTimeout = Integer.parseInt(jTextFieldTCPServerConnectionsTimeOut.getText());
        int corePoolSize = Integer.parseInt(jTextFieldTCPServerCorePoolSize.getText());
        int maxPoolSize = Integer.parseInt(jTextFieldTCPServerMaxPoolSize.getText());

        // Initialize and Start a new TCP Server
        fTCPServer = new TCPServer(serverName, serverDescription, listenerIPAddress, listenerPort, maxConnections, connectionsTimeout, corePoolSize, maxPoolSize);
        fUpdateStatusActive = true;
        fTCPServer.Start();

        // The following thread updates the UI every 1000ms (1 second)
        fUpdateStatusThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    while (fUpdateStatusActive)
                    {
                        try
                        {
                            if (fTCPServer != null)
                            {
                                Long newBytesIn = fTCPServer.getBytesIn();
                                Long newBytesOut = fTCPServer.getBytesOut();

                                jLabelBytesIn.setText(String.valueOf(newBytesIn) + " (" + String.valueOf((newBytesIn - fOldBytesIn)/1000) + "kb/sec)");
                                jLabelBytesOut.setText(String.valueOf(newBytesOut) + " (" + String.valueOf((newBytesOut - fOldBytesOut)/1000) + "kb/sec)");
                                jLabelClientsConnected.setText(String.valueOf(fTCPServer.getCurrentConnectionsNumber()));

                                fOldBytesIn = newBytesIn;
                                fOldBytesOut = newBytesOut;
                            }
                        }
                        catch (Exception ex)
                        {
                            System.err.println("<fUpdateStatusActive>" + ex.getMessage());
                        }
                        Thread.sleep(1000);
                    }
                }
                catch (Exception ex)
                {
                    System.err.println("<fUpdateStatusActive>" + ex.getMessage());
                }
            }
        });

        fUpdateStatusThread.start();

        jButtonStopServer.setEnabled(true);
        jButtonStartServer.setEnabled(false);
    }
    catch (Exception ex)
    {
        System.err.println(ex.getMessage());
    }
}//GEN-LAST:event_jButtonStartServerActionPerformed

private void jButtonStopServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStopServerActionPerformed
    try
    {
        if (fTCPServer != null)
        {
            fTCPServer.ForceStop();
        }

        if (fUpdateStatusThread != null)
        {
            fUpdateStatusActive = false;
            fUpdateStatusThread.interrupt();
            fUpdateStatusThread = null;
        }
    }
    catch (Exception ex)
    {
        System.err.println(ex.getMessage());
    }
    jButtonStopServer.setEnabled(false);
    jButtonStartServer.setEnabled(true);
}//GEN-LAST:event_jButtonStopServerActionPerformed

    public static void main(String args[])
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            public void run()
            {
                new frmTCPServer().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonStartServer;
    private javax.swing.JButton jButtonStopServer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelBytesIn;
    private javax.swing.JLabel jLabelBytesOut;
    private javax.swing.JLabel jLabelClientsConnected;
    private javax.swing.JTextField jTextFieldTCPServerConnectionsTimeOut;
    private javax.swing.JTextField jTextFieldTCPServerCorePoolSize;
    private javax.swing.JTextField jTextFieldTCPServerIP;
    private javax.swing.JTextField jTextFieldTCPServerMaxConnections;
    private javax.swing.JTextField jTextFieldTCPServerMaxPoolSize;
    private javax.swing.JTextField jTextFieldTCPServerPort;
    // End of variables declaration//GEN-END:variables
}
